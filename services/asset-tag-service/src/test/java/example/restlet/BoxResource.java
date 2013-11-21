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
public class BoxResource extends ServerResource {

    @Get
    public Box existingBox() {
        String id = getAttribute("id");
        return new Box(id, "B", new String[] { "item3", "item4" });
    }

    @Delete
    public String deleteBox() {
        String id = getAttribute("id"); //(String)getRequest().getAttributes().get("id");
        return "DELETE box: "+id;
    }

    /*
    @Put("application/xml")
    public String updateBox(String input) {
        String id = getAttribute("id"); //(String)getRequest().getAttributes().get("id");
        return "PUT box text/plain: "+id+": " + input;
    }*/

    @Put("application/json")
    public String updateBox(Box input) {
        String id = getAttribute("id"); //(String)getRequest().getAttributes().get("id");
        return "PUT box application/json: "+id+": " + input.getLabel()+" with "+input.getItems().length+" items";
    }

}
