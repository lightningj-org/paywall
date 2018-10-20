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
package org.lightningj.paywall.btcpayserver.vo;

import org.lightningj.paywall.JSONParsable;

import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 * Value object representing one BTC Pay Server JSON invoice.
 *
 * Doesn't implement the full API just the necessary parts to integrate with paywall-core requirements.
 *
 * Created by philip on 2018-10-17.
 */
public class Invoice extends JSONParsable{

    /**
     * Resource id
     */
    String id;

    /**
     * API token for invoice resource
     */
    String token;

    /**
     * Fixed price amount, denominated in `currency`
     */
    Double price;

    /**
     * ISO 4217 3-character currency code. This is the currency associated with the price field.
     */
    String currency;

    /**
     * Can be used by the merchant to assign their own internal Id to an invoice. If used, there should be a direct match between an orderId and an invoiceId.
     */
    String orderId;

    /**
     * Invoice description, from first line item of bill
     */
    String itemDesc;

    /**
     * `bitcoindonation` for donations, otherwise null
     */
    String itemCode;

    /**
     * Contact for notification of invoice status change. If missing, then account notification email address is notified.
     */
    String notificationEmail;

    /**
     * URL to which BitPay sends webhook notifications. HTTPS is mandatory.
     */
    String notificationURL;

    /**
     * URL to redirect your shopper back to your website after a successful purchase. Be sure to include "http://" or "https://" in the url.
     */
    String redirectURL;

    // paymentCodes skipped.

    /**
     * Order reference number from the point-of-sale (POS). It should be a unique identifer for each order that you
     * submit. Field is a passthru-variable returned in the payment notification post, without any modifications, for
     * you to match up the BitPay payment notification with the request that was sent to BitPay.
     */
    String posData;

    /**
     * Can be `high`, `medium`, or `low`. HIGH speed confirmations typically take 5-10 seconds, and can be used for
     * digital goods or low-risk items. LOW speed confirmations take about 1 hour, and should be used for high-value
     * items. If missing, then account transaction speed is used.
     */
    String transactionSpeed;

    /**
     * Indicates whether email and IPN notifications should be sent for this invoice. If missing, then account
     * notification settings are used.
     */
    Boolean fullNotifications;

    /**
     * Indicates whether IPN notifications should be sent for this invoice when the invoice expires or is refunded.
     * If true, then fullNotifications is automatically set to true.
     */
    Boolean extendedNotifications;

    // buyer skipped

    /**
     * Web address of invoice, expires at `expirationTime`
     */
    String url;

    /**
     * Can be `new` (invoice has not yet been fully paid), `paid` (received payment but has not yet been fully
     * confirmed), `confirmed` (confirmed based on the transaction speed settings), `complete` (confirmed by BitPay and
     * credited to the ledger), `expired` (can no longer receive payments), and `invalid` (invoice has received
     * payments but was invalid)
     */
    String status;

    /**
     * The total amount paid to the invoice in terms of the invoice transactionCurrency indicated in the smallest
     * possible unit for the corresponding transactionCurrency (e.g satoshis for BTC and BCH)
     */
    Long amountPaid;

    // paymentSubtotals skipped

    // paymentTotals skipped

    // minerFees skipped

    /**
     * UNIX time when invoice is last available to be paid, in milliseconds
     */
    Long expirationTime;

    /**
     * Time of API call
     */
    Long currentTime;

    /**
     * Can be `paidPartial`, `paidOver`, or false
     */
    Boolean exceptionStatus;

    // exchangeRates skipped

    // transactions skipped

    // creditedOverpaymentAmounts skipped

    // refundInfo skipped

    /**
     * The cryptocurrency used to pay the invoice. This field will only be available after a transaction is applied to
     * the invoice. Possible values are currently `BTC` or `BCH`.
     */
    String transactionCurrency;

    /**
     * Empty Constructor
     */
    public Invoice(){}

    /**
     * Json Parsable constructor.
     * @param jsonObject the json data to parse.
     * @throws JsonException if problem occurred parsing.
     */
    public Invoice(JsonObject jsonObject) throws JsonException {
        super(jsonObject);
    }

    // TODO Simple Constructor

    /**
     * See property comment
     */
    public String getId() {
        return id;
    }

    /**
     * See property comment
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * See property comment
     */
    public String getToken() {
        return token;
    }

    /**
     * See property comment
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * See property comment
     */
    public Double getPrice() {
        return price;
    }

    /**
     * See property comment
     */
    public void setPrice(Double price) {
        this.price = price;
    }

    /**
     * See property comment
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * See property comment
     */
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    /**
     * See property comment
     */
    public String getOrderId() {
        return orderId;
    }

    /**
     * See property comment
     */
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    /**
     * See property comment
     */
    public String getItemDesc() {
        return itemDesc;
    }

    /**
     * See property comment
     */
    public void setItemDesc(String itemDesc) {
        this.itemDesc = itemDesc;
    }

    /**
     * See property comment
     */
    public String getItemCode() {
        return itemCode;
    }

    /**
     * See property comment
     */
    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    /**
     * See property comment
     */
    public String getNotificationEmail() {
        return notificationEmail;
    }

    /**
     * See property comment
     */
    public void setNotificationEmail(String notificationEmail) {
        this.notificationEmail = notificationEmail;
    }

    /**
     * See property comment
     */
    public String getNotificationURL() {
        return notificationURL;
    }

    /**
     * See property comment
     */
    public void setNotificationURL(String notificationURL) {
        this.notificationURL = notificationURL;
    }

    /**
     * See property comment
     */
    public String getRedirectURL() {
        return redirectURL;
    }

