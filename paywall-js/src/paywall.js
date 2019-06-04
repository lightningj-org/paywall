// paywall-dev.js
// LIBRARY CLASS



(function(global){
    "use strict";


    /**
     * Enumeration of available states for a given payment flow.
     * @enum {string}
     * @readonly
     * @global
     */
    global.PaywallState = {
        /** Payment flow is new and no invoice have yet been generated. */
        NEW: "NEW",
        /** Invoice have been generated and is waiting to be settled. */
        INVOICE: "INVOICE",
        /**
         * Payment have been settled and the payment flow should be ready to perform the call.
         * If multiple calls is possible is up to the settlement type.
         */
        SETTLED: "SETTLED",
        /**
         * Payment type is of type pay per request and request have been processed successfully.
         */
        EXECUTED: "EXECUTED",
        /**
         * Generated settlement is not yet valid and need to wait until call can be performed.
         */
        NOT_YET_VALID: "NOT_YET_VALID",
        /**
         * Generated invoice of settlement (check which by checking if related Paywall object has settlement set)
         * have expired and new payment flow have to be generated.
         */
        EXPIRED: "EXPIRED",
        /**
         * Error occurred during processing of payment flow, see error object for details.
         */
        ERROR: "ERROR"
    };

    /**
     * TODO
     *
     * @constructor Paywall
     */
    global.Paywall = function Paywall() {
        var invoice;
        var settlement;
        var error;
        var executed = false;

        function getState() {
            if (error !== undefined) {
                return PaywallState.ERROR;
            }
            if (executed) {
                return PaywallState.EXECUTED;
            }
            if (invoice === undefined && settlement === undefined) {
                return PaywallState.NEW;
            }
            var now = Date.now();
            if (settlement === undefined) {
                if(new Date(invoice.invoiceExpireDate).getTime() < now){
                    // Invoice expired
                    return PaywallState.EXPIRED;
                }
                return PaywallState.INVOICE;
            } else {
                if(settlement.settlementValidFrom !== null){
                    var time = new Date(settlement.settlementValidFrom).getTime();
                    if(new Date(settlement.settlementValidFrom).getTime() > now){
                        // Settlement not yet valid.
                        return PaywallState.NOT_YET_VALID;
                    }
                }
                if(new Date(settlement.settlementValidUntil).getTime() < now){
                    // Settlement expired
                    return PaywallState.EXPIRED;
                }
                return PaywallState.SETTLED;
            }
        }

        var api = {
            /**
             * Method to retrieve invoice if exists in related payment flow.
             * @returns {Object} related invoice if generated in payment flow, otherwise undefined.
             * @memberof Paywall
             */
            getInvoice : function (){
                return invoice;
            },
            /**
             * Returns true if invoice exists in related payment flow.
             * @returns {boolean} true if invoice exist
             * @memberof Paywall
             */
            hasInvoice : function (){
                return invoice !== undefined;
            },
            /**
             * Help method to construct a PaywallTime object of invoice expiration date, used to
             * simply output remaining hours, minutes, etc of invoice.
             *
             * @returns {PaywallTime} if invoice exist
             * @throws error if no invoice currently exists in payment flow.
             * @memberof Paywall
             */
            getInvoiceExpiration : function(){
                if(invoice !== undefined){
                    return new PaywallTime(invoice.invoiceExpireDate);
                }
                throw("Invalid state " + getState() + " when calling method getInvoiceExpiration().");
            },
            /**
             * Method to retrieve settlement if exists in related payment flow.
             * @returns {Object} related settlement if generated in payment flow, otherwise undefined.
             * @memberof Paywall
             */
            getSettlement : function (){
                return settlement;
            },
            /**
             * Returns true if settlement exists in related payment flow.
             * @returns {boolean} true if settlement exist
             * @memberof Paywall
             */
            hasSettlement : function (){
                return settlement !== undefined;
            },
            /**
             * Help method to construct a PaywallTime object of settlement expiration date, used to
             * simply output remaining hours, minutes, etc of settlement validity.
             *
             * @returns {PaywallTime} if settlement exist
             * @throws error if no settlement currently exists in payment flow. It is best to check
             * this before calling method.
             * @memberof Paywall
             */
            getSettlementExpiration : function(){
                if(settlement !== undefined){
                    return new PaywallTime(settlement.settlementValidUntil);
                }
                throw("Invalid state " + getState() + " when calling method getSettlementExpiration().");
            },
            /**
             * Help method to construct a PaywallTime object of settlement valid from date, used to
             * simply output remaining hours, minutes, etc until settlement validity. If no validFrom
             * field is set in settlement is current date returned.
             *
             * @returns {PaywallTime} if settlement exist
             * @throws error if no settlement currently exists in payment flow. It is best to check
             * this before calling method.
             * @memberof Paywall
             */
            getSettlementValidFrom : function(){
                if(settlement !== undefined){
                    if(settlement.settlementValidFrom != null) {
                        return new PaywallTime(settlement.settlementValidFrom);
                    }
                    return new PaywallTime(new Date().toDateString());
                }
                throw("Invalid state " + getState() + " or when calling method getSettlementValidFrom().");
            },
            /**
             * Method to retrieve error object containing error information if error occurred during payment flow.
             * @returns {Object} related error if generated in payment flow, otherwise undefined.
             * @memberof Paywall
             */
            getError : function (){
                return error;
            },
            /**
             * Method to retrieve current state of payment flow, will return one of PaywallState enums.
             * State EXPIRED will be returned both if invoice or settlement have expired. If state is EXPIRED
             * and settlement is null, then it's the invoice that have expired.
             *
             * @return {string} one of PaywallState enumeration values.
             * @memberof Paywall
             */
            getState : getState
        };

        /* test-code */
        // Help code to access private fields during unit tests.
        api.setInvoice = function (inv) {
            invoice = inv;
        };
        api.setSettlement = function (setl) {
            settlement = setl;
        };
        api.setError = function (err) {
            error = err;
        };
        api.setExecuted = function (exec) {
            executed = exec;
        };
        api.getExecuted = function () {
            return executed;
        };
        /* end-test-code */

        return api;
    };


    /**
     * Helper class to calculate and present remaining validity or time until valid
     * of an invoice or an settlement.
     *
     * @param {string} timeStamp the timestamp to parse.
     * @constructor PaywallTime
     */
    global.PaywallTime = function PaywallTime(timeStamp) {
        var date = new Date(timeStamp);

        var api = {
            /**
             *
             * @returns {Date} returns the related timestamp as a Date object.
             * @memberof PaywallTime
             */
            getTimeStamp : function () {
                return date;
            },
            /**
             * Help method to get the difference between timestamp and current time. If
             * current time is after timestamp is 0 returned, never a negative number.
             * Used to indicate the remaining time of a invoice or settlement.
             * @returns a PaywallTimeUnit with the difference between given timestamp and current time.
             * @memberof PaywallTime
             */
            remaining : function () {
                return new PaywallTimeUnit(date.getTime() - Date.now());
            }
        };

        /* test-code */
        // Help code to access private fields during unit tests.

        /* end-test-code */

        return api;
    };

    /**
     * PaywallTimeUnit is a help class to return remaining time in time units
     * such as seconds, minutes, days etc.
     *
     * With this object it is possible to easy display a string of remaining
     * or time until in form HH:MM:SS etc.
     *
     * @param {number} timeInMS time difference in milliseconds, if less than 0 it will be set to 0.
     * @constructor PaywallTimeUnit
     */
    global.PaywallTimeUnit = function PaywallTimeUnit(timeInMS) {

        if(timeInMS < 0){
            timeInMS=0;
        }

        var addPadding = function(value){
            if(value < 10){
                return "0" + value;
            }
            return "" + value;
        };

        function seconds() {
            return addPadding(Math.floor((timeInMS % (1000 * 60)) / 1000));
        }

        function minutes() {
            return addPadding(Math.floor((timeInMS % (1000 * 60 * 60)) / (1000 * 60)));
        }

        function hours() {
            return addPadding(Math.floor((timeInMS % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)));
        }

        function days() {
            return "" + Math.floor(timeInMS / (1000 * 60 * 60 * 24));
        }

        var api = {
            /**
             *
             * @returns {number} number of milliseconds of remaining time. This is the total number
             * of milliseconds not from the last second and is not padded.
             * @function asMS
             * @memberof PaywallTimeUnit
             */
            asMS : function () {
                return timeInMS;
            },
            /**
             * Help method to display the seconds part of remaining time. The method returns
             * the number of seconds in addition to remaining minutes.
             * @returns {string} remaining time in seconds as two characters. i.e 01 or 12
             * @function seconds
             * @memberof PaywallTimeUnit
             */
            seconds : seconds,
            /**
             * Help method to display the minutes part of remaining time. The method returns
             * the number of minutes in addition to remaining hours.
             * @returns {string} remaining time in minutes as two characters. i.e 01 or 12
             * @function minutes
             * @memberof PaywallTimeUnit
             */
            minutes : minutes,
            /**
             * Help method to display the hours part of remaining time. The method returns
             * the number of hours in addition to remaining days.
             * @returns {string} remaining time in hours as two characters. i.e 01 or 12
             * @function hours
             * @memberof PaywallTimeUnit
             */
            hours : hours,
            /**
             * Method that returns the remaining days.
             *
             * @returns {number} remaining time in days without padding.
             * @function days
             * @memberof PaywallTimeUnit
             */
            days : days
        };

        /* test-code */
        // Help code to access private fields during unit tests.

        /* end-test-code */

        return api;
    };

})(this);




// 1. Create Object and initialize

// 2. Create Help Method, simple

// 3. Simple Jasmine

// 4. JSDoc generate