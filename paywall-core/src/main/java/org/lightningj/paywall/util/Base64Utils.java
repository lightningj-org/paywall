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
package org.lightningj.paywall.util;

import java.util.Base64;

/**
 * Help method for base64 encoding/decoding.
 *
 * Created by Philip Vendil on 2018-09-19.
 */
public class Base64Utils {

    /**
     * Help method to encode the data into a hexadecimal String
     * @param data the data to hex encode
     * @return String representation of the hex encoded data.
     */
    public static String encodeBase64String(byte[] data){
        if(data == null){
            return null;
        }
        return Base64.getEncoder().encodeToString(data);
    }

    /**
     * Method to decode a base64 encoded string.
     *
     * @param b64String the base64 string to decode
     * @return the decoded data.
     */
    public static byte[] decodeBase64String(String b64String){
        if(b64String == null){
            return null;
        }
        return Base64.getDecoder().decode(b64String);
    }
}
