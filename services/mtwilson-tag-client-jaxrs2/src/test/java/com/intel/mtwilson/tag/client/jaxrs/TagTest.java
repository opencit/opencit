/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.client.jaxrs;

import com.intel.mtwilson.My;
import com.intel.mtwilson.tag.model.KvAttribute;
import com.intel.mtwilson.tag.model.Selection;
import com.intel.mtwilson.tag.model.SelectionCollection;
import com.intel.mtwilson.tag.model.SelectionFilterCriteria;
import com.intel.mtwilson.tag.model.SelectionKvAttribute;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author ssbangal
 */
public class TagTest {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TagTest.class);

    private static Selections client = null;
    private static KvAttributes kvclient = null;
    private static SelectionKvAttributes skvclient = null;
    
    @BeforeClass
    public static void init() throws Exception {
        client = new Selections(My.configuration().getClientProperties());
        kvclient = new KvAttributes(My.configuration().getClientProperties());
        skvclient = new SelectionKvAttributes(My.configuration().getClientProperties());
    }
    
    @Test
    public void consolidatedTest() {
        
        Selection obj = new Selection();
        obj.setName("Test");
        obj.setDescription("Test Selection");                
        obj = client.createSelection(obj);
        
        obj.setDescription("Updated description");
        obj = client.editSelection(obj);
        
        KvAttribute kvAttrib = new KvAttribute();
        kvAttrib.setName("department");
        kvAttrib.setValue("finance");        
        kvAttrib = kvclient.createKvAttribute(kvAttrib);

        kvAttrib.setValue("HR");
        kvAttrib = kvclient.editKvAttribute(kvAttrib);        
        KvAttribute retrieveObj = kvclient.retrieveKvAttribute(kvAttrib.getId());
        
        SelectionKvAttribute skvAtt = new SelectionKvAttribute();
        skvAtt.setSelectionName("Test");
        skvAtt.setKvAttributeId(kvAttrib.getId());
        skvclient.createSelectionKvAttribute(skvAtt);
        
        SelectionFilterCriteria criteria = new SelectionFilterCriteria();
        criteria.nameEqualTo = "default";
        SelectionCollection objCollection = client.searchSelections(criteria);
        Selection sObj = null;
        for(Selection searchObj  : objCollection.getSelections()) {
            if (searchObj.getName().equalsIgnoreCase("default"))
                sObj = searchObj;            
        }

        if (sObj != null) {
            String sXml = client.retrieveSelectionAsXml(sObj.getId());
            log.debug("Selection as XML is {}", sXml);
            String sJson = client.retrieveSelectionAsJson(sObj.getId());
            log.debug("Selection as JSON is {}", sJson);
            String sEnc = client.retrieveSelectionAsEncryptedXml(sObj.getId());
            log.debug("Selection as Encrypted string is {}", sEnc);
        }
        
        client.deleteSelection(obj.getId());
    }
         
}
