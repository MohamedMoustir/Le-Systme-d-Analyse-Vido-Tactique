package com.savt.backend;

import io.netty.handler.logging.LogLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
@Slf4j
public class SavtBackendApplication {

	public static void main(String[] args) {

        SpringApplication.run(SavtBackendApplication.class, args);
	}

}
