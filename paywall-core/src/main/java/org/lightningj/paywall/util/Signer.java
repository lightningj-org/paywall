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
package org.lightningj.paywall.util;

import org.lightningj.paywall.InternalErrorException;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;

/**
 * Help methods to ECDSA Sign a given text.
 *
 * Created by philip on 2018-10-11.
 */
public class Signer {

    public  static final String ALG_SHA256_WITH_ECDSA = "SHA256withECDSA";

    private static final String SIGN_PROV = "BC";

    private String algorithm;
    private String provider;
    private Signature signer;

    /**
     * Constructor using the specified algoritm (one of ALG_ constants)
     * when generating signatures.
     *
     * Uses BC as provider.
     *
     * @param algorithm the algorithm to use.
     */
    public Signer(String algorithm){
        this(algorithm, SIGN_PROV);
    }

    /**
     * Constructor using the specified algoritm (one of ALG_ constants)
     * and provider when generating signatures.
     * @param algorithm the algorithm to use.
     * @param provider the provider to use.
     */
    public Signer(String algorithm, String provider){
        this.algorithm = algorithm;
        this.provider = provider;

    }

    /**
     * Method to generate a cryptographic signature of the data using the specificed algorithm
     * and the given curve of the given private key.
     * @param privKey the private key to use for the signature.
     * @param data the data to hash and sign.
     * @return the DER encoded signature.
     * @throws InternalErrorException if internal problems occurred generating the signature.
     */
    public byte[] sign(PrivateKey privKey, byte[] data) throws InternalErrorException{
        try {
            this.signer = Signature.getInstance(algorithm,provider);
            signer.initSign(privKey);
            signer.update(data);
            return signer.sign();
        }catch(Exception e){
            throw new InternalErrorException("Internal problem occurred generating " + algorithm + " signature: " + e.getMessage(),e);
        }
    }

    /**
     *
     * @param pubKey
     * @param data
     * @param signature
     * @return
     * @throws InternalErrorException
     */
    public boolean verify(PublicKey pubKey, byte[] data, byte[] signature) throws InternalErrorException{
        try {
            Signature signer = Signature.getInstance(algorithm,provider);
            signer.initVerify(pubKey);
            signer.update(data);
            return signer.verify(signature);
        }catch(Exception e){
            throw new InternalErrorException("Internal problem occurred verifying " + algorithm + " signature: " + e.getMessage(),e);
        }
    }
}
