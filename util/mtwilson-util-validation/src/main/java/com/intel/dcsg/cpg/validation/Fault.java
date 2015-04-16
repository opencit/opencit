/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.validation;

//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
//import com.fasterxml.jackson.annotation.JsonInclude;
//import com.fasterxml.jackson.annotation.JsonTypeInfo;
//import com.fasterxml.jackson.annotation.JsonValue;
import com.intel.mtwilson.util.validation.faults.Thrown;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
//import org.apache.commons.lang3.StringUtils;

/**
 * This class strongly resembles an Exception object but it is used differently.
 * 
 * In this validation package, Faults are collected, not thrown. This allows
 * an application to fully validate potentially complex user input, collect
 * all the faults, and then provide
 * complete guidance on any errors that may exist in the input. 
 * 
 * Contrast this to Exceptions, that can only be thrown one at a time and
 * interrupt the flow of execution, forcing the user to correct and resubmit
 * one error at a time until the input is completely validated.
 * 
 * XXX TODO make the Fault class implement Localizable, allow a Message object in a
 * constructor like Fault(Message) and Fault(Throwable,, Message) and then pass the locale to the 
 * Message object when toString() or toString(Locale) is called.
 * 
 * @since 1.1
 * @author jbuhacoff
 */
//@JsonInclude(JsonInclude.Include.NON_EMPTY)
//@JsonIgnoreProperties(ignoreUnknown=true)
//@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, property="type")
public class Fault implements Faults {
    private final String description;
//    private final Throwable cause;
    private final List<Fault> more = new ArrayList<>();
    
    public Fault(String description) {
        if( description == null ) { throw new IllegalArgumentException("Cannot create a fault with null description"); }
//        this.cause = null;
        this.description = description;
//        this.more = new Fault[0];
    }
    
    public Fault(String format, Object... args) {
        if( format == null ) { throw new IllegalArgumentException("Cannot create a fault with null format"); }
//        this.cause = null;
        this.description = String.format(format, args);
//        this.more = new Fault[0];
    }
    
    public Fault(Throwable e, String description) {
        if( description == null ) { throw new IllegalArgumentException("Cannot create a fault with null description"); }
//        this.cause = e;
        this.description = description;
        fault(e);
    }
    
    public Fault(Throwable e, String format, Object... args) {
        if( format == null ) { throw new IllegalArgumentException("Cannot create a fault with null format"); }
//        this.cause = e;
        this.description = String.format(format, args);
//        this.more = new Fault[0];
        fault(e);
    }
    
    /**
     * Faults from the given collection are copied to as "more faults" for this one.
     * It is safe to clear or continue using the given collection.
     * @param related faults that may have caused this one
     * @param format
     * @param args 
     */
    public Fault(Collection<Fault> related, String format, Object... args) {
        if( format == null ) { throw new IllegalArgumentException("Cannot create a fault with null format"); }
//        this.cause = null;
        this.description = String.format(format, args);
        fault(related);
    }

    public Fault(Fault related, String format, Object... args) {
        if( format == null ) { throw new IllegalArgumentException("Cannot create a fault with null format"); }
//        this.cause = null;
        this.description = String.format(format, args);
        fault(related);
    }
    
    public Fault(Faults related, String format, Object... args) {
        if( format == null ) { throw new IllegalArgumentException("Cannot create a fault with null format"); }
//        this.cause = null;
        this.description = String.format(format, args);
        fault(related);
    }
    
    /**
     * 
     * @return 
     */
//    @JsonValue
    @Override
    public String toString() {
        return String.format("[%s: %s]", getClass().getName(), description); // should never be null because we set it in every constructor
        /*
        return StringUtils.join(new String[] {
            (description == null ? "" : description), // XXX TODO if we have a localizable Message object use that instead
            (cause == null ? "" : (cause.getMessage() == null ? "" : "["+cause.getMessage()+"]")),
            (more == null ? "" : (more.length == 0 ? "" : String.format("(%d more)", more.length)))
        }, " "); 
        */
    }
    
    protected final void fault(Throwable e) {
        more.add(new Thrown(e));
    }

    protected final void fault(Fault fault) {
        more.add(fault);
    }
    
    protected final void fault(Faults faults) {
        more.addAll(faults.getFaults());
    }

    protected final void fault(Collection<Fault> faults) {
        more.addAll(faults);
    }

    public String getDescription() {
        return description;
    }
    
    /**
     * This method returns an array of faults related to this one. Typically
     * these are faults that resulted in the failure described by this fault.
     * 
     * @return a list of related Fault objects; may be empty if there aren't any other associated faults
     */
    @Override
    public List<Fault> getFaults() {
        return more;
    }
    
}
