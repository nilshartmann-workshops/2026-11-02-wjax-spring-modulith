package nh.demo.plantify;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PlantifyApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlantifyApplication.class, args);
    }

}
