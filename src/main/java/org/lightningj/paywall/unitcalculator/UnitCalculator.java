/*
 *************************************************************************
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
package org.lightningj.paywall.unitcalculator;

import org.lightningj.paywall.annotations.PaymentRequired;
import org.lightningj.paywall.web.CachableHttpServletRequest;

/**
 * Interface calculating the number of units that should be debited
 * for each call having the PaymentRequired annotation.
 *
 * Created by philip on 2018-10-29.
 */
public interface UnitCalculator {

    /**
     * Method that should calculate the number of units that should
     * be used for a given request with PaymentRequired annotation.
     * @param paymentRequired the related annotation.
     * @param request the HTTP request related to the call.
     * @return the number of units that should be debited to the PaymentHandler
     */
    int getUnits(PaymentRequired paymentRequired, CachableHttpServletRequest request);
}
