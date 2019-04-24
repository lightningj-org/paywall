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
package org.lightningj.paywall.spring.controller;

import org.lightningj.paywall.InternalErrorException;
import org.lightningj.paywall.qrcode.QRCodeGenerator;
import org.lightningj.paywall.spring.PaywallExceptionHandler;
import org.lightningj.paywall.spring.PaywallProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.logging.Logger;

/**
 * Controller to generate a QR Code and return a PNG Image.
 * <p>
 *     Has the following parameters:
 *     <ul>
 *       <li>d: the string to generate qr code for (Required).</li>
 *       <li>w: custom width of generated image (Optional).</li>
 *       <li>h: custom height of generated image (Optional).</li>
 *     </ul>
 * </p>
 */
@Controller
public class GenerateQRCodeController {

    protected static Logger log = Logger.getLogger(GenerateQRCodeController.class.getName());

    public static final String PARAMETER_DATA = "d";
    public static final String PARAMETER_WIDTH = "w";
    public static final String PARAMETER_HEIGHT = "h";

    public static final String PNG_CONTENT_TYPE = "image/png";

    @Autowired
    PaywallProperties paywallProperties;

    @Autowired
    QRCodeGenerator qrCodeGenerator;

    @Autowired
    PaywallExceptionHandler paywallExceptionHandler;

    /**
     * Main controller action generating a QR Code image for a given qr code
     * @param request the related http request.
     * @param response the related http response.
     * @return a settlement response containing either settled=false of a settlement token with meta data.
     */
    @GetMapping("/paywall/genqrcode")
    public void generateQRCode(HttpServletRequest request, HttpServletResponse response) throws Exception{
        // Parse parameters
        String data = getData(request);
        int width = getParameterWithDefaultAsInt(request, PARAMETER_WIDTH, PaywallProperties.QR_CODE_DEFAULT_WIDTH, paywallProperties.getQrCodeDefaultWidth());
        int height = getParameterWithDefaultAsInt(request, PARAMETER_HEIGHT, PaywallProperties.QR_CODE_DEFAULT_HEIGHT, paywallProperties.getQrCodeDefaultHeight());

        // Generate QR Code
        byte[] imageData = qrCodeGenerator.generatePNG(data, width, height);

        // Generate Response
        response.setContentType(PNG_CONTENT_TYPE);
        response.setContentLength(imageData.length);
        response.getOutputStream().write(imageData);
        response.getOutputStream().close();
    }


    /**
     * Method returning the required data parameter 'd' or throws IllegalArgumentException if not set.
     * @param request the http request to parse d parameter for.
     * @return the data to generate QR Code for.
     * @throws IllegalArgumentException if no 'd' parameter found.
     */
    private String getData(HttpServletRequest request) throws MissingServletRequestParameterException {
        String retval = request.getParameter(PARAMETER_DATA);
        if(retval == null || retval.trim().length() == 0){
            throw new IllegalArgumentException("Invalid request, parameter 'd' is required.");
        }
        return retval;
    }

    /**
     * Help method to parse width and heigth parameters with fallback to configured options.
     * @param request the http request to parse parameter for.
     * @param parameterName the name of the parameter to parse
     * @param defaultValue the default value from configuration.
     * @return a parsed parameter.
     */
    private int getParameterWithDefaultAsInt(HttpServletRequest request, String parameterName, String defaultSetting, String defaultValue) throws InternalErrorException, MissingServletRequestParameterException {
        String value = request.getParameter(parameterName);
        if(value == null || value.trim().length() == 0){
            try{
                return Integer.parseInt(defaultValue);
            }catch (NumberFormatException e){
                log.severe("Internal error in paywall configuration, setting '" + defaultSetting + "' must be an integer not " + defaultValue + ".");
                throw new InternalErrorException("Internal error in server when generating QR code.");
            }
        }else{
            try{
                return Integer.parseInt(value);
            }catch (NumberFormatException e){
                throw new IllegalArgumentException("Invalid parameter '" + parameterName + "', should be an integer, not " + value);
            }
        }
    }

    /**
     * Exception handler for this controller.
     * @param request the related http request.
     * @param response the related http response.
     * @param e the related exception
     * @return generated response object.
     */
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<Object> handleException(HttpServletRequest request, HttpServletResponse response, Exception e) {
        return paywallExceptionHandler.handleException(request,response,e);
    }
}
