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
package org.lightningj.paywall.spring.util;

import org.lightningj.paywall.web.CachableHttpServletRequest;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.http.HttpServletRequest;

/**
 * A specialized version of Spring ContentCachingRequestWrapper that also
 * implements CachableHttpServletRequest.
 *
 * @author philip 2019-02-14
 */
public class SpringCachableHttpServletRequest extends ContentCachingRequestWrapper implements CachableHttpServletRequest {
    /**
     * Create a new SpringCachableHttpServletRequest for the given servlet request.
     *
     * @param request the original servlet request
     */
    public SpringCachableHttpServletRequest(HttpServletRequest request) {
        super(request);
    }

    /**
     * Create a new SpringCachableHttpServletRequest for the given servlet request.
     *
     * @param request           the original servlet request
     * @param contentCacheLimit the maximum number of bytes to cache per request
     * @see #handleContentOverflow(int)
     * @since 4.3.6
     */
    public SpringCachableHttpServletRequest(HttpServletRequest request, int contentCacheLimit) {
        super(request, contentCacheLimit);
    }

    /**
     * Method to returned cached content of the stream data in order
     * to calculate payment data.
     *
     * @return cached content of the request. null if no body.
     */
    @Override
    public byte[] getCachedContent() {
        return getContentAsByteArray();
    }
}
