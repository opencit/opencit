/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.fs;

/**
 *
 * @author jbuhacoff
 */
public class FilesystemUtil {
    /**
     * must start with a letter, 
     * then it can have letters, digits, underscores, dots, and hyphens, but not two dots in a row, 
     * and must end with a letter or digit.
     * slashes, quotes, parenthesis, and other punctuation are not allowed since
     * feature ids are likely mapped to directory names and may be printed in debug
     * statements and we don't want to have to escape them everywhere
     */
    public static final String FEATURE_ID_REGEX = "(?:[a-zA-Z](?:\\.[a-zA-Z0-9]|[_-]+[a-zA-Z0-9]|[a-zA-Z0-9])*)";
    
}
