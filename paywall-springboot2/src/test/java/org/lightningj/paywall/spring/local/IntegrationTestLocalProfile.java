package org.lightningj.paywall.spring.local;

import org.lightningj.paywall.keymgmt.DummyKeyManager;
import org.lightningj.paywall.keymgmt.KeyManager;
import org.lightningj.paywall.lightninghandler.LightningHandler;
import org.lightningj.paywall.spring.MockedLightningHandler;
import org.lightningj.paywall.spring.SpringDefaultFileKeyManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"integration_paywall_local"})
public class IntegrationTestLocalProfile extends BaseLocalProfileBeanConfiguration{

    @Bean("lightningHandler")
    public LightningHandler getLightningHandler(){
        return new MockedLightningHandler();
    }

    @Bean("keyManager")
    public KeyManager getKeyManager(){
        return new DummyKeyManager();
    }
}
