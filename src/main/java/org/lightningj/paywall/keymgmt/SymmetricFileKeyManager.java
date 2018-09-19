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
import javax.crypto.SecretKey;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

/**
 * Symmetric File Key Manager using a AES 256 key stored on disk on a specified location.
 *
 * Created by Philip Vendil on 2018-09-17.
 */
abstract class SymmetricFileKeyManager extends FileKeyManager implements SymmetricKeyManager{

    protected static final String FILENAME = "/secret.key";

    private static final int AES_KEY_LENGTH = 128;

    protected Key secretKey;

    /**
     * Returns the key that should be used for symmetric operations for the given context.
     *
     * @param context related context. Same key is used for all contexts.
     * @return related key.
     * @throws UnsupportedOperationException if operation in combination with given context isn't
     * supported.
     */
    @Override
    public Key getSymmetricKey(KeyManager.Context context) throws UnsupportedOperationException, InternalErrorException {
        if(secretKey == null){
            File keyFile = getKeyFile();
            if(keyFile.exists()){
                secretKey = parseKeyFile(keyFile,getProtectPassphraseWithDefault());
            }else{
                secretKey = generateAndStore(keyFile, getProtectPassphraseWithDefault());
            }
        }
        return secretKey;
    }

    protected File getKeyFile() throws InternalErrorException{
        String path = getDirectory("symmetric key store", "/keys");
        return new File(path + FILENAME);
    }


    protected Key parseKeyFile(File keyFile, char[] protectPassphrase) throws InternalErrorException{
        try {
            byte[] storedData = Files.readAllBytes(keyFile.toPath());
            log.info("Loading existing symmetric key from file: " + keyFile.getPath());
            return KeySerializationHelper.deserializeSecretKey(storedData,protectPassphrase);

        } catch (IOException e) {
            throw new InternalErrorException("Internal error parsing AES key from file " + keyFile.getPath() + ": " + e.getMessage(),e);
        }
    }

    protected Key generateAndStore(File keyFile, char[] protectPassphrase) throws InternalErrorException{
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES", getProvider(null));
            keyGen.init(AES_KEY_LENGTH); // for example
            SecretKey secretKey = keyGen.generateKey();

            FileOutputStream fos = new FileOutputStream(keyFile);
            fos.write(KeySerializationHelper.serializeSecretKey(secretKey,protectPassphrase));
            fos.close();
            log.info("New symmetric key generated and stored in file: " + keyFile.getPath());
            return secretKey;
        }catch(NoSuchAlgorithmException | NoSuchProviderException e){
            throw new InternalErrorException("Internal error generating AES key: " + e.getMessage(),e);
        } catch (IOException e) {
            throw new InternalErrorException("Internal error storing generated AES key to file " + keyFile.getPath() + ": " + e.getMessage(),e);
        }
    }
}
