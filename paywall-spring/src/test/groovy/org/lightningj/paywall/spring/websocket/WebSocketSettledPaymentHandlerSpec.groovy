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

import org.lightningj.paywall.paymenthandler.PaymentHandler
import org.lightningj.paywall.paymenthandler.PaymentListener
import spock.lang.Specification

import java.time.Clock

/**
 * Unit tests from WebSocketSettledPaymentHandler
 *
 * @author Philip Vendil 2019-05-18
 */
class WebSocketSettledPaymentHandlerSpec extends Specification {

    WebSocketSettledPaymentHandler handler = new WebSocketSettledPaymentHandler()

    long currentTime = 10000

    def setup(){
        handler.paymentHandler = Mock(PaymentHandler)
        handler.clock = Mock(Clock)
        handler.clock.millis() >> { currentTime }
    }

    def """Verify that registerPaymentListener registers in underlying PaymentHandler and is added to expire structure,
then check hasPaymentListener returns valid value and unregisterPaymentListener unregisters properly and releases
underlying resources"""(){

        when: "Verify registerPaymentListener"
        handler.registerPaymentListener("abc123", 15000, Mock(PaymentListener))
        handler.registerPaymentListener("abc124", 14000, Mock(PaymentListener))
        handler.registerPaymentListener("abc125", 15000, Mock(PaymentListener))
        handler.registerPaymentListener("abc126", 11000, Mock(PaymentListener))
        // Check that already registered preImageHashes are ignored.
        handler.registerPaymentListener("abc126", 13000, Mock(PaymentListener))
        then:
        4 * handler.paymentHandler.registerListener(_ as PaymentListener)
        handler.paymentListenerMap.size() == 4
        handler.paymentListenerMap.get( "abc123") != null
        handler.paymentListenerMap.get( "abc124") != null
        handler.paymentListenerMap.get( "abc125") != null
        handler.paymentListenerMap.get( "abc126") != null
        handler.expiringListeners.size() == 4
        handler.expiringListeners[0].preImageHash == "abc126"
        handler.expiringListeners[1].preImageHash == "abc124"
        handler.expiringListeners[2].preImageHash == "abc125" || handler.expiringListeners[2].preImageHash == "abc123"
        handler.expiringListeners[3].preImageHash == "abc125" || handler.expiringListeners[3].preImageHash == "abc123"

        // Verify hasPaymentListener
        handler.hasPaymentListener("abc124")
        !handler.hasPaymentListener("abc127")

        when: "Verify unregisterPaymentListener"
        handler.unregisterPaymentListener("abc125")
        handler.unregisterPaymentListener("abc124")
        handler.unregisterPaymentListener("abc127")
        then:
        2 * handler.paymentHandler.unregisterListener(_ as PaymentListener)
        handler.paymentListenerMap.size() == 2
        handler.paymentListenerMap.get( "abc123") != null
        handler.paymentListenerMap.get( "abc124") == null
        handler.paymentListenerMap.get( "abc125") == null
        handler.paymentListenerMap.get( "abc126") != null
        handler.expiringListeners.size() == 4 // Nothing have been removed from expiringListeners.
    }

    def "Verify that expiringListeners clean it self up every CLEANUP_SIZE addition to paymentListenerMap and runs in background thread"(){
        when: // First build test structure of 99 listeners and verify that no cleanup have been done.
        // First add 10 valid
        (1..10).each{
            handler.registerPaymentListener("abc" + it, 15000 + it, Mock(PaymentListener))
        }
        // Add 80 expired
        (11 .. 90).each{
            handler.registerPaymentListener("abc" + it, 8000 + it, Mock(PaymentListener))
        }
        // Add 9 valid again
        (91 .. 99).each{
            handler.registerPaymentListener("abc" + it, 17000 + it, Mock(PaymentListener))
        }
        then:
        handler.paymentListenerMap.size() == 99
        handler.expiringListeners.size() == 99
        0 * handler.paymentHandler.unregisterListener(_ as PaymentListener)
        when: // Then add one more and verify that all expired have been cleaned up asyncronically after some time.
        // Unregister one handler to verify that it is not unregisterd again in cleanup.
        handler.unregisterPaymentListener("abc90")
        then:
        handler.paymentListenerMap.size() == 98
        handler.expiringListeners.size() == 99
        when:
        handler.registerPaymentListener("abc100", 16000, Mock(PaymentListener))
        handler.registerPaymentListener("abc101", 18000, Mock(PaymentListener))
        Thread.sleep(500)
        then:
        79 * handler.paymentHandler.unregisterListener(_ as PaymentListener)
        handler.paymentListenerMap.size() == 21
        handler.expiringListeners.size() == 21
    }


}
