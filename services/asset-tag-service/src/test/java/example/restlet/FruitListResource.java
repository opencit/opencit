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
public class FruitListResource extends ServerResource {

    @Get
    public String existingFruit() {
        return "GET existing fruit";
    }

    @Delete
    public String deleteFruit() {
        return "DELETE fruit";
    }

    @Put("text/plain")
    public String updateFruit(String input) {
        return "PUT fruit: " + input;
    }
}
