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
import org.lightningj.paywall.util.DigestUtils;
import org.lightningj.paywall.vo.RequestData;
import org.lightningj.paywall.web.CachableHttpServletRequest;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.Clock;

/**
 * Base class with help methods to generate digest of the significant
 * request data.
 *
 * Created by Philip Vendil on 2018-10-27.
 */
public abstract class BaseRequestPolicy implements RequestPolicy{

    protected Clock clock = Clock.systemDefaultZone();

    /**
     * Method in charge of generating a digest
     * of all significant data in a request that is needed
     * to determine that the call is same that is invoiced
     *
     * @param request the cachable http servlet request to aggregate request data for.
     * @return a secure cryptographic digest of all significant request data.
     *
     * @throws IllegalArgumentException if supplied request contained invalid data.
     * @throws IOException if i/o related problems occurred reading the request data.
     * @throws InternalErrorException if internal errors occurred reading the request data.
     */
    public RequestData significantRequestDataDigest(CachableHttpServletRequest request) throws IllegalArgumentException, IOException, InternalErrorException{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream daos = new DataOutputStream(baos);
        aggregateSignificantData(request,daos);
        return new RequestData(DigestUtils.sha256(baos.toByteArray()),clock.instant());
    }

    /**
     * Method to write all significant data.
     * @param request the cachable http servlet request to aggregate request data for.
     * @param daos data output stream to write data to.
     * @throws IllegalArgumentException if supplied request contained invalid data.
     * @throws IOException if i/o related problems occurred reading the request data.
     * @throws InternalErrorException if internal errors occurred reading the request data.
     */
    protected abstract void aggregateSignificantData(CachableHttpServletRequest request, DataOutputStream daos) throws IllegalArgumentException, IOException, InternalErrorException;
}
