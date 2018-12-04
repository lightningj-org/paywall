package org.lightningj.paywall.vo.amount;

import javax.json.JsonException;
import javax.json.JsonObject;

/**
 * Crypto amount denominated in BTC. i.e using "BTC" as currencyCode.
 * Value amount is in satosis.
 * Created by Philip Vendil on 2018-11-11.
 */
public class BTC extends CryptoAmount {

    /**
     * Empty Constructor
     */
    public BTC(){
        super();
        currencyCode = CURRENCY_CODE_BTC;
    }

    /**
     * Default Constructor with amount in without magnitude
     *
     * @param value amount in satoshis
     */
    public BTC(long value){
        this(value, Magnetude.NONE);
    }

    /**
     * Default Constructor with amount in satoshis with magnitude parameter
     * in order to specify sub satoshi amounts.
     *
     * @param value amount in satoshis
     * @param magnetude If sub units to the value it is possible to specify a magnitude.
     *                  for example set to MILLI for millisatoshis for BTC.
     */
    public BTC(long value, Magnetude magnetude){
        super(value, CURRENCY_CODE_BTC, magnetude);
    }

    /**
     * JSON Parseable constructor
     */
    public BTC(JsonObject jsonObject) throws JsonException {
        parseJson(jsonObject);
    }
}
