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

import org.bouncycastle.util.Arrays;

import java.io.*;
import java.math.BigInteger;

/**
 * Utility class for Base58 encoding and decoding of data. Important, this
 * class in not fully optimized and is recommended to be used only in places
 * where highly optimised encoding is not required.
 *
 * Created by Philip Vendil on 2018-10-10.
 */
public class Base58 {

    private static final char[] BASE58_CHARS = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray();

    private static final BigInteger VAL_58 = new BigInteger("58");

    private static final int[] REVERSE_BASE58_CHARS = new int[128];

    static{
        Arrays.fill(REVERSE_BASE58_CHARS,-1);
        for(int i=0;i<BASE58_CHARS.length;i++){
            REVERSE_BASE58_CHARS[BASE58_CHARS[i]] = (byte) i;
        }
    }

    /**
     * Method to base58 encode data.
     * @param data the data to encode.
     * @return a base58 encoded data string.
     */
    public static byte[] encode(byte[] data) {
        if(data == null){
            return new byte[0];
        }

        ByteArrayOutputStream result = new ByteArrayOutputStream();
        BigInteger val = new BigInteger(1, data);
        while(val.compareTo(BigInteger.ZERO) == 1){
          BigInteger rem = val.remainder(VAL_58);
          val = val.divide(VAL_58);
            result.write((int) BASE58_CHARS[rem.intValue()]);
        }
        for(int i=0;i<data.length;i++){
            if(data[i] == 0){
                result.write((int) BASE58_CHARS[0]);
            }else{
                break;
            }
        }
        return Arrays.reverse(result.toByteArray());
    }

    /**
     * Help method to encode data directly into a String
     * @param data the data to encode.
     * @return String representation of the base58 data.
     */
    public static String encodeToString(byte[] data) {
        String retval = new String(encode(data));
        return retval;
      // return new String(encode(data));
    }

    /**
     * Method to decode a base58 encoded byte array.
     * @param base58EncodedData the base58 encoded data to decode.
     * @return binary representation of the data.
     * @throws IllegalArgumentException if encoded data contained invalid encoding
     */
    public static byte[] decode(byte[] base58EncodedData) throws IllegalArgumentException{
        if(base58EncodedData.length == 0){
            return null;
        }
        byte[] convertedData = new byte[base58EncodedData.length];
       for(int i=0;i<convertedData.length;i++){
           convertedData[i] = (byte) REVERSE_BASE58_CHARS[base58EncodedData[i]];
           if(convertedData[i] < 0){
               throw new IllegalArgumentException("Bad base58 formatted data: " + new String(base58EncodedData));
           }
       }
       
       int zeros = 0;
       while(zeros < convertedData.length && convertedData[zeros]==0){
           zeros++;
       }

       byte[] buffer = new byte[convertedData.length];
       int index = buffer.length;
       for (int i = zeros; i < convertedData.length; ) {
           int rem = 0;
           for (int j = i; j < convertedData.length; j++) {
               int digit = (int) convertedData[j] & 0xFF;
               int temp = rem * 58 + digit;
               convertedData[j] = (byte) (temp / 256);
               rem = temp % 256;
           }
           buffer[--index] = (byte) rem;
           if (convertedData[i] == 0) {
             ++i;
           }
        }

        while (index < buffer.length && buffer[index] == 0) {
            index++;
        }

        return Arrays.copyOfRange(buffer, index - zeros, buffer.length);
    }

    /**
     * Utility method to decode a String directly.
     * @param base58EncodedString the base58 String to decode.
     * @return binary representation of the data.
     * @throws IllegalArgumentException if encoded data contained invalid encoding
     */
    public static byte[] decode(String base58EncodedString) throws IllegalArgumentException{
        return decode(base58EncodedString.getBytes());
    }
}
