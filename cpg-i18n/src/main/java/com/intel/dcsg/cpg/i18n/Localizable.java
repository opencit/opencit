/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.i18n;

import java.util.Locale;

/**
 * The Jersey Localizable interface defines the following methods:
 * Object[] getArguments()
 * String getKey()
 * String getResourceBundleName()
 * Which requires localizable objects to keep track of the bundle and key that is localizable. It is then the
 * responsibility of the localizing class (like a response filter) to load the resource and look up the key and
 * apply the arguments.
 * In contrast, this Localizable interface defines the following method:
 * void setLocale(Locale locale)
 * Which requires localizable objects to maintain enough information to localize themselves (either the above 
 * information or the string template and arguments only) and it is the responsibility of the localizable class
 * to load the resource and look up the key and apply the arguments. This allows the localizable class to delegate
 * that activity to any other class which provides a lot of flexibility. For example, a localizable object might
 * have several fields that need to be localized, each with a different key. That would not be possible using the
 * Jersey localizable interface because it assumes each object only has one key (and it really feels like it assumes
 * that each localizable object is just a wrapped string).
 * @author jbuhacoff
 */
public interface Localizable {
    void setLocale(Locale locale);
}
