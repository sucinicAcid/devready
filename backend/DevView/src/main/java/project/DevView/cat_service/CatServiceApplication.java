package project.DevView.cat_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class CatServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CatServiceApplication.class, args);
	}

}
