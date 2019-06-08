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
         * Generated invoice have expired and new payment flow have to be generated.
         */
        INVOICE_EXPIRED: "INVOICE_EXPIRED",
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
        SETTLEMENT_NOT_YET_VALID: "SETTLEMENT_NOT_YET_VALID",
        /**
         * Generated settlement have expired and new payment flow have to be generated.
         */
        SETTLEMENT_EXPIRED: "SETTLEMENT_EXPIRED",
        /**
         * Paywall API related error occurred during processing of payment flow, see paywallError object for details.
         */
        PAYWALL_ERROR: "PAYWALL_ERROR",
        /**
         * Regular API related error occurred during processing, see apiError object for details.
         */
        API_ERROR: "API_ERROR"
    };

    /**
     * Enumeration of available event types that might be triggered for a given payment flow.
     * The special ANY event is used when registering for event callback where all events should
     * be triggered.
     * @enum {string}
     * @readonly
     * @global
     */
    global.PaywallEventType = {
        /** Invoice have been generated and is waiting to be settled. */
        INVOICE: "INVOICE",
        /**
         * Generated invoice have expired and new payment flow have to be generated.
         */
        INVOICE_EXPIRED: "INVOICE_EXPIRED",
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
        SETTLEMENT_NOT_YET_VALID: "SETTLEMENT_NOT_YET_VALID",
        /**
         * Generated settlement have expired and new payment flow have to be generated.
         */
        SETTLEMENT_EXPIRED: "SETTLEMENT_EXPIRED",
        /**
         * Paywall API related error occurred during processing of payment flow, see paywallError object for details.
         */
        PAYWALL_ERROR: "PAYWALL_ERROR",
        /**
         * Regular API related error occurred during processing, see apiError object for details.
         */
        API_ERROR: "API_ERROR",
        /**
         * Special value used when registering new listener that should receive notification for all events
         * related to this paywall flow.
         */
        ALL: "ALL"
    };

    /**
     * TODO
     *
     * @constructor Paywall
     */
    global.Paywall = function Paywall() {
        var invoice;
        var settlement;
        var paywallError;
        var apiError;
        var executed = false;

        function getState() {
            if (apiError !== undefined) {
                return PaywallState.API_ERROR;
            }
            if (paywallError !== undefined) {
                return PaywallState.PAYWALL_ERROR;
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
                    return PaywallState.INVOICE_EXPIRED;
                }
                return PaywallState.INVOICE;
            } else {
                if(settlement.settlementValidFrom !== null){
                    var time = new Date(settlement.settlementValidFrom).getTime();
                    if(new Date(settlement.settlementValidFrom).getTime() > now){
                        // Settlement not yet valid.
                        return PaywallState.SETTLEMENT_NOT_YET_VALID;
                    }
                }
                if(new Date(settlement.settlementValidUntil).getTime() < now){
                    // Settlement expired
                    return PaywallState.SETTLEMENT_EXPIRED;
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
             * Method to retrieve error object containing error information if paywall related error occurred during payment flow.
             * @returns {Object} related error if generated in payment flow, otherwise undefined.
             * @memberof Paywall
             */
            getPaywallError : function (){
                return paywallError;
            },
            /**
             * Method to retrieve error object containing error information if underlying api error occurred during payment flow.
             * @returns {Object} related error if generated in payment flow, otherwise undefined.
             * @memberof Paywall
             */
            getAPIError : function (){
                return apiError;
            },
            /**
             * Method to retrieve current state of payment flow, will return one of PaywallState enums.
             * State EXPIRED will be returned both if invoice or settlement have expired. If state is EXPIRED
             * and settlement is null, then it's the invoice that have expired.
             *
             * @return {string} one of PaywallState enumeration values.
             * @memberof Paywall
             */
            getState : getState,

            /**
             * Method to add a listener, if listener already exists with given name it will be updated.
             * @param {string} name the unique name of the listener within this payment flow.
             * @param {PaywallEventType} type the type of event to listen to, or special ALL that receives all events.
             * @param {function} callback method that should be called on given event. The function should have two parameters
             * one PaywallEventType and one object containing the object data. Type of object differs for each event.
             * @memberof Paywall
             */
            registerListener : function (name, type, callback) {
                eventBus.addListener(name,type,callback);
            },

            /**
             * Method to remove listener with given name if exists.
             * @param {string} name the name of listener to remove.
             * @memberof Paywall
             */
            unregisterListener : function(name) {
                eventBus.removeListener(name);
            }

        };
        // Define eventBus that is dependant on API object.
        var eventBus = new PaywallEventBus(api);

        /* test-code */
        // Help code to access private fields during unit tests.
        api.setInvoice = function (inv) {
            invoice = inv;
        };
        api.setSettlement = function (setl) {
            settlement = setl;
        };
        api.setPaywallError = function (err) {
            paywallError = err;
        };
        api.setAPIError = function (err) {
            apiError = err;
        };
        api.setExecuted = function (exec) {
            executed = exec;
        };
        api.getExecuted = function () {
            return executed;
        };
        api.getEventBus = function (){
            return eventBus;
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


    /**
     * Private class in charge of maintaining all listeners for a in a payment flow.
     *
     * It allows for registration and un-registration of a listener and onEvent forwards
     * the event to all matching listeners.
     *
     * @param {Paywall|object} paywall the related payment flow.
     * @constructor PaywallEventBus
     */
    function PaywallEventBus(paywall) {
        var listeners = [];
        var currentState = paywall.getState();


        function checkStateTransition(){
            var newState = paywall.getState();
            if(currentState !== newState){
                currentState = newState;
                if(isFinalState(newState)){
                    clearInterval(stateChecker);
                }
                onEvent(getRelatedType(newState), getRelatedObject(newState));
            }
        }

        var stateChecker = setInterval(checkStateTransition, 1000);

        /**
         * Method to close underlying resources and background check.
         * @memberof PaywallEventBus
         */
        var close = function(){
            clearInterval(stateChecker);
        };
        this.close = close;

        /**
         * Method to add a listener, if listener already exists with given name it will be updated.
         * @param {string} name the unique name of the listener within this payment flow.
         * @param {PaywallEventType} type the type of event to listen to, or special ALL that receives all events.
         * @param {function} callback method that should be called on given event. The function should have two parameters
         * one PaywallEventType and one object containing the object data. Type of object differs for each event.
         * @memberof PaywallEventBus
         */
        var addListener = function(name, type, callback) {
            var index = findIndex(name);
            if(index === -1) {
                listeners.push({name: name, type: type, onEvent: callback});
            }else{
                listeners.splice(index,1,{name: name, type: type, onEvent: callback});
            }
        };
        this.addListener = addListener;

        /**
         * Method to remove listener with given name if exists.
         * @param {string} name the name of listener to remove.
         * @memberof PaywallEventBus
         */
        var removeListener = function(name) {
            var index = findIndex(name);
            if(index !== -1){
                listeners.splice(index,1);
            }
        };

        this.removeListener = removeListener;
        /**
         * Method called when a given event occurred and the method forwards the event
         * to a listeners that matches.
         * @param {PaywallEventType} type the type of event that has been triggered.
         * @param {*} object related data object, different data depending on event type. For instance
         * event type INVOICE will contain the invoice etc.
         * @memberof PaywallEventBus
         */
        var onEvent = function(type, object) {
            var matchingListeners = listeners.filter(function(item){
                return item.type === type || item.type === PaywallEventType.ALL;});
            for(var i=0;i<matchingListeners.length;i++){
                matchingListeners[i].onEvent(type, object);
            }
        };

        this.onEvent = onEvent;

        function findIndex(name){
            for(var i=0; i<listeners.length; i++){
                if(listeners[i].name === name){
                    return i;
                }
            }
            return -1;
        }

        /**
         * Help method to determine if background state checker should still be
         * run or if it can be ended.
         * @param {PaywallState|string} state the current state
         * @return {boolean} true if state is final and won't be changed in the future.
         */
        function isFinalState(state) {
            switch (state) {
                case PaywallState.NEW:
                case PaywallState.INVOICE:
                case PaywallState.SETTLEMENT_NOT_YET_VALID:
                case PaywallState.SETTLED:
                    return false;
            }
            return true;
        }

        /**
         * Help method to retrieve related event type for a given state.
         * @param {PaywallState|string} state the state to convert.
         * @return {PaywallEventType|string} the related event type.
         */
        function getRelatedType(state){
            switch (state) {
                case PaywallState.INVOICE:
                    return PaywallEventType.INVOICE;
                case PaywallState.INVOICE_EXPIRED:
                    return PaywallEventType.INVOICE_EXPIRED;
                case PaywallState.SETTLED:
                    return PaywallEventType.SETTLED;
                case PaywallState.EXECUTED:
                    return PaywallEventType.EXECUTED;
                case PaywallState.SETTLEMENT_NOT_YET_VALID:
                    return PaywallEventType.SETTLEMENT_NOT_YET_VALID;
                case PaywallState.SETTLEMENT_EXPIRED:
                    return PaywallEventType.SETTLEMENT_EXPIRED;
                case PaywallState.PAYWALL_ERROR:
                    return PaywallEventType.PAYWALL_ERROR;
                case PaywallState.API_ERROR:
                    return PaywallEventType.API_ERROR;
            }
            throw "Invalid state sent to Paywall EventBus: " + state;
        }

        /**
         * Help method to retrieve related object for a given state sent to event listeners.
         * @param {PaywallState|string} state the state to return related object of.
         * @return {*} state related data, either invoice, settlement or error.
         */
        function getRelatedObject(state){
            switch (state) {
                case PaywallState.INVOICE:
                    return paywall.getInvoice();
                case PaywallState.INVOICE_EXPIRED:
                    return paywall.getInvoice();
                case PaywallState.SETTLED:
                    return paywall.getSettlement();
                case PaywallState.EXECUTED:
                    return paywall.getSettlement();
                case PaywallState.SETTLEMENT_NOT_YET_VALID:
                    return paywall.getSettlement();
                case PaywallState.SETTLEMENT_EXPIRED:
                    return paywall.getSettlement();
                case PaywallState.PAYWALL_ERROR:
                    return paywall.getPaywallError();
                case PaywallState.API_ERROR:
                    return paywall.getAPIError();
            }
            throw "Invalid state sent to Paywall EventBus: " + state;
        }

        /* test-code */
        // Help code to access private fields during unit tests.

          this.getListeners = function () {
              return listeners;
          };

          this.isFinalState = isFinalState;
          this.getRelatedType = getRelatedType;
          this.getRelatedObject = getRelatedObject;
          this.getCurrentState = function(){
              return currentState;
          };
        /* end-test-code */
    }


    /* test-code */
    // Define PaywallEventBuss as class available to test scripts.
    global.PaywallEventBus = PaywallEventBus;
    /* end-test-code */

})(this);
