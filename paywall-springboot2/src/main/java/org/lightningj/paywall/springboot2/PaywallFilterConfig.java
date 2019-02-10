package org.lightningj.paywall.springboot2;

import org.lightningj.paywall.spring.PaywallProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import java.util.logging.Logger;

@Configuration
public class PaywallFilterConfig {
    Logger log = Logger.getLogger(PaywallFilter.class.getName());

     @Autowired
     private AutowireCapableBeanFactory beanFactory;

     @Autowired
     private PaywallProperties paywallProperties;

    @Bean
    public FilterRegistrationBean myFilter() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        Filter myFilter = new PaywallFilter();
        beanFactory.autowireBean(myFilter);
        registration.setFilter(myFilter);


        // TODO, a list of paths from config
        registration.addUrlPatterns("/*");
        return registration;
    }
}
