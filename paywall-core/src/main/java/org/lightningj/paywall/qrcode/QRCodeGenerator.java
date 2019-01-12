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
package org.lightningj.paywall.qrcode;

import org.lightningj.paywall.InternalErrorException;

/**
 * Interface for generating QR Code of text data used in payment flows.
 *
 * Created by Philip Vendil on 2018-12-18.
 */
public interface QRCodeGenerator {

    /**
     * Method to generate a PNG image QR code of the given text with the dimensions specified.
     * @param data the text data to encode into a QR Code
     * @param width the width of the image.
     * @param height the height of the image.
     * @return the image data in PNG format. null if text data was null.
     * @throws InternalErrorException if internal problem occurred generating the image.
     */
    byte[] generatePNG(String data, int width, int height) throws InternalErrorException;
}
