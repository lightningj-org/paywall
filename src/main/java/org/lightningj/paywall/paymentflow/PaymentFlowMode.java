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
package org.lightningj.paywall.paymentflow;

/**
 * Enumeration specifying different payment flows that can be used for
 * a given payment.
 *
 * Created by Philip Vendil on 2018-12-18.
 */
public enum PaymentFlowMode {

    /**
     * Local Flow where paywalled service, lightning handler and payment handler is in the same system and
     * symmentrical keys will be used to protect generated tokens.
     */
    LOCAL,
    /**
     * A distributed flow where paywalled services (including their own payment handler) is distributed on
     * different systems but have one central lightning handler node the end users are redirected
     * to for payments.
     */
    CENTRAL_LIGHTNING_HANDLER,

    /**
     * A distributed flow where paywalled services is distributed on
     * different systems but have one central payment handler and lightning handler node the end users are
     * redirected to for creating invoices and generating payments. (Reserved for future.)
     */
    CENTRAL_PAYMENT_HANDLER;
}
