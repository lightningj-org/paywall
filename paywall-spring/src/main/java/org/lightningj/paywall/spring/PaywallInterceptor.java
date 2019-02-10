package org.lightningj.paywall.spring;

import org.lightningj.paywall.annotations.PaymentRequired;
import org.lightningj.paywall.currencyconverter.CurrencyConverter;
import org.lightningj.paywall.lightninghandler.LightningHandler;
import org.lightningj.paywall.orderrequestgenerator.OrderRequestGeneratorFactory;
import org.lightningj.paywall.paymentflow.PaymentFlowManager;
import org.lightningj.paywall.paymenthandler.PaymentHandler;
import org.lightningj.paywall.tokengenerator.TokenGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.logging.Logger;

// TODO Move this
public class PaywallInterceptor implements HandlerInterceptor {

    @Autowired
    PaywallProperties paywallProperties;

    @Autowired
    LightningHandler lightningHandler;

    @Autowired
    TokenGenerator tokenGenerator;

    @Autowired
    CurrencyConverter currencyConverter;

    @Autowired
    OrderRequestGeneratorFactory orderRequestGeneratorFactory;

    @Autowired
    PaymentHandler paymentHandler;

    @Autowired
    PaymentFlowManager paymentFlowManager;

    Logger log = Logger.getLogger(PaywallInterceptor.class.getName());

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        PaymentRequired paymentRequired = null;
        if(handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            paymentRequired = handlerMethod.getMethodAnnotation(PaymentRequired.class);
            if (paymentRequired == null) {
                paymentRequired = AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getBeanType(), PaymentRequired.class);
            }

        }
        log.severe("Prehandle handler: " + paymentRequired);
        return true;
    }
}
