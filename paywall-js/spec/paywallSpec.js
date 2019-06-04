
describe("Verify that Paywall methods returns expected result", function(){
    beforeEach(function (){
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
    it("Verify getError returns the error", function() {
        var paywall = new Paywall();
        paywall.setError(errorInvalid1);
        expect(paywall.getError().status).toBe("UNAUTHORIZED");
    });

    afterEach(function (){
        jasmine.clock().uninstall();
    });
});


describe("Verify that Paywall.getState calculates the correct status", function() {
    beforeEach(function (){
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
        expect(paywall.getState()).toBe(PaywallState.EXPIRED);
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
        expect(paywall.getState()).toBe(PaywallState.EXPIRED);
    });
    it("Verify that Paywall object that have received settlement that is not yet valid returns NOT_YET_VALID.", function() {
        jasmine.clock().mockDate(new Date("2019-06-02T06:09:29.354+0000"));
        var paywall = new Paywall();
        paywall.setInvoice(invoice1);
        paywall.setSettlement(settlement2);
        expect(paywall.getState()).toBe(PaywallState.NOT_YET_VALID);
    });
    it("Verify that Paywall object that is pay per request and is executed returns EXECUTED.", function() {
        jasmine.clock().mockDate(new Date("2019-06-02T06:09:29.354+0000"));
        var paywall = new Paywall();
        paywall.setInvoice(invoice1);
        paywall.setSettlement(settlement2);
        paywall.setExecuted(true);
        expect(paywall.getState()).toBe(PaywallState.EXECUTED);
    });
    it("Verify that Paywall flow where error occurred returns ERROR.", function() {
        jasmine.clock().mockDate(new Date("2019-06-02T06:09:29.354+0000"));
        var paywall = new Paywall();
        paywall.setInvoice(invoice1);
        paywall.setSettlement(settlement2);
        paywall.setExecuted(true);
        paywall.setError(errorInvalid1);
        expect(paywall.getState()).toBe(PaywallState.ERROR);
    });
    afterEach(function (){
        jasmine.clock().uninstall();
    });
});

describe("Verify that PaywallTime calculates remaining times correctly", function() {
    beforeEach(function (){
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
    it("Expect PaywallState enum NEW has value 'NEW", function() {
        expect(PaywallState.NEW).toBe("NEW");
    });
    it("Expect PaywallState enum INVOICE has value 'INVOICE", function() {
        expect(PaywallState.INVOICE).toBe("INVOICE");
    });
    it("Expect PaywallState enum SETTLED has value 'SETTLED", function() {
        expect(PaywallState.SETTLED).toBe("SETTLED");
    });
    it("Expect PaywallState enum EXECUTED has value 'EXECUTED", function() {
        expect(PaywallState.EXECUTED).toBe("EXECUTED");
    });
    it("Expect PaywallState enum NOT_YET_VALID has value 'NOT_YET_VALID", function() {
        expect(PaywallState.NOT_YET_VALID).toBe("NOT_YET_VALID");
    });
    it("Expect PaywallState enum EXPIRED has value 'EXPIRED", function() {
        expect(PaywallState.EXPIRED).toBe("EXPIRED");
    });
    it("Expect PaywallState enum ERROR has value 'ERROR", function() {
        expect(PaywallState.ERROR).toBe("ERROR");
    });
});


