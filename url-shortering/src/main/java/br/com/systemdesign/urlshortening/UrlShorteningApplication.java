package br.com.systemdesign.urlshortening;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class UrlShorteningApplication {

    public static void main(String[] args) {
        SpringApplication.run(UrlShorteningApplication.class, args);
    }

}
