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

import org.lightningj.paywall.InternalErrorException;
import org.lightningj.paywall.annotations.PaymentRequired;
import org.lightningj.paywall.requestpolicy.RequestPolicy;
import org.lightningj.paywall.requestpolicy.UrlAndMethod;
import org.lightningj.paywall.requestpolicy.UrlMethodAndParameters;
import org.lightningj.paywall.requestpolicy.WithBody;

import java.util.HashMap;
import java.util.Map;

/**
 * UnitCalculatorFactory is in charge if delivering the correct
 * UnitCalculator implementation specified in the PaymentRequired annotation.
 *
 * Created by Philip Vendil on 2018-10-28.
 */
public class UnitCalculatorFactory {


    private DefaultUnitCalculator defaultUnitCalculator = new DefaultUnitCalculator();

    private Map<Class,UnitCalculator> customCalculators = new HashMap<>();

    /**
     * Method that returns a UnitCalculator instance that is specified
     * in the given PaymentRequired annotation.
     * @param paymentRequired the related PaymentRequired annotation.
     * @return related UnitCalculator, never null.
     * @throws InternalErrorException if problems occurred creating the related UnitCalculator object.
     */
    public UnitCalculator getUnitCalculator(PaymentRequired paymentRequired) throws InternalErrorException{
        if(paymentRequired.unitCalculator() == DefaultUnitCalculator.class){
            return defaultUnitCalculator;
        }
        return getCustomCalculator(paymentRequired);
    }


    private UnitCalculator getCustomCalculator(PaymentRequired paymentRequired) throws InternalErrorException{

        UnitCalculator customCalculator = customCalculators.get(paymentRequired.unitCalculator());
        if(customCalculator == null){
            try {
                customCalculator = paymentRequired.unitCalculator().newInstance();
                customCalculators.put(paymentRequired.unitCalculator(),customCalculator);
            }catch(Exception e){
                throw new InternalErrorException("Error constructing custom unit calculator : " + paymentRequired.unitCalculator() + ", message: " + e.getMessage(),e);
            }
        }
        return customCalculator;
    }
}
