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
package org.lightningj.paywall.spring.websocket

import org.lightningj.paywall.util.Base58
import spock.lang.Specification

import java.security.SecureRandom
import java.util.logging.Level
import java.util.logging.Logger

import static PaywallWebSocketConfig.*
/**
 * Unit tests for PaywallWebSocketConfig
 * <p>
 *     The major part of PaywallWebSocketConfig is done in the integration test in
 *     springboot2 project. Only utility methods are tested here.
 * </p>
 * @author Philip Vendil 2019-05-22
 */
class PaywallWebSocketConfigSpec extends Specification {

    PaywallWebSocketConfig paywallWebSocketConfig = new PaywallWebSocketConfig()
    String preImageHash

    def setup(){
        paywallWebSocketConfig.webSocketSettledPaymentHandler = Mock(WebSocketSettledPaymentHandler)
        paywallWebSocketConfig.log = Mock(Logger)

        byte[] preImageHashData = new byte[32]
        SecureRandom random = new SecureRandom()
        random.nextBytes(preImageHashData)
        preImageHash = Base58.encodeToString(preImageHashData)
    }

    def "Verify that unregisterListener with correct destination parses preImageHash correctly and calls webSocketSettledPaymentHandler"(){
        when:
        paywallWebSocketConfig.unregisterListener(CHECK_SETTLEMENT_QUEUE_PREFIX+ preImageHash)
        then:
        1 * paywallWebSocketConfig.webSocketSettledPaymentHandler.unregisterPaymentListener(preImageHash)
    }

    def "Verify that unregisterListener with incorrect destination does not call webSocketSettledPaymentHandler"(){
        when:
        paywallWebSocketConfig.unregisterListener("/other/"+ preImageHash)
        then:
        0 * paywallWebSocketConfig.webSocketSettledPaymentHandler.unregisterPaymentListener(_)
    }

    def "Verify that unregisterListener logs exception that happens when calls webSocketSettledPaymentHandler"(){
        when:
        paywallWebSocketConfig.unregisterListener(CHECK_SETTLEMENT_QUEUE_PREFIX+ preImageHash)
        then:
        1 * paywallWebSocketConfig.webSocketSettledPaymentHandler.unregisterPaymentListener(preImageHash) >> { throw new IOException("test")}
        1 * paywallWebSocketConfig.log.log(Level.SEVERE,"Paywall Internal Error Exception occurred when unregistering WebSocket listener: test",_ as IOException)
    }
}
