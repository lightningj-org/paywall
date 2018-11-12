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

import org.lightningj.paywall.InternalErrorException;
import org.lightningj.paywall.util.DigestUtils;
import org.lightningj.paywall.vo.PreImageData;

import java.security.SecureRandom;

/**
 * Default abstract base class containing help method for implementing
 * TokenGenerators.
 *
 * Created by Philip Vendil on 2018-10-29.
 */
public abstract class BaseTokenGenerator implements TokenGenerator{

    public static final int PREIMAGE_LENGTH = 32;

    SecureRandom secureRandom = null;
    /**
     * Method that should generate a random pre image data used to
     * create invoice.
     *
     * @return a newly created unique PreImageData
     * @throws InternalErrorException if internal errors occurred generating
     *                                the pre image data.
     */
    @Override
    public PreImageData genPreImageData() throws InternalErrorException {
        byte[] preImage = new byte[PREIMAGE_LENGTH];
        getSecureRandom().nextBytes(preImage);
        byte[] preImageHash = DigestUtils.sha256(preImage);
        return new PreImageData(preImage,preImageHash);
    }

    protected SecureRandom getSecureRandom() throws InternalErrorException{
        if(secureRandom == null) {
            try {
                secureRandom = new SecureRandom();
            } catch (Exception e) {
                throw new InternalErrorException("Internal error generating SecureRandom for TokenGenerator, message: " + e.getMessage(), e);
            }
        }
        return secureRandom;
    }
}
