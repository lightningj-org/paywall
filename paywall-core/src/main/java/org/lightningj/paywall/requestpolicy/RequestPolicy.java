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

import org.lightningj.paywall.InternalErrorException;
import org.lightningj.paywall.vo.RequestData;
import org.lightningj.paywall.web.CachableHttpServletRequest;

import java.io.IOException;

/**
 * Request policy is in charge of generating a digest
 * of all significant data in a request that is needed
 * to determine that the call is same that is invoiced.
 *
 * Created by Philip Vendil on 2018-10-25.
 */
public interface RequestPolicy {

    /**
     * Method in charge of generating a digest
     * of all significant data in a request that is needed
     * to determine that the call is same that is invoiced
     *
     * @param request the cachable http servlet request to aggregate request data for.
     * @return a RequestData containing a secure cryptographic digest of all significant request data.
     *
     * @throws IllegalArgumentException if supplied request contained invalid data.
     * @throws IOException if i/o related problems occurred reading the request data.
     * @throws InternalErrorException if internal errors occurred reading the request data.
     */
    RequestData significantRequestDataDigest(CachableHttpServletRequest request) throws IllegalArgumentException, IOException, InternalErrorException;
}
