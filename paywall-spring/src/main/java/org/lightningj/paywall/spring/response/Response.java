package org.lightningj.paywall.spring.response;

import org.lightningj.paywall.JSONParsable;

import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ResponseType", propOrder = {
        "status"
})
public abstract class Response extends JSONParsable {

    public static final String STATUS_OK = "OK";

    @XmlElement(required = true)
    private String status = STATUS_OK;

    public Response(){
        status = STATUS_OK;
    }

    protected Response(String status){
        this.status = status;
    }

    /**
     * JSON Parseable constructor
     *
     * @param jsonObject the json object to parse
     */
    protected Response(JsonObject jsonObject) throws JsonException {
        super(jsonObject);
    }

    /**
     *
     * @return the status, should generally be OK, used to make it simple to
     * determine from JSON response that processing was OK.
     */
    public String getStatus() {
        return status;
    }

    /**
     *
     * @param status the status, should generally be OK, used to make it simple to
     * determine from JSON response that processing was OK.
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Method that should set the objects property to Json representation.
     *
     * @param jsonObjectBuilder the json object build to use to set key/values in json
     * @throws JsonException if problems occurred converting object to JSON.
     */
    @Override
    public void convertToJson(JsonObjectBuilder jsonObjectBuilder) throws JsonException {
        addNotRequired(jsonObjectBuilder,"status",status);
    }

    /**
     * Method to read all properties from a JsonObject into this value object.
     *
     * @param jsonObject the json object to read key and values from and set object properties.
     * @throws JsonException if problems occurred converting object from JSON.
     */
    @Override
    public void parseJson(JsonObject jsonObject) throws JsonException {
        status = getStringIfSet(jsonObject,"status");
    }
}
