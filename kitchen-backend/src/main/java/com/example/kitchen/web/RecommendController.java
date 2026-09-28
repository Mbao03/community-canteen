package com.example.kitchen.web;

import com.example.kitchen.service.RecommendService;
import com.example.kitchen.utils.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/recommend")
public class RecommendController {

    @Resource
    private RecommendService recommendService;

    @GetMapping("/user")
    public Map<String, Object> recommendByUser(@RequestParam Integer userid,
                                               @RequestParam(required = false, defaultValue = "5") Integer limit) {
        return R.getResultMap(200, "success", recommendService.recommendByUserId(userid, limit));
    }
}
