/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package example.restlet;

/**
 *
 * @author jbuhacoff
 */
public class Box {
    private String id;
    private String label;
    private String[] items;
    
    public Box() { }
    public Box(String id, String label, String[] items) { this.id = id; this.label = label; this.items = items; }
    public Box(String label, String[] items) { this.id = null; this.label = label; this.items = items; }
    
    public void setId(String id) { this.id = id; }
    public String getId() { return id; }
    
    public void setLabel(String label) { this.label = label; }
    public String getLabel() { return label; }
    
    public void setItems(String[] items) { this.items = items; }
    public String[] getItems() { return items; }
}
