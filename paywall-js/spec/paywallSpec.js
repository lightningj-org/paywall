

describe("Verify that Paywall methods returns expected result", function(){
    beforeEach(function (){
        jasmine.clock().uninstall();
        jasmine.clock().install();
    });
    it("Verify that getInvoice returns invoice field if set", function () {
        var paywall = new Paywall();
        expect(paywall.getInvoice()).toBe(undefined);
        paywall.setInvoice(invoice1);
        expect(paywall.getInvoice()).toBe(invoice1);
    });
    it("Verify that hasInvoice returns true if field is set", function () {
        var paywall = new Paywall();
        expect(paywall.hasInvoice()).toBe(false);
        paywall.setInvoice(invoice1);
        expect(paywall.hasInvoice()).toBe(true);
    });
    it("Verify that getInvoiceExpiration returns a PaywallTime instance from the invoice expire field", function () {
        var paywall = new Paywall();
        paywall.setInvoice(invoice1);
        jasmine.clock().mockDate(new Date("2019-06-01T07:50:51.540+0000"));
        expect(paywall.getInvoiceExpiration().remaining().minutes()).toBe("13");
    });

    it("Verify that getInvoiceExpiration throws error if invoice is not yet set", function () {
        var paywall = new Paywall();
        jasmine.clock().mockDate(new Date("2019-06-01T07:50:51.540+0000"));
        expect( function(){ paywall.getInvoiceExpiration(); } ).toThrow("Invalid state NEW when calling method getInvoiceExpiration().");
    });

    it("Verify that getSettlement returns settlment field if set", function () {
        var paywall = new Paywall();
        expect(paywall.getSettlement()).toBe(undefined);
        paywall.setSettlement(settlement1);
        expect(paywall.getSettlement()).toBe(settlement1);
    });
    it("Verify that hasSettlement returns true if field is set", function () {
        var paywall = new Paywall();
        expect(paywall.hasSettlement()).toBe(false);
        paywall.setSettlement(settlement1);
        expect(paywall.hasSettlement()).toBe(true);
    });
    it("Verify that getSettlementExpiration returns a PaywallTime instance from the settlement expire field", function () {
        var paywall = new Paywall();
        paywall.setSettlement(settlement1);
        jasmine.clock().mockDate(new Date("2019-06-01T07:50:51.540+0000"));
        expect(paywall.getSettlementExpiration().remaining().minutes()).toBe("19");
    });
    it("Verify that getSettlementExpiration throws error if settlement is not set", function () {
        var paywall = new Paywall();
        jasmine.clock().mockDate(new Date("2019-06-01T07:50:51.540+0000"));
        expect( function(){ paywall.getSettlementExpiration(); } ).toThrow("Invalid state NEW when calling method getSettlementExpiration().");
    });
    it("Verify that getSettlementValidFrom returns a PaywallTime instance with current time if no valid from date is set.", function () {
        var paywall = new Paywall();
        paywall.setSettlement(settlement1);
        jasmine.clock().mockDate(new Date("2019-06-01T07:50:51.540+0000"));
        expect(paywall.getSettlementValidFrom().remaining().hours()).toBe("00");
        expect(paywall.getSettlementValidFrom().remaining().minutes()).toBe("00");
        expect(paywall.getSettlementValidFrom().remaining().seconds()).toBe("00");
    });
    it("Verify that getSettlementValidFrom returns a PaywallTime instance from the settlement valid from field", function () {
        var paywall = new Paywall();
        paywall.setSettlement(settlement2);
        jasmine.clock().mockDate(new Date("2019-06-01T07:50:51.540+0000"));
        expect(paywall.getSettlementValidFrom().remaining().hours()).toBe("22");
        expect(paywall.getSettlementValidFrom().remaining().minutes()).toBe("19");
    });
    it("Verify that getSettlementValidFrom throws error if settlement is not set", function () {
        var paywall = new Paywall();
        jasmine.clock().mockDate(new Date("2019-06-01T07:50:51.540+0000"));
        expect( function(){ paywall.getSettlementValidFrom(); } ).toThrow("Invalid state NEW or when calling method getSettlementValidFrom().");
    });
    it("Verify getPaywallError returns the paywall error", function() {
        var paywall = new Paywall();
        paywall.setPaywallError(errorInvalid1);
        expect(paywall.getPaywallError().status).toBe("UNAUTHORIZED");
    });
    it("Verify getAPIError returns the api error", function() {
        var paywall = new Paywall();
        paywall.setAPIError(apiError1);
        expect(paywall.getAPIError().status).toBe("INTERNAL_ERROR");
    });
    it("Verify that registerListener adds a listener to the event bus and unregister that removes it.", function () {
        var paywall = new Paywall();
        expect(paywall.getEventBus()).not.toBeUndefined();
        paywall.registerListener("Listener1",PaywallEventType.INVOICE, function (type, object) {});
        expect(paywall.getEventBus().getListeners().length).toBe(1);

        paywall.unregisterListener("Listener1");
        expect(paywall.getEventBus().getListeners().length).toBe(0);
    });

    afterEach(function (){
        jasmine.clock().uninstall();
    });
});


