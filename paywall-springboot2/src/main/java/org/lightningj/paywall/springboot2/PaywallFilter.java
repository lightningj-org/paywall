package org.lightningj.paywall.springboot2;

import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

// TOOD WebFilter and test
public class PaywallFilter extends GenericFilterBean {


    Logger log = Logger.getLogger(PaywallFilter.class.getName());

    /**
     * TODO Proper doc.
     * The <code>doFilter</code> method of the Filter is called by the container
     * each time a request/response pair is passed through the chain due to a
     * client request for a resource at the end of the chain. The FilterChain
     * passed in to this method allows the Filter to pass on the request and
     * response to the next entity in the chain.
     * <p>
     * A typical implementation of this method would follow the following
     * pattern:- <br>
     * 1. Examine the request<br>
     * 2. Optionally wrap the request object with a custom implementation to
     * filter content or headers for input filtering <br>
     * 3. Optionally wrap the response object with a custom implementation to
     * filter content or headers for output filtering <br>
     * 4. a) <strong>Either</strong> invoke the next entity in the chain using
     * the FilterChain object (<code>chain.doFilter()</code>), <br>
     * 4. b) <strong>or</strong> not pass on the request/response pair to the
     * next entity in the filter chain to block the request processing<br>
     * 5. Directly set headers on the response after invocation of the next
     * entity in the filter chain.
     *
     * @param request  The request to process
     * @param response The response associated with the request
     * @param chain    Provides access to the next filter in the chain for this
     *                 filter to pass the request and response to for further
     *                 processing
     * @throws IOException      if an I/O error occurs during this filter's
     *                          processing of the request
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        log.log(Level.SEVERE, "Filter in EFFECT");
        chain.doFilter(request,response);
    }

    /**
     * Make the FilterConfig of this filter available, if any.
     * Analogous to GenericServlet's {@code getServletConfig()}.
     * <p>Public to resemble the {@code getFilterConfig()} method
     * of the Servlet Filter version that shipped with WebLogic 6.1.
     *
     * @return the FilterConfig instance, or {@code null} if none available
     * @see GenericServlet#getServletConfig()
     */
    @Override
    public FilterConfig getFilterConfig() {
        return super.getFilterConfig();
    }
}
