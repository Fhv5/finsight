package io.github.fhv5.finsight;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class FinsightApplication {

	static void main(String[] args) {
		SpringApplication.run(FinsightApplication.class, args);
	}

}
