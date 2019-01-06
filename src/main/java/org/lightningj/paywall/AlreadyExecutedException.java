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

package org.lightningj.paywall;

/**
 * Exception thrown if related payment flow is payPerRequest and related
 * settlement has already been settled.
 *
 * Created by Philip Vendil on 2019-01-05.
 */
public class AlreadyExecutedException extends Exception {

    private byte[] preImageHash;

    /**
     * General exception indicating that something went wrong internally
     * not due to client request. Could be miss-configuration or underlying
     * components malfunctioning.
     *
     * @param preImageHash of related already executed invoice.
     * @param message descriptive message.
     */
    public AlreadyExecutedException(byte[] preImageHash, String message){
        super(message);
        this.preImageHash = preImageHash;
    }

    /**
     *
     * @return preImageHash of related already executed invoice.
     */
    public byte[] getPreImageHash() {
        return preImageHash;
    }
}
