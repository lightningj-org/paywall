/************************************************************************
 *                                                                       *
 *  LightningJ                                                           *
 *                                                                       *
 *  This software is free software; you can redistribute it and/or       *
 *  modify it under the terms of the GNU General Public License          *
 *  License as published by the Free Software Foundation; either         *
 *  version 3 of the License, or any later version.                      *
 *                                                                       *
 *  See terms of license at gnu.org.                                     *
 *                                                                       *
 *************************************************************************/
package org.lightningj.paywall.util;

import org.lightningj.paywall.InternalErrorException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Digest helper is a utility class to create digests.
 *
 * Created by Philip Vendil on 2018-09-16.
 */
public class DigestUtils {

    private static MessageDigest sha256Digest;

    public static byte[] sha256(byte[] data) throws InternalErrorException {
        if(sha256Digest == null){
            try {
                sha256Digest = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                throw new InternalErrorException("Internal error generated SHA256 digest: " + e.getMessage(),e);
            }
        }
        sha256Digest.reset();
        sha256Digest.update(data);
        return sha256Digest.digest();
    }
}
