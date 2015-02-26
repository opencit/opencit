/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.pipe;

/**
 * Generic interface for AllCapsNamingStrategy and PascalCaseNamingStrategy.
 * 
 * Transformations implementing this interface have the same type as input and
 * output so they can be chained.
 * 
 * Unlike the codec package the transformations are not necessarily reversible.
 * Unlike commons-collections Transformer interface this one uses generics
 * to transform objects of a specific type only. 
 * Whereas this transformer is among objects of the same type,
 * another (not yet written)
 * interface  TypeTransformer<In,Out> could transform 
 * between types, for example TypeTransformer<Object,Boolean> would be what
 * a generic predicate transformer would implement.
 * 
 * @author jbuhacoff
 */
public interface Transformer<T> {
    T transform(T input);
}
