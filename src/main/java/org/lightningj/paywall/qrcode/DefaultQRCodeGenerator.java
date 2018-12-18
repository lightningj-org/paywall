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

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.lightningj.paywall.InternalErrorException;

import java.io.ByteArrayOutputStream;

/**
 * Default implementation of QRCode generation using the zxing library to generate images.
 *
 * Created by Philip Vendil on 2018-12-18.
 */
public class DefaultQRCodeGenerator implements QRCodeGenerator{

    /**
     * Method to generate a PNG image QR code of the given text with the dimensions specified.
     * @param data the text data to encode into a QR Code
     * @param width the width of the image.
     * @param height the height of the image.
     * @return the image data in PNG format. null if text data was null.
     * @throws InternalErrorException if internal problem occurred generating the image.
     */
    @Override
    public byte[] generatePNG(String data, int width, int height) throws InternalErrorException{
        if(data == null){
            return null;
        }
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, width, height);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", baos);
            return baos.toByteArray();
        }catch (Exception e){
            throw new InternalErrorException("Internal error generating QR Code: " + e.getMessage(), e);
        }
    }
}