describe("Verify that Paywall.getState calculates the correct status", function() {
    beforeEach(function (){
        jasmine.clock().uninstall();
        jasmine.clock().install();
    });
    it("Verify that newly constructed Paywall object returns state NEW", function() {
       expect(new Paywall().getState()).toBe(PaywallState.NEW);
    });
    it("Verify that Paywall object that have received invoice that isn't expired returns INVOICE", function() {
        jasmine.clock().mockDate(new Date("2019-06-01T07:50:51.540+0000"));
        var paywall = new Paywall();
        paywall.setInvoice(invoice1);
        expect(paywall.getState()).toBe(PaywallState.INVOICE);
    });
    it("Verify that Paywall object that have received invoice that is expired and no settlement returns EXPIRED", function() {
        jasmine.clock().mockDate(new Date("2019-06-01T08:04:51.540+0000"));
        var paywall = new Paywall();
        paywall.setInvoice(invoice1);
        expect(paywall.getState()).toBe(PaywallState.INVOICE_EXPIRED);
    });
    it("Verify that Paywall object that have received settlement that is valid returns SETTLED even though invoice has expired.", function() {
        jasmine.clock().mockDate(new Date("2019-06-01T08:04:51.540+0000"));
        var paywall = new Paywall();
        paywall.setInvoice(invoice1);
        paywall.setSettlement(settlement1);
        expect(paywall.getState()).toBe(PaywallState.SETTLED);
    });
    it("Verify that Paywall object that have received settlement that is has expired returns EXPIRED.", function() {
        jasmine.clock().mockDate(new Date("2019-06-02T08:04:51.540+0000"));
        var paywall = new Paywall();
        paywall.setInvoice(invoice1);
        paywall.setSettlement(settlement1);
        expect(paywall.getState()).toBe(PaywallState.SETTLEMENT_EXPIRED);
    });
    it("Verify that Paywall object that have received settlement that is not yet valid returns NOT_YET_VALID.", function() {
        jasmine.clock().mockDate(new Date("2019-06-02T06:09:29.354+0000"));
        var paywall = new Paywall();
        paywall.setInvoice(invoice1);
        paywall.setSettlement(settlement2);
        expect(paywall.getState()).toBe(PaywallState.SETTLEMENT_NOT_YET_VALID);
    });
    it("Verify that Paywall object that is pay per request and is executed returns EXECUTED.", function() {
        jasmine.clock().mockDate(new Date("2019-06-02T06:09:29.354+0000"));
        var paywall = new Paywall();
        paywall.setInvoice(invoice1);
        paywall.setSettlement(settlement2);
        paywall.setExecuted(true);
        expect(paywall.getState()).toBe(PaywallState.EXECUTED);
    });
    it("Verify that Paywall flow where paywall related error occurred returns PAYWALL_ERROR.", function() {
        jasmine.clock().mockDate(new Date("2019-06-02T06:09:29.354+0000"));
        var paywall = new Paywall();
        paywall.setInvoice(invoice1);
        paywall.setSettlement(settlement2);
        paywall.setExecuted(true);
        paywall.setPaywallError(errorInvalid1);
        expect(paywall.getState()).toBe(PaywallState.PAYWALL_ERROR);
    });
    it("Verify that Paywall flow where underlying apu error occurred returns API_ERROR and that it has president over PAYWALL_ERROR.", function() {
        jasmine.clock().mockDate(new Date("2019-06-02T06:09:29.354+0000"));
        var paywall = new Paywall();
        paywall.setInvoice(invoice1);
        paywall.setSettlement(settlement2);
        paywall.setExecuted(true);
        paywall.setPaywallError(errorInvalid1);
        paywall.setAPIError(errorInvalid1);
        expect(paywall.getState()).toBe(PaywallState.API_ERROR);
    });
    afterEach(function (){
        jasmine.clock().uninstall();
    });
});

