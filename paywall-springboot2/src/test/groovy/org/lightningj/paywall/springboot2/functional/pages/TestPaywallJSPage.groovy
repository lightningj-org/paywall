package org.lightningj.paywall.springboot2.functional.pages

import geb.Page

class TestPaywallJSPage extends Page{

    static url = "/jstest/html/testpaywall.js.html"

    static at = {
        title == "Test paywall.js1"
    }

    static content = {
        onMethodNoPayPerRequestButton { $('#makePaywallRequest1') }
        onMethodPayPerRequestButton { $('#makePaywallRequest2') }
        onClassRequest1Button { $('#makePaywallRequest3') }
        onClassRequest2Button { $('#makePaywallRequest4') }

        makeNonPaymentRequestButton { $('#makeNonPaymentRequest') }
        makeTimeoutRequestButton { $('#makeTimeoutRequest') }
        makeAbortRequestButton { $('#makeAbortRequest') }
        makeErrorRequestButton { $('#makeErrorRequest') }
        makeAPIErrorRequestButton { $('#makeAPIErrorRequest') }
        makeAPIErrorNoPaywallRequestButton { $('#makeAPIErrorNoPaywallRequest') }

        invoicePanel(wait: true) { $('#invoicepanel', dynamic: true) }
        invoiceDescription(wait: true) { $('#invoicetext', dynamic: true).text() }
        invoiceAmount(wait: true) { $('#invoiceamount', dynamic: true).text() }
        invoiceNodeInfo(wait: true) { $('#invoiceNodeInfo', dynamic: true).text() }
        invoiceBolt11(wait: true) { $('#invoicebolt11', dynamic: true).text() }
        showInvoiceTimeRemaining(wait: true) { $('#showInvoiceTimeRemaining', dynamic: true) }
        payinvoiceButton(wait: true) { $('#payinvoice', dynamic: true) }

        showSettlementTimeRemainingPanel(wait: true) { $('#showSettlementTimeRemaining', dynamic: true) }
        reUsePaywallReq1Button(wait: true) { $('#reUsePaywallReq1', dynamic: true) }

        eventTable(wait: true) { $('#eventtable', dynamic: true) }

        clearAllButton(wait: true) { $('#clearAll', dynamic: true) }
    }

    void clearAll(){
        interact {
            moveToElement(clearAllButton)
        }

        clearAllButton.click()
    }

    int findEventCount(String eventName){
        return eventTable.find(".${eventName}").size()
    }

    List<String> findEventText(String eventName){
        def retval = []
        //eventTable.find(".${eventName}")
        eventTable.find(".${eventName}").collect {
            retval << it.next().text()
        }
        return retval
    }
}
