/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.io;

/**
 * Recommendations for implementing classes:
 * 
 * Use own class name as the return type.
 * Implement a method {@code protected void copyFrom(T source)} and
 * delegate the copying to it. This makes it easier
 * to implement copying in subclasses.
 * 
 * For example:
 * <pre>
 * public class Fruit implements Copyable {
 *   private String color;
 *   private Integer size;
 *   public Fruit copy() {
 *     Fruit newInstance = new Fruit();
 *     newInstance.copyFrom(this);
 *     return newInstance;
 *   }
 *   protected void copyFrom(Fruit source) {
 *     this.color = source.color;
 *     this.size = source.size;
 *   }
 * }
 * public class Apple extends Fruit implements Copyable {
 *   private Boolean crunchy;
 *   public Apple copy() {
 *     Apple newInstance = new Apple();
 *     newInstance.copyFrom(this);
 *     return newInstance;
 *   }
 *   protected void copyFrom(Apple source) {
 *     super.copy(source);
 *     this.crunchy = source.crunchy;
 *   }
 * }
 * </pre>
 * 
 * 
 * @author jbuhacoff
 */
public interface Copyable<T extends Copyable> {
    
    /**
     * @return a new instance of this class with a copy of all fields
     */
    T copy();
}
