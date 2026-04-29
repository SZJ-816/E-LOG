package com.elog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * E-LOG 企业数据管理平台启动类
 */
@SpringBootApplication
public class ELogApplication {

    public static void main(String[] args) {
        SpringApplication.run(ELogApplication.class, args);
        System.out.println("========================================");
        System.out.println("  E-LOG Platform Started Successfully!");
        System.out.println("  http://localhost:8080");
        System.out.println("========================================");
    }
}