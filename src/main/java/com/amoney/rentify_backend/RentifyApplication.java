package com.amoney.rentify_backend;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RentifyApplication {

	public static void main(String[] args) {
		Dotenv.configure()
				.ignoreIfMissing()
				.load()
				.entries()
				.forEach(entry -> {
					if (System.getenv(entry.getKey()) == null) {
						System.setProperty(entry.getKey(), entry.getValue());
					}
				});
		SpringApplication.run(RentifyApplication.class, args);
	}

}
