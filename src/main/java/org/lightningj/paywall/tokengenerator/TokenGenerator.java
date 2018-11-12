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

import org.lightningj.paywall.InternalErrorException;
import org.lightningj.paywall.vo.PreImageData;

/**
 * Interface for Token Generator in charge of generating
 * different types of Tokens throughout the systems such
 * as pre image data, JSON Web Tokens etc.
 *
 * Created by Philip Vendil on 2018-10-29.
 */
public interface TokenGenerator {

    /**
     * Method that should generate a random pre image data used to
     * create invoice.
     * @return a newly created unique PreImageData
     * @throws InternalErrorException if internal errors occurred generating
     * the pre image data.
     */
    PreImageData genPreImageData() throws InternalErrorException;
}
