/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package example.restlet;

import org.restlet.resource.Get;
import org.restlet.resource.Delete;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

/**
 * References: http://restlet.org/learn/guide/2.2/core/resource/
 *
 * @author jbuhacoff
 */
public class BoxListResource extends ServerResource {

    @Get
    public Box[] existingBox() {
        return new Box[] { new Box("A", new String[] { "item1", "item2" }) };
    }

    @Delete
    public String deleteBox() {
        return "DELETE box";
    }

    @Post("text/plain")
    public String addBox(Box input) {
        return "POST box: " + input.getLabel() +" with "+input.getItems().length+" items";
    }
}
