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
package org.lightningj.paywall.spring.util

import spock.lang.Specification

/**
 * Unit tests for PaywallRuntimeException
 */
class PaywallRuntimeExceptionSpec extends Specification {

    def "Verify constructor"(){
        when:
        RuntimeException e = new RuntimeException(new IOException())
        then:
        e.cause instanceof IOException
    }
}
