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
 * General exception indicating that something went wrong internally
 * not due to client request. Could be miss-configuration or underlying
 * components malfunctioning.
 *
 * Created by Philip Vendil on 2018-09-16.
 */
public class InternalErrorException extends Exception {

    /**
     * General exception indicating that something went wrong internally
     * not due to client request. Could be miss-configuration or underlying
     * components malfunctioning.
     *
     * @param message descriptive message.
     */
    public InternalErrorException(String message){
        super(message);
    }

    /**
     * General exception indicating that something went wrong internally
     * not due to client request. Could be miss-configuration or underlying
     * components malfunctioning.
     *
     * @param message descriptive message.
     * @param cause causing exception.
     */
    public InternalErrorException(String message, Throwable cause){
        super(message,cause);
    }
}
