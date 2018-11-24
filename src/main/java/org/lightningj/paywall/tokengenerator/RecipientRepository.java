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
package org.lightningj.paywall.tokengenerator;

import org.jose4j.jwk.JsonWebKey;
import org.lightningj.paywall.InternalErrorException;

import java.io.IOException;

/**
 * Interface for a reipient repository used to lookup a recipients encryption key.
 *
 * Created by Philip Vendil on 2018-11-21.
 */
public interface RecipientRepository {

    /**
     * Method to fetch a public key for a given recipients subject.
     *
     * @param context the related token context.
     * @param subject the subject of the recipient.
     * @return the key of the recipient used to encrypt data transferred to it.
     * @throws TokenException if subject's key couldn't be found.
     * @throws IOException if communication problems occurred with underlying components.
     * @throws InternalErrorException if internal errors occurred retrieving the public key.
     */
    JsonWebKey findRecipientKey(TokenContext context,String subject) throws TokenException, IOException, InternalErrorException;
}
