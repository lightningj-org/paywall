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
import java.util.logging.Logger;

/**
 * Base File Key Manager manages keys stored on disk.
 *
 *
 * Created by Philip Vendil on 2018-09-15.
 */
public abstract class FileKeyManager implements KeyManager{

    private static final String DEFAULT_PROTECT_PASSPHRASE = "ha3MPKSETz*Zyy!Rz38J3U!P";

    protected static Logger log =
            Logger.getLogger(FileKeyManager.class.getName());

    /**
     * Method to return the Security Provider to use in given context.
     *
     * Currently uses SUN provider.
     *
     * @param context the related context
     * @return the provider to use.
     */
    @Override
    public String getProvider(Context context) {
        return "BC";
    }

    /**
     * Returns the path of directory where key files are stored.
     *
     * @return the path to the directory where key store files are stored. Or null
     * if not configured.
     * @throws InternalErrorException if internal error occurred retrieving the key store path.
     */
    protected abstract String getKeyStorePath() throws InternalErrorException;

    /**
     * Method to retrieve the configured pass phrase used to protect generated keys.
     * @return the configured protect pass phrase or null if no passphrase is configured.
     *
     * @throws InternalErrorException  if internal error occurred retrieving configuration.
     */
    protected abstract String getProtectPassphrase() throws InternalErrorException;

    /**
     * Help method that sets a default pass phrase if not configured with a warning log entry.
     * @return passphrase of default pass phrase, never null
     * @throws InternalErrorException if internal error occurred retrieving configuration.
     */
    protected char[] getProtectPassphraseWithDefault() throws InternalErrorException{
        String retval = getProtectPassphrase();
        if(retval == null || retval.trim().equals("")){
            retval = DEFAULT_PROTECT_PASSPHRASE;
            log.warning("WARNING: no protection pass phrase for JSON Web Token keys set, using built-in default pass phrase, should not be used in production environments.");
        }
        return retval.toCharArray();
    }

    /**
     * Method that returns configured directory for storing using keys.
     * If not configured is temporary directory used, (when used in development
     * test environments).
     *
     * @param type display name of path used for logging purposes.
     * @param tempSubDir the sub directory used if config path is null and temporary directory
     *                   should be used.
     * @return the name of the directory used to store/fetch keys.
     * @throws InternalErrorException if internal problems occured.
     */
    protected String getDirectory(String type, String tempSubDir) throws InternalErrorException{
        String configPath = getKeyStorePath();
        return getDirectory(configPath,type,tempSubDir);
    }

    protected static String getDirectory(String configPath, String type, String tempSubDir) throws InternalErrorException{
        if(configPath == null || configPath.trim().equals("")){
            String tempDir = System.getProperty("java.io.tmpdir");
            if(!tempDir.endsWith("/")){
                tempDir += "/";
            }
            log.warning( "No " + type + " configured, using temporary directory " + tempDir + ". THIS SHOULD NOT BE USED IN TEST ENVIRONMENTS.");
            configPath = tempDir + tempSubDir;
        }

        File targetDir = new File(configPath);
        if(!targetDir.exists()){
            if(!targetDir.mkdirs()){
                throw new InternalErrorException("Error creating directory " + configPath + " as " + type + ", check it is writable by running user.");
            }
        }

        if(!targetDir.isDirectory() || !targetDir.canWrite() || !targetDir.canRead()){
            throw new InternalErrorException("Error using directory " + configPath + " as " + type + ", check that the directory exists and is writable/readable by running user.");
        }

        return targetDir.getPath();
    }
}
