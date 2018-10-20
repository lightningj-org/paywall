/************************************************************************
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

import spock.lang.Specification

/**
 * Unit tests for BTCPayServerFacade
 *
 * Created by Philip Vendil on 2018-10-14.
 */
class BTCPayServerFacadeSpec extends Specification {

    def "Verify that toString returns in lowercase"(){
        expect:
        BTCPayServerFacade.MERCHANT.toString() == "merchant"
        BTCPayServerFacade.POS.toString() == "pos"
        BTCPayServerFacade.PUBLIC.toString() == "public"
    }
}
