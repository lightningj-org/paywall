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
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.openssl.*;
import org.bouncycastle.openssl.bc.BcPEMDecryptorProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.openssl.jcajce.JcePEMEncryptorBuilder;
import org.lightningj.paywall.InternalErrorException;
import org.lightningj.paywall.util.HexUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

import static org.lightningj.paywall.util.DigestUtils.sha256;

/**
 * Class in charge of serializing a secret key.
 *
 * Created by Philip Vendil on 2018-09-16.
 */
public class KeySerializationHelper {

    private static final String ID_TAG = "Id :";
    private static final String GENERATED_TAG = "Generated :";
    private static final String HOSTNAME_TAG = "Hostname :";
    private static final String DATA_TAG = "Data :";

    private static final String BEGIN_PUBLIC_KEY_TAG = "-----BEGIN PUBLIC KEY-----";
    private static final String END_PUBLIC_KEY_TAG = "-----END PUBLIC KEY-----";

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    private static Base64.Encoder base64Encoder = Base64.getMimeEncoder(64,"\n".getBytes());
    private static SecureRandom secureRandom=new SecureRandom();
    /**
     * Method to serialize a secret key to key data. Deserialization should
     * be done with deserializeSecretKey.
     *
     * @param key the secret key to serialize, never null.
     * @return serialized byte array with header and data.
     * @throws InternalErrorException if internal problems occurred serializing the key.
     */
    public static byte[] serializeSecretKey(Key key, char[] protectPasshrase) throws InternalErrorException{
        try {
            String data = getHeader(key.getEncoded());
            data += DATA_TAG + HexUtils.encodeHexString(encryptSymmetricKey(key,protectPasshrase)) + "\n";
            return data.getBytes("UTF-8");
        }catch (Exception e){
            throw new InternalErrorException("Internal error encoding secret key data: " + e.getMessage(),e);
        }
    }

    /**
     * Method to deserialize key data of a secret key.
     *
     * @param data the serialized key data.
     * @return the raw key data
     * @throws InternalErrorException if problems occurred deserializing the secret key.
     */
    public static Key deserializeSecretKey(byte[] data, char[] protectPassphrase) throws InternalErrorException{
        try {
            BufferedReader reader = new BufferedReader(new StringReader(new String(data,"UTF-8")));
            Optional<String> result = reader.lines().map(String::trim).filter(s -> s.startsWith(DATA_TAG)).findFirst();
            if(result.isPresent()){
                return decryptSymmetricKey(HexUtils.decodeHexString(result.get().substring(DATA_TAG.length())), protectPassphrase);
            }
        }catch (Exception e){
            throw new InternalErrorException("Internal error decoding secret key data: " + e.getMessage(),e);
        }
        throw new InternalErrorException("Internal error decoding secret key data: no Data: tag found in secret key file");
    }

    /**
     * Method to serialize an asymmetric key pair to key data. Deserialization should
     * be done with deserializeAsymKeyPair.
     *
     * @param keyPair the key pair to serialize, never null.
     * @param protectPassphrase the password used to protect the private key.
     * @return an array of serialized byte array with header and data with size 2 and public is at
     * index 0 and private at index 1.
     * @throws InternalErrorException if internal problems occurred serializing the keys.
     */
    public static byte[][] serializeAsymKeyPair(KeyPair keyPair, char[] protectPassphrase) throws InternalErrorException{
        try {
            if(protectPassphrase == null || protectPassphrase.length == 0){
                throw new InternalErrorException("Error encrypting asymmetric key, no protect pass phrase defined.");
            }

            String header = getHeader(keyPair.getPublic().getEncoded());
            String publicKeyData = header;
            publicKeyData += BEGIN_PUBLIC_KEY_TAG +"\n";
            PKCS8EncodedKeySpec publicKeySpec = new PKCS8EncodedKeySpec(keyPair.getPublic().getEncoded());
            publicKeyData += base64Encoder.encodeToString(publicKeySpec.getEncoded()) +"\n";
            publicKeyData += END_PUBLIC_KEY_TAG +"\n";

            StringWriter encryptedPEMString = new StringWriter();
            PEMEncryptor pemEncryptor =  new JcePEMEncryptorBuilder("AES-256-CBC").build(protectPassphrase);
            JcaPEMWriter pemWriter = new JcaPEMWriter(encryptedPEMString);
            pemWriter.writeObject(keyPair.getPrivate(),pemEncryptor);
            pemWriter.close();

            String privateKeyData = header + encryptedPEMString.toString();

            return new byte[][] {publicKeyData.getBytes("UTF-8"), privateKeyData.getBytes("UTF-8")};
        }catch (Exception e){
            if(e instanceof InternalErrorException){
                throw (InternalErrorException) e;
            }
            throw new InternalErrorException("Internal error encoding asymmetric key data: " + e.getMessage(),e);
        }
    }

