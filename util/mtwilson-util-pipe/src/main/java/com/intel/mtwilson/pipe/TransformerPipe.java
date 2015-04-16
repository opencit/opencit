/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.pipe;

import java.util.Arrays;
import java.util.List;

/**
 * A transformer that encapsulates one or more other transformers in sequence,
 * piping the result of one transformer to the next, and returns the final
 * result.
 * 
 * If the list is empty, the input is returned unmodified (identity transformation)
 * 
 * @author jbuhacoff
 */
public class TransformerPipe<T> implements Transformer<T> {
    private List<Transformer<T>> transformers;

    public TransformerPipe(List<Transformer<T>> transformers) {
        this.transformers = transformers;
    }
    
    public TransformerPipe(Transformer<T>... transformers) {
        this(Arrays.asList(transformers));
    }

    @Override
    public T transform(T input) {
        T result = input;
        for(Transformer<T> transformer : transformers) {
            result = transformer.transform(result);
        }
        return result;
    }

    public List<Transformer<T>> getTransformers() {
        return transformers;
    }
    
}
