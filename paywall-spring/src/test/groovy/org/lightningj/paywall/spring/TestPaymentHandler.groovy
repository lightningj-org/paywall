package org.lightningj.paywall.spring

import org.lightningj.paywall.InternalErrorException
import org.lightningj.paywall.lightninghandler.LightningHandlerContext
import org.lightningj.paywall.paymenthandler.PaymentEventType
import org.lightningj.paywall.paymenthandler.data.PaymentData
import org.lightningj.paywall.vo.OrderRequest
import org.springframework.stereotype.Component

@Component("paymentHandler")
class TestPaymentHandler extends SpringPaymentHandler {
    /**
     * Method that should generate a new PaymentData for a given order request.
     * This is the first call in a payment flow and the implementation should
     * look up the order amount from the article id, units and other options in
     * the order request.
     *
     * The generated PaymentData should be at least MinimalPaymentData with preImageHash
     * and orderedAmount set.
     *
     * It is recommended that the PaymentData is persisted in this call but could
     * be skipped for performance in certain payment flows.
     *
     * @param preImageHash the unique preImageHash used to identify a payment flow
     *                     withing a lightning payment.
     * @param orderRequest the specification of the payment data that should be created calculated
     *                     from data in the PaymentRequired annotation.
     * @return a newly generated PaymentData signaling a new payment flow used to
     * create an Order value object.
     * @throws IOException if communication exception occurred in underlying components.
     * @throws InternalErrorException if internal exception occurred generating new payment data.
     */
    @Override
    protected PaymentData newPaymentData(byte[] preImageHash, OrderRequest orderRequest) throws IOException, InternalErrorException {
        return null
    }

    /**
     * Method to lookup a payment data in the payment handler.
     *
     * @param preImageHash the unique preImageHash used to identify a payment flow
     *                     withing a lightning payment.
     * @return return related payment data or null if not found.
     * @throws IOException if communication exception occurred in underlying components.
     * @throws InternalErrorException if internal exception occurred fetching related payment data.
     */
    @Override
    protected PaymentData findPaymentData(byte[] preImageHash) throws IOException, InternalErrorException {
        return null
    }

    /**
     * Method called on update events about a given payment data. This could be when
     * the payment is added as invoice in LND and contains complementary data or when
     * the invoice was settled and contains settled flag set and settled amount and date
     * (depending on the type of PaymentData used in PaymentHandler).
     *
     * The related payment data (using preImageHash as unique identifier) is automatically
     * looked up and the implementing method should at least persist the updated data.
     *
     * @param type the type of event such as INVOICE_CREATED or INVOICE_SETTLED.
     * @param paymentData the payment data to update and persist.
     * @param context the latest known state of the lightning handler.  Null if no known state exists.
     * @throws IOException if communication exception occurred in underlying components.
     * @throws InternalErrorException if internal exception occurred updating related payment data.
     */
    @Override
    protected void updatePaymentData(PaymentEventType type, PaymentData paymentData, LightningHandlerContext context) throws IOException, InternalErrorException {

    }
}