describe("Verify that PaywallTime calculates remaining times correctly", function() {
    beforeEach(function (){
        jasmine.clock().uninstall();
       jasmine.clock().install();
    });
    it("Verify that getTimeStamp() returns the set timeStamp as a Date", function() {
        var paywallTime = new PaywallTime(invoice1.invoiceExpireDate);
        expect(paywallTime.getTimeStamp().toUTCString()).toBe("Sat, 01 Jun 2019 08:03:51 GMT");
    });
    it("Verify that remaining() returns a PaywallTimeUnit with the difference between current date and specified date.", function () {
        jasmine.clock().mockDate(new Date("2019-06-01T07:50:51.540+0000"));
        var paywallTime = new PaywallTime(invoice1.invoiceExpireDate);

        expect(paywallTime.remaining().asMS()).toBe(780000);
        expect(paywallTime.remaining().seconds()).toBe("00");
        expect(paywallTime.remaining().minutes()).toBe("13");
        expect(paywallTime.remaining().hours()).toBe("00");
        expect(paywallTime.remaining().days()).toBe("0");

        jasmine.clock().mockDate(new Date("2019-05-20T07:12:59.540+0000"));
        expect(paywallTime.remaining().asMS()).toBe(1039852000);
        expect(paywallTime.remaining().seconds()).toBe("52");
        expect(paywallTime.remaining().minutes()).toBe("50");
        expect(paywallTime.remaining().hours()).toBe("00");
        expect(paywallTime.remaining().days()).toBe("12");

        jasmine.clock().mockDate(new Date("2019-06-01T01:12:59.540+0000"));
        expect(paywallTime.remaining().asMS()).toBe(24652000);
        expect(paywallTime.remaining().seconds()).toBe("52");
        expect(paywallTime.remaining().minutes()).toBe("50");
        expect(paywallTime.remaining().hours()).toBe("06");
        expect(paywallTime.remaining().days()).toBe("0");

        // Verify that expired invoice return 0 on all
        jasmine.clock().mockDate(new Date("2019-06-01T08:03:52.540+0000"));
        expect(paywallTime.remaining().asMS()).toBe(0);
        expect(paywallTime.remaining().seconds()).toBe("00");
        expect(paywallTime.remaining().minutes()).toBe("00");
        expect(paywallTime.remaining().hours()).toBe("00");
        expect(paywallTime.remaining().days()).toBe("0");
    });

    afterEach(function (){
       jasmine.clock().uninstall();
    });
});


