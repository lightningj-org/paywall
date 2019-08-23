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
         * Generated invoice have expired and a new payment flow have to be generated.
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
         * Request was aborted by the user.
         */
        ABORTED : "ABORTED"
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
         * Generated invoice have expired and a new payment flow have to be generated.
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
         * Special value used when registering new listener that should receive notification for all events
         * related to this paywall flow.
         */
        ALL: "ALL"
    };

    /**
     * Enumeration of known status values in the status field of response objects json object.
     * @enum {string}
     * @readonly
     * @global
     */
    global.PaywallResponseStatus = {
        /** Processing went ok, no exception occurred. */
        OK: "OK",
        /**
         * Invalid data was sent to service..
         */
        BAD_REQUEST: "BAD_REQUEST",
        /**
         * Temporary internal problems at the service. Possible to try again.
         */
        SERVICE_UNAVAILABLE: "SERVICE_UNAVAILABLE",
        /**
         * Usually due to invalid token sent to service.
         */
        UNAUTHORIZED: "UNAUTHORIZED",
        /**
         * Internal error occurred at the service.
         */
        INTERNAL_SERVER_ERROR: "INTERNAL_SERVER_ERROR"
    };


    /**
     * Enumeration indicating the BTC unit that should be used when displaying and invoice amount.
     * @enum {string}
     * @readonly
     * @global
     */
    global.BTCUnit = {
        /** BTC, i.e 100.000.000 Satoshis */
        BTC: "BTC",
        /** One thousand part of BTC, i.e 100.000 Satoshis */
        MILLIBTC: "MILLIBTC",
        /**
         * In BIT, i.e 100 Satoshis.
         */
        BIT: "BIT",
        /**
         * In Satoshis.
         */
        SAT: "SAT",
        /**
         * In milli satoshis, 1/1000 satoshi.
         */
        MILLISAT: "MILLISAT",
        /**
         * In nano satoshis, 1/1000.000 satoshi.
         */
        NANOSAT: "NANOSAT"
    };

    /**
     * Internal Enum of used http statuses.
     * @enum {number}
     * @readonly
     * @global
     */
    var HttpStatus = {
        /** Payment is required notification. **/
        PAYMENT_REQUIRED : 402
    };


    /**
     * Internal Enum of used http headers.
     * @enum {string}
     * @readonly
     * @global
     */
    var HttpHeader = {
        /** The related payload is a paywall related message. **/
        PAYWALL_MESSAGE : "PAYWALL_MESSAGE"
    };


    /**
     * Internal Enum of Magnetudes used in JSON Amount objects.
     * @enum {number}
     * @readonly
     * @global
     */
    var Magnetude = {
        /** Base unit. Satoshis for BTC. **/
        NONE : "NONE",
        /** One thousand part of the base unit **/
        MILLI : "MILLI",
        /** One millionth part of the base unit **/
        NANO : "NANO"
    };


    /**
     * Internal Enum of Currency Codes used in JSON Amount objects.
     * @enum {number}
     * @readonly
     * @global
     */
    var CurrencyCode = {
        /** Bitcoin BTC **/
        BTC : "BTC"
    };



    /**
     * PaywallHttpRequest is the main class in this library. It's an paywall enhanced XMLHttpRequest what
     * wraps a standard XMLHttpRequest and checks if response is 402 (Payment Required). If that is the case
     * a web socket is automatically open to listen for settlement and later use that settlement token to automatically
     * perform the call again with the same parameters.
     *
     * @constructor PaywallHttpRequest
     */
    global.PaywallHttpRequest = function PaywallHttpRequest() {
        var invoice;
        var settlement;
        var paywallError;
        var executed = false;
        var aborted = false;

        var xmlHttpRequest = new XMLHttpRequest();

        var waitingInvoice = false;


        var xhrOpenData = {method: null, url: null, async: true, username: null, password: null,
            eventListeners:[], uploadEventListeners:[]};
        var xhrSendData = {body: null, requestHeaders: []};


        function getPaywallState() {
            if (paywallError !== undefined) {
                return PaywallState.PAYWALL_ERROR;
            }
            if (aborted){
                return PaywallState.ABORTED;
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

        /**
         * Method to construct a full URL from a url value from invoice. It checks if url start with 'http', if not
         * it adds the window.location.origin before the given url value.
         * @param {String} url the url from invoice object, to QR, checkSettlement or WebSocket.
         * @return {String} the full url to the given end point.
         */
        function constructFullURL(url){
            if(url.startsWith("http")){
                return url;
            }
            return window.location.origin + url;
        }

        /**
         * Help method to cache registered event listeners for XMLHttpRequest.
         */
        function addEventListener(eventListenerList, type, callback, options){
            var index = findEventListenerIndex(eventListenerList, type);
            if(index === -1) {
                eventListenerList.push({type: type, callback: callback, options: options});
            }else{
                eventListenerList.splice(index,1,{type: type, callback: callback, options: options});
            }
        }

        /**
         * Help method to remove registered event listeners for XMLHttpRequest from cache.
         */
        function removeEventListener(eventListenerList, type) {
            var index = findEventListenerIndex(type);
            if(index !== -1){
                eventListenerList.splice(index,1);
            }
        }

        /**
         * Help method to find a specific event listener by type.
         *
         * @param eventListenerList the cache of event listeners, download or upload listeners.
         * @param type the type of event listener to find.
         * @return {number} index of found event listener by type
         */
        function findEventListenerIndex(eventListenerList,type ){
            for(var i=0; i<eventListenerList.length; i++){
                if(eventListenerList[i].type === type){
                    return i;
                }
            }
            return -1;
        }

        /**
         * Ready State handler set in XMLHttpRequest after settlement have been done.
         */
        function afterSettlementReadyStateHandler(){
            populateResponseAttributes();
            triggerOnReadyStateHandler();
        }

        /**
         * Paywall event listener that listens on all events for a SETTLED event and the
         * reinitializes the wrapped XMLHttpRequest and performs the API call again automatically.
         * @param type the type of event.
         * @param object the related object (settlement if type is settled).
         */
        var paywallOnReadyStateChangeListener = function (type, object) {
            if(type === PaywallEventType.SETTLED){
                paywallWebSocketHandler.close();
                waitingInvoice = false;
                settlement = object;
                xmlHttpRequest = new XMLHttpRequest();
                xmlHttpRequest.onreadystatechange = afterSettlementReadyStateHandler;
                populateAllEventListeners();

                if(xhrOpenData.async === undefined) {
                    xmlHttpRequest.open(xhrOpenData.method,xhrOpenData.url);
                }else{
                    xmlHttpRequest.open(xhrOpenData.method,xhrOpenData.url,
                        xhrOpenData.async, xhrOpenData.username, xhrOpenData.password);
                }
                populateRequestAttributes();
                setTokenHeader();

                xmlHttpRequest.send(xhrSendData.body);
            }
        };

        /**
         * On ready state handler for wrapped XMLHttpRequest that checks if response has PAYMENT_REQUIRED
         * and then open up a websocket to wait for settlement.
         */
        function onReadyStateHandler() {
            if(!waitingInvoice) {
                if (xmlHttpRequest.readyState === XMLHttpRequest.HEADERS_RECEIVED) {
                    if (xmlHttpRequest.status === HttpStatus.PAYMENT_REQUIRED) {
                        waitingInvoice = true;
                    } else {
                        populateResponseAttributes();
                        populateAllEventListeners();
                        triggerOnReadyStateHandler();
                    }
                } else {
                    populateResponseAttributes();
                    triggerOnReadyStateHandler();
                    if(hasPaywallErrorOccurred()){
                        handlePaywallError();
                    }
                }
            }else{
                if(xmlHttpRequest.readyState === XMLHttpRequest.DONE) {
                    if(!hasPaywallErrorOccurred()) {
                        invoice = JSON.parse(xmlHttpRequest.responseText);
                        paywallEventBus.triggerEventFromState();
                        paywallEventBus.addListenerFirst("OnReadyStateListener", PaywallEventType.ALL, paywallOnReadyStateChangeListener);
                        paywallWebSocketHandler.connect(invoice);
                    }else{
                        handlePaywallError();
                    }
                }
            }
        }

        /**
         * Help method that checks if ready state is DONE and related message is a paywall related
         * error.
         * @return {boolean} if paywall related error has occurred.
         */
        function hasPaywallErrorOccurred(){
            if(xmlHttpRequest.readyState === XMLHttpRequest.DONE){
                if(xmlHttpRequest.getResponseHeader(HttpHeader.PAYWALL_MESSAGE) === "TRUE"){
                    var errorType = xmlHttpRequest.status / 100;
                    if(errorType === 4 || errorType === 5 ){
                        return true;
                    }
                }
            }
            return false;
        }

        /**
         * Help method that parses the related response text as a paywall error and
         * triggers related event.
         */
        function handlePaywallError(){
            paywallError = JSON.parse(xmlHttpRequest.responseText);
            paywallEventBus.onEvent(PaywallEventType.PAYWALL_ERROR, paywallError);
        }

        /**
         * Method that trigger ready state handler in underlying XMLHttpRequest object.
         * It also checks if related payment is pay per request and sets the state as executed
         * if download was successful.
         */
        function triggerOnReadyStateHandler() {

            if(api.onstatechange !== undefined){
                api.readyState = xmlHttpRequest.readyState;
                api.onstatechange();
            }

            if(xmlHttpRequest.readyState === XMLHttpRequest.DONE &&
                settlement !== undefined && settlement.payPerRequest){
                var status = xmlHttpRequest.status / 100;
                if(status === 2) { // If no error occurred.
                    executed = true;
                    paywallEventBus.onEvent(PaywallEventType.EXECUTED, settlement);
                }
            }
        }

        /**
         * Help method to populate all base eventlisteners that should be populated
         * in underlying XMLHttpRequest object from start. (Before determining if this is
         * a paywalled request or not). The event handler populated are timeout, abort and error.
         */
        function populateBaseEventListeners() {
            xmlHttpRequest.ontimeout = api.ontimeout;
            xmlHttpRequest.onabort = api.onabort;
            xmlHttpRequest.onerror = api.onerror;

            for(var i=0; i <xhrOpenData.eventListeners.length ; i++){
                var listener = xhrOpenData.eventListeners[i];
                if(listener.type === "timeout"  || listener.type === "abort" || listener.type === "error" ) {
                    xmlHttpRequest.addEventListener(listener.type, listener.callback, listener.options);
                }
            }

            if(xmlHttpRequest.upload !== undefined){
                xmlHttpRequest.upload.ontimeout = api.upload.ontimeout;
                xmlHttpRequest.upload.onabort = api.upload.onabort;
                xmlHttpRequest.upload.onerror = api.upload.onerror;

                for(var j=0; j <xhrOpenData.uploadEventListeners.length ; j++){
                    var listener1 = xhrOpenData.uploadEventListeners[j];
                    if(listener1.type === "timeout"  || listener1.type === "abort" || listener1.type === "error" ) {
                        xmlHttpRequest.upload.addEventListener(listener1.type, listener1.callback, listener1.options);
                    }
                }
            }

        }

        /**
         * Help method that populates all event handlers to the underlying XMLHttpRequest object
         * after payment have been settled.
         *
         */
        function populateAllEventListeners() {
            xmlHttpRequest.onloadstart = api.onloadstart;
            xmlHttpRequest.onload = api.onload;
            xmlHttpRequest.onprogress = api.onprogress;
            xmlHttpRequest.onabort = api.onabort;
            xmlHttpRequest.onerror = api.onerror;
            xmlHttpRequest.ontimeout = api.ontimeout;
            xmlHttpRequest.onloadend = api.onloadend;

            for(var i=0; i <xhrOpenData.eventListeners.length ; i++){
                var listener = xhrOpenData.eventListeners[i];
                xmlHttpRequest.addEventListener(listener.type, listener.callback, listener.options);
            }

            if(xmlHttpRequest.upload !== undefined){
                xmlHttpRequest.upload.onloadstart = api.upload.onloadstart;
                xmlHttpRequest.upload.onload = api.upload.onload;
                xmlHttpRequest.upload.onprogress = api.upload.onprogress;
                xmlHttpRequest.upload.onabort = api.upload.onabort;
                xmlHttpRequest.upload.onerror = api.upload.onerror;
                xmlHttpRequest.upload.ontimeout = api.upload.ontimeout;
                xmlHttpRequest.upload.onloadend = api.upload.onloadend;

                for(var j=0; j <xhrOpenData.uploadEventListeners.length ; j++){
                    var listener1 = xhrOpenData.uploadEventListeners[j];
                    xmlHttpRequest.upload.addEventListener(listener1.type, listener1.callback, listener1.options);
                }
            }
        }

        /**
         * Help method that populates all request attributes before performing a call to the service.
         */
        function populateRequestAttributes() {
            xmlHttpRequest.timeout = api.timeout;
            xmlHttpRequest.withCredentials = api.withCredentials;
        }

        /**
         * Help method that sets the settlement token in as a request header if state is SETTLED.
         */
        function setTokenHeader(){
            if(getPaywallState() === PaywallState.SETTLED){
                xmlHttpRequest.setRequestHeader("Payment", api.paywall.getSettlement().token);
            }
        }

        /**
         * Help method to populate response attribute from the underlying XMLHttpRequest object.
         * @param {boolean} onlyStatus if only status related attributes should be populated and not the
         * actual response.
         */
        function populateResponseAttributes(onlyStatus) {
            api.status = xmlHttpRequest.status;
            api.statusText = xmlHttpRequest.statusText;
            api.responseURL = xmlHttpRequest.responseURL;
            if(onlyStatus === undefined || onlyStatus === false){
                api.responseType = xmlHttpRequest.responseType;
                api.response = xmlHttpRequest.response;
                api.responseText = xmlHttpRequest.responseText;
                api.responseXML = xmlHttpRequest.responseXML;
            }
        }

        var api = {
            timeout : 0,
            withCredentials : false,

            /**
             * Upload Event XMLHttpRequestEventTarget containing event listeners for upload events.
             * Only called after passing paywall.
             *
             * @memberof PaywallHttpRequest
             * @namespace PaywallHttpRequest.upload
             */
            upload: {
                onload: undefined,
                onloadstart: undefined,
                onloadend: undefined,
                onerror: undefined,
                onprogress: undefined,
                onstatechange: undefined,
                ontimeout: undefined,

                /**
                 * Method to add upload event listener. The listener is cached until
                 * passing paywall. See standard EventTarget documentation for details.
                 * @param type type of event.
                 * @param callback the call back function.
                 * @param options callback options, see EventTarget documentation.
                 * @memberof PaywallHttpRequest.upload
                 */
                addEventListener : function(type, callback, options){
                    addEventListener(xhrOpenData.uploadEventListeners,type,callback,options);
                },

                /**
                 * Method to remove upload event listener. The listener is cached until
                 * passing paywall. See standard EventTarget documentation for details.
                 * @param type type of event.
                 * @param callback the call back function.
                 * @param options callback options, see EventTarget documentation.
                 * @memberof PaywallHttpRequest.upload
                 */
                removeEventListener : function(type, callback, options){
                    removeEventListener(xhrOpenData.uploadEventListeners,type);
                },

                /**
                 * Method that shouldn't be called will throw error.
                 * @memberof PaywallHttpRequest.upload
                 */
                dispatchEvent : function(event){
                    throw("Internal dispatchEvent should never be called.");
                }
            },

            readyState: XMLHttpRequest.UNSENT,

            // response attributes
            responseURL : undefined,
            status: undefined,
            statusText: undefined,
            responseType : undefined,
            response: undefined,
            responseText: undefined,
            responseXML: undefined,

            // Events
            onload: undefined,
            onloadstart: undefined,
            onloadend: undefined,
            onerror: undefined,
            onprogress: undefined,
            onstatechange: undefined,
            ontimeout: undefined,

            /**
             * Method to add download event listener. The listener is cached until
             * passing paywall. See standard EventTarget documentation for details.
             * @param type type of event.
             * @param callback the call back function.
             * @param options callback options, see EventTarget documentation.
             * @memberof PaywallHttpRequest
             */
            addEventListener : function(type, callback, options){
                addEventListener(xhrOpenData.eventListeners,type,callback,options);
            },

            /**
             * Method to remove download event listener. The listener is cached until
             * passing paywall. See standard EventTarget documentation for details.
             * @param type type of event.
             * @param callback the call back function.
             * @param options callback options, see EventTarget documentation.
             * @memberof PaywallHttpRequest
             */
            removeEventListener : function(type, callback, options){
                removeEventListener(xhrOpenData.eventListeners,type);
            },

            /**
             * Method that shouldn't be called will throw error.
             * @memberof PaywallHttpRequest
             */
            dispatchEvent : function(event){
                throw("Internal dispatchEvent should never be called.");
            },


            /**
             * Method to abort the call and close all underlying resources such as event bus
             * and web socket.
             * @memberof PaywallHttpRequest
             */
            abort : function(){
              aborted = true;
              paywallEventBus.close();
              paywallWebSocketHandler.close();
              xmlHttpRequest.abort();
            },

            // HERE
            /**
             * Method to set the related request header in underlying XMLHttpRequest object.
             * The value is cached so subsequent call after passing paywall it is set again
             * automatically.
             * @param name request header name.
             * @param value request header value.
             * @memberof PaywallHttpRequest
             */
            setRequestHeader : function(name, value){
              xhrSendData.requestHeaders.push({name: name, value: value});
            },

            /**
             * Method to open a connection to given URL and initializes underlying resources
             * for paywall handling.
             *
             * @param method http method to use
             * @param url url to connecto to
             * @param async if call should be asynchronical (default true), optional.
             * @param username username to use with the call, optional
             * @param password password to use with the call, optional
             * @memberof PaywallHttpRequest
             */
            open : function(method, url, async, username, password){
                // Reset send data
                xhrOpenData.method = method;
                xhrOpenData.url = url;
                xhrSendData = {body: null, requestHeaders: []};
                xmlHttpRequest.onreadystatechange = onReadyStateHandler;
                populateBaseEventListeners();
                if(async === undefined) {
                    xmlHttpRequest.open(method, url);
                }else{
                    xhrOpenData.async = async;
                    xhrOpenData.username = username;
                    xhrOpenData.password = password;
                    xmlHttpRequest.open(method, url, async, username, password);
                }
            },

            /**
             * Method to send the request, data is cached to be sent again automatically
             * after payment is settled.
             * @param body optional body data to upload.
             * @memberof PaywallHttpRequest
             */
            send : function(body){
                xhrSendData.body = body;
                populateRequestAttributes();
                populateBaseEventListeners();
                for(var i=0;i<xhrSendData.requestHeaders.length;i++){
                    var requestHeader = xhrSendData.requestHeaders[0];
                    xmlHttpRequest.setRequestHeader(requestHeader.name, requestHeader.value);
                }
                setTokenHeader();
                xmlHttpRequest.send(body);
            },


            /**
             * Method to fetch response header for underlying XMLHttpRequest object.
             * @param name of resonse header.
             * @return {string} response header value.
             * @memberof PaywallHttpRequest
             */
            getResponseHeader : function(name){
                return xmlHttpRequest.getResponseHeader(name);
            },

            /**
             * Method to fetch all response headers from underlying XMLHttpRequest object.
             * @return {string} all response headers value.
             * @memberof PaywallHttpRequest
             */
            getAllResponseHeaders: function(){
                return xmlHttpRequest.getAllResponseHeaders();

            },

            /**
             * Method to override the default mime type sent in response.
             * @param mime mine-type to override with.
             * @memberof PaywallHttpRequest
             */
            overrideMimeType : function(mime){
                return xmlHttpRequest.overrideMimeType(mime);
            },

            /**
             * Paywall section containing paywall extensions to standard XMLHttpRequest methods.
             * @memberof PaywallHttpRequest
             * @namespace PaywallHttpRequest.paywall
             */
            paywall : {

                /**
                 * Method to retrieve invoice if exists in related payment flow.
                 * @returns {Object} related invoice if generated in payment flow, otherwise undefined.
                 * @memberof PaywallHttpRequest.paywall
                 */
                getInvoice: function () {
                    return invoice;
                },
                /**
                 * Returns true if invoice exists in related payment flow.
                 * @returns {boolean} true if invoice exist
                 * @memberof PaywallHttpRequest.paywall
                 */
                hasInvoice: function () {
                    return invoice !== undefined;
                },
                /**
                 * Help method to construct a PaywallTime object of invoice expiration date, used to
                 * simply output remaining hours, minutes, etc of invoice.
                 *
                 * @returns {PaywallTime} if invoice exist
                 * @throws error if no invoice currently exists in payment flow.
                 * @memberof PaywallHttpRequest.paywall
                 */
                getInvoiceExpiration: function () {
                    if (invoice !== undefined) {
                        return new PaywallTime(invoice.invoiceExpireDate);
                    }
                    throw("Invalid state " + getPaywallState() + " when calling method getInvoiceExpiration().");
                },
                /**
                 * Help method to construct to retrieve a  PaywallAmount object of invoice amount that
                 * is easy converted by a specified unit.
                 *
                 * @see PaywallAmount
                 * @returns {PaywallAmount} if invoice exist
                 * @throws error if no invoice currently exists in payment flow.
                 * @memberof PaywallHttpRequest.paywall
                 */
                getInvoiceAmount: function () {
                    if (invoice !== undefined) {
                        return new PaywallAmount(invoice.invoiceAmount);
                    }
                    throw("Invalid state " + getPaywallState() + " when calling method getInvoiceAmount().");
                },

                /**
                 * Method to retrieve settlement if exists in related payment flow.
                 * @returns {Object} related settlement if generated in payment flow, otherwise undefined.
                 * @memberof PaywallHttpRequest.paywall
                 */
                getSettlement: function () {
                    return settlement;
                },
                /**
                 * Returns true if settlement exists in related payment flow.
                 * @returns {boolean} true if settlement exist
                 * @memberof PaywallHttpRequest.paywall
                 */
                hasSettlement: function () {
                    return settlement !== undefined;
                },
                /**
                 * Help method to construct a PaywallTime object of settlement expiration date, used to
                 * simply output remaining hours, minutes, etc of settlement validity.
                 *
                 * @returns {PaywallTime} if settlement exist
                 * @throws error if no settlement currently exists in payment flow. It is best to check
                 * this before calling method.
                 * @memberof PaywallHttpRequest.paywall
                 */
                getSettlementExpiration: function () {
                    if (settlement !== undefined) {
                        return new PaywallTime(settlement.settlementValidUntil);
                    }
                    throw("Invalid state " + getPaywallState() + " when calling method getSettlementExpiration().");
                },
                /**
                 * Help method to construct a PaywallTime object of settlement valid from date, used to
                 * simply output remaining hours, minutes, etc until settlement validity. If no validFrom
                 * field is set in settlement is current date returned.
                 *
                 * @returns {PaywallTime} if settlement exist
                 * @throws error if no settlement currently exists in payment flow. It is best to check
                 * this before calling method.
                 * @memberof PaywallHttpRequest.paywall
                 */
                getSettlementValidFrom: function () {
                    if (settlement !== undefined) {
                        if (settlement.settlementValidFrom != null) {
                            return new PaywallTime(settlement.settlementValidFrom);
                        }
                        return new PaywallTime(new Date().toDateString());
                    }
                    throw("Invalid state " + getPaywallState() + " or when calling method getSettlementValidFrom().");
                },
                /**
                 * Help method returning the full URL to the QR Code generation link. The invoice object can return
                 * a relative url and this help method always ensures a full URL is returned.
                 *
                 * @return {String} full URL to the QR Code generation link.
                 */
                genQRLink: function(){
                    if(invoice !== undefined){
                        return constructFullURL(invoice.qrLink);
                    }
                    throw("Invalid state " + getPaywallState() + " or when calling method genQRLink().");
                },
                /**
                 * Help method returning the full URL to the Check Settlement Endpoint link. The invoice object can return
                 * a relative url and this help method always ensures a full URL is returned.
                 *
                 * @return {String} full URL to the Check Settlement Endpoint link.
                 */
                genCheckSettlementLink: function(){
                    if(invoice !== undefined){
                        return constructFullURL(invoice.checkSettlementLink);
                    }
                    throw("Invalid state " + getPaywallState() + " or when calling method genCheckSettlementLink().");
                },
                /**
                 * Help method returning the full URL to the Check Settlement WebSocket Endpoint link. The invoice
                 * object can return a relative url and this help method always ensures a full URL is returned.
                 *
                 * @return {String} full URL to the Check Settlement WebSocket Endpoint link.
                 */
                genCheckSettlementWebSocketLink: function(){
                    if(invoice !== undefined){
                        return constructFullURL(invoice.checkSettlementWebSocketEndpoint);
                    }
                    throw("Invalid state " + getPaywallState() + " or when calling method genCheckSettlementWebSocketLink().");
                },
                /**
                 * Method to retrieve error object containing error information if paywall related error occurred during payment flow.
                 * @returns {Object} related error if generated in payment flow, otherwise undefined.
                 * @memberof PaywallHttpRequest.paywall
                 */
                getPaywallError: function () {
                    return paywallError;
                },
                /**
                 * Method to retrieve current state of payment flow, will return one of PaywallState enums.
                 * State EXPIRED will be returned both if invoice or settlement have expired. If state is EXPIRED
                 * and settlement is null, then it's the invoice that have expired.
                 *
                 * @return {string} one of PaywallState enumeration values.
                 * @memberof PaywallHttpRequest.paywall
                 */
                getState: getPaywallState,

                /**
                 * Method to add a listener, if listener already exists with given name it will be updated.
                 * Multiple listeners for the same type is supported.
                 *
                 * @param {string} name the unique name of the listener within this payment flow.
                 * @param {PaywallEventType} type the type of event to listen to, or special ALL that receives all events.
                 * @param {function} callback method that should be called on given event. The function should have two parameters
                 * one PaywallEventType and one object containing the object data. Type of object differs for each event.
                 * @memberof PaywallHttpRequest.paywall
                 */
                addEventListener: function (name, type, callback) {
                    paywallEventBus.addListener(name, type, callback);
                },

                /**
                 * Method to remove listener with given name if exists.
                 * @param {string} name the name of listener to remove.
                 * @memberof PaywallHttpRequest.paywall
                 */
                removeEventListener: function (name) {
                    paywallEventBus.removeListener(name);
                }
            }

        };

        // Define eventBus that is dependant on API object.
        var paywallEventBus = new PaywallEventBus(api);
        var paywallWebSocketHandler = new PaywallWebSocket(api,paywallEventBus);



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


        return api;
    };

    /**
     * PaywallAmount is a help class to display invoiced amount in specified unit.
     *
     * With this object it is possible to easy display convert invoiced amount
     * in specified unit.
     *
     * @param {object} amount json object from invoice.
     * @constructor PaywallAmount
     */
    global.PaywallAmount = function PaywallAmount(amount) {

        function normalizeAmount(){
            if(amount.value === undefined){
                throw "Invalid invoice amount object in PaywallAmount.";
            }
            if(amount.currencyCode !== undefined && amount.currencyCode !== CurrencyCode.BTC){
                throw "Invalid invoice currency code " + amount.currencyCode + " in PaywallAmount, currently only BTC is supported.";
            }
            var magnetude = Magnetude.NONE;
            if(amount.magnetude !== undefined){
                magnetude = amount.magnetude;
            }
            switch (magnetude) {
                case Magnetude.NONE:
                    return amount.value;
                case Magnetude.MILLI:
                    return amount.value / 1000;
                case Magnetude.NANO:
                    return amount.value / 1000000;
            }
            throw "Invalid magnetude from invoice " + magnetude  + " in PaywallAmount.";
        }

        var api = {
            /**
             * Method to retrieve the specified amount in specified unit, currently is only BTCUnit enum values specified.
             * @see BTCUnit
             * @param unit the unit that the amount should be displayed as. Currently is only BTCUnit defined units supported
             * @returns {number} the amount in specified unit.
             * @function as
             * @memberof PaywallAmount
             */
            as : function (unit) {
                var sats = normalizeAmount();
                switch (unit) {
                    case BTCUnit.BTC:
                        return sats / 100000000;
                    case BTCUnit.MILLIBTC:
                        return sats / 100000;
                    case BTCUnit.BIT:
                        return sats / 100;
                    case BTCUnit.SAT:
                        return sats;
                    case BTCUnit.MILLISAT:
                        return sats * 1000;
                    case BTCUnit.NANOSAT:
                        return sats * 1000000;
                    default:
                        throw "Unsupported unit " + unit + " used with PaywallAmount, only BTCUnit enumerated values are supported";
                }
            }
        };


        return api;
    };


    /**
     * Private class in charge of maintaining all listeners for a in a payment flow.
     *
     * It allows for registration and un-registration of a listener and onEvent forwards
     * the event to all matching listeners.
     *
     * @param {Paywall|object} paywallHttpRequest the related payment flow.
     * @constructor PaywallEventBus
     */
    function PaywallEventBus(paywallHttpRequest) {
        var listeners = [];
        var currentState = paywallHttpRequest.paywall.getState();


        function checkStateTransition(){
            var newState = paywallHttpRequest.paywall.getState();
            if(currentState !== newState){
                currentState = newState;
                if(newState !== PaywallState.SETTLED) {
                    if (isFinalState(newState)) {
                        clearInterval(stateChecker);
                    }
                    onEvent(getRelatedType(newState), getRelatedObject(newState));
                }
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
         * Method to add a listener to first position in eventBus, if listener already exists with given name it will be removed and the new callback
         * will be added first.
         * @param {string} name the unique name of the listener within this payment flow.
         * @param {PaywallEventType} type the type of event to listen to, or special ALL that receives all events.
         * @param {function} callback method that should be called on given event. The function should have two parameters
         * one PaywallEventType and one object containing the object data. Type of object differs for each event.
         * @memberof PaywallEventBus
         */
        var addListenerFirst = function(name, type, callback) {
            var index = findIndex(name);
            if(index !== -1) {
                listeners.splice(index,1);
            }
            listeners.unshift({name: name, type: type, onEvent: callback});
        };
        this.addListenerFirst = addListenerFirst;

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
            currentState = paywallHttpRequest.paywall.getState();
            var matchingListeners = listeners.filter(function(item){
                return item.type === type || item.type === PaywallEventType.ALL;});
            for(var i=0;i<matchingListeners.length;i++){
                matchingListeners[i].onEvent(type, object);
            }
        };

        this.onEvent = onEvent;

        /**
         * Method to trigger an event from the current status of the payment flow
         * the event type and related object will be calculated automatically.
         * @memberof PaywallEventBus
         */
        var triggerEventFromState = function () {
            var state = paywallHttpRequest.paywall.getState();
            onEvent(getRelatedType(state),getRelatedObject(state));
        };
        this.triggerEventFromState = triggerEventFromState;

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
                    return paywallHttpRequest.paywall.getInvoice();
                case PaywallState.INVOICE_EXPIRED:
                    return paywallHttpRequest.paywall.getInvoice();
                case PaywallState.SETTLED:
                    return paywallHttpRequest.paywall.getSettlement();
                case PaywallState.EXECUTED:
                    return paywallHttpRequest.paywall.getSettlement();
                case PaywallState.SETTLEMENT_NOT_YET_VALID:
                    return paywallHttpRequest.paywall.getSettlement();
                case PaywallState.SETTLEMENT_EXPIRED:
                    return paywallHttpRequest.paywall.getSettlement();
                case PaywallState.PAYWALL_ERROR:
                    return paywallHttpRequest.paywall.getPaywallError();
            }
            throw "Invalid state sent to Paywall EventBus: " + state;
        }

    }



    /**
     * Private class in charge of maintaining WebSocket connection and callbacks
     * to the related payment flow.
     *
     * @param {Paywall|object} paywall the related payment flow.
     * @param {PaywallEventBus|object} eventBus the related payment flow event bus.
     * @constructor PaywallWebSocket
     */
    function PaywallWebSocket(paywall, eventBus) {

        var socket;
        var stompSocket;

        function processWebSocketMessage(message){
            if(message.body){
                var object = JSON.parse(message.body);
                if(object.status === PaywallResponseStatus.OK){
                    var eventType = getSettledStatus(object);
                    console.debug("Paywall WebSocket, received message if type: " + eventType);
                    eventBus.onEvent(eventType,object);
                }else{
                    console.debug("Paywall WebSocket, received error message: " + object);
                    eventBus.onEvent(PaywallEventType.PAYWALL_ERROR,object);
                }
            }else{
                console.debug("Paywall WebSocket, received empty message, ignoring.");
            }
        }

        function processWebSocketError(error){
            var errorObject;
            if(error.headers !== undefined){
                errorObject = {status: PaywallResponseStatus.SERVICE_UNAVAILABLE,
                    message: error.headers.message,
                    errors: [error.headers.message]
                };
            }else{
                errorObject = {status: PaywallResponseStatus.SERVICE_UNAVAILABLE,
                    message: error,
                    errors: [error  ]
                };
            }

            console.debug("Paywall WebSocket connection error: " + errorObject);
            eventBus.onEvent(PaywallEventType.PAYWALL_ERROR,errorObject);
        }

        function getSettledStatus(settlement){
            var now = Date.now();
            if(settlement.settlementValidFrom !== null){
                if(new Date(settlement.settlementValidFrom).getTime() > now){
                    // Settlement not yet valid.
                    return PaywallEventType.SETTLEMENT_NOT_YET_VALID;
                }
            }
            if(new Date(settlement.settlementValidUntil).getTime() < now){
                // Settlement expired
                return PaywallEventType.SETTLEMENT_EXPIRED;
            }
            return PaywallEventType.SETTLED;
        }



        /**
         * Method to close underlying resources and background check.
         * @param {object} invoice the newly generated invoice.
         * @memberof PaywallWebSocket
         */
        var connect = function(invoice){
            socket = new SockJS(paywall.paywall.genCheckSettlementWebSocketLink());
            stompSocket = Stomp.over(socket);

            var headers = {"token": invoice.token};
            stompSocket.connect({}, function(frame) {
                stompSocket.subscribe(invoice.checkSettlementWebSocketQueue, processWebSocketMessage, headers);
            }, processWebSocketError);
        };
        this.connect = connect;

        /**
         * Method to close underlying WebSocket.
         * @memberof PaywallWebSocket
         */
        var close = function(){
            if(stompSocket !== undefined){
                console.debug("Paywall WebSocket, closing connection.");
                stompSocket.disconnect();
                socket.close();
            }
        };
        this.close = close;



    }



})(this);
