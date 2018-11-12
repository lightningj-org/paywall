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

import javax.servlet.http.HttpServletRequest;

/**
 * Interface specifying a cachable HTTP Servlet Request where input data is
 * cached and can be run multiple times.
 *
 * Created by Philip Vendil on 2018-10-25.
 */
public interface CachableHttpServletRequest extends HttpServletRequest {

    /**
     * Method to returned cached content of the stream data in order
     * to calculate payment data.
     * @return cached content of the request. null if no body.
     */
    byte[] getCachedContent();
}
