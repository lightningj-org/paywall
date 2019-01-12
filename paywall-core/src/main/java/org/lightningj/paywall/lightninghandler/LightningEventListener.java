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
package org.lightningj.paywall.lightninghandler;

/**
 * Base interface of components interested in getting notifications about events
 * related to lightning invoices. If this interface is implemented are notifications
 * sent for all invoices.
 *
 * Created by Philip Vendil on 2018-11-24.
 */
public interface LightningEventListener {

    /**
     * This method every time an lightning invoice was added or settled.
     * @param event the related lightning event.
     * @see LightningEvent
     */
    void onLightningEvent(LightningEvent event);
}
