package com.primera_app_spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.primera_app_spring.storage.StorageProperties;

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class PrimeraAppSpringApplication {

	public static void main(String[] args) {
		SpringApplication.run(PrimeraAppSpringApplication.class, args);
		
	}

}
