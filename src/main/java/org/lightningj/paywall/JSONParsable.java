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
import org.lightningj.paywall.util.HexUtils;

import javax.json.*;
import java.time.Instant;

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
            if(value instanceof Instant){
                jsonObjectBuilder.add(key,((Instant) value).toEpochMilli());
            }
            if(value instanceof JSONParsable){
                jsonObjectBuilder.add(key,((JSONParsable) value).toJson());
            }

        }
    }

    protected void add(JsonObjectBuilder jsonObjectBuilder, String key, Object value) throws JsonException{
        if(value == null){
            throw new JsonException("Error building JSON object, required key " + key + " is null.");
        }
        addNotRequired(jsonObjectBuilder,key,value);
    }

    /**
     * Help method used in parseJson implementation to fetch a string value
     * if set in JsonObject otherwise returns null.
     */
    protected String getStringIfSet(JsonObject object, String key){
        return getString(object,key,false);
    }

    /**
     * Help method used in parseJson implementation to fetch a string value
     * if set in JsonObject otherwise returns null or throws JsonException if required.
     * @throws JSONParsable if field is not set but required.
     */
    protected String getString(JsonObject object, String key, boolean required) throws JsonException{
        if(object.containsKey(key) && !object.isNull(key)){
            return object.getString(key);
        }
        if(required){
            throw new JsonException("Error parsing JSON data, field key " + key + " is required.");
        }
        return null;
    }

    /**
     * Help method used in parseJson implementation to fetch a hex string value and decode it into byte[]
     * if set in JsonObject otherwise returns null.
     */
    protected byte[] getByteArrayFromHexIfSet(JsonObject object, String key){
        return getByteArrayFromHex(object,key,false);
    }

    /**
     * Help method used in parseJson implementation to fetch a hex string value and decode
     * it into byte array if set in JsonObject otherwise returns null or throws JsonException if required.
     * @throws JSONParsable if field is not set but required.
     */
    protected byte[] getByteArrayFromHex(JsonObject object, String key, boolean required) throws JsonException{
        if(object.containsKey(key) && !object.isNull(key)){
            try {
                return HexUtils.decodeHexString(object.getString(key));
            }catch (JsonException e){
              throw e;
            }catch (Exception e){
                throw new JsonException("Error parsing JSON data, problem decoding hex data from field " + key + ".");
            }
        }
        if(required){
            throw new JsonException("Error parsing JSON data, field key " + key + " is required.");
        }
        return null;
    }

    /**
     * Help method used in parseJson implementation to fetch a long value
     * if set in JsonObject otherwise returns null.
     */
    protected Long getLongIfSet(JsonObject object, String key){
        return getLong(object,key,false);
    }

    /**
     * Help method used in parseJson implementation to fetch a long value
     * if set in JsonObject otherwise returns null or throws JsonException if required.
     * @throws JSONParsable if field is not set but required.
     */
    protected Long getLong(JsonObject object, String key, boolean required) throws JsonException{
        if(object.containsKey(key) && !object.isNull(key)){
            try {
                return object.getJsonNumber(key).longValueExact();
            }catch(Exception e){
                throw new JsonException("Error parsing JSON data, field key " + key + " is not a number.");
            }
        }
        if(required){
            throw new JsonException("Error parsing JSON data, field key " + key + " is required.");
        }
        return null;
    }

    /**
     * Help method used in parseJson implementation to fetch a int value
     * if set in JsonObject otherwise returns null.
     */
    protected Integer getIntIfSet(JsonObject object, String key){
        return getInt(object,key,false);
    }

    /**
     * Help method used in parseJson implementation to fetch a int value
     * if set in JsonObject otherwise returns null or throws JsonException if required.
     * @throws JSONParsable if field is not set but required.
     */
    protected Integer getInt(JsonObject object, String key, boolean required) throws JsonException{
        if(object.containsKey(key) && !object.isNull(key)){
            try{
                return object.getJsonNumber(key).intValueExact();
            }catch(Exception e){
                throw new JsonException("Error parsing JSON data, field key " + key + " is not a number.");
            }
        }
        if(required){
            throw new JsonException("Error parsing JSON data, field key " + key + " is required.");
        }
        return null;
    }

    /**
     * Help method used in parseJson implementation to fetch a boolean value
     * if set in JsonObject otherwise returns null.
     */
    protected Boolean getBooleanIfSet(JsonObject object, String key){
        return getBoolean(object,key,false);
    }

    /**
     * Help method used in parseJson implementation to fetch a boolean value
     * if set in JsonObject otherwise returns null.
     * @throws JSONParsable if field is not set but required.
     */
    protected Boolean getBoolean(JsonObject object, String key, boolean required) throws JsonException{
        if(object.containsKey(key) && !object.isNull(key)){
            return object.getBoolean(key);
        }
        if(required){
            throw new JsonException("Error parsing JSON data, field key " + key + " is required.");
        }
        return null;
    }

    /**
     * Help method used in parseJson implementation to fetch a double value
     * if set in JsonObject otherwise returns null.
     */
    protected Double getDoubleIfSet(JsonObject object, String key){
        return getDouble(object,key,false);
    }

    /**
     * Help method used in parseJson implementation to fetch a double value
     * if set in JsonObject otherwise returns null.
     * @throws JSONParsable if field is not set but required.
     */
    protected Double getDouble(JsonObject object, String key, boolean required) throws JsonException{
        if(object.containsKey(key) && !object.isNull(key)){
            try{
                return object.getJsonNumber(key).doubleValue();
            }catch(Exception e){
                throw new JsonException("Error parsing JSON data, field key " + key + " is not a number.");
            }
        }
        if(required){
            throw new JsonException("Error parsing JSON data, field key " + key + " is required.");
        }
        return null;
    }

    /**
     * Help method used in parseJson implementation to fetch a json object
     * if set in JsonObject otherwise returns null.
     */
    protected JsonObject getJsonObjectIfSet(JsonObject object, String key){
        return getJsonObject(object,key,false);
    }

    /**
     * Help method used in parseJson implementation to fetch a json object
     * if set in JsonObject otherwise returns null.
     * @throws JSONParsable if field is not set but required.
     */
    protected JsonObject getJsonObject(JsonObject object, String key, boolean required) throws JsonException{
        if(object.containsKey(key) && !object.isNull(key)){
            return object.getJsonObject(key);
        }
        if(required){
            throw new JsonException("Error parsing JSON data, field key " + key + " is required.");
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
