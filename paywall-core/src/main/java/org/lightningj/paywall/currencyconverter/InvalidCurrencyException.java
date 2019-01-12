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

package org.lightningj.paywall.currencyconverter;

/**
 * Exception thrown by a CurrencyConverter when receiving an amount
 * in an by the implementation unsupported currency.
 *
 * Created by Philip Vendil on 2018-11-11.
 */
public class InvalidCurrencyException extends Exception {

    /**
     * Exception thrown by a CurrencyConverter when receiving an amount
     * in an by the implementation unsupported currency.
     *
     * @param message descriptive message.
     */
    public InvalidCurrencyException(String message){
        super(message);
    }

    /**
     * Exception thrown by a CurrencyConverter when receiving an amount
     * in an by the implementation unsupported currency.
     *
     * @param message descriptive message.
     * @param cause causing exception.
     */
    public InvalidCurrencyException(String message, Throwable cause){
        super(message,cause);
    }
}
