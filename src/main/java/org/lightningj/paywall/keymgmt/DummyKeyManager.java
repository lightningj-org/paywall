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

package org.lightningj.paywall.keymgmt;

import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.lightningj.paywall.InternalErrorException;
import org.lightningj.paywall.btcpayserver.BTCPayServerKeyContext;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Dummy Key Manager that can be used in test setups, should be used in
 * production setups.
 *
 * Created by Philip Vendil on 2018-09-14.
 */
public class DummyKeyManager implements SymmetricKeyManager, AsymmetricKeyManager, RecipientKeyManager {

    private KeyPair asymmetricKeyPair;
    private KeyPair btcPayServerKey;
    private SecretKey symmetricKey;

    public DummyKeyManager(){
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");
            keyPairGenerator.initialize(2048);
            asymmetricKeyPair = keyPairGenerator.genKeyPair();

            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES", "BC");
            keyGenerator.init(256);
            symmetricKey = keyGenerator.generateKey();

            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC","BC");
            ECNamedCurveParameterSpec curveSpec = ECNamedCurveTable.getParameterSpec(DefaultFileKeyManager.BTCPAY_SERVER_ECDSA_CURVE);
            keyGen.initialize(curveSpec);
            btcPayServerKey = keyGen.generateKeyPair();

        }catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
            throw new RuntimeException("Error creating Dummy Key Manager: " + e.getMessage(),e);
        }
    }



    /**
     * Returns the key that should be used for symmetric operations for the given context.
     *
     * @param context related context.
     * @return related key.
     * @throws UnsupportedOperationException if operation in combination with given context isn't
     *                                       supported.
     */
    @Override
    public Key getSymmetricKey(Context context) throws UnsupportedOperationException {
        return symmetricKey;
    }

    /**
     * Returns the public key that should be used for asymmetric operations for the given context.
     *
     * @param context related context.
     * @return related public key.
     * @throws UnsupportedOperationException if operation in combination with given context isn't
     *                                       supported.
     */
    @Override
    public PublicKey getPublicKey(Context context) throws UnsupportedOperationException {
        if(context instanceof BTCPayServerKeyContext){
            return btcPayServerKey.getPublic();
        }
        return asymmetricKeyPair.getPublic();
    }

    /**
     * Returns the private key that should be used for asymmetric operations for the given context.
     *
     * @param context related context.
     * @return related private key.
     * @throws UnsupportedOperationException if operation in combination with given context isn't
     *                                       supported.
     */
    @Override
    public PrivateKey getPrivateKey(Context context) throws UnsupportedOperationException {
        if(context instanceof BTCPayServerKeyContext){
            return btcPayServerKey.getPrivate();
        }
        return asymmetricKeyPair.getPrivate();
    }

    /**
     * Own keys are trusted in dummy setup.
     *
     */
    @Override
    public Map<String,PublicKey> getTrustedKeys(Context context) throws UnsupportedOperationException, InternalErrorException {
        Map<String,PublicKey> keys = new HashMap<>();
        keys.put(KeySerializationHelper.genKeyId(asymmetricKeyPair.getPublic().getEncoded()),asymmetricKeyPair.getPublic());
        return keys;
    }

    /**
     * Own keys are trusted in dummy setup.
     *
     */
    @Override
    public Map<String, PublicKey> getReceipients(Context context) throws UnsupportedOperationException, InternalErrorException {
        Map<String,PublicKey> keys = new HashMap<>();
        keys.put(KeySerializationHelper.genKeyId(asymmetricKeyPair.getPublic().getEncoded()),asymmetricKeyPair.getPublic());
        return keys;
    }

    @Override
    public String getProvider(Context context) {
        return "BC";
    }
}
