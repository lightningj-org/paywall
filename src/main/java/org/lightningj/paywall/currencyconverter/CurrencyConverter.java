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

import org.lightningj.paywall.InternalErrorException;
import org.lightningj.paywall.vo.amount.Amount;
import org.lightningj.paywall.vo.amount.CryptoAmount;

import java.io.IOException;

/**
 * Interface for component to convert an amount in either fiat or crypto currency
 * into an amount that is understandable to the related LightningHandler or equivalent.
 *
 * Created by Philip Vendil on 2018-11-07.
 */
public interface CurrencyConverter {

    /**
     * Main method to convert amount created by payment handler into currency understandable
     * by the used Lightning handler.
     *
     * @param amount the amount to convert into crypto currency amount understandable by used LightningHandler.
     * @return corresponding CrytoAmount with a currency lightning handler can understand.
     * @throws InvalidCurrencyException  if CurrencyConverter received an amount
     * in unsupported currency.
     * @throws IOException if communication problems occurred with underlying components.
     * @throws InternalErrorException if general internal error occurred converting the currency, such as
     * configuration error.
     */
    CryptoAmount convert(Amount amount) throws InvalidCurrencyException, IOException,InternalErrorException;
}
