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
package org.lightningj.paywall.paywalltademo.paymenthandler;

import javax.persistence.*;

/**
 * Article Data is a demo table for looking up price for a given unit
 * of article id.
 *
 * It is a simple JPA Entity with following columns:
 * <ul>
 * <li>id: primary id in database</li>
 * <li>articleId: unique article id for a payment required resource.</li>
 * <li>price: the price in satoshis for per unit.</li>
 * </ul>
 */
@Entity
public class ArticleData {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String articleId;

    private long price;


    /**
     *
     * @return primary id in database
     */
    public Integer getId() {
        return id;
    }

    /**
     *
     * @param id primary id in database
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     *
     * @return unique article id for a payment required resource.
     */
    public String getArticleId() {
        return articleId;
    }

    /**
     *
     * @param articleId unique article id for a payment required resource.
     */
    public void setArticleId(String articleId) {
        this.articleId = articleId;
    }

    /**
     *
     * @return the price in satoshis for per unit.
     */
    public long getPrice() {
        return price;
    }

    /**
     *
     * @param price the price in satoshis for per unit.
     */
    public void setPrice(long price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "ArticleData{" +
                "id=" + id +
                ", articleId='" + articleId + '\'' +
                ", price=" + price +
                '}';
    }
}
