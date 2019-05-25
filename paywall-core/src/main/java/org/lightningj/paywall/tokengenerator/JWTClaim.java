package org.lightningj.paywall.tokengenerator;

import org.jose4j.json.JsonUtil;
import org.jose4j.jwt.JwtClaims;
import org.lightningj.paywall.JSONParsable;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import java.io.StringReader;
import java.util.Map;


/**
 * Abstract base that a value object can be added as a claim or parsed from a JWTToken Claim.
 * Created by philip on 2018-11-14.
 */
public abstract class JWTClaim extends JSONParsable{

    /**
     * Base empty constructor
     */
    public JWTClaim() {
        super();
    }

    /**
     * JSON Parseable constructor
     *
     * @param jsonObject the jsonObject to parse
     * @throws JsonException if problems occurred parsing the JWT claim.
     */
    public JWTClaim(JsonObject jsonObject) throws JsonException {
        super(jsonObject);
    }

    /**
     * Constructor populating the value object from a the claimName in jwtClaims.
     *
     * @param jwtClaims the jwtClaims inside a JWT Token.
     */
    @SuppressWarnings("unchecked")
    public JWTClaim(JwtClaims jwtClaims){
        try {
            // Convert into JSON String and parse it as json constructor.
            String claimJsonString = JsonUtil.toJson((Map<String, ?>) jwtClaims.getClaimValue(getClaimName()));
            parseJson(Json.createReader(new StringReader(claimJsonString)).readObject());
        }catch(Exception e){
            throw new JsonException("Exception parsing JSON data for claim " + getClaimName() + " in JWT token: " + e.getMessage(),e);
        }
    }

    /**
     *
     * @return the claim name to set for implementing data in generated JWT token
     */
    public abstract String getClaimName();


}
