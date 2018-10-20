/*
 * **********************************************************************
 *                                                                       *
 *  LightningJ                                                           *
 *                                                                       *
 *  This software is free software; you can redistribute it and/or       *
 *  modify it under the terms of the GNU General Public License          *
 *  License as published by the Free Software Foundation; either         *
 *  version 3 of the License, or any later version.                      *
 *                                                                       *
 *  See terms of license at gnu.org.                                     *
 *                                                                       *
 *************************************************************************/
package org.lightningj.paywall.btcpayserver

import org.lightningj.paywall.btcpayserver.BTCPayServerKeyContext
import spock.lang.Specification

/**
 * Unit tests for BTCPayServerKeyContext
 *
 * Created by philip on 2018-10-07.
 */
class BTCPayServerKeyContextSpec extends Specification {

    def "Verify toString"(){
        expect:
        BTCPayServerKeyContext.INSTANCE.toString() == "BTCPayServerKeyContext{}"
    }
}
