package com.ahana.botmonitoring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RpmBotMonitoringApplication {

	public static void main(String[] args) {
		SpringApplication.run(RpmBotMonitoringApplication.class, args);
	}

}
