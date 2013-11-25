/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.xml;

import com.intel.mtwilson.api.ApiException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import org.junit.Test;
import com.intel.mtwilson.atag.xml.attrselect.*;
import org.apache.commons.io.IOUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * 
 *
 * @author jbuhacoff
 */
public class ReadXmlTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ReadXmlTest.class);

    // probably should be moved to a new utility module cpg-xml or cpg-jaxb 
    
    // this pair is used when root element has @XmlRootEelment annotation when building
    private <T> T fromRootXML(InputStream in, Class<T> valueType) throws IOException, JAXBException {
        String document = IOUtils.toString(in, "UTF-8");
        return fromRootXML(document, valueType);
    }
    private <T> T fromRootXML(String document, Class<T> valueType) throws IOException, JAXBException {
        JAXBContext jc = JAXBContext.newInstance( valueType );
        Unmarshaller u = jc.createUnmarshaller();
        Object o = u.unmarshal( new StreamSource( new StringReader( document ) ) ); // commented otu due to "Expected elements are (none)" error since xjc does not annotate root element <selections> with a tag and maybe timestamp.
        return (T)o;
    }
    
    // probably should be moved to a new utility module cpg-xml or cpg-jaxb 
    
    // this pair is used when root element does NOT have @XmlRootEelment annotation when building
    private <T> T fromXML(InputStream in, Class<T> valueType) throws IOException, JAXBException {
        String document = IOUtils.toString(in, "UTF-8");
        return fromXML(document, valueType);
    }
    private <T> T fromXML(String document, Class<T> valueType) throws IOException, JAXBException {
        JAXBContext jc = JAXBContext.newInstance( valueType );
        Unmarshaller u = jc.createUnmarshaller();
        JAXBElement<T> e = u.unmarshal( new StreamSource( new StringReader( document ) ), valueType);
        return e.getValue();
    }
    
    /**
     * Sample output for selection 1:
     * 
{"selection":[{"attribute":[{"value":"Country=US","oid":"2.5.4.789.1"},{"value":"State=CA","oid":"2.5.4.789.1"},{"value":"State=TX","oid":"2.5.4.789.1"},{"value":"City=Folsom","oid":"2.5.4.789.1"},{"value":"City=El Paso","oid":"2.5.4.789.1"}],"id":null}]}
     * 
     * Sample output for selection 2:
     * 
{"selection":[{"attribute":[],"id":"0b52784b-4588-4c73-900d-a1bac622dde1"}]}
     * 
     * Sample output for selection 3:
     * 
{"selection":[{"attribute":[{"value":"US","oid":"2.5.4.6"},{"value":"CA","oid":"2.5.4.8"},{"value":"TX","oid":"2.5.4.8"},{"value":"Folsom","oid":"2.5.4.7"},{"value":"El Paso","oid":"2.5.4.7"}],"id":null}]}
     * 
     * Sample output for Selection 4:
     * 
{"selection":[{"attribute":[{"value":"Country=US","oid":null},{"value":"State=CA","oid":null},{"value":"State=TX","oid":null},{"value":"City=Folsom","oid":null},{"value":"City=El Paso","oid":null}],"id":null}]}
     * 
     * Sample output for selection 5:
     * 
{"selection":[{"attribute":[{"value":"Country=US","oid":"2.5.4.789.1"},{"value":"State=CA","oid":"2.5.4.789.1"},{"value":"City=Folsom","oid":"2.5.4.789.1"}],"id":null},{"attribute":[{"value":"Country=US","oid":"2.5.4.789.1"},{"value":"State=TX","oid":"2.5.4.789.1"},{"value":"City=El Paso","oid":"2.5.4.789.1"}],"id":null}]}
     * 
     * Sample output for selection 6:
     * 
{"selection":[{"attribute":[],"id":"0b52784b-4588-4c73-900d-a1bac622dde1","name":null},{"attribute":[],"id":"bbbe1c68-4792-454c-9295-1a1234e1aa9f","name":null}]}
     * 
     * Sample output for selection 7:
     * 
{"selection":[{"attribute":[{"value":"Country=US","oid":"2.5.4.789.1"},{"value":"State=CA","oid":"2.5.4.789.1"},{"value":"City=Folsom","oid":"2.5.4.789.1"}],"id":"8ed9140b-e6a1-41b2-a8d4-258948633153","name":"California"},{"attribute":[{"value":"Country=US","oid":"2.5.4.789.1"},{"value":"State=TX","oid":"2.5.4.789.1"},{"value":"City=El Paso","oid":"2.5.4.789.1"}],"id":"24e7d7be-f337-47f7-a1af-9a4dbdaeb69d","name":null},{"attribute":[{"value":"Country=CA","oid":"2.5.4.789.1"},{"value":"Province=Quebec","oid":"2.5.4.789.1"},{"value":"City=Quebec City","oid":"2.5.4.789.1"}],"id":null,"name":"Canada"}]}
     * 
     * 
     * @throws Exception
     */    
    @Test
    public void testReadSelection1() throws Exception {
        InputStream in = getClass().getResourceAsStream("/samplexml/selection6.xml");
        SelectionsType selections = fromXML(in, SelectionsType.class);
        in.close();
        ObjectMapper mapper = new ObjectMapper();
        log.debug("selection1: {}", mapper.writeValueAsString(selections));
    }
    
    public class TagSelection {
        public List<tag> tagList;
        public String    name;
        public String    id;
    }
    
    public class tag {
        private String name;
        private String value;
        private String oid;
        
        tag(String name, String value, String oid){
            this.name =name;
            this.value=value;
            this.oid=oid;                
        }
        
        String getName() { return this.name;}
        String getValue() {return this.value;}
        String getOid() { return this.oid;}
    };
    
    public TagSelection getTagSelectionFromXml(String xml) throws ParserConfigurationException, SAXException, IOException {
        TagSelection ret = new TagSelection();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml));
        Document doc = builder.parse(is);
        ArrayList<tag> tagList = new ArrayList<tag>();
        int cnt=0;
        NodeList nodeList = doc.getElementsByTagName("attribute");
        for (int s = 0; s < nodeList.getLength(); s++) {
            Node fstNode = nodeList.item(s);
            if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
                Element fstElmnt = (Element) fstNode;
                String idValue = fstElmnt.getAttribute("oid");                
                Element lstNmElmnt = (Element) nodeList.item(cnt++);
                NodeList lstNm = lstNmElmnt.getChildNodes();
                String currentAction = ((Node) lstNm.item(0)).getNodeValue();
                if (currentAction != null) {
                    tagList.add(new tag("",idValue,currentAction));
                }

            }
        }
       
        nodeList = doc.getElementsByTagName("selection");
        Node fstNode = nodeList.item(0);
        Element e = (Element) fstNode;
        ret.id = e.getAttribute("id"); 
        ret.name= e.getAttribute("name"); 
        ret.tagList = tagList;
        
        return ret;
    }
    
    @Test
    public void testParseXmlSelection() throws ParserConfigurationException, SAXException, IOException {
        String xml = "<selections xmlns=\"urn:intel-mtwilson-asset-tag-attribute-selections\">\n"
                + "<selection id=\"1\" name=\"default\">\n"
                + "<attribute oid=\"1.3.6.1.4.1.99999.1\">US</attribute>\n"
                + "<attribute oid=\"1.3.6.1.4.1.99999.2\">CA</attribute>\n"
                + "<attribute oid=\"1.3.6.1.4.1.99999.3\">Folsom</attribute>\n"
                + "<attribute oid=\"1.3.6.1.4.1.99999.3\">Santa Clara</attribute>\n"
                + "</selection>\n"
                + "</selections>";
       
        TagSelection selection = getTagSelectionFromXml(xml);
        System.out.println("got selection with name "+ selection.name + " and id of " + selection.id);
    }
    
}
