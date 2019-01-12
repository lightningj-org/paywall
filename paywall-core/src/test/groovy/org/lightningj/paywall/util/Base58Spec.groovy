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
package org.lightningj.paywall.util

import spock.lang.Specification

/**
 * Unit tests for Base58 utility class.
 *
 * Created by Philip Vendil on 2018-10-10.
 */
class Base58Spec extends Specification {

    def "Verify that Base58 encode() encodes correctly"(){
        expect:
        Base58.encodeToString(HexUtils.decodeHexString("00313233")) == "1HXRC"
        Base58.encodeToString(HexUtils.decodeHexString("005a1fc5dd9e6f03819fca94a2d89669469667f9a074655946")) == "19DXstMaV43WpYg4ceREiiTv2UntmoiA9j"
        Base58.encodeToString("Hello World".getBytes()) == "JxF12TrwUP45BMd"
        Base58.encodeToString(BigInteger.valueOf(3471844090L).toByteArray()) == "16Ho7Hs"
        Base58.encodeToString([0] as byte[]) == "1"
        Base58.encodeToString([0,0,0,0,0,0,0] as byte[]) == "1111111"
        Base58.encodeToString(null) == ""
    }

    def "Verify Base58 decode() decodes correctly"(){
        expect:
        Base58.decode("1HXRC") == HexUtils.decodeHexString("00313233")
        Base58.decode("19DXstMaV43WpYg4ceREiiTv2UntmoiA9j") == HexUtils.decodeHexString("005a1fc5dd9e6f03819fca94a2d89669469667f9a074655946")
        Base58.decode("JxF12TrwUP45BMd") == "Hello World".getBytes()
        Base58.decode("16Ho7Hs") == BigInteger.valueOf(3471844090L).toByteArray()
        Base58.decode("1") == [0] as byte[]
        Base58.decode("1111111") == [0,0,0,0,0,0,0] as byte[]
        Base58.decode("") == null
    }

    def "Verify that decode throws InvalidArgumentException for invalid formatted data"(){
        when:
        Base58.decode("invalöd")
        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Bad base58 formatted data: invalöd"
    }
}
