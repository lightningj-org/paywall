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
package org.lightningj.paywall.spring.websocket;

import org.lightningj.paywall.InternalErrorException;
import org.lightningj.paywall.paymenthandler.PaymentHandler;
import org.lightningj.paywall.paymenthandler.PaymentListener;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A WebSocket specific implementation when listing of settled payment handler.
 * <p>
 *     Used when there exists a local payment handler.
 * </p>
 *
 * The class maintains a map of registered payment listeners and cleans up itself after related
 * invoice have expired. Regular usage should unregister itself when disconnecting.
 *
 * @author Philip Vendil 2019-05-15
 */
public class WebSocketSettledPaymentHandler extends BaseWebSocketSettledHandler<PaymentListener>{


    @Autowired
    PaymentHandler paymentHandler;

    @Override
    protected void registerListener(PaymentListener paymentListener) throws InternalErrorException {
        paymentHandler.registerListener(paymentListener);
    }

    @Override
    protected void unregisterListener(PaymentListener paymentListener) throws InternalErrorException {
        paymentHandler.unregisterListener(paymentListener);
    }
}
