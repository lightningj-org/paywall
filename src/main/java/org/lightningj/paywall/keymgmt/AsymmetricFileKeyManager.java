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
package org.lightningj.paywall.keymgmt;

import org.lightningj.paywall.InternalErrorException;

import javax.crypto.KeyGenerator;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.*;

/**
 * Asymmetric File Key Manager using a RSA 2048 but key stored on disk on a specified location.
 *
 * Created by Philip Vendil on 2018-09-17.
 */
abstract class AsymmetricFileKeyManager extends FileKeyManager implements AsymmetricKeyManager {

    protected static final String PRIVATE_KEYNAME = "/asynckey.prv";
    protected static final String PUBLIC_KEYNAME = "/asynckey.pub";

    private static final int RSA_KEY_LENGTH = 2048;

    protected KeyPair keyPair;

    // TODO Truststore

//    /**
//     * Returns the key that should be used for symmetric operations for the given context.
//     *
//     * @param context related context. Same key is used for all contexts.
//     * @return related key.
//     * @throws UnsupportedOperationException if operation in combination with given context isn't
//     * supported.
//     */
//    @Override
//    public Key getSymmetricKey(Context context) throws UnsupportedOperationException, InternalErrorException {
//        if(secretKey == null){
//            File keyFile = getKeyFile();
//            if(keyFile.exists()){
//                secretKey = parseKeyFile(keyFile,getProtectPassphraseWithDefault());
//            }else{
//                secretKey = generateAndStore(keyFile, getProtectPassphraseWithDefault());
//            }
//        }
//        return secretKey;
//    }

    protected File getPublicKeyFile() throws InternalErrorException{
        String path = getDirectory("asymmetric key store", "/keys");
        return new File(path + PUBLIC_KEYNAME);
    }

    protected File getPrivateKeyFile() throws InternalErrorException{
        String path = getDirectory("asymmetric key store", "/keys");
        return new File(path + PRIVATE_KEYNAME);
    }


    protected KeyPair parseKeyFile(File publicKeyFile, File privateKeyFile, char[] protectPassphrase) throws InternalErrorException{
        try {
            byte[] publicKeyData = Files.readAllBytes(publicKeyFile.toPath());
            byte[] privateKeyData = Files.readAllBytes(privateKeyFile.toPath());
            log.info("Loading existing asymmetric key from files " + publicKeyFile.getPath() + " and " + privateKeyFile.getPath());
            return KeySerializationHelper.deserializeKeyPair(publicKeyData,privateKeyData,protectPassphrase, KeyFactory.getInstance("RSA",getProvider(null)));

        } catch (Exception e) {
            throw new InternalErrorException("Internal error parsing RSA key from files " + publicKeyFile.getPath() + " and " + privateKeyFile.getPath() + ": " + e.getMessage(),e);
        }
    }

    protected KeyPair generateAndStore(File publicKeyFile, File privateKeyFile, char[] protectPassphrase) throws InternalErrorException{
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA", getProvider(null));
            keyGen.initialize(RSA_KEY_LENGTH); // for example
            KeyPair keyPair = keyGen.generateKeyPair();

            byte[][] keyData = KeySerializationHelper.serializeKeyPair(keyPair,protectPassphrase);

            FileOutputStream fos = new FileOutputStream(publicKeyFile);
            fos.write(keyData[0]);
            fos.close();
            fos = new FileOutputStream(privateKeyFile);
            fos.write(keyData[1]);
            fos.close();
            log.info("New asymmetric key generated and stored in files " + publicKeyFile.getPath() + " and " + privateKeyFile.getPath());
            return keyPair;
        }catch(NoSuchAlgorithmException | NoSuchProviderException e){
            throw new InternalErrorException("Internal error generating RSA key: " + e.getMessage(),e);
        } catch (IOException e) {
            throw new InternalErrorException("Internal error storing generated RAS key pair to files " + publicKeyFile.getPath() + " and " + privateKeyFile.getPath() + " : " + e.getMessage(),e);
        }
    }
}
