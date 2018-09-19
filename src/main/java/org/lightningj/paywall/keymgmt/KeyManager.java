/************************************************************************
 *                                                                       *
 *  LightningJ                                                           *
 *                                                                       *
 *  This software is free software; you can redistribute it and/or       *
 *  modify it under the terms of the GNU General Public License          *
 *  License as published by the Free Software Foundation; either         *
 *  version 3 of the License, or any later version.                      *
 *                                                                       *
 *  See terms of license at gnu.org.                                     *
 *                                                                       *
 *************************************************************************/

package org.lightningj.paywall.keymgmt;

/**
 * Base interface for a KeyManager in charge of maintaining symmetric and asymmetric keys.
 *
 * Created by Philip Vendil on 2018-09-14.
 */
public interface KeyManager {

    Context CONTEXT_PAYMENT_TOKEN = new Context("PAYMENT_TOKEN");
    Context CONTEXT_INVOICE_TOKEN = new Context("INVOICE_TOKEN");
    Context CONTEXT_SETTLEMENT_TOKEN = new Context("SETTLEMENT_TOKEN");

    /**
     * Extendable class specifying context of crytographic operations.
     */
    class Context{

        private String contextType;

        /**
         * Default constructor.
         *
         * @param contextType type of context
         */
        Context(String contextType){
            this.contextType = contextType;
        }

        /**
         *
         * @return type of context
         */
        public String getContextType(){
            return contextType;
        }

        @Override
        public String toString() {
            return "Context{" +
                    "contextType='" + contextType + '\'' +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Context context = (Context) o;

            return contextType != null ? contextType.equals(context.contextType) : context.contextType == null;
        }

        @Override
        public int hashCode() {
            return contextType != null ? contextType.hashCode() : 0;
        }
    }


    /**
     * Method to return the Security Provider to use in given context.
     * @param context the related context
     * @return the provider to use.
     */
    String getProvider(Context context);

}
