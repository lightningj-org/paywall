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
package org.lightningj.paywall.qrcode

import org.lightningj.paywall.util.Base64Utils
import spock.lang.Specification

/**
 * Unit tests for DefaultQRCodeGenerator.
 *
 * Created by Philip Vendil on 2018-12-18.
 */
class DefaultQRCodeGeneratorSpec extends Specification {

    DefaultQRCodeGenerator generator = new DefaultQRCodeGenerator()

    def "Verify that generatePNG generates a valid QRCode"(){
        expect:
        generator.generatePNG(null,10,10) == null
        Base64Utils.encodeBase64String(generator.generatePNG("lntb500u1pwpj37xpp55935pf8dl3ky5ppzqp08axy4h4a7vuyu9dgzycln38s4nr45xhrsdqqcqzysrzjq0c8ywxvx4kz6u8nr8gff36vprk63n349xc5pk7ttj9fpc3585fl5900kvqqqpqqqqqqqqlgqqqqqqgqjqf5ejaw67g3ag90tjzqm32wmn8hjrlp9rdqhzv3jylzllefeq4uep2q455qy683txl2ffq28hfllq7lsk706h9yh83s656syrqwh26hsqv27wk7",200,200)) == expectedQRCode
    }

    def expectedQRCode = "iVBORw0KGgoAAAANSUhEUgAAAMgAAADIAQAAAACFI5MzAAADMUlEQVR42u3XPdKtKBAGYEhgC5DI1jTBLUjCT6JbkAS3hglsAROYnppgzrlV3tvxzGf6VHkQmrf7kPH2kB/5kR/570onvMYYg98EP8VMHEZcsWsYIxzeztvaUTKvWY6Yl3oXlzXDiaaJmCS9NVljZd153BnRQptQcOKKMEk1K7TyvHaUEKb/fb737U3guZI7hTXnJOsv5/MincwuyaeTyd1tlhEjI7vnqFnwJtYxjgMjnXo1br94HuKxK+8QMrwV8koTI3bjt7cY6YLGzuQpmLpOLe+BkBEez5+itXBlcuFAyTii17LTImTdp69TeJNurifTbsrSN9q0GQgZWavghQmnqvAFxWEkdNlNp83FXaidD4yMLMSSLa+hzUuaHUa6TIzv0svhTWwCJSHUczbjajT66XvVbwIlqF0svE3uaCbUgZCRaJb1ZEuj187s5/m8SqcnT8zVQrtenn8K8Y9CZrmvzy5PFRvjFSPjHs352d1JHddTL4x0iBW+Cwe3y667ag4hI8ldZk1vCCV5NTUwshPCmIHwu599JtphpNvliHm2LtxXsnIgBBJWrElvmo6rML05hIxkyVKPZnm38rQWJZ7o1euJEPcUQuhASF/i/TyxqOcJaVsfjIyrendf58ToqQnPDiHdQiSN0MwdvUnT5+68i7oz0bJJP0k/s4/b+BsRKtaY+X2fTEGkD4zQkDd1Gi+YKWQ5HUKgeq8Llg6JucHbxkAJ1CD8cBbmymRjDiNpM7XmSTW5T+o+UTKODn2MekZjpjWhJKT1PhIksp3hFDaMdMtjJyZ6JqMXM8PI8NPsrjKbBmnU+efuvEo3nsO4UY8iR/D0s5++yzTxeFo2L10bmL0GRuQNUxD0isk9sKmfVfUqY980NEAGsQ/z0/OZb++SmRWmCbghT2Hss2O8Smdm12uCqwIL6PJECS1MENmINTGTzxR7l78z1j2ZWJm0EF/59iowpW0GbpV2NVavv+e3N3HVz9D+rSpW5q+3vcusFXQZT7YVJumv7/mdEOVhfk6y9olNSNlU4YeHFlPoU1DiiqZQ6nZiAiImoYQwlZkZAYL23GTGyM//rB/5kf+R/AUYW4TuGDPk5wAAAABJRU5ErkJggg=="


}
