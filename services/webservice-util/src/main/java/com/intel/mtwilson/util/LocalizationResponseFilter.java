/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.util;

import com.intel.mtwilson.i18n.Localizable;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import java.util.List;
import java.util.Locale;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class LocalizationResponseFilter implements ContainerResponseFilter {
    private static Logger log = LoggerFactory.getLogger(LocalizationResponseFilter.class);
    private String[] availableLocales = null;
    
    public void setAvailableLocales(String[] availableLocales) {
        this.availableLocales = availableLocales;
    }
    
    @Override
    public ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
        log.debug("LocalizationResponseFilter");
        List<Locale> locales = request.getAcceptableLanguages();
        log.debug("Client acceptable languages: {}", locales);
        Object entity = response.getResponse().getEntity();
        log.debug("Response entity is null? {}", entity == null);
        if( entity != null ) { log.debug("Response entity class: {}", entity.getClass().getName()); log.debug("Response entity localizable? {}", entity instanceof Localizable); }
        if( entity != null && entity instanceof Localizable && availableLocales != null ) {
            log.debug("Processing localizable response");
            log.debug("Available locales: {}", StringUtils.join(availableLocales, ", "));
            try {
                for(Locale locale : locales) {
                    log.debug("Checking requested locale: {}", locale.toString());
                    if( ArrayUtils.contains(availableLocales, locale.getLanguage()) ) {  // XXX does not handle languages whose codes have changed; you'd have to add entries for those in the configured available lanaguages list
                        log.debug("Requested locale is available: {}", locale.toString());
                        ((Localizable)entity).setLocale(locale);
                        break;
                    }
                }
            }
            catch(Exception e) {
                log.error("Failed to set locale on object of class: {}", entity.getClass().getName(), e);
            }
        }
        return response;
    }
    
}
