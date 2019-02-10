package org.lightningj.paywall.springboot2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
public class DemoApplication {

	// 1. Start with application greeting OK
	// 2. Register bean, auto configure
	// 3. application config in bean?
	// 4. Filter how is this done?

	public static void main(String[] args) {
		// TODO


		SpringApplication.run(DemoApplication.class, args);
	}

}

