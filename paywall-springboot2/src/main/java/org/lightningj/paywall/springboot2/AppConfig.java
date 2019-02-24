package org.lightningj.paywall.springboot2;

import org.lightningj.paywall.lightninghandler.LightningHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

// Here all bean definitions

// TODO create Implementation of everything for local

@ComponentScan("org.lightningj.paywall.spring")
@Configuration
public class AppConfig {

    @Bean
    public Greeting getGreeting(){
        return new Greeting(1,"asdf");
    }


}
