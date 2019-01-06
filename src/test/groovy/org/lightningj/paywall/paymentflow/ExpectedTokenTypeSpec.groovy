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
package org.lightningj.paywall.paymentflow

import spock.lang.Specification
import spock.lang.Unroll

import static org.lightningj.paywall.paymentflow.ExpectedTokenType.*
import static org.lightningj.paywall.tokengenerator.TokenContext.*

/**
 * Unit tests for ExpectedTokenType
 */
class ExpectedTokenTypeSpec extends Specification {

    @Unroll
    def "Expect #expectedTokenType getTokenContext() returns context #expectedContext"(){
        expect:
        expectedTokenType.getTokenContext() == expectedContext
        where:
        expectedTokenType      | expectedContext
        PAYMENT_TOKEN          | CONTEXT_PAYMENT_TOKEN_TYPE
        INVOICE_TOKEN          | CONTEXT_INVOICE_TOKEN_TYPE
        SETTLEMENT_TOKEN       | CONTEXT_SETTLEMENT_TOKEN_TYPE
    }
}
