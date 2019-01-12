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
package org.lightningj.paywall.paymenthandler.data;

/**
 * General interface indicating implementing type is some form
 * of PaymentData (eventually persistable). Implementing classes
 * should NOT implement this class but extending classes such
 * as MinimalPaymentData.
 *
 * @see MinimalPaymentData
 * Created by Philip Vendil on 2018-12-10.
 */
public interface PaymentData {
}
