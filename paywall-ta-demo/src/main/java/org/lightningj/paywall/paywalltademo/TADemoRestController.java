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
package org.lightningj.paywall.paywalltademo;

import org.lightningj.paywall.annotations.PaymentRequired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Proof of Concept REST controller with @PaymentRequired Mappings returning
 * TA Prediction in json format on
 *
 */
@RestController
public class TADemoRestController {

    private static final String template = "Bitcoin number is probably going %s.";
    private final AtomicLong counter = new AtomicLong();

    private final SecureRandom taEngine = new SecureRandom();

    /**
     * REST Endpoint that has a non pay per request PaymentRequired annotation.
     *
     * @return json object with call counter and TA Prediction
     */
    @PaymentRequired(articleId = "tademo1", payPerRequest = true)
    @RequestMapping("/tademo")
    public TADemoResult tademo() {
        boolean goingUp = taEngine.nextBoolean();
        return new TADemoResult(counter.incrementAndGet(),
                String.format(template, (goingUp ? "up":"down")),
                goingUp);
    }

    /**
     * Same REST Endpoint without payment required
     *
     * @return json object with call counter and TA Prediction
     */
    @RequestMapping("/tademonopayment")
    public TADemoResult tademoNoPayment() {
        boolean goingUp = taEngine.nextBoolean();
        return new TADemoResult(counter.incrementAndGet(),
                String.format(template, (goingUp ? "up":"down")),
                goingUp);
    }

}

