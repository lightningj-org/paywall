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
package org.lightningj.paywall.tokengenerator

import org.lightningj.paywall.keymgmt.DummyKeyManager
import org.lightningj.paywall.keymgmt.SymmetricKeyManager
import org.lightningj.paywall.util.BCUtils
import org.lightningj.paywall.util.DigestUtils
import org.lightningj.paywall.vo.PreImageData
import spock.lang.Specification

/**
 * Unit tests for BaseTokenGenerator
 *
 * Created by Philip Vendil on 2018-10-29.
 */
class BaseTokenGeneratorSpec extends Specification {

    SymmetricKeyManager keyManager = new DummyKeyManager()

    BaseTokenGenerator baseTokenGenerator
    def setupSpec(){
        BCUtils.installBCProvider()
    }

    def setup(){
        baseTokenGenerator = new SymmetricKeyTokenGenerator(keyManager)
    }

    def "Verify that correct PreImageData is generated"(){
        when:
        PreImageData d1 = baseTokenGenerator.genPreImageData()
        PreImageData d2 = baseTokenGenerator.genPreImageData()
        then:
        verifyPreImageData(d1)
        verifyPreImageData(d2)
        d1.preImage != d2.preImage

    }

    private void verifyPreImageData(PreImageData data){
        assert data.preImage.length == BaseTokenGenerator.PREIMAGE_LENGTH
        assert DigestUtils.sha256(data.preImage) == data.preImageHash
    }
}
