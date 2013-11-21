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
public class FruitResource extends ServerResource {

    /*
    @Get
    public String existingFruit() {
        String id = (String)getRequest().getAttributes().get("id");
        return "GET existing fruit: "+id;
    }*/
    
    @Get
    public Fruit existingFruit() {
        String id = getAttribute("id");
        return new Fruit(id, "apple", "red");
    }

    @Delete
    public String deleteFruit() {
        String id = getAttribute("id"); //(String)getRequest().getAttributes().get("id");
        return "DELETE fruit: "+id;
    }

    @Put("txt")  // not text/plain
    public String updateFruit(String input) {
        String id = getAttribute("id"); //(String)getRequest().getAttributes().get("id");
        return "PUT fruit text/plain: "+id+": " + input;
    }

    @Put("json") // not application/json
    public String updateFruit(Fruit input) {
        String id = getAttribute("id"); //(String)getRequest().getAttributes().get("id");
        return "PUT fruit application/json: "+id+": " + input.getColor()+" "+input.getName();
    }

}