describe("Verify PaywallState enum values", function() {
    it("Expect PaywallState enum NEW has value 'NEW'", function() {
        expect(PaywallState.NEW).toBe("NEW");
    });
    it("Expect PaywallState enum INVOICE has value 'INVOICE'", function() {
        expect(PaywallState.INVOICE).toBe("INVOICE");
    });
    it("Expect PaywallState enum INVOICE_EXPIRED has value 'INVOICE_EXPIRED'", function() {
        expect(PaywallState.INVOICE_EXPIRED).toBe("INVOICE_EXPIRED");
    });
    it("Expect PaywallState enum SETTLED has value 'SETTLED'", function() {
        expect(PaywallState.SETTLED).toBe("SETTLED");
    });
    it("Expect PaywallState enum EXECUTED has value 'EXECUTED'", function() {
        expect(PaywallState.EXECUTED).toBe("EXECUTED");
    });
    it("Expect PaywallState enum SETTLEMENT_NOT_YET_VALID has value 'SETTLEMENT_NOT_YET_VALID'", function() {
        expect(PaywallState.SETTLEMENT_NOT_YET_VALID).toBe("SETTLEMENT_NOT_YET_VALID");
    });
    it("Expect PaywallState enum SETTLEMENT_EXPIRED has value 'SETTLEMENT_EXPIRED'", function() {
        expect(PaywallState.SETTLEMENT_EXPIRED).toBe("SETTLEMENT_EXPIRED");
    });
    it("Expect PaywallState enum PAYWALL_ERROR has value 'PAYWALL_ERROR'", function() {
        expect(PaywallState.PAYWALL_ERROR).toBe("PAYWALL_ERROR");
    });
    it("Expect PaywallState enum API_ERROR has value 'API_ERROR'", function() {
        expect(PaywallState.API_ERROR).toBe("API_ERROR");
    });
});

describe("Verify PaywallEventType enum values", function() {
    it("Expect PaywallEventType enum ALL has value 'ALL'", function() {
        expect(PaywallEventType.ALL).toBe("ALL");
    });
    it("Expect PaywallEventType enum INVOICE has value 'INVOICE'", function() {
        expect(PaywallEventType.INVOICE).toBe("INVOICE");
    });
    it("Expect PaywallEventType enum INVOICE_EXPIRED has value 'INVOICE_EXPIRED'", function() {
        expect(PaywallEventType.INVOICE_EXPIRED).toBe("INVOICE_EXPIRED");
    });
    it("Expect PaywallEventType enum SETTLED has value 'SETTLED'", function() {
        expect(PaywallEventType.SETTLED).toBe("SETTLED");
    });
    it("Expect PaywallEventType enum EXECUTED has value 'EXECUTED'", function() {
        expect(PaywallEventType.EXECUTED).toBe("EXECUTED");
    });
    it("Expect PaywallEventType enum SETTLEMENT_NOT_YET_VALID has value 'SETTLEMENT_NOT_YET_VALID'", function() {
        expect(PaywallEventType.SETTLEMENT_NOT_YET_VALID).toBe("SETTLEMENT_NOT_YET_VALID");
    });
    it("Expect PaywallEventType enum SETTLEMENT_EXPIRED has value 'SETTLEMENT_EXPIRED'", function() {
        expect(PaywallEventType.SETTLEMENT_EXPIRED).toBe("SETTLEMENT_EXPIRED");
    });
    it("Expect PaywallEventType enum PAYWALL_ERROR has value 'PAYWALL_ERROR'", function() {
        expect(PaywallEventType.PAYWALL_ERROR).toBe("PAYWALL_ERROR");
    });
    it("Expect PaywallEventType enum API_ERROR has value 'API_ERROR'", function() {
        expect(PaywallEventType.API_ERROR).toBe("API_ERROR");
    });
});

describe("Verify PaywallResponseStatus enum values", function() {
    it("Expect PaywallResponseStatus enum OK has value 'OK'", function() {
        expect(PaywallResponseStatus.OK).toBe("OK");
    });
    it("Expect PaywallResponseStatus enum BAD_REQUEST has value 'BAD_REQUEST'", function() {
        expect(PaywallResponseStatus.BAD_REQUEST).toBe("BAD_REQUEST");
    });
    it("Expect PaywallResponseStatus enum SERVICE_UNAVAILABLE has value 'SERVICE_UNAVAILABLE'", function() {
        expect(PaywallResponseStatus.SERVICE_UNAVAILABLE).toBe("SERVICE_UNAVAILABLE");
    });
    it("Expect PaywallResponseStatus enum UNAUTHORIZED has value 'UNAUTHORIZED'", function() {
        expect(PaywallResponseStatus.UNAUTHORIZED).toBe("UNAUTHORIZED");
    });
    it("Expect PaywallResponseStatus enum INTERNAL_SERVER_ERROR has value 'INTERNAL_SERVER_ERROR'", function() {
        expect(PaywallResponseStatus.INTERNAL_SERVER_ERROR).toBe("INTERNAL_SERVER_ERROR");
    });
});