    /**
     * Method to serialize an btc pay server access token key pair to key data. Deserialization should
     * be done with deserializeBTCPayServerKeyPair.
     *
     * The files saved is a Encrypted PEM file for the private key and the public
     * key stored as a hexadecimal string.
     *
     * @param keyPair the key pair to serialize, never null.
     * @param protectPassphrase the password used to protect the private key.
     * @return an array of serialized byte array with header and data with size 2 and public is at
     * index 0 and private at index 1.
     * @throws InternalErrorException if internal problems occurred serializing the keys.
     */
    public static byte[][] serializeBTCPayServerKeyPair(KeyPair keyPair, char[] protectPassphrase) throws InternalErrorException{
        try {
            if(protectPassphrase == null || protectPassphrase.length == 0){
                throw new InternalErrorException("Error encrypting BTC Pay Server Token Access key, no protect pass phrase defined.");
            }
            String header = getHeader(keyPair.getPublic().getEncoded());
            org.bouncycastle.jce.interfaces.ECPublicKey pubKey = (org.bouncycastle.jce.interfaces.ECPublicKey) keyPair.getPublic();
            String publicKeyData = HexUtils.encodeHexString(pubKey.getQ().getEncoded(true));
            StringWriter encryptedPEMString = new StringWriter();
            PEMEncryptor pemEncryptor =  new JcePEMEncryptorBuilder("AES-256-CBC").build(protectPassphrase);
            JcaPEMWriter pemWriter = new JcaPEMWriter(encryptedPEMString);
            pemWriter.writeObject(keyPair.getPrivate(),pemEncryptor);
            pemWriter.close();

            String privateKeyData = header + encryptedPEMString.toString();

            return new byte[][] {publicKeyData.getBytes("UTF-8"), privateKeyData.getBytes("UTF-8")};
        }catch (Exception e){
            if(e instanceof InternalErrorException){
                throw (InternalErrorException) e;
            }
            throw new InternalErrorException("Internal error encoding asymmetric key data: " + e.getMessage(),e);
        }
    }

    /**
     * Method to deserialize an asymmetric key data into a key pair.
     *
     * @param publicKeyData the serialized public key data, never null.
     * @param privateKeyData the serialized private key data, never null.
     * @param keyFactory the keyFactory to reconstruct the keys with.
     * @return a reconstructed key pair.
     * @throws InternalErrorException if internal problems occurred deserializing the keys.
     */
    public static KeyPair deserializeAsymKeyPair(byte[] publicKeyData, byte[] privateKeyData, char[] protectPassphrase, KeyFactory keyFactory) throws InternalErrorException{
        try {
            PublicKey publicKey = deserializePublicKey(publicKeyData,keyFactory);

            PEMDecryptorProvider decryptor = new BcPEMDecryptorProvider(protectPassphrase);
            PEMParser parser = new PEMParser(new InputStreamReader(new ByteArrayInputStream(privateKeyData)));
            Object o = parser.readObject();
            if(o instanceof PEMEncryptedKeyPair) {
                PEMEncryptedKeyPair pemEncryptedKeyPair = (PEMEncryptedKeyPair) o;

                PEMKeyPair pemKeyPair = pemEncryptedKeyPair.decryptKeyPair(decryptor);
                PrivateKey privateKey = new JcaPEMKeyConverter().getPrivateKey(pemKeyPair.getPrivateKeyInfo());
                return new KeyPair(publicKey, privateKey);
            }else{
                throw new InternalErrorException("Error parsing encrypted asymmetric key. Stored private key isn't an Encrypted Key");
            }
        }catch (Exception e){
            if(e instanceof InternalErrorException){
                throw (InternalErrorException) e;
            }
            throw new InternalErrorException("Internal error decoding asymmetric key data (Check protect passphrase): " + e.getMessage(),e);
        }
    }

