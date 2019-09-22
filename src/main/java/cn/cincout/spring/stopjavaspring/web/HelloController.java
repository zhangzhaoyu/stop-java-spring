package cn.cincout.spring.stopjavaspring.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhaoyu on 19-9-22.
 *
 * @author zhaoyu
 * @sine 1.8
 */
@RestController
public class HelloController {
    @GetMapping(value = "")
    public Map<String, String> hello() {
        Map<String, String> map = new HashMap<>();
        map.put("hello", "world");
        return map;
    }
}