describe("Verify PaywallEventBus public methods handle events properly", function() {

    beforeEach(function(){
        this.paywall = new Paywall();
        this.paywall.setInvoice(invoice1);
        this.paywall.setSettlement(settlement1);
        this.paywall.setPaywallError(errorInvalid1);
        this.paywall.setAPIError(apiError1);

        this.eventBus = new PaywallEventBus(this.paywall);
    });

    it("Expect addListener adds listener correctly, callbacks are called on onEvent and removeListener removes expected listeners", function() {
        var eventBus = this.eventBus;
        expect(eventBus.getListeners().length).toBe(0);

        var listener1Calls = [];
        eventBus.addListener("Listener1", PaywallEventType.API_ERROR, function(type, object){
            listener1Calls.push({type: type, object: object});
        });
        expect(eventBus.getListeners().length).toBe(1);
        expect(eventBus.getListeners()[0].name).toBe("Listener1");
        expect(eventBus.getListeners()[0].type).toBe(PaywallEventType.API_ERROR);
        expect(eventBus.getListeners()[0].onEvent).toBeDefined();

        var listener2Calls = [];
        eventBus.addListener("Listener2", PaywallEventType.ALL, function(type, object){
            listener2Calls.push({type: type, object: object});
        });
        expect(eventBus.getListeners().length).toBe(2);
        expect(eventBus.getListeners()[1].name).toBe("Listener2");
        expect(eventBus.getListeners()[1].type).toBe(PaywallEventType.ALL);
        expect(eventBus.getListeners()[1].onEvent).toBeDefined();

        var listener3Calls = [];
        eventBus.addListener("Listener3", PaywallEventType.PAYWALL_ERROR, function(type, object){
            listener3Calls.push({type: type, object: object});
        });
        expect(eventBus.getListeners().length).toBe(3);
        expect(eventBus.getListeners()[2].name).toBe("Listener3");
        expect(eventBus.getListeners()[2].type).toBe(PaywallEventType.PAYWALL_ERROR);
        expect(eventBus.getListeners()[2].onEvent).toBeDefined();

        eventBus.onEvent(PaywallEventType.API_ERROR, {name: "apierror1"});
        expect(listener1Calls.length).toBe(1);
        expect(listener1Calls[0].type).toBe(PaywallEventType.API_ERROR);
        expect(listener1Calls[0].object.name).toBe("apierror1");
        expect(listener2Calls.length).toBe(1);
        expect(listener2Calls[0].type).toBe(PaywallEventType.API_ERROR);
        expect(listener2Calls[0].object.name).toBe("apierror1");
        expect(listener3Calls.length).toBe(0);

        // Verify that addListener replaces existing listener
        var listener4Calls = [];
        eventBus.addListener("Listener2", PaywallEventType.PAYWALL_ERROR, function(type, object){
            listener4Calls.push({type: type, object: object});
        });
        expect(eventBus.getListeners().length).toBe(3);
        expect(eventBus.getListeners()[1].name).toBe("Listener2");
        expect(eventBus.getListeners()[1].type).toBe(PaywallEventType.PAYWALL_ERROR);
        expect(eventBus.getListeners()[1].onEvent).toBeDefined();

        eventBus.onEvent(PaywallEventType.PAYWALL_ERROR, {name: "paywallerror1"});
        expect(listener3Calls.length).toBe(1);
        expect(listener3Calls[0].type).toBe(PaywallEventType.PAYWALL_ERROR);
        expect(listener3Calls[0].object.name).toBe("paywallerror1");
        expect(listener4Calls.length).toBe(1);
        expect(listener4Calls[0].type).toBe(PaywallEventType.PAYWALL_ERROR);
        expect(listener4Calls[0].object.name).toBe("paywallerror1");
        expect(listener1Calls.length).toBe(1);

        eventBus.removeListener("NonExisting");
        expect(eventBus.getListeners().length).toBe(3);

        eventBus.removeListener("Listener2");
        expect(eventBus.getListeners().length).toBe(2);
        expect(eventBus.getListeners()[0].name).toBe("Listener1");
        expect(eventBus.getListeners()[1].name).toBe("Listener3");

        eventBus.removeListener("Listener1");
        expect(eventBus.getListeners().length).toBe(1);
        expect(eventBus.getListeners()[0].name).toBe("Listener3");

        eventBus.removeListener("Listener3");
        expect(eventBus.getListeners().length).toBe(0);

    });

    it("Verify that close() calls clearInterval", function(){
        var clearIntervalSpy = spyOn(window, 'clearInterval');
        this.eventBus.close();
        expect(clearIntervalSpy).toHaveBeenCalled();
    });

    it("Verify that getRelatedType() throws error given state " + PaywallState.NEW, function () {
        var eventBus = this.eventBus;
        expect( function(){ eventBus.getRelatedType(PaywallState.NEW); } ).toThrow("Invalid state sent to Paywall EventBus: " + PaywallState.NEW);
    });

    it("Verify that getRelatedObject() throws error given state " + PaywallState.NEW, function () {
        var eventBus = this.eventBus;
        expect( function(){ eventBus.getRelatedObject(PaywallState.NEW); } ).toThrow("Invalid state sent to Paywall EventBus: " + PaywallState.NEW);
    });

    var stateTestData = [
        {state: PaywallState.INVOICE, expectedFinal: false, expectedType: PaywallEventType.INVOICE, expectedObject: invoice1},
        {state: PaywallState.INVOICE_EXPIRED, expectedFinal: true, expectedType: PaywallEventType.INVOICE_EXPIRED, expectedObject: invoice1},
        {state: PaywallState.SETTLEMENT_NOT_YET_VALID, expectedFinal: false, expectedType: PaywallEventType.SETTLEMENT_NOT_YET_VALID, expectedObject: settlement1},
        {state: PaywallState.SETTLED, expectedFinal: false, expectedType: PaywallEventType.SETTLED, expectedObject: settlement1},
        {state: PaywallState.SETTLEMENT_EXPIRED, expectedFinal: true, expectedType: PaywallEventType.SETTLEMENT_EXPIRED, expectedObject: settlement1},
        {state: PaywallState.EXECUTED, expectedFinal: true, expectedType: PaywallEventType.EXECUTED, expectedObject: settlement1},
        {state: PaywallState.PAYWALL_ERROR, expectedFinal: true, expectedType: PaywallEventType.PAYWALL_ERROR, expectedObject: errorInvalid1},
        {state: PaywallState.API_ERROR, expectedFinal: true, expectedType: PaywallEventType.API_ERROR, expectedObject: apiError1}
        ];

    using(stateTestData, function(testData){
        it("Verify that finalState for state " + testData.state + " is " + testData.expectedFinal, function () {
            expect(this.eventBus.isFinalState(testData.state)).toBe(testData.expectedFinal);
        } );
        it("Verify that getRelatedType for state " + testData.state + " returns " + testData.expectedType, function () {
            expect(this.eventBus.getRelatedType(testData.state)).toBe(testData.expectedType);
        } );
        it("Verify that getRelatedObject for state " + testData.state + " returns expected object.", function () {
            expect(this.eventBus.getRelatedObject(testData.state)).toBe(testData.expectedObject);
        } );
    });

});

