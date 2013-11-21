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
public class SeedResource extends ServerResource {

    @Get
    public String existingSeed() {
        String id = getAttribute("id"); //(String)getRequest().getAttributes().get("id");
        return "GET existing seed: "+id;
    }

    @Delete
    public String deleteSeed() {
        String id = getAttribute("id"); //(String)getRequest().getAttributes().get("id");
        return "DELETE seed: "+id;
    }

    @Put("text/plain")
    public String updateSeed(String input) {
        String id = getAttribute("id"); //(String)getRequest().getAttributes().get("id");
        return "PUT seed: "+id+": " + input;
    }
}
