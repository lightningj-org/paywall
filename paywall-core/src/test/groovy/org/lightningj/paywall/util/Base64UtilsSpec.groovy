package org.lightningj.paywall.util

import spock.lang.Specification

/**
 * Unit tests for Base64Utils.
 *
 * Created by Philip Vendil on 2018-11-23.
 */
class Base64UtilsSpec extends Specification {

    def "Verify encodeHexString encodes to hex string"(){
        expect:
        Base64Utils.encodeBase64String(null) == null
        Base64Utils.encodeBase64String("123".getBytes("UTF-8")) == "MTIz"
    }

    def "Verify decodeHexString encodes to hex string"(){
        expect:
        Base64Utils.decodeBase64String(null) == null
        new String(Base64Utils.decodeBase64String("MTIz"),"UTF-8") == "123"
    }
}