describe("Verify PaywallEventBus background thread transistions state properly", function() {

    beforeEach(function(){
        jasmine.clock().install();
        this.paywall = new Paywall();

        this.eventBus = new PaywallEventBus(this.paywall);

    });


    it("Verify that checkStateTransition is run in background and updates and triggers states properly.", function(){
        var clearIntervalSpy = spyOn(window, 'clearInterval');
        jasmine.clock().mockDate(new Date("2019-06-01T07:50:51.540+0000"));
        // Setup listeners
        var listener1Calls = [];
        this.eventBus.addListener("Listener1", PaywallEventType.INVOICE,  function(type, object){
            listener1Calls.push({type: type, object: object});
        });
        var listener2Calls = [];
        this.eventBus.addListener("Listener2", PaywallEventType.ALL,  function(type, object){
            listener2Calls.push({type: type, object: object});
        });

        // expect state to be NEW
        expect(this.paywall.getState()).toBe(PaywallState.NEW);
        expect(this.eventBus.getCurrentState()).toBe(PaywallState.NEW);

        // Let statecheck run once, and verify that no event is triggered
        jasmine.clock().tick(1001);
        expect(this.paywall.getState()).toBe(PaywallState.NEW);
        expect(this.eventBus.getCurrentState()).toBe(PaywallState.NEW);
        expect(clearIntervalSpy).not.toHaveBeenCalled();
        expect(listener1Calls.length).toBe(0);
        expect(listener2Calls.length).toBe(0);

        // Change paywall state to INVOICE
        this.paywall.setInvoice(invoice1);
        expect(this.paywall.getState()).toBe(PaywallState.INVOICE);
        expect(this.eventBus.getCurrentState()).toBe(PaywallState.NEW);

        // Let statecheck run, and verify that event is triggered
        jasmine.clock().tick(1001);
        expect(clearIntervalSpy).not.toHaveBeenCalled();
        expect(this.paywall.getState()).toBe(PaywallState.INVOICE);
        expect(this.eventBus.getCurrentState()).toBe(PaywallState.INVOICE);
        expect(listener1Calls.length).toBe(1);
        expect(listener1Calls[0].type).toBe(PaywallEventType.INVOICE);
        expect(listener1Calls[0].object).toBe(invoice1);
        expect(listener2Calls.length).toBe(1);
        expect(listener2Calls[0].type).toBe(PaywallEventType.INVOICE);
        expect(listener2Calls[0].object).toBe(invoice1);

        // Change paywall state to EXECUTED
        this.paywall.setSettlement(settlement1);
        this.paywall.setExecuted(true);
        expect(this.paywall.getState()).toBe(PaywallState.EXECUTED);
        expect(this.eventBus.getCurrentState()).toBe(PaywallState.INVOICE);

        // Let statecheck run, and verify that event is triggered and clearInterval was called since state is final.
        jasmine.clock().tick(1001);
        expect(clearIntervalSpy).toHaveBeenCalled();
        expect(this.paywall.getState()).toBe(PaywallState.EXECUTED);
        expect(this.eventBus.getCurrentState()).toBe(PaywallState.EXECUTED);
        expect(listener1Calls.length).toBe(1);
        expect(listener1Calls[0].type).toBe(PaywallEventType.INVOICE);
        expect(listener1Calls[0].object).toBe(invoice1);
        expect(listener2Calls.length).toBe(2);
        expect(listener2Calls[0].type).toBe(PaywallEventType.INVOICE);
        expect(listener2Calls[0].object).toBe(invoice1);
        expect(listener2Calls[1].type).toBe(PaywallEventType.EXECUTED);
        expect(listener2Calls[1].object).toBe(settlement1);
    });

    afterEach(function (){
        jasmine.clock().uninstall();
    });
});

