package org.lightningj.paywall.paywalltademo;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * Special ResourceHandler handler reading files direcly from disk instead
 * of cached static content.
 */
@Configuration
public class JavascriptTestResourceHandler implements WebMvcConfigurer {



    /**
     * Method that adds resource handlers for reloading of directories
     * ../paywall-js/build/dist/ and src/main/resources/static/.
     *
     * @param registry the resource handler registry to add to.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        File jsDistDir = new File("paywall-js/build/dist/");
        registry.addResourceHandler("/js/**")
                .addResourceLocations("file:" + jsDistDir.getAbsolutePath() + "/");
        File testJsHTMLDir = new File("paywall-ta-demo/src/main/resources/static/");
        registry.addResourceHandler("/*.html")
                .addResourceLocations("file:" + testJsHTMLDir.getAbsolutePath() + "/")
                .setCacheControl(CacheControl.maxAge(300, TimeUnit.MILLISECONDS).cachePublic());
    }
}
