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

package org.lightningj.paywall.tokengenerator;

/**
 * Exception thrown by TokenGenerator if verification of a token failed.
 *
 * Created by Philip Vendil on 2018-11-11.
 */
public class TokenException extends Exception {

    private Reason reason;

    /**
     * Exception thrown by TokenGenerator if verification of a token failed.
     *
     * @param message descriptive message.
     */
    public TokenException(String message, Reason reason){
        super(message);
        this.reason = reason;
    }

    /**
     * Exception thrown by TokenGenerator if verification of a token failed.
     *
     * @param message descriptive message.
     * @param cause causing exception.
     */
    public TokenException(String message, Throwable cause, Reason reason){
        super(message,cause);
        this.reason = reason;
    }

    /**
     *
     * @return the reason on why that was wrong with to token
     */
    public Reason getReason() {
        return reason;
    }

    /**
     * A List of reasons on why that was wrong with to token.
     */
    public enum Reason{
        EXPIRED,
        NOT_YET_VALID,
        NOT_FOUND,
        INVALID
    }
}
