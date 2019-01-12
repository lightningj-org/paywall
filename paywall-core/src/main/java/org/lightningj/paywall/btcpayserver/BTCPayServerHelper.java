/**
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
package org.lightningj.paywall.btcpayserver;


import org.lightningj.paywall.InternalErrorException;
import org.lightningj.paywall.util.Base58;
import org.lightningj.paywall.util.Signer;

import java.io.UnsupportedEncodingException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.lightningj.paywall.util.DigestUtils.ripeMD160;
import static org.lightningj.paywall.util.DigestUtils.sha256;
import static org.lightningj.paywall.util.HexUtils.decodeHexString;
import static org.lightningj.paywall.util.HexUtils.encodeHexString;

/**
 * Help class for generating SIN value for a given EC Public Key.
 *
 * Created by philip on 2018-10-10.
 */
public class BTCPayServerHelper {

    private static final String version = "0F";
    private static final String type = "02";

    private KeyFactory bcECKeyFactory;
    private Signer signer;

    protected static Logger log =
            Logger.getLogger(BTCPayServerHelper.class.getName());

    public BTCPayServerHelper(){
        try {
           bcECKeyFactory =  KeyFactory.getInstance("EC","BC");
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            log.log(Level.SEVERE,"Internal error setup BTC Pay Server utilities: " + e.getMessage(),e);
        }
        signer = new Signer(Signer.ALG_SHA256_WITH_ECDSA);
    }

    /**
     * Help method to generate the hex representation of a public key used in BTC Pay Server
     * to identify an access key.
     *
     * @param publicKey the public key to convert to BTCPay hex representation.
     * @return hexadecimal encoded of the compressed Q point.
     */
    public String pubKeyInHex(ECPublicKey publicKey) throws InternalErrorException{
        org.bouncycastle.jce.interfaces.ECPublicKey bcPubKey;
        if(publicKey instanceof org.bouncycastle.jce.interfaces.ECPublicKey){
            bcPubKey = (org.bouncycastle.jce.interfaces.ECPublicKey) publicKey;
        }else{
            try {
                bcPubKey = (org.bouncycastle.jce.interfaces.ECPublicKey) bcECKeyFactory.generatePublic(new X509EncodedKeySpec(publicKey.getEncoded()));
            } catch (InvalidKeySpecException e) {
                throw new InternalErrorException("Internal error converting BTC Pay Server Public key to hex, invalid public key: " + e.getMessage(),e);
            }
        }
        return encodeHexString(bcPubKey.getQ().getEncoded(true));
    }

    /**
     * Method to generate a signature header value in BTC Pay Server request, by first appending requestURL and data
     * and then sign it using the private key an ECDSAWithSHA256 algorithm and converting it into hex.
     * @param privateKey the private key to use when signing.
     * @param requestURL the request URL done.
     * @param data the request data (usually the JSON data about to be sent).
     * @return a hexadecimal string of the signature to set as header value in the request.
     * @throws InternalErrorException if problems occurred generating the signature.
     */
    public String genSignature(PrivateKey privateKey, String requestURL, String data) throws InternalErrorException{
        try{
            return encodeHexString(signer.sign(privateKey, (requestURL + data).getBytes("UTF-8")));
        }catch (UnsupportedEncodingException e){
            throw new InternalErrorException("Internal error generating BTCPay Server signature, unsupported encoding UTF-8: " + e.getMessage(),e);
        }
    }

    /**
     * Method to generate a SIN for a given EC Public Key encoded in hex format.
     *
     * @param publicKeyHex hex encoded compressed Q value of public key, same value as uploaded
     *                     to BTC Pay Server.
     * @return the generated SIN value.
     * @throws InternalErrorException if internal errors occurred generating the SIN Value.
     */
    public String toSIN(String publicKeyHex) throws InternalErrorException{
        byte[] pubKeyHash = ripeMD160(sha256(decodeHexString(publicKeyHex)));

        String initialSIN = version + type + encodeHexString(pubKeyHash);
        String last4Bytes = encodeHexString(sha256(sha256(decodeHexString(initialSIN)))).substring(0,8);

        return Base58.encodeToString(decodeHexString(initialSIN + last4Bytes));
    }

}
