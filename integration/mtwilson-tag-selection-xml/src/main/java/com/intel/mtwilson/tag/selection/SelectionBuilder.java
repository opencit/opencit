/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tag.selection;

import com.intel.mtwilson.tag.selection.xml.*;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * Example:
 * 
        SelectionsType selections = SelectionBuilder.factory()
        *       .cacheMode("off")
                .selection()
                .textAttributeKV("Country", "US")
                .textAttributeKV("State", "CA")
                .textAttributeKV("City", "Folsom")
                .textAttributeKV("City", "El Paso")
                .build();
 * 
 * @author jbuhacoff
 */
public class SelectionBuilder {
    public static SelectionBuilder factory() { return new SelectionBuilder(); }
    
    private OptionsType options = null;
    private SelectionsType selections = new SelectionsType();
    private DefaultType defaultSelections = new DefaultType();
    private SelectionType currentSelection = null;
    private boolean defaultSelection = false;
    
    public SelectionBuilder options() {
         if( options == null ) {
             options = new OptionsType();
         }
         return this;
    }
    
    /**
     * 
     * @param value case-sensitive; must be either "on" or "off"
     * @return 
     */
    public SelectionBuilder cacheMode(String value) {
        CacheModeAttribute cacheMode = CacheModeAttribute.fromValue(value);
        CacheType cache = new CacheType();
        cache.setMode(cacheMode);
        options();
        options.setCache(cache);
        return this;
    }
    
    private void closeSelection() {
        if( defaultSelection ) {
            defaultSelections.getSelection().add(currentSelection);
        }
        else {
            selections.getSelection().add(currentSelection);            
        }
    }
    
    public SelectionBuilder defaultSelection() {
        if( currentSelection != null ) {
            closeSelection();
        }
        currentSelection = new SelectionType();
        defaultSelection = true;
        return this;
    }
    
    public SelectionBuilder selection() {
        if( currentSelection != null ) {
            closeSelection();
        }
        currentSelection = new SelectionType();
        defaultSelection = false;
        return this;
    }
    
    /**
     * If you get NullPointerException make sure you have called selection()
     * before calling this method.
     * 
     * @param name
     * @return 
     */
    public SelectionBuilder name(String name) {
        currentSelection.setName(name);
        return this;
    }
    
    /**
     * If you get NullPointerException make sure you have called selection()
     * before calling this method.
     * 
     * @param id
     * @return 
     */
    public SelectionBuilder id(String id) {
        currentSelection.setId(id);
        return this;
    }
    
    /**
     * If you get NullPointerException make sure you have called selection()
     * before calling this method.
     * 
     * @param date
     * @return 
     */
    public SelectionBuilder notAfter(Date date) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        try {
            XMLGregorianCalendar xmlCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
            currentSelection.setNotAfter(xmlCalendar);
            return this;
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * If you get NullPointerException make sure you have called selection()
     * before calling this method.
     * 
     * @param date
     * @return 
     */
    public SelectionBuilder notBefore(Date date) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        try {
            XMLGregorianCalendar xmlCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
            currentSelection.setNotBefore(xmlCalendar);
            return this;
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * If you get NullPointerException make sure you have called selection()
     * before calling this method.
     * 
     * @param uuid
     * @return 
     */
    public SelectionBuilder subjectUuid(String uuid) {
        if( currentSelection == null ) {
            currentSelection = new SelectionType();
        }
        currentSelection.getSubject().clear(); // there can be only one 
        UuidSubjectType uuidSubject = new UuidSubjectType();
        uuidSubject.setValue(uuid);
        SubjectType subject = new SubjectType();
        subject.setUuid(uuidSubject);
        currentSelection.getSubject().add(subject);
        return this;
    }
    
    /**
     * If you get NullPointerException make sure you have called selection()
     * before calling this method.
     * 
     * @param ip
     * @return 
     */
    public SelectionBuilder subjectIp(String ip) {
        currentSelection.getSubject().clear(); // there can be only one 
        IpSubjectType ipSubject = new IpSubjectType();
        ipSubject.setValue(ip);
        SubjectType subject = new SubjectType();
        subject.setIp(ipSubject);
        currentSelection.getSubject().add(subject);
        return this;
    }
    
    /**
     * If you get NullPointerException make sure you have called selection()
     * before calling this method.
     * 
     * @param name
     * @return 
     */
    public SelectionBuilder subjectName(String name) {
        currentSelection.getSubject().clear(); // there can be only one 
        NameSubjectType nameSubject = new NameSubjectType();
        nameSubject.setValue(name);
        SubjectType subject = new SubjectType();
        subject.setName(nameSubject);
        currentSelection.getSubject().add(subject);
        return this;
    }
    
    /**
     * If you get NullPointerException make sure you have called selection()
     * before calling this method.
     * 
     * @param oid
     * @param text
     * @return 
     */
    public SelectionBuilder textAttribute(String oid, String text) {
        AttributeType attribute = new AttributeType();
        attribute.setOid(oid);
        TextAttributeType textAttribute = new TextAttributeType();
        textAttribute.setValue(text);
        attribute.setText(textAttribute);
        currentSelection.getAttribute().add(attribute);
        return this;
    }
    
    /**
     * If you get NullPointerException make sure you have called selection()
     * before calling this method.
     * 
     * @param oid
     * @param value
     * @return 
     */
    public SelectionBuilder derAttribute(String oid, byte[] value) {
        AttributeType attribute = new AttributeType();
        attribute.setOid(oid);
        DerAttributeType derAttribute = new DerAttributeType();
        derAttribute.setValue(value);
        attribute.setDer(derAttribute);
        currentSelection.getAttribute().add(attribute);
        return this;
    }
    /**
     * If you get NullPointerException make sure you have called selection()
     * before calling this method.
     * 
     * @param name
     * @param value
     * @return 
     */
    public SelectionBuilder textAttributeKV(String name, String value) {
        textAttribute("2.5.4.789.1", String.format("%s=%s", name, value));
        return this;
    }
    
    public SelectionsType build() {
        if( currentSelection != null ) {
            closeSelection();
            currentSelection = null;
        }
        if( defaultSelections != null ) {
            selections.setDefault(defaultSelections);
        }
        if( options != null ) {
            selections.setOptions(options);
        }
        return selections;
    }
}
