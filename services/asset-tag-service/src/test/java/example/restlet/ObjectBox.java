/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package example.restlet;

/**
 *
 * @author jbuhacoff
 */
public class ObjectBox {
    private String id;
    private String label;
    private Fruit[] items;
    
    public ObjectBox() {
        
    }
    public ObjectBox(String id, String label, Fruit[] items) { this.id = id; this.label = label; this.items = items; }
    public ObjectBox(String label, Fruit[] items) { this.id = null; this.label = label; this.items = items; }
    
    public void setId(String id) { this.id = id; }
    public String getId() { return id; }
    
    public void setLabel(String label) { this.label = label; }
    public String getLabel() { return label; }
    
    public void setItems(Fruit[] items) { this.items = items; }
    public Fruit[] getItems() { return items; }
}
