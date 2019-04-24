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
package org.lightningj.paywall.spring.response;

import javax.xml.bind.annotation.XmlEnum;

/**
 * XML, JSON version of internal Magnetude object.
 */
@XmlEnum()
public enum  Magnetude {

    NONE(org.lightningj.paywall.vo.amount.Magnetude.NONE),
    MILLI(org.lightningj.paywall.vo.amount.Magnetude.MILLI),
    NANO(org.lightningj.paywall.vo.amount.Magnetude.NANO);

    private org.lightningj.paywall.vo.amount.Magnetude intMagnetude;

    Magnetude(org.lightningj.paywall.vo.amount.Magnetude intMagnetude){
        this.intMagnetude = intMagnetude;
    }

    /**
     *
     * @return the internal magnetude enum representation.
     */
    public org.lightningj.paywall.vo.amount.Magnetude asInternalMagnetude(){
        return intMagnetude;
    }

    /**
     * Method to convert an internal magnetude to JSON/XML representation.
     * @param intMagnetude internal magnetude.
     * @return JSON/XML converted version.
     */
    public static Magnetude fromInternalMagnetude(org.lightningj.paywall.vo.amount.Magnetude intMagnetude){
        return Magnetude.valueOf(intMagnetude.name());
    }

}
