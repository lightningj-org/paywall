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
 * Unit tests for Hex Utils
 *
 * Created by Philip Vendil on 2018-09-19.
 */
class HexUtilsSpec extends Specification {

    def "Verify encodeHexString encodes to hex string"(){
        expect:
        HexUtils.encodeHexString("123".getBytes("UTF-8")) == "313233"
    }

    def "Verify decodeHexString encodes to hex string"(){
        expect:
        new String(HexUtils.decodeHexString("313233"),"UTF-8") == "123"
    }
}