    /**
     * Method to deserialize an BTC Pay Server key data into a key pair.
     *
     * The method only parses the private key and generates the public key from it automatically.
     *
     * @param privateKeyData the serialized private key data, never null.
     * @param keyFactory the keyFactory to reconstruct the keys with.
     * @return a reconstructed key pair.
     * @throws InternalErrorException if internal problems occurred deserializing the keys.
     */
    public static KeyPair deserializeBTCPayServerKeyPair(byte[] privateKeyData, char[] protectPassphrase, KeyFactory keyFactory, String eCCurveName) throws InternalErrorException{
        try {
            PEMDecryptorProvider decryptor = new BcPEMDecryptorProvider(protectPassphrase);
            PEMParser parser = new PEMParser(new InputStreamReader(new ByteArrayInputStream(privateKeyData)));
            Object o = parser.readObject();
            if(o instanceof PEMEncryptedKeyPair) {
                PEMEncryptedKeyPair pemEncryptedKeyPair = (PEMEncryptedKeyPair) o;

                PEMKeyPair pemKeyPair = pemEncryptedKeyPair.decryptKeyPair(decryptor);
                PrivateKey privateKey = new JcaPEMKeyConverter().getPrivateKey(pemKeyPair.getPrivateKeyInfo());
                if(privateKey instanceof ECPrivateKey){
                    ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec(eCCurveName);

                    ECPoint Q = ecSpec.getG().multiply(((org.bouncycastle.jce.interfaces.ECPrivateKey) privateKey).getD());

                    ECPublicKeySpec pubSpec = new ECPublicKeySpec(Q, ecSpec);
                    PublicKey publicKey = keyFactory.generatePublic(pubSpec);
                    return new KeyPair(publicKey, privateKey);
                }else{
                    throw new InternalErrorException("Invalid key type when parsing encrypted btc pay server token access key. Stored private key isn't an EC key.");
                }
            }else{
                throw new InternalErrorException("Error parsing encrypted btc pay server token access key. Stored private key isn't an Encrypted Key");
            }
        }catch (Exception e){
            if(e instanceof InternalErrorException){
                throw (InternalErrorException) e;
            }
            throw new InternalErrorException("Internal error decoding btc pay server token access key data (Check protect passphrase): " + e.getMessage(),e);
        }
    }

    /**
     * Help method to decode a public key data only.
     *
     * @param publicKeyData the public key data to parse.
     * @param keyFactory the keyFactory to reconstruct the keys with.
     * @return a reconstructed public key.
     * @throws InternalErrorException if internal problems occurred deserializing the keys.
     */
    public static PublicKey deserializePublicKey(byte[] publicKeyData, KeyFactory keyFactory) throws InternalErrorException{
        try {
            return keyFactory.generatePublic(new X509EncodedKeySpec(parsePEMData(publicKeyData, BEGIN_PUBLIC_KEY_TAG, END_PUBLIC_KEY_TAG)));
        }catch(Exception e){
          throw new InternalErrorException("Error parsing public key: " + e.getMessage(),e);
        }
    }

    /**
     * Help method generating help data for the serialized key, such as
     * generation date and hostname
     *
     * @param keyData data used as source for id generation (first 16 bytes of SHA256 hash of data).
     * @return header data used in key serialization.
     * @throws InternalErrorException if internal problems occurred finding local hostname.
     */
    private static String getHeader(byte[] keyData) throws InternalErrorException {
        try {
            String retval = ID_TAG + HexUtils.encodeHexString(sha256(keyData)).substring(0,16) + "\n";
            retval += GENERATED_TAG + dateFormat.format(new Date()) + "\n";
            retval += HOSTNAME_TAG + InetAddress.getLocalHost().getHostName() + "\n";
            return retval;
        }catch (UnknownHostException e){
            throw new InternalErrorException("Problem finding local hostname when serializing key: " + e.getMessage(),e);
        }
    }

    /**
     * Help method to base64 decode all data between the PEM tags.
     */
    private static byte[] parsePEMData(byte[] data, String beginTag, String endTag) throws UnsupportedEncodingException {
        String[] lines = new String(data,"UTF-8").split("\n");
        boolean started = false;
        String result = "";
        for(String line : lines){
            if(line.startsWith(beginTag)){
                started = true;
                continue;
            }
            if(line.startsWith(endTag)){
                break;
            }
            if(started){
                result += line;
            }
        }
        return Base64.getDecoder().decode(result);
    }

