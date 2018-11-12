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

import javax.xml.bind.DatatypeConverter;

/**
 * Help method for hex encoding/decoding.
 *
 * Created by Philip Vendil on 2018-09-19.
 */
public class HexUtils {

    /**
     * Help method to encode the data into a hexadecimal String
     * @param data the data to hex encode
     * @return String representation of the hex encoded data.
     */
    public static String encodeHexString(byte[] data){
        if(data == null){
            return null;
        }
        return DatatypeConverter.printHexBinary(data);
    }

    /**
     * Method to decode a hexadecimal encoded string.
     *
     * @param hexString the hexadecimal string to decode
     * @return the decoded data.
     */
    public static byte[] decodeHexString(String hexString){
        if(hexString == null){
            return null;
        }
        return DatatypeConverter.parseHexBinary(hexString);
    }
}
