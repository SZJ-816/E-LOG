package cn.edu.tjrac;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LogSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(LogSystemApplication.class, args);
    }
}
