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

import org.bouncycastle.jce.provider.BouncyCastleProvider
import spock.lang.Specification

import java.security.Security

/**
 * Unit tests for BCUtils.
 *
 * Created by Philip Vendil on 2018-09-19.
 */
class BCUtilsSpec extends Specification {

    def "Verify that installBCProvider only installs itself once, even though called multiple times"(){
        when:
        BCUtils.installBCProvider()
        BCUtils.installBCProvider()
        then:
        Security.getProvider("BC") instanceof BouncyCastleProvider
        Security.providers.findAll{it instanceof BouncyCastleProvider}.size() == 1
    }
}
