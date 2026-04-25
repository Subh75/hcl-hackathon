package com.favouritepayee;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class FavouritePayeeApplication {

    public static void main(String[] args) {
        SpringApplication.run(FavouritePayeeApplication.class, args);
    }
}
