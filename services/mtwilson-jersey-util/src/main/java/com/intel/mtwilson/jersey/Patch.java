/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jersey;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.HashMap;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 *
 * @author jbuhacoff
 */
@JsonSerialize(include=JsonSerialize.Inclusion.NON_EMPTY) // jackson 1.9
@JsonInclude(JsonInclude.Include.NON_EMPTY) // jackson 2.0
public class Patch<T,L extends PatchLink<T>> {
    private T select;
    private T replace;
    private L link;
    private L unlink;
    private T insert;
    private T delete;
    private T test;

    public L getLink() {
        return link;
    }

    public L getUnlink() {
        return unlink;
    }

    public T getDelete() {
        return delete;
    }

    public T getInsert() {
        return insert;
    }

    public T getReplace() {
        return replace;
    }

    public T getSelect() {
        return select;
    }

    public T getTest() {
        return test;
    }

    public void setDelete(T delete) {
        this.delete = delete;
    }

    public void setInsert(T insert) {
        this.insert = insert;
    }

    public void setReplace(T replace) {
        this.replace = replace;
    }

    public void setSelect(T select) {
        this.select = select;
    }

    public void setTest(T test) {
        this.test = test;
    }

    public void link(String linkName, L target) {/*
        ArrayList<L> links = link.get(linkName);
        if( links == null ) {
            links = new ArrayList<L>();
            link.put(linkName, links);
        }
        links.add(target);*/
    }

    public void unlink(String linkName, L target) {/*
        ArrayList<L> unlinks = unlink.get(linkName);
        if( unlinks == null ) {
            unlinks = new ArrayList<L>();
            unlink.put(linkName, unlinks);
        }
        unlinks.add(target);*/
    }
    

    
}
