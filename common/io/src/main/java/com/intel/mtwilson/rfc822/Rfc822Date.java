/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.rfc822;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class Rfc822Date {
    private static final Logger log = LoggerFactory.getLogger(Rfc822Date.class);
    
    public static final SimpleDateFormat rfc822DateInputs[] = new SimpleDateFormat[] { new SimpleDateFormat("EEE, d MMM yy HH:mm:ss z"), new SimpleDateFormat("EEE, d MMM yy HH:mm z"), new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z"), new SimpleDateFormat("EEE, d MMM yyyy HH:mm z"), new SimpleDateFormat("d MMM yy HH:mm z"), new SimpleDateFormat("d MMM yy HH:mm:ss z"), new SimpleDateFormat("d MMM yyyy HH:mm z"), new SimpleDateFormat("d MMM yyyy HH:mm:ss z") };
    private static final SimpleDateFormat rfc822DateOutput = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
    private Date date;
    
    public Rfc822Date(String text) {
        date = parse(text);
    }
    
    public Rfc822Date(Date date) {
        this.date = date;
    }
    
    @Override
    public String toString() {
        return format(date);
    }
    
    public Date toDate() {
        return date;
    }
    
    public static String format(Date date) {
        return rfc822DateOutput.format(date);        
    }
    
    public static Date parse(String text) {
        for(SimpleDateFormat f : rfc822DateInputs) {
            try {
                Date date = f.parse(text);
                return date;
            }
            catch(ParseException e) {
                log.trace("Failed to parse date input {} using pattern {}", new String[] { text, f.toPattern() });  // ignore errors because we can try the next format
            }
        }
        throw new IllegalArgumentException("Date is not in RFC822 format: "+text);        
    }
}
