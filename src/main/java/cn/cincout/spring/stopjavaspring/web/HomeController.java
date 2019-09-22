package cn.cincout.spring.stopjavaspring.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by zhaoyu on 19-9-22.
 *
 * @author zhaoyu
 * @sine 1.8
 */
@Controller
public class HomeController {
    // 计数器
    public AtomicInteger started = new AtomicInteger();

    public AtomicInteger ended = new AtomicInteger();

    @RequestMapping("/hello")
    @ResponseBody
    public String index() {

        System.out.println(Thread.currentThread().getName() + " -> " + this + " Get one, got: " + started.addAndGet(1));
        try {
            Thread.sleep(1000 * 10); // 模拟一个执行时间很长的任务
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println(Thread.currentThread().getName() + " -> " + this + "  Finish one, finished: " + ended.addAndGet(1));
        return "hello";
    }
}
