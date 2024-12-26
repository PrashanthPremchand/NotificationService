package com.prashanth.microservices;

import org.springframework.boot.SpringApplication;

public class TestNotificationServicesApplication {

	public static void main(String[] args) {
		SpringApplication.from(NotificationServicesApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
