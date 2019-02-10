package org.lightningj.paywall.springboot2;

import org.lightningj.paywall.currencyconverter.CurrencyConverter;
import org.lightningj.paywall.spring.local.LocalProfileBeanConfiguration;
import org.springframework.context.annotation.Bean;

//@Configuration
public class CustomLocalProfileBeanConfiguration extends LocalProfileBeanConfiguration {

    @Bean({"currencyConverter"})
    @Override
    public CurrencyConverter getCurrencyConverter() {
        return new CustomCurrencyConverter();
    }
}
