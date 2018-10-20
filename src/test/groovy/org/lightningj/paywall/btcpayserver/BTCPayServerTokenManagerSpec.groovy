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
package org.lightningj.paywall.btcpayserver

import org.lightningj.paywall.btcpayserver.vo.Token
import spock.lang.Specification

import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit

import static BTCPayServerFacade.*

/**
 * Unit tests for BTCPayServerTokenManager
 * Created by Philip Vendil on 2018-10-14.
 */
class BTCPayServerTokenManagerSpec extends Specification {

    BTCPayServerTokenManager mngr = new BTCPayServerTokenManager()

    def "Verify that put sets token in cache get returns non expired only"(){
        setup:
        mngr.clock = Mock(Clock)
        mngr.clock.instant() >> { Instant.ofEpochMilli(60 * 60 * 1000)}

        def validToken = new Token("abc123",Instant.ofEpochMilli(60*60*1000).minus(16,ChronoUnit.MINUTES),MERCHANT)
        def expiredToken = new Token("abc124",Instant.ofEpochMilli(60*60*1000).minus(14,ChronoUnit.MINUTES),POS)

        when: // Verify that put sets tokens correctly
        mngr.put(validToken)
        mngr.put(expiredToken)

        then:
        mngr.tokenCache.size() == 2
        mngr.tokenCache[MERCHANT] == validToken
        mngr.tokenCache[POS] == expiredToken

        mngr.get(MERCHANT) == validToken
        mngr.get(POS) == null
        mngr.tokenCache.size() == 1

        when:
        mngr.clear()
        then:
        mngr.tokenCache.size() == 0
    }
}
