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
package org.lightningj.paywall.util;

import org.lightningj.paywall.InternalErrorException;

/**
 * Class containing help methods parsing setting values.
 *
 * Created by Philip Vendil on 2019-01-17.
 */
public class SettingUtils {

    /**
     * Help method that checks that given setting isn't null or empty
     * otherwise it throws an InternalErrorException with error message
     * that related key should be set.
     *
     * @param settingValue the value to set.
     * @param settingKey the key to use in error message
     * @return the returned value.
     * @throws InternalErrorException if settingValue was empty.
     */
    public static String checkRequiredString(String settingValue, String settingKey) throws InternalErrorException{
        if(isEmpty(settingValue)){
            throw new InternalErrorException("Invalid server configuration, check that setting " + settingKey + " is set in configuration.");
        }
        return settingValue;
    }

    /**
     * Method that returns related string or default value if setting is empty.
     * @param settingValue the value of setting
     * @param defaultValue the default value if not set or empty.
     * @return either the setting or the default value
     */
    public static String checkStringWithDefault(String settingValue, String defaultValue){
        if(isEmpty(settingValue)){
            return defaultValue;
        }
        return settingValue;
    }

    /**
     * Help method to parse an integer for a required setting.
     * @param settingValue the value to convert to an integer.
     * @param settingKey the key to use in error message
     * @return a integer version of the setting.
     * @throws InternalErrorException if setting value was empty or non-integer.
     */
    public static int checkRequiredInteger(String settingValue, String settingKey) throws InternalErrorException{
        try {
            return Integer.parseInt(checkRequiredString(settingValue,settingKey).trim());
        }catch (NumberFormatException e){
            throw new InternalErrorException("Invalid server configuration, check that setting " + settingKey + " has a number value, not " + settingValue);
        }
    }

    /**
     * Method that returns related integer or default value if setting is empty.
     * @param settingValue the value of setting
     * @param settingKey the related setting key
     * @param defaultValue the default value if not set or empty.
     * @return either the setting or the default value
     * @throws InternalErrorException if setting value was empty or non-integer.
     */
    public static int checkIntegerWithDefault(String settingValue, String settingKey, int defaultValue) throws InternalErrorException{
        if(isEmpty(settingValue)){
            return defaultValue;
        }
        return checkRequiredInteger(settingValue,settingKey);
    }

    /**
     * Help method to parse an long for a required setting.
     * @param settingValue the value to convert to an long.
     * @param settingKey the key to use in error message
     * @return a long version of the setting.
     * @throws InternalErrorException if setting value was empty or non-integer.
     */
    public static long checkRequiredLong(String settingValue, String settingKey) throws InternalErrorException{
        try {
            return Long.parseLong(checkRequiredString(settingValue,settingKey).trim());
        }catch (NumberFormatException e){
            throw new InternalErrorException("Invalid server configuration, check that setting " + settingKey + " has a number value, not " + settingValue);
        }
    }

    /**
     * Method that returns related long or default value if setting is empty.
     * @param settingValue the value of setting
     * @param settingKey the related setting key
     * @param defaultValue the default value if not set or empty.
     * @return either the setting or the default value
     * @throws InternalErrorException if setting value was empty or non-integer.
     */
    public static long checkLongWithDefault(String settingValue, String settingKey, long defaultValue) throws InternalErrorException{
        if(isEmpty(settingValue)){
            return defaultValue;
        }
        return checkRequiredLong(settingValue,settingKey);
    }

    /**
     * Help method to parse a boolean for a required setting.
     * @param settingValue the value to convert to an boolean.
     * @param settingKey the key to use in error message
     * @return a integer version of the setting.
     * @throws InternalErrorException if setting value was empty or non-boolean.
     */
    public static boolean checkRequiredBoolean(String settingValue, String settingKey) throws InternalErrorException{
        if(validBooleanSyntax(settingValue)){
            return Boolean.parseBoolean(settingValue.trim());
        } else {
            throw new InternalErrorException("Invalid server configuration, check that setting " + settingKey + " is either true or false, not " + settingValue + ".");
        }
    }

    /**
     * Method that returns related boolean or default value if setting is empty.
     * @param settingValue the value of setting
     * @param settingKey the related setting key
     * @param defaultValue the default value if not set or empty.
     * @return either the setting or the default value
     * @throws InternalErrorException if setting value was non-boolean.
     */
    public static boolean checkBooleanWithDefault(String settingValue, String settingKey, boolean defaultValue) throws InternalErrorException{
        if(isEmpty(settingValue)){
            return defaultValue;
        }
        return checkRequiredBoolean(settingValue,settingKey);
    }

    /**
     * Help method to see if string is null or only contains spaces.
     * @param value the string to check
     * @return true if null or empty.
     */
    public static boolean isEmpty(String value){
        return value == null || value.trim().equals("");
    }

    private static boolean validBooleanSyntax(String value) {
        if(value != null){
            value = value.trim().toLowerCase();
            return value.equals("true") || value.equals("false");
        }

        return false;
    }


}
