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

import spock.lang.Specification

/**
 * Unit tests for TokenException
 */
class TokenExceptionSpec extends Specification {

    def "Verify constructor and getter of reason"(){
        when:
        def te = new TokenException("test", TokenException.Reason.EXPIRED)
        then:
        te.message == "test"
        te.reason == TokenException.Reason.EXPIRED

        when:
        te = new TokenException("test", new IOException(), TokenException.Reason.EXPIRED)
        then:
        te.message == "test"
        te.cause instanceof IOException
        te.reason == TokenException.Reason.EXPIRED
    }
}
