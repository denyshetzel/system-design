package br.com.systemdesign.urlshortening;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@EnableRetry
@SpringBootApplication
public class UrlShorteningApplication {

    static void main(String[] args) {
        SpringApplication.run(UrlShorteningApplication.class, args);
    }

}
