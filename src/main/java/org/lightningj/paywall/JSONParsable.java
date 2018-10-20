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
package org.lightningj.paywall;

import org.lightningj.lnd.util.JsonGenUtils;

import javax.json.*;

/**
 * Base class for value classes that should be JSON Parsable.
 *
 * Contains helper methods to convert to and from JSON and a toString() implementation
 * that generates a pretty printed json string.
 *
 * Created by Philip Vendil on 2018-10-17.
 */
public abstract class JSONParsable {

    /**
     * Base empty constructor
     */
    protected JSONParsable(){}

    /**
     * JSON Parseable constructor
     */
    protected JSONParsable(JsonObject jsonObject) throws JsonException{
        parseJson(jsonObject);
    }

    /**
     * Help method to convert object into JSON as a JsonObjectBuilder
     */
    public JsonObjectBuilder toJson() throws JsonException {
        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        convertToJson(jsonObjectBuilder);
        return jsonObjectBuilder;
    }

    /**
     * Help method to convert the data to a String representation.
     * @param prettyPrint true if generated string should be pretty printed, otherwise compact.
     * @return JSON representation of the data.
     * @throws JsonException if problems occurred converting object to JSON.
     */
    public String toJsonAsString(boolean prettyPrint) throws JsonException{
        return JsonGenUtils.jsonToString(toJson(),prettyPrint);
    }

    /**
     * Method that should set the objects property to Json representation.
     * @param jsonObjectBuilder the json object build to use to set key/values in json
     * @throws JsonException if problems occurred converting object to JSON.
     */
    public abstract void convertToJson(JsonObjectBuilder jsonObjectBuilder) throws JsonException;

    /**
     * Method to read all properties from a JsonObject into this value object.
     * @param jsonObject the json object to read key and values from and set object properties.
     * @throws JsonException if problems occurred converting object from JSON.
     */
    public abstract void parseJson(JsonObject jsonObject) throws JsonException;

    /**
     * Help method that can be used in convertToJson implementations to set a key
     * in json if not null, otherwise the key is skipped.
     */
    protected void addNotRequired(JsonObjectBuilder jsonObjectBuilder, String key, Object value){
        if(value != null){
            if(value instanceof String){
                jsonObjectBuilder.add(key,(String) value);
            }
            if(value instanceof Integer){
                jsonObjectBuilder.add(key,(Integer) value);
            }
            if(value instanceof Long){
                jsonObjectBuilder.add(key,(Long) value);
            }
            if(value instanceof Double){
                jsonObjectBuilder.add(key,(Double) value);
            }
            if(value instanceof Boolean){
                jsonObjectBuilder.add(key,(Boolean) value);
            }
        }
    }

    /**
     * Help method used in parseJson implementation to fetch a string value
     * if set in JsonObject otherwise returns null.
     */
    protected String getStringIfSet(JsonObject object, String key){
        if(object.containsKey(key) && !object.isNull(key)){
            return object.getString(key);
        }
        return null;
    }

    /**
     * Help method used in parseJson implementation to fetch a long value
     * if set in JsonObject otherwise returns null.
     */
    protected Long getLongIfSet(JsonObject object, String key){
        if(object.containsKey(key) && !object.isNull(key)){
            return object.getJsonNumber(key).longValueExact();
        }
        return null;
    }

    /**
     * Help method used in parseJson implementation to fetch a int value
     * if set in JsonObject otherwise returns null.
     */
    protected Integer getIntIfSet(JsonObject object, String key){
        if(object.containsKey(key) && !object.isNull(key)){
            return object.getJsonNumber(key).intValueExact();
        }
        return null;
    }

    /**
     * Help method used in parseJson implementation to fetch a boolean value
     * if set in JsonObject otherwise returns null.
     */
    protected Boolean getBooleanIfSet(JsonObject object, String key){
        if(object.containsKey(key) && !object.isNull(key)){
            return object.getBoolean(key);
        }
        return null;
    }

    /**
     * Help method used in parseJson implementation to fetch a double value
     * if set in JsonObject otherwise returns null.
     */
    protected Double getDoubleIfSet(JsonObject object, String key){
        if(object.containsKey(key) && !object.isNull(key)){
            return object.getJsonNumber(key).doubleValue();
        }
        return null;
    }

    /**
     * Converts data content to pretty printed Json string.
     */
    @Override
    public String toString(){
        return toJsonAsString(true);
    }

}
