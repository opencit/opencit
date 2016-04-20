package test.xml;

/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */


import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import org.junit.Test;
import com.intel.mtwilson.feature.xml.*;
import org.apache.commons.io.IOUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;


/**
 * 
 *
 * @author jbuhacoff
 */
public class ReadFeatureXmlTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ReadFeatureXmlTest.class);

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
     * Sample output for feature 1:
     * 
{"id":"feature1","version":"0.1","name":"Feature #1","provider":{"name":"Intel","url":"http://www.intel.com"},"description":"An example feature","license":{"copyright":"2014 Intel Corporation. All rights reserved.","url":"file:///LICENSE.TXT"},"includes":null,"requires":null,"conflicts":null,"links":null}
     * 
     * Sample output for feature 2:
     * 
{"id":"feature2","version":"0.1","name":"Feature #2","provider":{"name":"Intel","url":"http://www.intel.com"},"description":"A second example feature which depends on the first feature","license":{"copyright":"2014 Intel Corporation. All rights reserved.","url":"file:///LICENSE.TXT"},"includes":null,"requires":{"feature_ref":{"id":"feature1","version":null}},"conflicts":null,"links":null}
     * 
     * Sample output for feature 3:
     * 
{"id":"feature3","version":"0.1","name":"Feature #3","provider":{"name":"Intel","url":"http://www.intel.com"},"description":"A third example feature which depends on the first feature and conflicts with the second feature","license":{"copyright":"2014 Intel Corporation. All rights reserved.","url":"file:///LICENSE.TXT"},"includes":null,"requires":{"feature_ref":{"id":"feature1","version":null}},"conflicts":{"feature_ref":{"id":"feature2","version":null}},"links":null}
     * 
     * Sample output for feature 4:
     * 
{"id":"feature4","version":"0.1","name":"Feature #4","provider":{"name":"Intel","url":"http://www.intel.com"},"description":"An example feature","license":{"copyright":"2014 Intel Corporation. All rights reserved.","url":"file:///LICENSE.TXT"},"includes":{"components":{"component":{"id":"componentA","version":"0.76","name":"Component A","provider":{"name":"Intel","url":"http://www.intel.com"},"description":"An important sub-component","license":{"copyright":"2014 Intel Corporation. All rights reserved.","url":"file:///LICENSE.TXT"},"includes":null,"requires":null,"conflicts":null,"links":null}}},"requires":null,"conflicts":null,"links":{"link":{"value":"mailto:feature4-users@provider.com","rel":"mailing list","type":null}}}
{"id":"feature4","version":"0.1","name":"Feature #4","provider":{"name":"Intel","url":"http://www.intel.com"},"description":"An example feature","license":{"copyright":"2014 Intel Corporation. All rights reserved.","url":"file:///LICENSE.TXT"},"includes":{"components":{"component":{"id":"componentA","version":"0.76","name":"Component A","provider":{"name":"Intel","url":"http://www.intel.com"},"description":"An important sub-component","license":{"copyright":"2014 Intel Corporation. All rights reserved.","url":"file:///LICENSE.TXT"},"includes":null,"requires":null,"conflicts":null,"links":null}}},"requires":null,"conflicts":null,"links":{"link":{"href":"mailto:feature4-users@provider.com","rel":"mailing list","type":null}}}
     * 
     * @throws Exception
     */    
    @Test
    public void testReadFeatureType() throws Exception {
        InputStream in = getClass().getResourceAsStream("/feature-xml-examples/feature4.xml");
        FeatureType feature = fromXML(in, FeatureType.class);
        in.close();
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(new PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy());
        log.debug("feature1: {}", mapper.writeValueAsString(feature));
    }
    /*
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
    public void testParseFeature() throws ParserConfigurationException, SAXException, IOException {
        String xml = "<selections xmlns=\"urn:mtwilson-tag-selection\">\n"
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
    */
    
}
