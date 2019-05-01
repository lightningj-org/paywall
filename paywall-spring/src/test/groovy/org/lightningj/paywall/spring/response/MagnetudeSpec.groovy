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
package org.lightningj.paywall.spring.response

import spock.lang.Specification
import spock.lang.Unroll

/**
 * Unit test for json/xml representation of Magnitude.
 */
class MagnetudeSpec extends Specification {

    @Unroll
    def "Expect that internal magnetude #intMagnetude is same as #magnetude"() {
        expect:
        magnetude.asInternalMagnetude() == intMagnetude

        where:
        magnetude       | intMagnetude
        Magnetude.NONE  | org.lightningj.paywall.vo.amount.Magnetude.NONE
        Magnetude.MILLI | org.lightningj.paywall.vo.amount.Magnetude.MILLI
        Magnetude.NANO  | org.lightningj.paywall.vo.amount.Magnetude.NANO
    }

    @Unroll
    def "Expect fromInternalMagnetude converts correctly from #intMagnetude to #magnetude"() {
        expect:
        Magnetude.fromInternalMagnetude(intMagnetude) == magnetude

        where:
        magnetude       | intMagnetude
        Magnetude.NONE  | org.lightningj.paywall.vo.amount.Magnetude.NONE
        Magnetude.MILLI | org.lightningj.paywall.vo.amount.Magnetude.MILLI
        Magnetude.NANO  | org.lightningj.paywall.vo.amount.Magnetude.NANO
    }
}
