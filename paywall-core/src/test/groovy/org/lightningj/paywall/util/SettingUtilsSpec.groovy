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
package org.lightningj.paywall.util

import org.lightningj.paywall.InternalErrorException
import spock.lang.Specification
import spock.lang.Unroll

import static org.lightningj.paywall.util.SettingUtils.*
/**
 * Unit tests for SettingUtils.
 *
 * Created by Philip Vendil on 2019-01-17.
 */
class SettingUtilsSpec extends Specification {

    @Unroll
    def "Verify that checkRequiredString throws InternalErrorException if settingValue is #settingValue"(){
        when:
        checkRequiredString(settingValue, "some.setting")
        then:
        def e = thrown InternalErrorException
        e.message == "Invalid server configuration, check that setting some.setting is set in configuration."
        where:
        settingValue << [null, "", " "]
    }

    def "Verify that checkRequiredString returns same string for non-empty setting"(){
        expect:
        checkRequiredString(" asdf ", null) == " asdf "
    }

    def "Verify that checkStringWithDefault returns default value if not set"(){
        expect:
        checkStringWithDefault(" ",  "true") == "true"
    }

    def "Verify that checkStringWithDefault returns setting value if set"(){
        expect:
        checkStringWithDefault(" false ","true") == " false "
    }


    @Unroll
    def "Verify that checkRequiredInteger throws InternalErrorException if settingValue is #settingValue"(){
        when:
        checkRequiredInteger(settingValue, "some.setting")
        then:
        def e = thrown InternalErrorException
        e.message == errorMessage
        where:
        settingValue     | errorMessage
        null             | "Invalid server configuration, check that setting some.setting is set in configuration."
        ""               | "Invalid server configuration, check that setting some.setting is set in configuration."
        " "              | "Invalid server configuration, check that setting some.setting is set in configuration."
        "abc"            | "Invalid server configuration, check that setting some.setting has a number value, not abc"
        "123abc"         | "Invalid server configuration, check that setting some.setting has a number value, not 123abc"
    }

    def "Verify that checkRequiredInteger returns integer representation of setting value"(){
        expect:
        checkRequiredInteger(" 123 ", null) == 123
    }

    def "Verify that checkIntegerWithDefault returns default value if not set"(){
        expect:
        checkIntegerWithDefault(" ", null, 123) == 123
    }

    def "Verify that checkIntegerWithDefault returns setting value if set"(){
        expect:
        checkIntegerWithDefault(" 432 ", null, 12) == 432
    }

    def "Verify that checkIntegerWithDefault throws InternalErrorException for invalid setting value"(){
        when:
        checkIntegerWithDefault(" invalidint ", "some.setting", 123)
        then:
        def e = thrown InternalErrorException
        e.message == "Invalid server configuration, check that setting some.setting has a number value, not  invalidint "
    }

    @Unroll
    def "Verify that checkRequiredLong throws InternalErrorException if settingValue is #settingValue"(){
        when:
        checkRequiredLong(settingValue, "some.setting")
        then:
        def e = thrown InternalErrorException
        e.message == errorMessage
        where:
        settingValue     | errorMessage
        null             | "Invalid server configuration, check that setting some.setting is set in configuration."
        ""               | "Invalid server configuration, check that setting some.setting is set in configuration."
        " "              | "Invalid server configuration, check that setting some.setting is set in configuration."
        "abc"            | "Invalid server configuration, check that setting some.setting has a number value, not abc"
        "123abc"         | "Invalid server configuration, check that setting some.setting has a number value, not 123abc"
    }

    def "Verify that checkRequiredLong returns integer representation of setting value"(){
        expect:
        checkRequiredLong(" 1231231201921 ", null) == 1231231201921L
    }

    def "Verify that checkLongWithDefault returns default value if not set"(){
        expect:
        checkLongWithDefault(" ", null, 1231231201921L) == 1231231201921L
    }

    def "Verify that checkLongWithDefault returns setting value if set"(){
        expect:
        checkLongWithDefault(" 432 ", null, 1231231201921L) == 432
    }

    def "Verify that checkLongWithDefault throws InternalErrorException for invalid setting value"(){
        when:
        checkLongWithDefault(" invalidlong ", "some.setting", 1231231201921L)
        then:
        def e = thrown InternalErrorException
        e.message == "Invalid server configuration, check that setting some.setting has a number value, not  invalidlong "
    }

    @Unroll
    def "Verify that checkRequiredBoolean throws InternalErrorException if settingValue is #settingValue"(){
        when:
        checkRequiredBoolean(settingValue, "some.setting")
        then:
        def e = thrown InternalErrorException
        e.message == errorMessage
        where:
        settingValue     | errorMessage
        null             | "Invalid server configuration, check that setting some.setting is either true or false, not null."
        ""               | "Invalid server configuration, check that setting some.setting is either true or false, not ."
        " "              | "Invalid server configuration, check that setting some.setting is either true or false, not  ."
        "abc"            | "Invalid server configuration, check that setting some.setting is either true or false, not abc."
        "NonBoolean"     | "Invalid server configuration, check that setting some.setting is either true or false, not NonBoolean."
    }

    def "Verify that checkRequiredBoolean returns boolean representation of setting value"(){
        expect:
        checkRequiredBoolean(" tRue ", null)
        !checkRequiredBoolean(" fAlse ", null)
    }

    def "Verify that checkBooleanWithDefault returns default value if not set"(){
        expect:
        checkBooleanWithDefault(" ", null, true)
    }

    def "Verify that checkBooleanWithDefault returns setting value if  set"(){
        expect:
        !checkBooleanWithDefault(" false ", null, true)
    }

    def "Verify that checkBooleanWithDefault throws InternalErrorException for invalid setting value"(){
        when:
        checkBooleanWithDefault(" invalidboolean ", "some.setting", true)
        then:
        def e = thrown InternalErrorException
        e.message == "Invalid server configuration, check that setting some.setting is either true or false, not  invalidboolean ."
    }

}
