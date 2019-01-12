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
package org.lightningj.paywall.web;

/**
 * Class containing constants for HTTP parameter, header and cookie names.
 *
 * Created by Philip Vendil on 2018-12-30.
 */
public class HTTPConstants {

    /**
     * HTTP Header value set with a settlement JWT token sent with
     * the payment request in order to process the data.
     */
    public static final String HEADER_PAYMENT = "Payment";

    /**
     * Cookie name used when redirecting an invoice request controller
     * to check payment.
     */
    public static final String COOKIE_INVOICE_REQUEST = "InvoiceRequest";

    /**
     * Cookie name used when redirecting an order request to a central payment controller
     * and checking for payment.
     */
    public static final String COOKIE_PAYMENT_REQUEST = "PaymentRequest";
}
