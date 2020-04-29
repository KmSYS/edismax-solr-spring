package net.kmsys;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan
public class EdismaxSolrSpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(EdismaxSolrSpringApplication.class, args);
    }
}
