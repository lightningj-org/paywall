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
package org.lightningj.paywall.paywalltademo;

/**
 * Response object used by TADemoRestController to generate JSON Data.
 *
 * @author Philip Vendil
 */
public class TADemoResult {

    private long id;
    private String prediction;
    private boolean goingUp;

    public TADemoResult(long id, String prediction, boolean goingUp) {
        this.id = id;
        this.prediction = prediction;
        this.goingUp = goingUp;
    }

    public long getId() {
        return id;
    }

    public String getPrediction() {
        return prediction;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setPrediction(String prediction) {
        this.prediction = prediction;
    }

    public boolean isGoingUp() {
        return goingUp;
    }

    public void setGoingUp(boolean goingUp) {
        this.goingUp = goingUp;
    }
}
