/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package example.restlet;

import org.restlet.resource.Get;
import org.restlet.resource.Delete;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;

/**
 * References: http://restlet.org/learn/guide/2.2/core/resource/
 *
 * @author jbuhacoff
 */
public class SeedListResource extends ServerResource {

    @Get
    public String existingSeed() {
        return "GET existing seed";
    }

    @Delete
    public String deleteSeed() {
        return "DELETE seed";
    }

    @Put("text/plain")
    public String updateSeed(String input) {
        return "PUT seed: " + input;
    }
}
