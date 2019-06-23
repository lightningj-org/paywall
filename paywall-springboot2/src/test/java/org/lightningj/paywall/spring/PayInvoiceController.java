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
package org.lightningj.paywall.spring;

import org.lightningj.paywall.lightninghandler.LightningHandler;
import org.lightningj.paywall.util.Base58;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Test controller to simulate payment of a specified preImageHash
 * @author philip 2019-06-19
 */
@Controller
public class PayInvoiceController {

    @Autowired
    LightningHandler lightningHandler;

    /**
     * Method to indicate MockedLightningHandler used in test scripts to mark related
     * preImageHash as settled.
     *
     * @param preImageHash Base58 encoded preImageHash.
     * @throws Exception if problems occurred, handled by handleException method.
     */
    @RequestMapping("/paywall/test/payinvoice/{preImageHash}")
    @ResponseBody
    public String payInvoice(@PathVariable("preImageHash") String preImageHash) throws Exception{
        assert lightningHandler instanceof MockedLightningHandler;
        ((MockedLightningHandler) lightningHandler).simulateSettleInvoice(Base58.decode(preImageHash));
        return "Invoice with preImageHash: " +preImageHash + " settled.";
    }


}
