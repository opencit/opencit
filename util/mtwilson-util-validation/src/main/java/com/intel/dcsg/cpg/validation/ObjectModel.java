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
 * You can construct an ObjectModel with its subclass-specific constructors
 * and/or setter methods. The model will only be validated when you first call isValid()
 * or if you change it after that.  Repeated calls to isValid() without modifying
 * the object will immediately return the cached result.
 * 
 * @since 1.1
 * @author jbuhacoff
 */
public abstract class ObjectModel implements Model, Faults {
    private transient Integer lastHashCode = null;
    private transient ArrayList<Fault> faults = null;
    
    abstract protected void validate();
    
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
     * If the model has changed since the last call to isValid() then it will be revalidated.
     * We rely on the model's hashCode to detect changes so classes that extend ObjectModel
     * are responsible for implmenting a hashCode() function that reflects changes.
     * 
     * This method is marked @JsonIgnore because when a Model instance is serialized and later
     * deserialized, the application should not rely on the previous validity of the data
     * and should instead instantiate a new Model and call isValid() and getFaults() to check
     * the validity of the instance data. If an application needs to serialize these values,
     * like for reporting, wrap the Model instance in a ModelReport decorator, which 
     * allows isValid() and getFaults() to be serialized.
     * 
     * @return true if the model is valid
     */
	@org.codehaus.jackson.annotate.JsonIgnore
	@com.fasterxml.jackson.annotation.JsonIgnore
    @Override
    public final boolean isValid() {
        if( faults == null || lastHashCode == null || lastHashCode != hashCode() ) {
            faults = new ArrayList<>(); // make a new list instead of clearing so that a caller can say getFaults(), make a change, then getFaults() again, and compare the faults. 
            validate();
            lastHashCode = hashCode();
        }
        return faults.isEmpty();
    }
    
    /**
     * This method is marked @JsonIgnore because when a Model instance is serialized and later
     * deserialized, the application should not rely on the previous validity of the data
     * and should instead instantiate a new Model and call isValid() and getFaults() to check
     * the validity of the instance data. If an application needs to serialize these values,
     * like for reporting, wrap the Model instance in a ModelReport decorator, which 
     * allows isValid() and getFaults() to be serialized but is not a Model itself.
     * 
     * 
     * @return a list of faults 
     */
	@org.codehaus.jackson.annotate.JsonIgnore
	@com.fasterxml.jackson.annotation.JsonIgnore
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
