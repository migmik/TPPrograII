package com.tijetravel.tijefront;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

// Los usuarios se autentican en la API; no creamos usuarios locales de Spring Security.
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class TijeFrontApplication extends SpringBootServletInitializer {
    public static void main(String[] args) {
        SpringApplication.run(TijeFrontApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(TijeFrontApplication.class);
    }
}
