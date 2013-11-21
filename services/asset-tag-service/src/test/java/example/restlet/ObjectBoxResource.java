/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package example.restlet;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
//import javax.ws.rs.core.MediaType;
//import javax.ws.rs.core.Variant;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.restlet.data.Encoding;
import org.restlet.representation.ByteArrayRepresentation;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Get;
import org.restlet.resource.Delete;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * References: http://restlet.org/learn/guide/2.2/core/resource/
 * http://restlet.org/learn/javadocs/2.2/jse/api/
 * 
 * @author jbuhacoff
 */
public class ObjectBoxResource extends ServerResource {
    private final Logger log = LoggerFactory.getLogger(getClass());

    public ObjectBoxResource() {
//        getVariants().add(new InputRepresentation(this)); // can't do it with InputRepresentation
//        getVariants().add(Variant.encodings(MediaType.APPLICATION_OCTET_STREAM).build().get(0));
    }
    
    @Get
    public ObjectBox existingBox() {
        String id = getAttribute("id");
        return new ObjectBox(id, "B", new Fruit[] { new Fruit("pineapple", "yellow") });
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

    @Put("json:txt") //  application/json:text/plain  doesn't work,  try json:txt
    public String updateBox(ObjectBox input) {
        String id = getAttribute("id"); //(String)getRequest().getAttributes().get("id");
        return "PUT box application/json: "+id+": " + input.getLabel()+" with "+input.getItems().length+" items";
    }

//    @Put("bin:txt")  // bin is "binary file" which i guess is application/octet-stream ... the default is application/octet-stream anyway but can i write just :txt ?
//    public String updateBox(InputRepresentation input) throws IOException {
    //@Put("cust:txt") // when you fix the client to actually send application/octet-stream, this works ok but somehow also accepts text/plain
    @Put("bin:txt") // "bin" is "binary file" which is application/octet-stream,  and does NOT match on text/plain like 
    public String updateBox(InputStream input) throws IOException {
        List<Encoding> encodings = getRequest().getEntity().getEncodings();
        String encodinglist = StringUtils.join(encodings, ", ");
        String id = getAttribute("id");
        byte[] data = IOUtils.toByteArray(input);
//        byte[] data = IOUtils.toByteArray(input.getStream());
//        return "PUT box application/octet-stream: "+id+": "+data.length+" bytes: "+new String(data);
        return "PUT box application/octet-stream  id: "+id+"   "+data.length+" bytes: "+new String(data)+"  encodings: "+encodinglist+"   entity media type: "+getRequest().getEntity().getMediaType().getName();
    }
}
