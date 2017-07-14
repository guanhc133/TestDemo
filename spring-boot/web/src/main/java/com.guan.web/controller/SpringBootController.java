package com.guan.web.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 描述：
 * <p>
 * #
 * </p>
 * User: guanhc Date: 2017/7/7 ProjectName:spring-boot Version:
 */
@RestController
public class SpringBootController {

    @RequestMapping("hello")
    String home(){
        return "spring boot";
    }

    @RequestMapping("showPage")
    public void showPage(HttpServletRequest request) {

    }
}
