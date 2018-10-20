/************************************************************************
 *                                                                       *
 *  LightningJ                                                           *
 *                                                                       *
 *  This software is free software; you can redistribute it and/or       *
 *  modify it under the terms of the GNU General Public License          *
 *  License as published by the Free Software Foundation; either         *
 *  version 3 of the License, or any later version.                      *
 *                                                                       *
 *  See terms of license at gnu.org.                                     *
 *                                                                       *
 *************************************************************************/
package org.lightningj.paywall.btcpayserver

import org.lightningj.paywall.btcpayserver.vo.Invoice
import spock.lang.Specification

/**
 * Unit tests for BTCPayServerResponseParser.
 *
 * Created by Philip Vendil on 2018-10-18.
 */
class BTCPayServerResponseParserSpec extends Specification {

    BTCPayServerResponseParser parser = new BTCPayServerResponseParser();

    def "Verify that parseInvoice with null value returns null"(){
        expect:
        parser.parseInvoice(null) == null
    }

    def "Verify that parseInvoice with BTCPay Server invoice returns an Invoice"(){
        when:
        Invoice i = parser.parseInvoice(invoiceData)
        then:
        i.id == "HGgydgG9JRTcRoYDnKrtKk"
        i.posData == null
        i.status == "new"
    }

    static final invoiceData = """{
    "facade": "pos/invoice",
    "data": {
        "url": "https://btcpay302112.lndyn.com/invoice?id=HGgydgG9JRTcRoYDnKrtKk",
        "posData": null,
        "status": "new",
        "btcPrice": "0.00001591",
        "btcDue": "0.00003791",
        "cryptoInfo": [
            {
                "cryptoCode": "BTC",
                "paymentType": "LightningLike",
                "rate": 6285.9216482124,
                "exRates": {
                    "USD": 0
                },
                "paid": "0.00000000",
                "price": "0.00001591",
                "due": "0.00001591",
                "paymentUrls": {
                    "BIP21": null,
                    "BIP72": null,
                    "BIP72b": null,
                    "BIP73": null,
                    "BOLT11": "lightning:lntb15910n1pduryyrpp5ztlfregsmt7rxjqfkl2p546d3junpjr6437tljghdyzg3xkundlqdpj2pskjepqw3hjqar9wd69xar0wfjnzgpgfaexgetjypy5gw3q9ycqzysxqzuyckp6shmdpepj2q9mn00nscsv5h6439k6tlnmrhwyranacvek9aypthgwtc7nuxztp9ldhue3937y03nstwxfculx08fcl2kyn34xgkcqrgvtmn"
                },
                "address": "lntb15910n1pduryyrpp5ztlfregsmt7rxjqfkl2p546d3junpjr6437tljghdyzg3xkundlqdpj2pskjepqw3hjqar9wd69xar0wfjnzgpgfaexgetjypy5gw3q9ycqzysxqzuyckp6shmdpepj2q9mn00nscsv5h6439k6tlnmrhwyranacvek9aypthgwtc7nuxztp9ldhue3937y03nstwxfculx08fcl2kyn34xgkcqrgvtmn",
                "url": "https://btcpay302112.lndyn.com/i/BTC_LightningLike/HGgydgG9JRTcRoYDnKrtKk",
                "totalDue": "0.00001591",
                "networkFee": "0.00000000",
                "txCount": 0,
                "cryptoPaid": "0.00000000"
            },
            {
                "cryptoCode": "BTC",
                "paymentType": "BTCLike",
                "rate": 6285.9216482124,
                "exRates": {
                    "USD": 0
                },
                "paid": "0.00000000",
                "price": "0.00001591",
                "due": "0.00003791",
                "paymentUrls": {
                    "BIP21": "bitcoin:tb1qynflqpm75000f6zkdxyv9fkm4fhedr47syqsqr?amount=0.00003791",
                    "BIP72": "bitcoin:tb1qynflqpm75000f6zkdxyv9fkm4fhedr47syqsqr?amount=0.00003791&r=https://btcpay302112.lndyn.com/i/HGgydgG9JRTcRoYDnKrtKk",
                    "BIP72b": "bitcoin:?r=https://btcpay302112.lndyn.com/i/HGgydgG9JRTcRoYDnKrtKk",
                    "BIP73": "https://btcpay302112.lndyn.com/i/HGgydgG9JRTcRoYDnKrtKk",
                    "BOLT11": null
                },
                "address": "tb1qynflqpm75000f6zkdxyv9fkm4fhedr47syqsqr",
                "url": "https://btcpay302112.lndyn.com/i/BTC/HGgydgG9JRTcRoYDnKrtKk",
                "totalDue": "0.00003791",
                "networkFee": "0.00002200",
                "txCount": 0,
                "cryptoPaid": "0.00000000"
            }
        ],
        "price": 0.1,
        "currency": "USD",
        "exRates": {
            "USD": 0
        },
        "buyerTotalBtcAmount": null,
        "itemDesc": null,
        "orderId": null,
        "guid": "b2e3696e-5627-4e18-bea4-612a1f0f0582",
        "id": "HGgydgG9JRTcRoYDnKrtKk",
        "invoiceTime": 1539412098000,
        "expirationTime": 1539412998000,
        "currentTime": 1539412099613,
        "lowFeeDetected": false,
        "btcPaid": "0.00000000",
        "rate": 6285.9216482124,
        "exceptionStatus": false,
        "paymentUrls": {
            "BIP21": "bitcoin:tb1qynflqpm75000f6zkdxyv9fkm4fhedr47syqsqr?amount=0.00003791",
            "BIP72": "bitcoin:tb1qynflqpm75000f6zkdxyv9fkm4fhedr47syqsqr?amount=0.00003791&r=https://btcpay302112.lndyn.com/i/HGgydgG9JRTcRoYDnKrtKk",
            "BIP72b": "bitcoin:?r=https://btcpay302112.lndyn.com/i/HGgydgG9JRTcRoYDnKrtKk",
            "BIP73": "https://btcpay302112.lndyn.com/i/HGgydgG9JRTcRoYDnKrtKk",
            "BOLT11": null
        },
        "refundAddressRequestPending": false,
        "buyerPaidBtcMinerFee": null,
        "bitcoinAddress": "tb1qynflqpm75000f6zkdxyv9fkm4fhedr47syqsqr",
        "token": "BodnkXGPkuDWs5C4wFEvK9",
        "flags": {
            "refundable": false
        },
        "paymentSubtotals": {
            "BTC_LightningLike": 1591,
            "BTC": 1591
        },
        "paymentTotals": {
            "BTC_LightningLike": 1591,
            "BTC": 3791
        },
        "amountPaid": 0,
        "minerFees": {
            "BTC": {
                "satoshisPerByte": 22,
                "totalFee": 2200
            }
        },
        "exchangeRates": {
            "BTC": {
                "USD": 0
            }
        },
        "supportedTransactionCurrencies": {
            "BTC": {
                "enabled": true
            }
        },
        "addresses": {
            "BTC_LightningLike": "lntb15910n1pduryyrpp5ztlfregsmt7rxjqfkl2p546d3junpjr6437tljghdyzg3xkundlqdpj2pskjepqw3hjqar9wd69xar0wfjnzgpgfaexgetjypy5gw3q9ycqzysxqzuyckp6shmdpepj2q9mn00nscsv5h6439k6tlnmrhwyranacvek9aypthgwtc7nuxztp9ldhue3937y03nstwxfculx08fcl2kyn34xgkcqrgvtmn",
            "BTC": "tb1qynflqpm75000f6zkdxyv9fkm4fhedr47syqsqr"
        },
        "paymentCodes": {
            "BTC_LightningLike": {
                "BIP21": null,
                "BIP72": null,
                "BIP72b": null,
                "BIP73": null,
                "BOLT11": "lightning:lntb15910n1pduryyrpp5ztlfregsmt7rxjqfkl2p546d3junpjr6437tljghdyzg3xkundlqdpj2pskjepqw3hjqar9wd69xar0wfjnzgpgfaexgetjypy5gw3q9ycqzysxqzuyckp6shmdpepj2q9mn00nscsv5h6439k6tlnmrhwyranacvek9aypthgwtc7nuxztp9ldhue3937y03nstwxfculx08fcl2kyn34xgkcqrgvtmn"
            },
            "BTC": {
                "BIP21": "bitcoin:tb1qynflqpm75000f6zkdxyv9fkm4fhedr47syqsqr?amount=0.00003791",
                "BIP72": "bitcoin:tb1qynflqpm75000f6zkdxyv9fkm4fhedr47syqsqr?amount=0.00003791&r=https://btcpay302112.lndyn.com/i/HGgydgG9JRTcRoYDnKrtKk",
                "BIP72b": "bitcoin:?r=https://btcpay302112.lndyn.com/i/HGgydgG9JRTcRoYDnKrtKk",
                "BIP73": "https://btcpay302112.lndyn.com/i/HGgydgG9JRTcRoYDnKrtKk",
                "BOLT11": null
            }
        }
    }
}"""
}
