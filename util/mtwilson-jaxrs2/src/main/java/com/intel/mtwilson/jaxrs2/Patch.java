/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jaxrs2;

import com.intel.mtwilson.repository.FilterCriteria;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
//import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 *
 * @author jbuhacoff
 */
@JsonSerialize(include=JsonSerialize.Inclusion.NON_EMPTY) // jackson 1.9
@JsonInclude(JsonInclude.Include.NON_EMPTY) // jackson 2.0
public class Patch<T,F extends FilterCriteria<T>,L extends PatchLink<T>> {
    private F select;
    private Map<String,Object> replace;
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

    public Map<String,Object> getReplace() {
        return replace;
    }

    public F getSelect() {
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

    public void setReplace(Map<String,Object> replace) {
        this.replace = replace;
    }

    public void setSelect(F select) {
        this.select = select;
    }

    public void setTest(T test) {
        this.test = test;
    }

    public void setLink(L link) {
        this.link = link;
    }

    public void setUnlink(L unlink) {
        this.unlink = unlink;
    }
    

    
}
