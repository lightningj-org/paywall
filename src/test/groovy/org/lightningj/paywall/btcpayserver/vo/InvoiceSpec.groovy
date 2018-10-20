package org.lightningj.paywall.btcpayserver.vo

import spock.lang.Specification

import static org.lightningj.paywall.JSONParsableSpec.toJsonObject
/**
 * Unit tests for Invoice
 *
 * Created by Philip Vendil on 2018-10-18.
 */
class InvoiceSpec extends Specification {

    def "Verify that toJsonAsString works as expected"(){
        expect:
        new Invoice().toJsonAsString(false) == "{}"
        genSimpleInvoice().toJsonAsString(false) == """{"id":"1234","price":12.0,"currency":"BTC"}"""
        genFullInvoice().toJsonAsString(false) == """{"id":"1234","token":"abc","price":1.2,"currency":"BTC","orderId":"SomeOrderId","itemDesc":"SomeItemDesc","itemCode":"SomeItemCode","notificationEmail":"SomeNotificationEmail","notificationURL":"SomeNotificationURL","redirectURL":"SomeRedirectURL","posData":"SomePosData","transactionSpeed":"SomeTransactionSpeed","fullNotifications":true,"extendedNotifications":false,"url":"SomeUrl","status":"SomeStatus","amountPaid":1234,"expirationTime":12345,"currentTime":123456,"exceptionStatus":true,"transactionCurrency":"transactionCurrency"}"""
    }

    def "Verify that parsing of JSON data works as expected"(){
        when:
        Invoice i = new Invoice(toJsonObject("{ }"))
        then:
        i.id == null
        when: // Simple Invoice
        i = new Invoice(toJsonObject("""{"id":"1234","price":12,"currency":"BTC"}"""))
        then:
        i.id == "1234"
        i.price == 12L
        i.currency == "BTC"
        when: // Full Invoice
        i = new Invoice(toJsonObject("""{"id":"1234","token":"abc","price":12,"currency":"BTC","orderId":"SomeOrderId","itemDesc":"SomeItemDesc","itemCode":"SomeItemCode","notificationEmail":"SomeNotificationEmail","notificationURL":"SomeNotificationURL","redirectURL":"SomeRedirectURL","posData":"SomePosData","transactionSpeed":"SomeTransactionSpeed","fullNotifications":true,"extendedNotifications":false,"url":"SomeUrl","status":"SomeStatus","amountPaid":1234,"expirationTime":12345,"currentTime":123456,"exceptionStatus":true,"transactionCurrency":"transactionCurrency"}"""))
        Invoice fi = genFullInvoice()
        then:
        for(String key : i.properties.keySet()){
            fi.properties.get(key) == i.properties.get(key)
        }
    }



    private Invoice genSimpleInvoice(){
        def r = new Invoice()
        r.setId("1234")
        r.setPrice(12L)
        r.setCurrency("BTC")
        return r
    }

    private Invoice genFullInvoice(){
        def r = new Invoice()
        r.id = "1234"
        r.token = "abc"
        r.price = 1.2
        r.currency = "BTC"
        r.orderId = "SomeOrderId"
        r.itemDesc = "SomeItemDesc"
        r.itemCode = "SomeItemCode"
        r.notificationEmail = "SomeNotificationEmail"
        r.notificationURL = "SomeNotificationURL"
        r.redirectURL = "SomeRedirectURL"
        r.posData = "SomePosData"
        r.transactionSpeed = "SomeTransactionSpeed"
        r.fullNotifications = true
        r.extendedNotifications = false
        r.url = "SomeUrl"
        r.status = "SomeStatus"
        r.amountPaid = 1234L
        r.expirationTime =  12345L
        r.currentTime = 123456L
        r.exceptionStatus = true
        r.transactionCurrency = "transactionCurrency"
        return r
    }

}
