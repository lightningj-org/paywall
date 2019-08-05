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
import org.lightningj.paywall.btcpayserver.BTCPayServerHelper;


import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.*;
import java.security.interfaces.ECPublicKey;
import java.time.Clock;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Asymmetric and Symmetric File Key Manager using a RSA 2048 but key stored on disk on a specified location.
 *
 * Created by Philip Vendil on 2018-09-17.
 */
public abstract class DefaultFileKeyManager extends FileKeyManager implements AsymmetricKeyManager, SymmetricKeyManager {

    static final String ASYM_PRIVATE_KEYNAME = "/asymkey_prv.pem";
    static final String ASYM_PUBLIC_KEYNAME = "/asymkey_pub.pem";

    protected static final String BTCPAY_SERVER_PRIVATE_KEYNAME = "/btcpayserver_key_prv.pem";
    protected static final String BTCPAY_SERVER_PUBLIC_KEYNAME = "/btcpayserver_key_pub_sin_@SIN@.pem";

    protected static final String SYMMENTRIC_FILENAME = "/secret.key";

    private static final long CACHE_TIME = 5 * 60 * 1000; // 5 Min

    private static final int RSA_KEY_LENGTH = 2048;

    static final String BTCPAY_SERVER_ECDSA_CURVE="secp256k1";

    private static final int AES_KEY_LENGTH = 128;

    private KeyFactory rsaKeyFactory;
    private KeyFactory ecKeyFactory;
    KeyPair asymKeyPair;
    KeyPair btcKeyPair;
    protected Key secretKey;

    long trustStoreCacheExpireDate = 0;
    Map<String,PublicKey> trustedSigningKeysCache = new ConcurrentHashMap<>();

    protected Clock clock = Clock.systemDefaultZone();

    protected BTCPayServerHelper btcPayServerHelper = new BTCPayServerHelper();

