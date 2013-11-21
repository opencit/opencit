/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package example.restlet;

/**
 *
 * @author jbuhacoff
 */
public class Fruit {
    private String id;
    private String name;
    private String color;
    
    public Fruit() {}
    public Fruit(String id,String name, String color) { this.id = id; this.name = name; this.color = color; }
    public Fruit(String name, String color) { this.id = null; this.name = name; this.color = color; }
    
    public void setId(String id) { this.id = id; }
    public String getId() { return id; }
    
    public void setName(String name) { this.name = name; }
    public String getName() { return name; }
    
    public void setColor(String color) { this.color = color; }
    public String getColor() { return color; }
}
