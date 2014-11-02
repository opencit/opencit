/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.codec;

/**
 * This descendant of IllegalArgumentException is for classes that work with base64 strings and
 * expect their inputs to be valid base64 strings. In other words, any user input must
 * already be validated before passing it into such classes, and they cannot recover
 * from an invalid input because they are not operating at a level where they can 
 * request alternative data. In that situation, they throw this Base64FormatException.
 * 
 * Classes that handle user input directly should use the Base64 class in 
 * Apache Commons Codec
 * and handle DecoderExceptions directly, with a note that some of those methods
 * will automatically strip non-base64 characters when decoding and never throw
 * exceptions.
 * 
 *
 * @since 0.1
 * @author jbuhacoff
 */
public class Base64FormatException extends IllegalArgumentException {
    public Base64FormatException() {
        super();
    }
    public Base64FormatException(Throwable cause) {
        super(cause);
    }
    public Base64FormatException(String message, Throwable cause) {
        super(message, cause);
    }
    public Base64FormatException(String message) {
        super(message);
    }
    public Base64FormatException(String format, Object... args) {
        super(String.format(format, args));
    }
}
