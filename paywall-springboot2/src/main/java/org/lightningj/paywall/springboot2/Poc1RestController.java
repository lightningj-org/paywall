/*
 * ***********************************************************************
 *                                                                       *
 *  LightningJ                                                           *
 *                                                                       *
 *  This software is free software; you can redistribute it and/or       *
 *  modify it under the terms of the GNU Lesser General Public License   *
 *  (LGPL-3.0-or-later)                                                  *
 *  License as published by the Free Software Foundation; either         *
 *  version 3 of the License, or any later version.                      *
 *                                                                       *
 *  See terms of license at gnu.org.                                     *
 *                                                                       *
 *************************************************************************/
package org.lightningj.paywall.springboot2;

import org.lightningj.paywall.annotations.PaymentRequired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Proof of Concept REST controller with @PaymentRequired Mappings on method level.
 * Used in various Integration and Functional Test
 *
 */
@RestController
public class Poc1RestController {

    private static final String template = "PocService1, %s!";
    private final AtomicLong counter = new AtomicLong();

    /**
     * REST Endpoint that has a non pay per request PaymentRequired annotation.
     * @param name value returned in generated string response.
     * @return json object with call counter and string 'PocService1, 'name''
     */
    @PaymentRequired(articleId = "abc123")
    @RequestMapping("/poc1")
    public PocResult poc1(@RequestParam(value="name", defaultValue="Poc1") String name) {
        return new PocResult(counter.incrementAndGet(),
                String.format(template, name));
    }

    /**
     * REST Endpoint that has a pay per request PaymentRequired annotation.
     * @return json object with call counter and string 'PocService1, Pay Per Request'
     */
    @PaymentRequired(articleId = "abcPayPerRequest", payPerRequest = true)
    @RequestMapping("/poc1PayPerRequest")
    public PocResult poc1PayPerRequest() {
        return new PocResult(counter.incrementAndGet(),
                String.format(template, "Pay Per Request"));
    }

    /**
     * REST Endpoint that has no PaymentRequired annotation.
     * @return json object with call counter and string 'PocService1, No Payment Required'
     */
    @RequestMapping("/poc1NoPaymentRequired")
    public PocResult poc1NoPaymentRequired() {
        return new PocResult(counter.incrementAndGet(),
                String.format(template, "No Payment Required"));
    }

    /**
     * REST Endpoint that generates an IOException, behind a paywall
     * @return json object with spring standard JSON Error
     * @throws IOException on each call to test internal problems
     */
    @PaymentRequired(articleId = "abcPayPerRequest", payPerRequest = true)
    @RequestMapping("/poc1ApiError")
    public PocResult poc1APIError() throws IOException{
        throw new IOException("Some API Error.");
    }

    /**
     * REST Endpoint that generates an IOException, without paywall
     * @return json object with spring standard JSON Error
     * @throws IOException on each call to test internal problems
     */
    @RequestMapping("/poc1ApiErrorNoPaywall")
    public PocResult poc1APIErrorNoPaywall() throws IOException{
        throw new IOException("Some API Error, No Paywall.");
    }
}

