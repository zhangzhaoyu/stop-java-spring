package cn.cincout.spring.stopjavaspring;

import cn.cincout.spring.stopjavaspring.web.HomeController;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.threads.ThreadPoolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@Slf4j
@RestController
public class StopJavaSpringApplication implements ApplicationContextAware {

    @Autowired
    static ApplicationContext context;

    public static void main(String[] args) {
        SpringApplication.run(StopJavaSpringApplication.class, args);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("begin to shutdown ... ");
			/*try {
				TimeUnit.SECONDS.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}*/
            HomeController one = context.getBean(HomeController.class);
            System.out.println(one + " -> started: " + one.started.get() + " ended:" + one.ended.get());
            log.info("bye bye, app stopped ...");
        }));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    @RequestMapping("/pause")
    public String pause() throws InterruptedException {
        Thread.sleep(10000);
        return "Pause complete";
    }

    @Bean
    public GracefulShutdown gracefulShutdown() {
        return new GracefulShutdown();
    }

    @Bean
    public WebServerFactoryCustomizer tomcatCustomizer() {
        return factory -> {
            if (factory instanceof TomcatServletWebServerFactory) {
                ((TomcatServletWebServerFactory) factory)
                        .addConnectorCustomizers(gracefulShutdown());
            }
        };
    }

    private static class GracefulShutdown implements TomcatConnectorCustomizer,
            ApplicationListener<ContextClosedEvent> {

        private static final Logger log = LoggerFactory.getLogger(GracefulShutdown.class);

        private volatile Connector connector;

        @Override
        public void customize(Connector connector) {
            this.connector = connector;
        }

        @Override
        public void onApplicationEvent(ContextClosedEvent event) {
            // 暂停接收新的请求
            this.connector.pause();
            Executor executor = this.connector.getProtocolHandler().getExecutor();
            //
            if (executor instanceof ThreadPoolExecutor) {
                try {
                    // 等待 30s 线程池org.apache.tomcat.util.threads.ThreadPoolExecutor 完成所有请求
                    ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) executor;
                    threadPoolExecutor.shutdown();
                    if (!threadPoolExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                        log.warn("Tomcat thread pool did not shut down gracefully within "
                                + "30 seconds. Proceeding with forceful shutdown");
                    }
                }
                catch (InterruptedException ex) {
                    // 30s 未完成 强制关闭 该逻辑也可以写在bash 中
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