    // Symmetric Key Encryption Parameters
    private static final int GCM_AUTHENTICATION_TAG_SIZE = 128;
    private static final int GCM_IV_NONCE_BYTES = 12;
    private static final int PBKDF2_ITERATIONS = 65536;
    private static final int PBKDF2_SALT_BYTES = 32;
    private static final int AES_KEY_LENGTH_BITS = 256;
    private static final String ENC_CIPHER = "AES";
    private static final String WRAP_CIPHERSCHEME = "AES/GCM/NoPadding";
    private static final String PBKDF2_SCHEME = "PBKDF2WithHmacSHA256";

    /**
     * Help method to encrypt a symmetric key using the specified passphrase
     * and AES/GCM/NoPadding scheme. The stored key is not compatible with openssl.
     * @param key the key to encrypt.
     * @param protectPassphrase the password to use as encryption seed.
     * @return encoded symmetric key with salt and IV values.
     * @throws InternalErrorException if problems occurred encrypting the symmetric key.
     */
    private static byte[] encryptSymmetricKey(Key key, char[] protectPassphrase) throws InternalErrorException{
        if(protectPassphrase == null || protectPassphrase.length == 0){
            throw new InternalErrorException("Error encrypting symmetric key, no protect pass phrase defined.");
        }
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream das = new DataOutputStream(baos);
            byte[] salt = secureRandom.generateSeed(PBKDF2_SALT_BYTES);
            byte[] nonce = secureRandom.generateSeed(GCM_IV_NONCE_BYTES);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF2_SCHEME);
            KeySpec keyspec = new PBEKeySpec(protectPassphrase, salt, PBKDF2_ITERATIONS, AES_KEY_LENGTH_BITS);
            SecretKey pbeKey = factory.generateSecret(keyspec);
            SecretKey encKey = new SecretKeySpec(pbeKey.getEncoded(), ENC_CIPHER);

            Cipher wrapCipher = Cipher.getInstance(WRAP_CIPHERSCHEME);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_AUTHENTICATION_TAG_SIZE, nonce);
            wrapCipher.init(Cipher.WRAP_MODE, encKey, spec);

            das.write(salt);
            das.write(nonce);
            byte[] wrappedData = wrapCipher.wrap(key);
            das.writeInt(wrappedData.length);
            das.write(wrappedData);
            das.flush();
            return baos.toByteArray();
        }catch(Exception e){
            throw new InternalErrorException("Internal error encrypting secret key: " + e.getMessage(), e);
        }
    }

    /**
     * Help method to decrypt an encrypted symmetric key using the specified passphrase
     * and AES/GCM/NoPadding scheme. The stored key is not compatible with openssl.
     * @param encryptedData the encrypted key data to decrypt.
     * @param protectPassphrase the password to use as encryption seed.
     * @return a Symmetric Key object.
     * @throws InternalErrorException if problems occurred encrypting the symmetric key.
     */
    private static Key decryptSymmetricKey(byte[] encryptedData, char[] protectPassphrase) throws InternalErrorException {
        try {
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(encryptedData));
            byte[] salt = new byte[PBKDF2_SALT_BYTES];
            dis.read(salt);
            byte[] nonce = new byte[GCM_IV_NONCE_BYTES];
            dis.read(nonce);
            int length = dis.readInt();
            byte[] wrappedData = new byte[length];
            dis.read(wrappedData);

            SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF2_SCHEME);
            KeySpec keyspec = new PBEKeySpec(protectPassphrase, salt, PBKDF2_ITERATIONS, AES_KEY_LENGTH_BITS);
            SecretKey pbeKey = factory.generateSecret(keyspec);
            SecretKey encKey = new SecretKeySpec(pbeKey.getEncoded(), ENC_CIPHER);

            Cipher wrapCipher = Cipher.getInstance(WRAP_CIPHERSCHEME);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_AUTHENTICATION_TAG_SIZE, nonce);

            wrapCipher.init(Cipher.UNWRAP_MODE, encKey, spec);
            return wrapCipher.unwrap(wrappedData, "AES",Cipher.SECRET_KEY);
        }catch(Exception e){
            throw new InternalErrorException("Internal error encrypting secret key, (check passphrase) : " + e.getMessage(), e);
        }
    }
}
