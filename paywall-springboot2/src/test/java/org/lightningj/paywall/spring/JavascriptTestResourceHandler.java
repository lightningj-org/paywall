package org.lightningj.paywall.spring;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * TODO
 */
@Configuration
public class JavascriptTestResourceHandler implements WebMvcConfigurer {

    /**
     * TODO
     *
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        File jsDistDir = new File("../paywall-js/build/dist/");
        File testJsHTMLDir = new File("src/main/resources/static/");


        registry.addResourceHandler("/jstest/js/**")
                .addResourceLocations("file:" + jsDistDir.getAbsolutePath() + "/")
                .setCacheControl(CacheControl.maxAge(300, TimeUnit.MILLISECONDS).cachePublic());
        registry.addResourceHandler("/jstest/html/**").addResourceLocations("file:" + testJsHTMLDir.getAbsolutePath() + "/")
                .setCacheControl(CacheControl.maxAge(300, TimeUnit.MILLISECONDS).cachePublic());
    }
}
