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

import java.util.concurrent.atomic.AtomicLong;

/**
 * Proof of Concept REST controller with @PaymentRequired Mappings on class level.
 * Used in various Integration and Functional Test
 *
 */
@PaymentRequired(articleId = "abc456")
@RestController
public class Poc2RestController {

    private static final String template = "PocService2, %s!";
    private final AtomicLong counter = new AtomicLong();

    /**
     * Simple REST Endpoint.
     *
     * @return json object with call counter and string 'PocService2, poc2_1'
     */
    @RequestMapping("/poc2_1")
    public PocResult poc2_1() {
        return new PocResult(counter.incrementAndGet(),
                String.format(template, "poc2_1"));
    }

    /**
     * Simple REST Endpoint.
     *
     * @return json object with call counter and string 'PocService2, poc2_2'
     */
    @RequestMapping("/poc2_2")
    public PocResult poc2_2() {
        return new PocResult(counter.incrementAndGet(),
                String.format(template, "poc2_2"));
    }

}

