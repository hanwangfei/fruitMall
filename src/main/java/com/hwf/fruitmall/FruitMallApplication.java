package com.hwf.fruitmall;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@MapperScan(basePackages = {"com.hwf.fruitmall.model.dao"})
@EnableSwagger2//开启swagger自动生成API文档
public class FruitMallApplication {

    public static void main(String[] args) {
        System.out.println("中文测试");

        SpringApplication.run(FruitMallApplication.class, args);
    }

}
