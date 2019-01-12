package org.lightningj.paywall.paymenthandler

import spock.lang.Specification

/**
 * Unit tests for BasePaymentListener.
 *
 * Created by philip on 2018-12-05.
 */
class BasePaymentListenerSpec extends Specification {

    def "Verify constructor and getters"(){
        when:
        def l = new TestPaymentListener("abc".bytes,PaymentEventType.INVOICE_CREATED, true)
        then:
        l.getPreImageHash() == "abc".bytes
        l.getType() == PaymentEventType.INVOICE_CREATED
        l.unregisterAfterEvent()
    }

    static class TestPaymentListener extends BasePaymentListener{

        TestPaymentListener(byte[] preImageHash, PaymentEventType type, boolean unregisterAfterEvent) {
            super(preImageHash, type, unregisterAfterEvent)
        }

        @Override
        void onPaymentEvent(PaymentEvent event) {

        }
    }
}