    /**
     * Returns the public key that should be used for asymmetric operations for the given context.
     *
     * @param context related context.
     * @return related public key.
     * @throws UnsupportedOperationException if operation in combination with given context isn't
     *                                       supported.
     */
    @Override
    public PublicKey getPublicKey(Context context) throws UnsupportedOperationException, InternalErrorException {
        if(context instanceof BTCPayServerKeyContext){
            return getBTCPayServerKeyPair().getPublic();
        }
        return getAsymKeyPair().getPublic();
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
    public PrivateKey getPrivateKey(Context context) throws UnsupportedOperationException, InternalErrorException {
        if(context instanceof BTCPayServerKeyContext){
            return getBTCPayServerKeyPair().getPrivate();
        }
        return getAsymKeyPair().getPrivate();
    }

    /**
     * Method retrieve a list of trusted public keys used to verify signatures.
     *
     * @param context related context.
     * @return A map of keyId of trusted public keys.
     * @throws UnsupportedOperationException if operation in combination with given context isn't
     * supported.
     * @throws InternalErrorException if internal error occurred retrieving the public keys.
     */
    @Override
    public Map<String,PublicKey> getTrustedKeys(Context context) throws UnsupportedOperationException, InternalErrorException {
        if(hasCacheExpired(trustStoreCacheExpireDate)){
            synchronized (this){
                trustedSigningKeysCache.clear();
                for(File trustedKeyFile : getAsymTrustStoreFiles()){
                    try {
                        log.fine("Parsing trusted public key file: " + trustedKeyFile.getPath());
                        PublicKey trustedKey = KeySerializationHelper.deserializePublicKey(Files.readAllBytes(trustedKeyFile.toPath()), getRSAKeyFactory());
                        trustedSigningKeysCache.put(KeySerializationHelper.genKeyId(trustedKey.getEncoded()),trustedKey);
                    }catch(Exception e){
                        log.log(Level.SEVERE,"Error parsing trusted public key file: "+ trustedKeyFile.getPath() + ", error: " + e.getMessage(),e);
                    }
                }
                trustStoreCacheExpireDate = clock.millis() + CACHE_TIME;
            }
        }

        return trustedSigningKeysCache;
    }


    /**
     * Returns the key that should be used for symmetric operations for the given context.
     *
     * @param context related context. Same key is used for all contexts.
     * @return related key.
     * @throws UnsupportedOperationException if operation in combination with given context isn't
     * supported.
     */
    @Override
    public Key getSymmetricKey(Context context) throws UnsupportedOperationException, InternalErrorException {
        if(secretKey == null){
            File keyFile = getSymmetricKeyFile();
            if(keyFile.exists()){
                secretKey = parseSymmetricKeyFile(keyFile,getProtectPassphraseWithDefault());
            }else{
                secretKey = generateAndStoreSymmetricKey(keyFile, getProtectPassphraseWithDefault());
            }
        }
        return secretKey;
    }

    /**
     * Returns the path of directory where trusted public key files are stored.
     *
     * @return the path to the directory where trusted public key store files are stored. Or null
     * if not configured.
     * @throws InternalErrorException if internal error occurred retrieving the trust store path.
     */
    protected abstract String getAsymTrustStorePath() throws InternalErrorException;


    /**
     * Help method to parse existing keys or generate new ones if not exists.
     * @return the related asymmetric key pair.
     * @throws InternalErrorException if problems occurred reading the keys from file.
     */
    protected KeyPair getAsymKeyPair() throws InternalErrorException {
        if(asymKeyPair == null){
            File publicKeyFile = getAsymPublicKeyFile();
            File privateKeyFile = getAsymPrivateKeyFile();
            if(checkExists(publicKeyFile, privateKeyFile)){
                asymKeyPair = parseAsymKeyFiles(publicKeyFile, privateKeyFile, getProtectPassphraseWithDefault());
            }else{
                asymKeyPair = generateAndStoreAsymKeys(publicKeyFile, privateKeyFile, getProtectPassphraseWithDefault());
            }
        }
        return asymKeyPair;
    }

    /**
     * Help method to parse existing keys for BTCPayServer or generate new ones if not exists.
     * @return the related btc pay server key pair.
     * @throws InternalErrorException if problems occurred reading the keys from file.
     */
    protected KeyPair getBTCPayServerKeyPair() throws InternalErrorException {
        if(btcKeyPair == null){
            File privateKeyFile = getBTCPayServerPrivateKeyFile();
            if(privateKeyFile.exists()){
                btcKeyPair = parseBTCPayServerKeyFiles(privateKeyFile, getProtectPassphraseWithDefault());
            }else{
                btcKeyPair = generateAndStoreBTCPayServerKeys(privateKeyFile, getProtectPassphraseWithDefault());
            }
        }
        return btcKeyPair;
    }

    /**
     * Help method that checks that both files exists before loading. if only
     * one exists it logs a warning.
     */
    private boolean checkExists(File publicKeyFile, File privateKeyFile){
        if(publicKeyFile.exists() && privateKeyFile.exists()){
            return true;
        }
        if(!publicKeyFile.exists() && !privateKeyFile.exists()){
            return false;
        }
        if(!publicKeyFile.exists()){
            log.warning("Warning, couldn't find public asymmetric key file: " + publicKeyFile.getPath() + ", regenerating both keys");
        }
        if(!privateKeyFile.exists()){
            log.warning("Warning, couldn't find privateKeyFile asymmetric key file: " + privateKeyFile.getPath() + ", regenerating both keys");
        }
        return false;
    }

    /**
     *
     * @return returns a File reference to the configured public key file.
     * @throws InternalErrorException if internal problems occurred loading file.
     */
    private File getAsymPublicKeyFile() throws InternalErrorException{
        String path = getDirectory("asymmetric key store", "/keys");
        return new File(path + ASYM_PUBLIC_KEYNAME);
    }

    /**
     *
     * @return returns a File reference to the configured private key file.
     * @throws InternalErrorException if internal problems occurred loading file.
     */
    private File getAsymPrivateKeyFile() throws InternalErrorException{
        String path = getDirectory("asymmetric key store", "/keys");
        return new File(path + ASYM_PRIVATE_KEYNAME);
    }

    /**
     *
     * @return returns a File reference to the configured private key file.
     * @throws InternalErrorException if internal problems occurred loading file.
     */
    private File getBTCPayServerPrivateKeyFile() throws InternalErrorException{
        String path = getDirectory("BTC Pay Server key store", "/keys");
        return new File(path + BTCPAY_SERVER_PRIVATE_KEYNAME);
    }

    /**
     * Method that parses the two asymmetric key files into a KeyPair.
     */
    private KeyPair parseAsymKeyFiles(File publicKeyFile, File privateKeyFile, char[] protectPassphrase) throws InternalErrorException{
        try {
            byte[] publicKeyData = Files.readAllBytes(publicKeyFile.toPath());
            byte[] privateKeyData = Files.readAllBytes(privateKeyFile.toPath());
            log.info("Loading existing asymmetric key from files " + publicKeyFile.getPath() + " and " + privateKeyFile.getPath());
            return KeySerializationHelper.deserializeAsymKeyPair(publicKeyData,privateKeyData,protectPassphrase, getRSAKeyFactory());

        } catch (Exception e) {
            throw new InternalErrorException("Internal error parsing RSA key from files " + publicKeyFile.getPath() + " and " + privateKeyFile.getPath() + ": " + e.getMessage(),e);
        }
    }

    /**
     * Method that parses the two btc pay server key files into a KeyPair.
     */
    private KeyPair parseBTCPayServerKeyFiles(File privateKeyFile, char[] protectPassphrase) throws InternalErrorException{
        try {
            byte[] privateKeyData = Files.readAllBytes(privateKeyFile.toPath());
            KeyPair keyPair = KeySerializationHelper.deserializeBTCPayServerKeyPair(privateKeyData,protectPassphrase, getECKeyFactory(), BTCPAY_SERVER_ECDSA_CURVE);
            assert keyPair.getPublic() instanceof ECPublicKey;
            String sin = btcPayServerHelper.toSIN(btcPayServerHelper.pubKeyInHex((ECPublicKey) keyPair.getPublic()));
            log.info("Loading existing BTC Pay Server access key from file " + privateKeyFile.getPath() + ", SIN: " + sin);
            return keyPair;
        } catch (Exception e) {
            throw new InternalErrorException("Internal error parsing BTC Pay Server access key from file " + privateKeyFile.getPath() + ": " + e.getMessage(),e);
        }
    }

    /**
     * Method that generates and stores a new asymmetric key pair into two files.
     */
    private KeyPair generateAndStoreAsymKeys(File publicKeyFile, File privateKeyFile, char[] protectPassphrase) throws InternalErrorException{
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA", getProvider(null));
            keyGen.initialize(RSA_KEY_LENGTH);
            KeyPair keyPair = keyGen.generateKeyPair();

            byte[][] keyData = KeySerializationHelper.serializeAsymKeyPair(keyPair,protectPassphrase);

            FileOutputStream fos = new FileOutputStream(publicKeyFile);
            fos.write(keyData[0]);
            fos.close();
            fos = new FileOutputStream(privateKeyFile);
            fos.write(keyData[1]);
            fos.close();
            log.info("New asymmetric key generated and stored in files " + publicKeyFile.getPath() + " and " + privateKeyFile.getPath());
            return keyPair;
        }catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new InternalErrorException("Internal error generating RSA key: " + e.getMessage(), e);
        }catch (IOException e) {
            throw new InternalErrorException("Internal error storing generated RSA key pair to files " + publicKeyFile.getPath() + " and " + privateKeyFile.getPath() + " : " + e.getMessage(),e);
        }
    }

    /**
     * Method that generates and stores a new asymmetric key pair into two files.
     */
    private KeyPair generateAndStoreBTCPayServerKeys(File privateKeyFile, char[] protectPassphrase) throws InternalErrorException{
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC", getProvider(null));
            ECNamedCurveParameterSpec curveSpec = ECNamedCurveTable.getParameterSpec(BTCPAY_SERVER_ECDSA_CURVE);
            keyGen.initialize(curveSpec);
            KeyPair keyPair = keyGen.generateKeyPair();

            byte[][] keyData = KeySerializationHelper.serializeBTCPayServerKeyPair(keyPair,protectPassphrase);

            String sIN = btcPayServerHelper.toSIN(new String(keyData[0]));

            File publicKeyFile = genPublicBTCPayServerFile(sIN);

            FileOutputStream fos = new FileOutputStream(publicKeyFile);
            fos.write(keyData[0]);
            fos.close();

            fos = new FileOutputStream(privateKeyFile);
            fos.write(keyData[1]);
            fos.close();
            log.info("New BTC Pay Server access key generated and stored in files " + privateKeyFile.getPath()  + " and " + publicKeyFile.getPath() +", key SIN: " + sIN);

            return keyPair;
        }catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
            throw new InternalErrorException("Internal error generating EC key for BTCPay Server access token: " + e.getMessage(), e);
        }catch (IOException e) {
            throw new InternalErrorException("Internal error storing generated BTCPay Server access token key pair to file " + privateKeyFile.getPath() + " : " + e.getMessage(),e);
        }
    }

    private File[] getAsymTrustStoreFiles() throws InternalErrorException{
        if(getAsymTrustStorePath() == null || getAsymTrustStorePath().trim().equals("")){
            log.warning("Warning: no trust store directory configured, using own public key as trust. Should not be used in production.");
            return new File[] {getAsymPublicKeyFile()};
        }
        File dir = new File(getAsymTrustStorePath());
        if(!dir.exists() || !dir.isDirectory() || !dir.canRead()){
            throw new InternalErrorException("Internal error parsing public keys in trust store directory: " + dir.getPath() + " check that it exists and is readable");
        }

        return dir.listFiles((d, name) -> name.toLowerCase().endsWith(".pem"));
    }

    private boolean hasCacheExpired(long expireDate){
        return expireDate < clock.millis();
    }


    protected KeyFactory getRSAKeyFactory() throws InternalErrorException {
        if(rsaKeyFactory == null) {
            try {
                rsaKeyFactory = KeyFactory.getInstance("RSA", getProvider(null));
            } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
                throw new InternalErrorException("Internal error generating RSA key: " + e.getMessage(), e);
            }
        }
        return rsaKeyFactory;
    }

    protected KeyFactory getECKeyFactory() throws InternalErrorException {
        if(ecKeyFactory == null) {
            try {
                ecKeyFactory = KeyFactory.getInstance("EC", getProvider(null));
            } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
                throw new InternalErrorException("Internal error generating RSA key: " + e.getMessage(), e);
            }
        }
        return ecKeyFactory;
    }

    protected File getSymmetricKeyFile() throws InternalErrorException{
        String path = getDirectory("symmetric key store", "/keys");
        return new File(path + SYMMENTRIC_FILENAME);
    }


    protected Key parseSymmetricKeyFile(File keyFile, char[] protectPassphrase) throws InternalErrorException{
        try {
            byte[] storedData = Files.readAllBytes(keyFile.toPath());
            log.info("Loading existing symmetric key from file: " + keyFile.getPath());
            return KeySerializationHelper.deserializeSecretKey(storedData,protectPassphrase);

        } catch (IOException e) {
            throw new InternalErrorException("Internal error parsing AES key from file " + keyFile.getPath() + ": " + e.getMessage(),e);
        }
    }

    protected Key generateAndStoreSymmetricKey(File keyFile, char[] protectPassphrase) throws InternalErrorException{
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES", getProvider(null));
            keyGen.init(AES_KEY_LENGTH);
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

    /**
     * Method to return the BTC Pay Server public file name with sIN value filename.
     * @param sIN the related sIN value
     * @return the file name for related value
     * @throws InternalErrorException if problems occurred looking up the file name.
     */
    protected File genPublicBTCPayServerFile(String sIN) throws InternalErrorException{
        return new File(getDirectory("BTC Pay Server key store", "/keys") + (BTCPAY_SERVER_PUBLIC_KEYNAME.replace("@SIN@",sIN)));
    }
}
