package org.lightningj.paywall.springboot2;

import org.lightningj.paywall.annotations.PaymentRequired;
import org.lightningj.paywall.spring.PaywallProperties;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicLong;

@PaymentRequired(articleId = "abc565")
@RestController
    public class GreetingController {

        private static final String template = "Hello, %s!";
        private final AtomicLong counter = new AtomicLong();

        private PaywallProperties paywallProperties;

        public GreetingController(PaywallProperties paywallProperties){
            this.paywallProperties = paywallProperties;
        }

        @PaymentRequired(articleId = "abc123")
        @RequestMapping("/greeting")
        public Greeting greeting(@RequestParam(value="name", defaultValue="World") String name) {
            return new Greeting(counter.incrementAndGet(),
                    String.format(template, paywallProperties.getLndHostname()));
        }

    @RequestMapping("/greeting2")
    public Greeting greeting2(@RequestParam(value="name", defaultValue="World2") String name) {
        return new Greeting(counter.incrementAndGet(),
                String.format(template, paywallProperties.getLndHostname()));
    }
    }

