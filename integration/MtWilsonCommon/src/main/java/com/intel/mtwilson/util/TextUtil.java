package com.intel.mtwilson.util;

import java.util.Collection;
import java.util.Iterator;

/**
 *
 * @author jbuhacoff
 */
public class TextUtil {
    public static String join(Collection<String> list) {
        if( list.isEmpty() ) { return ""; }
        Iterator<String> it = list.iterator();
        StringBuilder str = new StringBuilder(it.next());
        while(it.hasNext()) {
            str.append(", ").append(it.next());
        }
        return str.toString();
    }
}
