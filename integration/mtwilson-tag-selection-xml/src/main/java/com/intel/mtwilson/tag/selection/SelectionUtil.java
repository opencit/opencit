/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tag.selection;

import com.intel.mtwilson.tag.selection.xml.DefaultType;
import com.intel.mtwilson.tag.selection.xml.SelectionType;
import com.intel.mtwilson.tag.selection.xml.SelectionsType;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 *
 * @author jbuhacoff
 */
public class SelectionUtil {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SelectionUtil.class);

    /**
     * Returns a new SelectionsType instance with only the selections and
     * default selections that are
     * valid today. The options section is copied to the new selections instance
     * for convenience.
     * The copy is a "shallow" copy - the copy refers to the same selections,
     * default selections, and options in the input.
     * To make a "deep" copy you can first serialize the selections, then
     * de-serialize into a new deep copy, and then call this method with the 
     * deep copy as input.
     */
    public static SelectionsType copySelectionsValidOn(SelectionsType selections, Date validOn) {
        GregorianCalendar today = new GregorianCalendar();
        today.setTime(validOn); // http://docs.oracle.com/javase/7/docs/api/java/util/GregorianCalendar.html
        SelectionsType copy = new SelectionsType();
        for (SelectionType selection : selections.getSelection()) {
            // skip if the selection is not currently valid (notBefore<today<notAfter) ; if notBefore or notAfter are not defined, then validity is assumed
            if (selection.getNotBefore() != null && today.before(selection.getNotBefore().toGregorianCalendar())) {
                log.debug("skipping selection because of notBefore date {}", selection.getNotBefore().toString());
                continue;
            }
            if (selection.getNotAfter() != null && today.after(selection.getNotAfter().toGregorianCalendar())) {
                log.debug("skipping selection because of notAfter date {}", selection.getNotAfter().toString());
                continue;
            }
            copy.getSelection().add(selection);
        }
        // repeat the same action for the default selections
        if( selections.getDefault() != null ) {
            copy.setDefault(new DefaultType());
            for (SelectionType selection : selections.getDefault().getSelection()) {
                // skip if the selection is not currently valid (notBefore<today<notAfter) ; if notBefore or notAfter are not defined, then validity is assumed
                if (selection.getNotBefore() != null && today.before(selection.getNotBefore().toGregorianCalendar())) {
                    log.debug("skipping default selection because of notBefore date {}", selection.getNotBefore().toString());
                    continue;
                }
                if (selection.getNotAfter() != null && today.after(selection.getNotAfter().toGregorianCalendar())) {
                    log.debug("skipping default selection because of notAfter date {}", selection.getNotAfter().toString());
                    continue;
                }
                copy.getDefault().getSelection().add(selection);
            }
        }
        // copy the options
        copy.setOptions(selections.getOptions());
        return copy;
    }
    
}
