package org.lightningj.paywall.util;

import javax.xml.bind.DatatypeConverter;

/**
 * Created by philip on 2018-09-19.
 */
public class HexUtils {

    /**
     * Help method to encode the data into a hexadecimal String
     * @param data
     * @return
     */
    public static String encodeHexString(byte[] data){
        return DatatypeConverter.printHexBinary(data);
    }

    /**
     *
     * @param hexString
     * @return
     */
    public static byte[] decodeHexString(String hexString){
        return DatatypeConverter.parseHexBinary(hexString);
    }
}
