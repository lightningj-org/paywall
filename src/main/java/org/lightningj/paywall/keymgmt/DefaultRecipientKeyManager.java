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

import org.lightningj.paywall.InternalErrorException;

import java.io.File;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Default implementation of Recipeint Key Manager that reads all RSA public keys from disk and caches
 * them for 5 minutes.
 *
 * Created by Philip Vendil on 2018-09-17.
 */
abstract class DefaultRecipientKeyManager implements RecipientKeyManager {

    protected static Logger log =
            Logger.getLogger(DefaultRecipientKeyManager.class.getName());

    private static final long CACHE_TIME = 5 * 60 * 1000; // 5 Min

    protected long reciepientsStoreCacheExpireDate = 0;
    protected Map<String,PublicKey> trustedRecipientsKeysCache = new ConcurrentHashMap<>();

    protected Clock clock = Clock.systemDefaultZone();
    private KeyFactory rsaKeyFactory;


    /**
     * Retrieves a list of public keys that should be used included in encrypted envelopes of generated
     * tokens.
     * @param context related context.
     * @return a map of keyId to public keys that should be recipients of encrypted messages.
     * @throws UnsupportedOperationException if operation in combination with given context isn't
     * supported.
     * @throws InternalErrorException if internal error occurred retrieving the public keys.
     */
    @Override
    public Map<String, PublicKey> getReceipients(Context context) throws UnsupportedOperationException, InternalErrorException {
        if(hasCacheExpired(reciepientsStoreCacheExpireDate)){
            synchronized (this){
                trustedRecipientsKeysCache.clear();
                File[] asymRecipientFiles = getAsymRecipientsFiles();
                if(asymRecipientFiles != null) {
                    for (File recipientsKeyFile : getAsymRecipientsFiles()) {
                        try {
                            log.fine("Parsing recipient public key file: " + recipientsKeyFile.getPath());
                            PublicKey recipientKey = KeySerializationHelper.deserializePublicKey(Files.readAllBytes(recipientsKeyFile.toPath()), getRSAKeyFactory());
                            trustedRecipientsKeysCache.put(KeySerializationHelper.genKeyId(recipientKey.getEncoded()), recipientKey);
                        } catch (Exception e) {
                            log.log(Level.SEVERE, "Error parsing recipient public key file: " + recipientsKeyFile.getPath() + ", error: " + e.getMessage(), e);
                        }
                    }
                }else{
                    log.warning("Warning: no recipients store directory configured, using own public key as recipient. Should not be used in production.");
                    PublicKey defaultPubKey = getAsymmetricKeyManager().getPublicKey(context);
                    trustedRecipientsKeysCache.put(KeySerializationHelper.genKeyId(defaultPubKey.getEncoded()), defaultPubKey);
                }

                reciepientsStoreCacheExpireDate = clock.millis() + CACHE_TIME;
            }
        }

        return trustedRecipientsKeysCache;
    }

    /**
     * Returns the path of directory where recipients public key files are stored used to encrypt
     * messages to.
     *
     * @return the path to the directory where recipients public key files are stored used to encrypt
     * messages to. Or null if not configured.
     * @throws InternalErrorException if internal error occurred retrieving the recipients store path.
     */
    protected abstract String getAsymRecipientsStorePath() throws InternalErrorException;

    /**
     * @return Returns the related asymmetric key manager.
     */
    protected abstract AsymmetricKeyManager getAsymmetricKeyManager();



    private File[] getAsymRecipientsFiles() throws InternalErrorException{
        if(getAsymRecipientsStorePath() == null || getAsymRecipientsStorePath().trim().equals("")){
            return null;
        }
        File dir = new File(getAsymRecipientsStorePath());
        if(!dir.exists() || !dir.isDirectory() || !dir.canRead()){
            throw new InternalErrorException("Internal error parsing public keys in recipients store directory: " + dir.getPath() + " check that it exists and is readable");
        }

        return dir.listFiles((d, name) -> name.toLowerCase().endsWith(".pem"));
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

    private boolean hasCacheExpired(long expireDate){
        return expireDate < clock.millis();
    }

}
