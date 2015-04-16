/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.validation;

import com.intel.mtwilson.util.validation.faults.Invalid;
import com.intel.mtwilson.util.validation.faults.Thrown;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * This is a convenience base class for objects that implement the Model 
 * interface. It handles Fault collection and caching of validation results.
 * 
 * The difference between BuilderModel and ObjectModel is that classes extending
 * BuilderModel may add faults at any time, and isValid() will return
 * false until the build() method is called and then it returns true if there
 * are no errors or false if there are errors.  But getFaults() can be checked
 * to see the errors SO FAR at any point even before calling build() and isValid().
 * 
 * Classes extending BuilderModel are responsible for calling done() from within
 * their final build() or equivalent method.
 * 
 * Classes extending BuilderModel call any of the fault() methods "as they go"
 * instead of evaluating everything in a single validation method as in ObjectModel.
 * 
 * For builder objects that are reusable, a reset() method is provided to clear
 * the faults and the done flag.
 * 
 * @since 1.1
 * @author jbuhacoff
 */
public abstract class BuilderModel implements Model {
    private transient final ArrayList<Fault> faults = new ArrayList<>();
    private transient boolean isDone = false;
    
    /**
     * Call this when you are done building your object, ie. from your build() method.
     * The "done" flag indicates that the building phase has ended so any faults
     * have been recorded. 
     */
    protected final void done() { isDone = true; }
    
    /**
     * Provided so subclasses that are re-usable can clear the model state.
     */
    protected final void reset() { isDone = false; faults.clear(); }
    
    protected final void fault(Fault fault) {
        faults.add(fault);
    }

    protected final void fault(String description) {
        faults.add(new Fault(description));
    }
    
    protected final void fault(String format, Object... args) {
        faults.add(new Fault(format, args));
    }
    
    protected final void fault(Throwable e, String description) {
        faults.add(new Thrown(e, description));
    }
    
    protected final void fault(Throwable e, String format, Object... args) {
        faults.add(new Thrown(e, format, args));
    }

    protected final void fault(Model m, String format, Object... args) {
        faults.add(new Invalid(m, format, args));
    }
        
    /**
     * While the object is being built, always returns false. After the
     * object has been built, eg called the build() method, returns true if
     * there are no faults, and false if there are faults. 
     */
    @Override
    public final boolean isValid() {
        return isDone && faults.isEmpty();
    }
    
    /**
     * 
     * @return a list of faults 
     */
    @Override
    public final List<Fault> getFaults() {
        return faults;
    }
    
    /**
     * Default implementation for derived classes automatically generates the 
     * logic to compare non-static non-transient fields using reflection.
     * This is slower than writing a custom method but provides a nice default
     * behavior with no additional work by the derived class.
     * 
     * Derived classes that need to speed up the implementation can override
     * this method and use something like this:
     * MyDerivedClass rhs = (MyDerivedClass)other;
     * return new EqualsBuilder().append(myMemberVar, rhs.myMemberVar).isEquals();
     * 
     * See also: the lombok project can generate equals() and hashCode() via annotations http://projectlombok.org/features/EqualsAndHashCode.html
     * 
     * 
     * @param other
     * @return object comparison result conforming to the Java spec
     */
    @Override
    public boolean equals(Object other) {
        if( other == null ) { return false; }
        if( other == this ) { return true; }
        if( other.getClass() != this.getClass() ) { return false; } // this refers to the subclass, not the abstract base class
        return EqualsBuilder.reflectionEquals(this, other, false);
    }
    
    /**
     * Default implementation for derived classes automatically generates the
     * hash code using reflection to process non-static and non-transient fields.
     * This is slower than writing a custom method for each class to specify
     * the fields to be used in the calculation (can still use HashCodeBuilder)
     * but provides a nice default behavior with no additional work by the 
     * derived class. 
     * 
     * Derived classes that need to speed up the implementation
     * can override this method and use something like this:
     * return new HashCodeBuilder(17,37).append(myMemberVariable).toHashCode();
     * 
     * See also: the lombok project can generate equals() and hashCode() via annotations http://projectlombok.org/features/EqualsAndHashCode.html
     * 
     * @return a hash code conforming to the Java specification
     */
    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this, false)+getClass().hashCode(); // this & getClass() return the subclass, not the abstract base class
    }
    
}
