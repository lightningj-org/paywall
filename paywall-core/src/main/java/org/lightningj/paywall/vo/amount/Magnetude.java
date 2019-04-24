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
package org.lightningj.paywall.vo.amount;

/**
 * Represents the magnitude of a unit, usually a subunit of a crypto currency.
 * For example if BTC is used, the value is specified in satoshi, but with a magnitude
 * of milli is the value specified in millisatoshi.
 *
 * Created by Philip Vendil on 2018-11-07.
 */
public enum Magnetude {

    NONE,
    MILLI,
    NANO
}