describe("Verify PaywallWebSocket processWebSocketError callback function", function() {

    beforeEach(function(){
        this.paywall = new Paywall();
        this.paywallWebSocket = this.paywall.getWebSocketHandler();
    });

    it("Verify that processWebSocketError converts to paywall api error object", function(){
        var objects = [];
        this.paywall.registerListener("PaywallErrorListener", PaywallEventType.PAYWALL_ERROR,  function(type, object){
            objects.push(object);
        });

        this.paywallWebSocket.processWebSocketError({headers:{message: "Some Message"}});

        expect(objects.length).toBe(1);
        expect(objects[0].status).toBe(PaywallResponseStatus.SERVICE_UNAVAILABLE);
        expect(objects[0].message).toBe("Some Message");
        expect(objects[0].errors.length).toBe(1);
        expect(objects[0].errors[0]).toBe("Some Message");

    });
});

describe("Verify PaywallWebSocket processWebSocketMessage callback function", function() {

    beforeEach(function(){
        jasmine.clock().uninstall();
        jasmine.clock().install();
        this.paywall = new Paywall();
        this.paywallWebSocket = this.paywall.getWebSocketHandler();
    });

    it("Verify that processWebSocketMessage converts valid settlement with event type SETTLED", function(){
        jasmine.clock().mockDate(new Date("2019-06-01T08:04:51.540+0000"));

        var objects = [];
        this.paywall.registerListener("PaywallListener", PaywallEventType.SETTLED,  function(type, object){
            objects.push(object);
        });
        this.paywall.setInvoice(invoice1);

        this.paywallWebSocket.processWebSocketMessage({body:JSON.stringify(settlement1)});

        expect(objects.length).toBe(1);
        expect(objects[0].status).toBe(PaywallResponseStatus.OK);
        expect(objects[0].type).toBe("settlement");

    });

    it("Verify that processWebSocketMessage converts not yet valid settlement with event type SETTLEMENT_NOT_YET_VALID", function(){
        jasmine.clock().mockDate(new Date("2019-06-02T06:09:29.354+0000"));

        var objects = [];
        this.paywall.registerListener("PaywallListener", PaywallEventType.SETTLEMENT_NOT_YET_VALID,  function(type, object){
            objects.push(object);
        });
        this.paywall.setInvoice(invoice1);

        this.paywallWebSocket.processWebSocketMessage({body:JSON.stringify(settlement2)});

        expect(objects.length).toBe(1);
        expect(objects[0].status).toBe(PaywallResponseStatus.OK);
        expect(objects[0].type).toBe("settlement");

    });

    it("Verify that processWebSocketMessage converts not expired settlement with event type SETTLEMENT_EXPIRED", function(){
        jasmine.clock().mockDate(new Date("2019-06-02T08:04:51.540+0000"));

        var objects = [];
        this.paywall.registerListener("PaywallListener", PaywallEventType.SETTLEMENT_EXPIRED,  function(type, object){
            objects.push(object);
        });
        this.paywall.setInvoice(invoice1);

        this.paywallWebSocket.processWebSocketMessage({body:JSON.stringify(settlement1)});

        expect(objects.length).toBe(1);
        expect(objects[0].status).toBe(PaywallResponseStatus.OK);
        expect(objects[0].type).toBe("settlement");

    });

    it("Verify that processWebSocketMessage converts paywall error messate correctly with event type PAYWALL_ERROR", function(){
        var objects = [];
        this.paywall.registerListener("PaywallListener", PaywallEventType.PAYWALL_ERROR,  function(type, object){
            objects.push(object);
        });
        this.paywall.setInvoice(invoice1);

        this.paywallWebSocket.processWebSocketMessage({body:JSON.stringify(errorInvalid1)});

        expect(objects.length).toBe(1);
        expect(objects[0].status).toBe(PaywallResponseStatus.UNAUTHORIZED);
        expect(objects[0].message).toBe("JWT Token Problem: Unable to decrypt token: Authentication tag check failed. Message=A1qdSfqu7PEOt1ddbMaqpA calculated=sfJe73RWoSJeMdKmLTokjw");

    });

    afterEach(function (){
        jasmine.clock().uninstall();
    });
});

// The PaywallWebSocket connect and close methods are tested during functional tests.
