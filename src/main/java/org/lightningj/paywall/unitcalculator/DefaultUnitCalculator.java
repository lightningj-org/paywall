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
 * Default Magnetude Calculator takes the parameter units from the
 * PaymentRequired annotation (default 1)
 *
 * Created by Philip Vendil on 2018-10-29.
 */
public class DefaultUnitCalculator implements UnitCalculator{

    /**
     * Default Magnetude Calculator takes the parameter units from the
     * PaymentRequired annotation (default 1)
     *
     * @param paymentRequired the related annotation.
     * @param request         the HTTP request related to the call.
     * @return the number of units that should be debited to the PaymentHandler
     */
    @Override
    public int getUnits(PaymentRequired paymentRequired, CachableHttpServletRequest request) {
        return paymentRequired.units();
    }
}
