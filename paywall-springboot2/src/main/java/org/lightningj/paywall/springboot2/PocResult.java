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
package org.lightningj.paywall.springboot2;

import javax.xml.bind.annotation.*;

/**
 * Response object used by Poc1RestController and Poc2RestController to display result in JSON or XML.
 *
 * @author philip
 */
@XmlRootElement(name = "DemoResult")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SettlementResponseType", propOrder = {
        "id",
        "content"
})
public class PocResult {

    @XmlElement()
    private long id;
    @XmlElement()
    private String content;

    public PocResult(){
    }
    public PocResult(long id, String content) {
        this.id = id;
        this.content = content;
    }

    public long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
