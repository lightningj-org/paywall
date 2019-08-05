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
package org.lightningj.paywall.requestpolicy;

/**
 * Enumeration specifying type of policy used for aggregating
 * significant request data.
 *
 * Created by Philip Vendil on 2018-10-28.
 */
public enum RequestPolicyType {
    /**
     * Policy that checks the URL and Method of a request.
     */
    URL_AND_METHOD,
    /**
     * Policy that checks the URL and Method and all parameters of a request.
     */
    URL_METHOD_AND_PARAMETERS,
    /**
     * Policy that checks the URL, Method, all parameters and full body data of a HTTP request.
     */
    WITH_BODY,
    /**
     * Custom implementation of calculating significant request data.
     */
    CUSTOM;
}
