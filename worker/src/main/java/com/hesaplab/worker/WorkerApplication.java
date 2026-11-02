package com.hesaplab.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WorkerApplication {
    public static void main(String[] args) {
        var context = SpringApplication.run(WorkerApplication.class, args);
        if (context.getEnvironment().matchesProfiles("send-demo")) {
            context.close();
        }
    }
}
