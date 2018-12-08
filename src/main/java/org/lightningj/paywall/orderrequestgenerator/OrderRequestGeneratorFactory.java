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
package org.lightningj.paywall.orderrequestgenerator;

import org.lightningj.paywall.InternalErrorException;
import org.lightningj.paywall.annotations.PaymentRequired;

import java.util.HashMap;
import java.util.Map;

/**
 * OrderRequestGeneratorFactory is in charge if delivering the correct
 * OrderRequestGenerator implementation specified in the PaymentRequired annotation.
 *
 * Created by Philip Vendil on 2018-10-28.
 */
public class OrderRequestGeneratorFactory {


    private DefaultOrderRequestGenerator defaultOrderRequest = new DefaultOrderRequestGenerator();

    private Map<Class,OrderRequestGenerator> customGenerators = new HashMap<>();

    /**
     * Method that returns a OrderRequestGenerator instance that is specified
     * in the given PaymentRequired annotation.
     * @param paymentRequired the related PaymentRequired annotation.
     * @return related OrderRequestGenerator, never null.
     * @throws InternalErrorException if problems occurred creating the related OrderRequestGenerator object.
     */
    public OrderRequestGenerator getGenerator(PaymentRequired paymentRequired) throws InternalErrorException{
        if(paymentRequired.orderRequestGenerator() == DefaultOrderRequestGenerator.class){
            return defaultOrderRequest;
        }
        return getCustomGenerator(paymentRequired);
    }


    private OrderRequestGenerator getCustomGenerator(PaymentRequired paymentRequired) throws InternalErrorException{

        OrderRequestGenerator customGenerator = customGenerators.get(paymentRequired.orderRequestGenerator());
        if(customGenerator == null){
            try {
                customGenerator = paymentRequired.orderRequestGenerator().newInstance();
                customGenerators.put(paymentRequired.orderRequestGenerator(),customGenerator);
            }catch(Exception e){
                throw new InternalErrorException("Error constructing custom order request generator : " + paymentRequired.orderRequestGenerator() + ", message: " + e.getMessage(),e);
            }
        }
        return customGenerator;
    }
}