    /**
     * See property comment
     */
    public void setRedirectURL(String redirectURL) {
        this.redirectURL = redirectURL;
    }

    /**
     * See property comment
     */
    public String getPosData() {
        return posData;
    }

    /**
     * See property comment
     */
    public void setPosData(String posData) {
        this.posData = posData;
    }

    /**
     * See property comment
     */
    public String getTransactionSpeed() {
        return transactionSpeed;
    }

    /**
     * See property comment
     */
    public void setTransactionSpeed(String transactionSpeed) {
        this.transactionSpeed = transactionSpeed;
    }

    /**
     * See property comment
     */
    public Boolean isFullNotifications() {
        return fullNotifications;
    }

    /**
     * See property comment
     */
    public void setFullNotifications(Boolean fullNotifications) {
        this.fullNotifications = fullNotifications;
    }

    /**
     * See property comment
     */
    public Boolean isExtendedNotifications() {
        return extendedNotifications;
    }

    /**
     * See property comment
     */
    public void setExtendedNotifications(Boolean extendedNotifications) {
        this.extendedNotifications = extendedNotifications;
    }

    /**
     * See property comment
     */
    public String getUrl() {
        return url;
    }

    /**
     * See property comment
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * See property comment
     */
    public String getStatus() {
        return status;
    }

    /**
     * See property comment
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * See property comment
     */
    public long getAmountPaid() {
        return amountPaid;
    }

    /**
     * See property comment
     */
    public void setAmountPaid(long amountPaid) {
        this.amountPaid = amountPaid;
    }

    /**
     * See property comment
     */
    public Long getExpirationTime() {
        return expirationTime;
    }

    /**
     * See property comment
     */
    public void setExpirationTime(Long expirationTime) {
        this.expirationTime = expirationTime;
    }

    /**
     * See property comment
     */
    public Long getCurrentTime() {
        return currentTime;
    }

    /**
     * See property comment
     */
    public void setCurrentTime(Long currentTime) {
        this.currentTime = currentTime;
    }

    /**
     * See property comment
     */
    public Boolean getExceptionStatus() {
        return exceptionStatus;
    }

    /**
     * See property comment
     */
    public void setExceptionStatus(Boolean exceptionStatus) {
        this.exceptionStatus = exceptionStatus;
    }

    /**
     * See property comment
     */
    public String getTransactionCurrency() {
        return transactionCurrency;
    }

    /**
     * See property comment
     */
    public void setTransactionCurrency(String transactionCurrency) {
        this.transactionCurrency = transactionCurrency;
    }

    // supportedTransactionCurrencies skipped

    // buyerProvidedInfo skipped

    @Override
    public void convertToJson(JsonObjectBuilder jsonObjectBuilder) throws JsonException {
        addNotRequired(jsonObjectBuilder,"id", id);
        addNotRequired(jsonObjectBuilder,"token", token);
        addNotRequired(jsonObjectBuilder,"price", price);
        addNotRequired(jsonObjectBuilder,"currency", currency);
        addNotRequired(jsonObjectBuilder,"orderId", orderId);
        addNotRequired(jsonObjectBuilder,"itemDesc", itemDesc);
        addNotRequired(jsonObjectBuilder,"itemCode", itemCode);
        addNotRequired(jsonObjectBuilder,"notificationEmail", notificationEmail);
        addNotRequired(jsonObjectBuilder,"notificationURL", notificationURL);
        addNotRequired(jsonObjectBuilder,"redirectURL", redirectURL);

        addNotRequired(jsonObjectBuilder,"posData", posData);
        addNotRequired(jsonObjectBuilder,"transactionSpeed", transactionSpeed);
        addNotRequired(jsonObjectBuilder,"fullNotifications", fullNotifications);
        addNotRequired(jsonObjectBuilder,"extendedNotifications", extendedNotifications);
        addNotRequired(jsonObjectBuilder,"url", url);
        addNotRequired(jsonObjectBuilder,"status", status);
        addNotRequired(jsonObjectBuilder,"amountPaid", amountPaid);
        addNotRequired(jsonObjectBuilder,"expirationTime", expirationTime);
        addNotRequired(jsonObjectBuilder,"currentTime", currentTime);
        addNotRequired(jsonObjectBuilder,"exceptionStatus", exceptionStatus);
        addNotRequired(jsonObjectBuilder,"transactionCurrency", transactionCurrency);


    }

    @Override
    public void parseJson(JsonObject o) throws JsonException {
        id = getStringIfSet(o,"id");
        token = getStringIfSet(o,"token");
        price = getDoubleIfSet(o,"price");
        currency = getStringIfSet(o,"currency");
        orderId =getStringIfSet(o,"orderId");
        itemDesc = getStringIfSet(o,"itemDesc");
        itemCode = getStringIfSet(o,"itemCode");
        notificationEmail = getStringIfSet(o,"notificationEmail");
        notificationURL = getStringIfSet(o,"notificationURL");
        redirectURL = getStringIfSet(o,"redirectURL");
        posData = getStringIfSet(o,"posData");
        transactionSpeed = getStringIfSet(o,"transactionSpeed");
        fullNotifications = getBooleanIfSet(o,"fullNotifications");
        extendedNotifications = getBooleanIfSet(o,"extendedNotifications");
        url = getStringIfSet(o,"url");
        status = getStringIfSet(o,"status");
        amountPaid = getLongIfSet(o,"amountPaid");
        expirationTime = getLongIfSet(o,"expirationTime");
        currentTime = getLongIfSet(o,"currentTime");
        exceptionStatus = getBooleanIfSet(o,"exceptionStatus");
        transactionCurrency = getStringIfSet(o,"transactionCurrency");

    }
}
