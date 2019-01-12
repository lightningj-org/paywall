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
 * Special class used as default custom Request Policy that signal that
 * one of the default defined request policys should be used.
 *
 * Created by philip on 2018-12-18.
 */
public class NoCustomRequestPolicy implements RequestPolicy {

    /**
     * This method is not used.
     */
    @Override
    public RequestData significantRequestDataDigest(CachableHttpServletRequest request) throws IllegalArgumentException, IOException, InternalErrorException {
        return null;
    }
}
