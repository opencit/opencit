/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jaxrs2.server.resource;

import com.intel.mtwilson.jaxrs2.AbstractDocument;
import com.intel.mtwilson.repository.SearchableRepository;
import com.intel.mtwilson.jaxrs2.DocumentCollection;
import com.intel.mtwilson.repository.FilterCriteria;
import com.intel.mtwilson.repository.Locator;

/**
 * 
 * @author jbuhacoff
 */
//public interface SimpleRepository<T extends Document, C extends DocumentCollection<T>, F extends FilterCriteria<T>, L extends Locator<T>> {
public interface DocumentRepository<T extends AbstractDocument, C extends DocumentCollection<T>, F extends FilterCriteria<T>, L extends Locator<T>> extends SearchableRepository<T,L,C,F> {


}
