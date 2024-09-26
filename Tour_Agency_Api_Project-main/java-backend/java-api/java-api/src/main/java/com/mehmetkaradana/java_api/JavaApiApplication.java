package com.mehmetkaradana.java_api;

import java.util.concurrent.Executors;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JavaApiApplication {

	public static void main(String[] args) {
        
		 var executor = Executors.newVirtualThreadPerTaskExecutor();
         executor.submit(() -> {
            System.out.println("Hello from Virtual Thread!");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
               // e.printStackTrace();
            }
        });
        executor.shutdown();

		SpringApplication.run(JavaApiApplication.class, args);
	}

}
