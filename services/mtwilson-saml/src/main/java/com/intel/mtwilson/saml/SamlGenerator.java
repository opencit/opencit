/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.saml;

import com.intel.dcsg.cpg.configuration.CommonsConfiguration;
import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.mtwilson.tag.model.x509.*;
import com.intel.mtwilson.datatypes.TxtHost;
import com.intel.dcsg.cpg.io.Resource;
import com.intel.mtwilson.tag.model.X509AttributeCertificate;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.*;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.XMLSignatureException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLObjectBuilder;
import org.opensaml.common.SAMLVersion;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.AttributeValue;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.Subject;
import org.opensaml.saml2.core.SubjectConfirmation;
import org.opensaml.saml2.core.SubjectConfirmationData;
import org.opensaml.saml2.core.impl.AssertionMarshaller;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObjectBuilder;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.schema.XSAny;
import org.opensaml.xml.schema.XSBase64Binary;
import org.opensaml.xml.util.XMLHelper;
import org.w3c.dom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * When we respond with an assertion, if we want to prevent caching we should include these headers:
 * Cache-Control: no-cache, no-store, must-revalidate, private
 * Pragma: no-cache
 * But there is no harm in the client caching the attestation results for as long as THEY feel comfortable with it.
 * 
 * @author jbuhacoff
 */
public class SamlGenerator {
    private Logger log = LoggerFactory.getLogger(getClass());
    private static XMLObjectBuilderFactory builderFactory;
    private String issuerName; // for example, http://1.2.3.4/AttestationService
    private String issuerServiceName; // for example "AttestationService"
    private Integer validitySeconds; // for example 3600 for one hour
    private SAMLSignature signatureGenerator = null;
    private SamlAssertion samlAssertion = null;
//    private Resource keystoreResource = null;
    
    /**
     * Compatibility constructor 
     * @param keystoreResource  ignored
     * @param configuration  commons-configuration object 
     * @throws ConfigurationException 
     */
    public SamlGenerator(Resource keystoreResource, org.apache.commons.configuration.Configuration configuration) throws ConfigurationException {
        this(new CommonsConfiguration(configuration));
    }
    
    /**
     * Configuration keys:
     * saml.issuer=http://1.2.3.4/AttestationService          # used in SAML
     * saml.validity.seconds=3600 # 3600 seconds = 1 hour
     * saml.keystore.file=C:/Intel/CloudSecurity/SAML.jks         # path for keystore file (absolute or relative to the intel/cloudsecurity folder)
     * saml.keystore.password=password         # password for keystore file
     * saml.key.alias=forSigning           # alias of the signing key in the keystore file
     * saml.key.password=password           # password of the signing key
     * jsr105Provider=org.jcp.xml.dsig.internal.dom.XMLDSigRI # SAML XML signature provider
     * keystore.path=.            # disk path to SAML keystore (currently ignored)
     * 
     * 
     * @param configuration with the keys described
     * @throws ConfigurationException 
     */
    public SamlGenerator(com.intel.dcsg.cpg.configuration.Configuration configuration /*Resource keystoreResource, org.apache.commons.configuration.Configuration configuration*/) throws ConfigurationException {
        builderFactory = getSAMLBuilder();
        try {
            signatureGenerator = new SAMLSignature(/*keystoreResource,*/ configuration);
        } catch (ClassNotFoundException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableEntryException | IllegalAccessException | InstantiationException | IOException | CertificateException ex) {
            log.error("Cannot load SAML signature generator: "+ex.getMessage(), ex);
        }
        setIssuer(configuration.get("saml.issuer", "AttestationService"));
        setValiditySeconds(Integer.valueOf(configuration.get("saml.validity.seconds", "3600")));
    }
    
    
    public final void setIssuer(String issuer) {
        this.issuerName = issuer;
        this.issuerServiceName = "AttestationService-0.5.4"; 
    }
    
    /**
     * 
     * @param seconds number of seconds or null to not set any expiration
     */
    public final void setValiditySeconds(Integer seconds) {
        this.validitySeconds = seconds;
    }
    
    /*
    public void setKeystoreResource(Resource keystoreResource) {
        this.keystoreResource = keystoreResource;
    }*/
    
    /**
     * Input is a Host record with all the attributes to assert
     * Output is XML containing the SAML assertions
     * 
     * From /hosts/trust we get BIOS:1,VMM:1
     * From /hosts/location we get location
     * From /pollhosts we get trust level "unknown/untrusted/trusted" and timestamp
     * From /hosts/reports/trust we get host name, mle info string, created on, overall trust status, and verified on
     * From /hosts/reports/manifest we get PCR values, trust status, and verified on for each PCR
     * 
     * @return @SamlAssertion
     * @throws MarshallingException 
     */
    public SamlAssertion generateHostAssertion(TxtHost host, X509AttributeCertificate tagCertificate, Map<String, String> vmMetaData) throws MarshallingException, ConfigurationException, UnknownHostException, GeneralSecurityException, XMLSignatureException, MarshalException {
        samlAssertion = new SamlAssertion();
        Assertion assertion = createAssertion(host, tagCertificate, vmMetaData);

        AssertionMarshaller marshaller = new AssertionMarshaller();
        Element plaintextElement = marshaller.marshall(assertion);
        
        String originalAssertionString = XMLHelper.nodeToString(plaintextElement);
        System.out.println("Assertion String: " + originalAssertionString);

        // add signatures and/or encryption
        signAssertion(plaintextElement);
        
        samlAssertion.assertion =  XMLHelper.nodeToString(plaintextElement);
        System.out.println("Signed Assertion String: " + samlAssertion.assertion );
        return samlAssertion;
    }
    
    /**
     * Generates a multi-host SAML assertion which contains an AttributeStatement
     * for each host containing a Host_Address attribute with the host IP address
     * or hostname and the trust attributes as for a single-host assertion.
     * The Subject of the multi-host SAML assertion should not be used because
     * it is simply the collection hosts in the assertion and no statements
     * are made about the collection as a whole.
     * 
     * @param hosts
     * @return
     * @throws SamlException 
     */
    public SamlAssertion generateHostAssertions(Collection<TxtHostWithAssetTag> hosts) throws SamlException {
        try {
            samlAssertion = new SamlAssertion();
            Assertion assertion = createAssertion(hosts);

            AssertionMarshaller marshaller = new AssertionMarshaller();
            Element plaintextElement = marshaller.marshall(assertion);

            String originalAssertionString = XMLHelper.nodeToString(plaintextElement);
            System.out.println("Assertion String: " + originalAssertionString);

            // add signatures and/or encryption
            signAssertion(plaintextElement);

            samlAssertion.assertion =  XMLHelper.nodeToString(plaintextElement);
            System.out.println("Signed Assertion String: " + samlAssertion.assertion );
            
            return samlAssertion;
        }
        catch(Exception e) {
            throw new SamlException(e);
        }
    }
 
	public static XMLObjectBuilderFactory getSAMLBuilder() throws ConfigurationException{
 
		if(builderFactory == null){
			// OpenSAML 2.3
			 DefaultBootstrap.bootstrap();
	         builderFactory = Configuration.getBuilderFactory();
		}
 
		return builderFactory;
	}
 
 
        // create the issuer
        
        private Issuer createIssuer() {
            // Create Issuer
            SAMLObjectBuilder issuerBuilder = (SAMLObjectBuilder)  builderFactory.getBuilder(Issuer.DEFAULT_ELEMENT_NAME);
            Issuer issuer = (Issuer) issuerBuilder.buildObject();
            issuer.setValue(this.issuerName);
            return issuer;
        }
        // create the Subject Name
        
        private NameID createNameID(String hostName) {
            // Create the NameIdentifier
            SAMLObjectBuilder nameIdBuilder = (SAMLObjectBuilder) builderFactory.getBuilder(NameID.DEFAULT_ELEMENT_NAME);
            NameID nameId = (NameID) nameIdBuilder.buildObject();
            nameId.setValue(hostName);
//            nameId.setNameQualifier(input.getStrNameQualifier()); optional:  
            nameId.setFormat(NameID.UNSPECIFIED); // !!! CAN ALSO USE X509 SUBJECT FROM HOST CERTIFICATE instead of host name in database   
            return nameId;
        }
        private NameID createNameID(TxtHost host) {
            return createNameID(host.getHostName().toString());
        }

        
        // create the Subject and Subject Confirmation
        
        private SubjectConfirmation createSubjectConfirmation(TxtHost host) throws ConfigurationException, UnknownHostException {
            SAMLObjectBuilder subjectConfirmationBuilder = (SAMLObjectBuilder)  builderFactory.getBuilder(SubjectConfirmation.DEFAULT_ELEMENT_NAME);
            SubjectConfirmation subjectConfirmation = (SubjectConfirmation) subjectConfirmationBuilder.buildObject();
            subjectConfirmation.setMethod(SubjectConfirmation.METHOD_SENDER_VOUCHES); 
            subjectConfirmation.setSubjectConfirmationData(createSubjectConfirmationData());
            // Create the NameIdentifier
            SAMLObjectBuilder nameIdBuilder = (SAMLObjectBuilder) builderFactory.getBuilder(NameID.DEFAULT_ELEMENT_NAME);
            NameID nameId = (NameID) nameIdBuilder.buildObject();
            nameId.setValue(issuerServiceName);
//            nameId.setNameQualifier(input.getStrNameQualifier()); optional:  
            nameId.setFormat(NameID.UNSPECIFIED); // !!! CAN ALSO USE X509 SUBJECT FROM HOST CERTIFICATE instead of host name in database   
            subjectConfirmation.setNameID(nameId);
            return subjectConfirmation;
        }
        
        /**
         * 
         * The SubjectConfirmationData element may be extended with custom information that we want to include, both as attributes or as child elements.
         * 
         * See also section 2.4.1.2 Element <SubjectConfirmationData> of http://docs.oasis-open.org/security/saml/v2.0/saml-core-2.0-os.pdf
         * 
         * @param host
         * @return
         * @throws ConfigurationException
         * @throws UnknownHostException 
         */
        private SubjectConfirmationData createSubjectConfirmationData() throws ConfigurationException, UnknownHostException {
            SAMLObjectBuilder confirmationMethodBuilder = (SAMLObjectBuilder)  builderFactory.getBuilder(SubjectConfirmationData.DEFAULT_ELEMENT_NAME);
            SubjectConfirmationData confirmationMethod = (SubjectConfirmationData) confirmationMethodBuilder.buildObject();
            DateTime now = new DateTime();
            // Required to add to cache
            samlAssertion.created_ts = now.toDate();
            
            confirmationMethod.setNotBefore(now); 
            if( validitySeconds != null ) {
                confirmationMethod.setNotOnOrAfter(now.plusSeconds(validitySeconds));
                // Required to add to cache
                samlAssertion.expiry_ts = confirmationMethod.getNotOnOrAfter().toDate();
            }
            InetAddress localhost = InetAddress.getLocalHost();
            confirmationMethod.setAddress(localhost.getHostAddress()); // NOTE: This is the ATTESTATION SERVICE IP ADDRESS,  **NOT** THE HOST ADDRESS
            return confirmationMethod;
        }
        
        private Subject createSubject(TxtHost host) throws ConfigurationException, UnknownHostException {
            // Create the Subject
            SAMLObjectBuilder subjectBuilder = (SAMLObjectBuilder)  builderFactory.getBuilder(Subject.DEFAULT_ELEMENT_NAME);
            Subject subject = (Subject) subjectBuilder.buildObject();
            subject.setNameID(createNameID(host));
            subject.getSubjectConfirmations().add(createSubjectConfirmation(host));
            return subject;
        }
        
        // create the host attributes 
        
        /**
         * An attribute can be multi-valued, but this method builds a single-valued
         * String attribute such as FirstName=John or IPAddress=1.2.3.4
         * @param name
         * @param value
         * @return
         * @throws ConfigurationException 
         */
	private Attribute createStringAttribute(String name, String value) throws ConfigurationException {
            SAMLObjectBuilder attrBuilder = (SAMLObjectBuilder)  builderFactory.getBuilder(Attribute.DEFAULT_ELEMENT_NAME);
            Attribute attr = (Attribute) attrBuilder.buildObject();
            attr.setName(name);

            XMLObjectBuilder xmlBuilder =  builderFactory.getBuilder(XSString.TYPE_NAME);
            XSString attrValue = (XSString) xmlBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
            attrValue.setValue(value);

            attr.getAttributeValues().add(attrValue);
            return attr;
	}

        /**
         * This method builds a single-valued boolean attribute such as isTrusted=true
         * @param name
         * @param value
         * @return
         * @throws ConfigurationException 
         */
	private Attribute createBooleanAttribute(String name, boolean value) throws ConfigurationException {
            SAMLObjectBuilder attrBuilder = (SAMLObjectBuilder)  builderFactory.getBuilder(Attribute.DEFAULT_ELEMENT_NAME);
            Attribute attr = (Attribute) attrBuilder.buildObject();
            attr.setName(name);

            XMLObjectBuilder xmlBuilder =  builderFactory.getBuilder(XSAny.TYPE_NAME);
            XSAny attrValue = (XSAny) xmlBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSAny.TYPE_NAME);
            attrValue.setTextContent( value ? "true" : "false" );

            attr.getAttributeValues().add(attrValue);
            return attr;
	}
        
        /**
         * Creates a base64-encoded attribute
         * @param name
         * @param value
         * @return
         * @throws ConfigurationException 
         */
	private Attribute createBase64BinaryAttribute(String name, byte[] value) throws ConfigurationException {
            SAMLObjectBuilder attrBuilder = (SAMLObjectBuilder)  builderFactory.getBuilder(Attribute.DEFAULT_ELEMENT_NAME);
            Attribute attr = (Attribute) attrBuilder.buildObject();
            attr.setName(name);

            XMLObjectBuilder xmlBuilder =  builderFactory.getBuilder(XSBase64Binary.TYPE_NAME);
            XSBase64Binary attrValue = (XSBase64Binary) xmlBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSBase64Binary.TYPE_NAME);
            attrValue.setValue(Base64.encodeBase64String(value));

            attr.getAttributeValues().add(attrValue);
            return attr;
	}
    
        /*  works but not needed
        private List<Attribute> createStringAttributes(Map<String,String> attributes) throws ConfigurationException {
            ArrayList<Attribute> list = new ArrayList<Attribute>();
            for(Map.Entry<String,String> e : attributes.entrySet()) {
                Attribute attr = createStringAttribute(e.getKey(), e.getValue());
                list.add(attr);
            }
            return list;
        }
        * 
        */

	// currently unused but probably works
	/*
	private Attribute createComplexAttribute(String name, String xmlValue) throws ConfigurationException {
            SAMLObjectBuilder attrBuilder = (SAMLObjectBuilder)  builderFactory.getBuilder(Attribute.DEFAULT_ELEMENT_NAME);
            Attribute attr = (Attribute) attrBuilder.buildObject();
            attr.setName(name);

            XMLObjectBuilder stringBuilder =  builderFactory.getBuilder(XSString.TYPE_NAME);
            XSAny attrValue = (XSAny) stringBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSAny.TYPE_NAME);
            attrValue.setTextContent(xmlValue);

            attr.getAttributeValues().add(attrValue);
            return attr;
	}
	*/
//        private final String DEFAULT_OID = "2.5.4.789.1";
        private AttributeStatement createHostAttributes(TxtHost host, X509AttributeCertificate tagCertificate, Map<String, String> vmMetaData) throws ConfigurationException {
            // Builder Attributes
            SAMLObjectBuilder attrStatementBuilder = (SAMLObjectBuilder)  builderFactory.getBuilder(AttributeStatement.DEFAULT_ELEMENT_NAME);
            AttributeStatement attrStatement = (AttributeStatement) attrStatementBuilder.buildObject();
            // add host attributes (both for single host and multi-host assertions)
            attrStatement.getAttributes().add(createStringAttribute("Host_Name", host.getHostName().toString()));  
            attrStatement.getAttributes().add(createStringAttribute("Host_Address", host.getIPAddress())); 
//            attrStatement.getAttributes().add(createStringAttribute("Host_UUID", host.getUuid()));  
//            attrStatement.getAttributes().add(createStringAttribute("Host_AIK_SHA1", host.getUuid()));  
            

            // Create the attribute statements that are trusted
            attrStatement.getAttributes().add(createBooleanAttribute("Trusted", host.isBiosTrusted() && host.isVmmTrusted()));
            attrStatement.getAttributes().add(createBooleanAttribute("Trusted_BIOS", host.isBiosTrusted()));
            if( host.isBiosTrusted() ) {
                attrStatement.getAttributes().add(createStringAttribute("BIOS_Name", host.getBios().getName()));
                attrStatement.getAttributes().add(createStringAttribute("BIOS_Version", host.getBios().getVersion()));
                attrStatement.getAttributes().add(createStringAttribute("BIOS_OEM", host.getBios().getOem()));
            }
            attrStatement.getAttributes().add(createBooleanAttribute("Trusted_VMM", host.isVmmTrusted()));
            if( host.isVmmTrusted() ) {
                attrStatement.getAttributes().add(createStringAttribute("VMM_Name", host.getVmm().getName()));
                attrStatement.getAttributes().add(createStringAttribute("VMM_Version", host.getVmm().getVersion()));
                attrStatement.getAttributes().add(createStringAttribute("VMM_OSName", host.getVmm().getOsName()));
                attrStatement.getAttributes().add(createStringAttribute("VMM_OSVersion", host.getVmm().getOsVersion()));                
            }
            
            //attrStatement.getAttributes().add(createBooleanAttribute("Trusted_Location", host.isLocationTrusted()));
            //if( host.isLocationTrusted() ) {
            //    attrStatement.getAttributes().add(createStringAttribute("Location", host.getLocation()));            
            //}
            if (tagCertificate != null) {
                // add the asset tag attestation status and if the status is trusted, then add all the attributes. In order to uniquely
                // identify all the asset tags on the client side, we will just append the text ATAG for all of them.
                attrStatement.getAttributes().add(createBooleanAttribute("Asset_Tag", host.isAssetTagTrusted()));
                attrStatement.getAttributes().add(createStringAttribute("Asset_Tag_Certificate_Sha1", Sha1Digest.digestOf(tagCertificate.getEncoded()).toString()));
                if( host.isAssetTagTrusted()) {
                    // get all microformat attributes
                    List<UTF8NameValueMicroformat> microformatAttributes = tagCertificate.getAttributes(UTF8NameValueMicroformat.class);
                    for(UTF8NameValueMicroformat microformatAttribute : microformatAttributes) {
                        log.debug("microformat attribute OID {} name {} value {}", UTF8NameValueMicroformat.OID, microformatAttribute.getName(), microformatAttribute.getValue());
                        attrStatement.getAttributes().add(createStringAttribute(String.format("TAG[" + microformatAttribute.getName() + "]"),microformatAttribute.getValue()));
                    }
                    // get all name-valuesequence attributes
                    List<UTF8NameValueSequence> nameValueSequenceAttributes = tagCertificate.getAttributes(UTF8NameValueSequence.class);
                    for(UTF8NameValueSequence nameValueSequenceAttribute : nameValueSequenceAttributes) {
                        log.debug("namevaluesequence attribute OID {} name {} values {}", UTF8NameValueSequence.OID, nameValueSequenceAttribute.getName(), nameValueSequenceAttribute.getValues());
                        attrStatement.getAttributes().add(createStringAttribute(String.format("TAG[" + nameValueSequenceAttribute.getName() + "]"), StringUtils.join(nameValueSequenceAttribute.getValues(), ",")));
                    }
                    // all attributes including above and any other custom attributes will be available directly via the certificate
                    attrStatement.getAttributes().add(createBase64BinaryAttribute("TagCertificate", tagCertificate.getEncoded()));
                } else {
                    log.debug("Since Asset tag is not verified, no attributes would be added");
                }
            } else {
                log.debug("Since asset tag is not provisioned, asset tag attribute will not be added to the assertion.");
            }

            if( host.getAikCertificate() != null ) {
                attrStatement.getAttributes().add(createStringAttribute("AIK_Certificate", host.getAikCertificate()));
                attrStatement.getAttributes().add(createStringAttribute("AIK_SHA1", host.getAikSha1()));
            }
            else if( host.getAikPublicKey() != null ) {
                attrStatement.getAttributes().add(createStringAttribute("AIK_PublicKey", host.getAikPublicKey()));                
                attrStatement.getAttributes().add(createStringAttribute("AIK_SHA1", host.getAikSha1()));
            }
            
            if (host.getBindingKeyCertificate() != null && !host.getBindingKeyCertificate().isEmpty()) {
                attrStatement.getAttributes().add(createStringAttribute("Binding_Key_Certificate", host.getBindingKeyCertificate()));                
            }
            
            if (vmMetaData != null && !vmMetaData.isEmpty()) {
                for (Map.Entry<String, String> entry : vmMetaData.entrySet()) {
                    attrStatement.getAttributes().add(createStringAttribute(entry.getKey(), entry.getValue()));
                }
            }
            
            return attrStatement;
            
        }

        /*
        private AttributeStatement createHostAttributes(TxtHost host, ManifestType pcrManifest) throws ConfigurationException {
            AttributeStatement attrStatement = createHostAttributes(host);
            attrStatement.getAttributes().add(createComplexAttribute("Manifest", pcrManifest);

            return attrStatement;
            
        }
        */
        
        /**
         * Creates an assertion with attributes of the host
         * 
         * ID attribute: see section 5.4.2  "References" of http://docs.oasis-open.org/security/saml/v2.0/saml-core-2.0-os.pdf
         * 
         * @param host
         * @return 
         */
        private Assertion createAssertion(TxtHost host, X509AttributeCertificate tagCertificate, Map<String, String> vmMetaData) throws ConfigurationException, UnknownHostException {
            // Create the assertion
            SAMLObjectBuilder assertionBuilder = (SAMLObjectBuilder)  builderFactory.getBuilder(Assertion.DEFAULT_ELEMENT_NAME);
            Assertion assertion = (Assertion) assertionBuilder.buildObject();
            assertion.setID("HostTrustAssertion"); // ID is arbitrary, only needs to be unique WITHIN THE DOCUMENT, and is required so that the Signature element can refer to it, for example #HostTrustAssertion
            assertion.setIssuer(createIssuer());
            DateTime now = new DateTime();
            assertion.setIssueInstant(now);
            assertion.setVersion(SAMLVersion.VERSION_20);
            assertion.setSubject(createSubject(host));
            assertion.getAttributeStatements().add(createHostAttributes(host, tagCertificate, vmMetaData));

            return assertion;
        }

        /**
         * Differences from createAssertion:
         * - the assertion ID is "MultipleHostTrustAssertion" instead of "HostTrustAssertion"
         * - there is no overall Subject for the assertion because it's for multiple host
         * - each host is identified with host attributes within its own attribute statement
         * 
         * @param hosts
         * @return
         * @throws ConfigurationException
         * @throws UnknownHostException 
         */
        private Assertion createAssertion(Collection<TxtHostWithAssetTag> hosts) throws ConfigurationException, UnknownHostException {
            // Create the assertion
            SAMLObjectBuilder assertionBuilder = (SAMLObjectBuilder)  builderFactory.getBuilder(Assertion.DEFAULT_ELEMENT_NAME);
            Assertion assertion = (Assertion) assertionBuilder.buildObject();
            assertion.setID("MultipleHostTrustAssertion"); // ID is arbitrary, only needs to be unique WITHIN THE DOCUMENT, and is required so that the Signature element can refer to it, for example #HostTrustAssertion
            assertion.setIssuer(createIssuer());
            DateTime now = new DateTime();
            assertion.setIssueInstant(now);
            assertion.setVersion(SAMLVersion.VERSION_20);
//            assertion.setSubject(createSubject(host));
            for(TxtHostWithAssetTag host : hosts) {
                assertion.getAttributeStatements().add(createHostAttributes(host.getHost(), host.getTagCertificate(), null));            
            }

            return assertion;
        }
        
 
        private void signAssertion(Element assertion) throws GeneralSecurityException, XMLSignatureException, MarshalException {
            // Signature
            //   SignedInfo
            //     CanonicalizationMethod  Algorithm=http://www.w3.org/2001/10/xml-exc-c14n#
            //     SignatureMethod  Algorithm=http://www.w3.org/2000/09/xmldsig#rsa-sha1
            //     Reference URI="#HostTrustAssertion"
            //       Transforms
            //         Transform Algorithm=http://www.w3.org/2000/09/xmldsig#enveloped-signature
            //         Transform Algorithm=http://www.w3.org/2001/10/xml-exc-c14n#
            //       DigestMethod Algorithm=http://www.w3.org/2000/09/xmldsig#rsa-sha1
            //       DigestValue (the digest value as text content)
            //   SignatureValue (the signature as text content)
            //   KeyInfo
            //     X509Data
            //       X509Certificate (the certificate as text content)
            // KeyInfo: can include Certificate (to make it easy to find in a public keystore or verify its CA)
            if( signatureGenerator != null ) {
                signatureGenerator.signSAMLObject(assertion);                
            }
        }
        
    public SamlAssertion generateVMAssertion(TxtHost host, Map<String, String> vmMetaData) throws MarshallingException, ConfigurationException, UnknownHostException, GeneralSecurityException, XMLSignatureException, MarshalException {
        samlAssertion = new SamlAssertion();

        SAMLObjectBuilder assertionBuilder = (SAMLObjectBuilder) builderFactory.getBuilder(Assertion.DEFAULT_ELEMENT_NAME);
        Assertion assertion = (Assertion) assertionBuilder.buildObject();
        assertion.setID("VMTrustAssertion"); 
        assertion.setIssuer(createIssuer());
        DateTime now = new DateTime();
        assertion.setIssueInstant(now);
        assertion.setVersion(SAMLVersion.VERSION_20);
        
        // Create the Subject
        SAMLObjectBuilder subjectBuilder = (SAMLObjectBuilder)  builderFactory.getBuilder(Subject.DEFAULT_ELEMENT_NAME);
        Subject subject = (Subject) subjectBuilder.buildObject();
        subject.setNameID(createNameID(vmMetaData.get("VM_Instance_Id")));
        
        SAMLObjectBuilder subjectConfirmationBuilder = (SAMLObjectBuilder)  builderFactory.getBuilder(SubjectConfirmation.DEFAULT_ELEMENT_NAME);
        SubjectConfirmation subjectConfirmation = (SubjectConfirmation) subjectConfirmationBuilder.buildObject();
        subjectConfirmation.setMethod(SubjectConfirmation.METHOD_SENDER_VOUCHES); 
        subjectConfirmation.setSubjectConfirmationData(createSubjectConfirmationData());
        // Create the NameIdentifier
        SAMLObjectBuilder nameIdBuilder = (SAMLObjectBuilder) builderFactory.getBuilder(NameID.DEFAULT_ELEMENT_NAME);
        NameID nameId = (NameID) nameIdBuilder.buildObject();
        nameId.setValue(issuerServiceName);
        nameId.setFormat(NameID.UNSPECIFIED); // !!! CAN ALSO USE X509 SUBJECT FROM HOST CERTIFICATE instead of host name in database   
        subjectConfirmation.setNameID(nameId);

            
        subject.getSubjectConfirmations().add(subjectConfirmation);
        
        assertion.setSubject(subject);
        assertion.getAttributeStatements().add(createVMAttributes(host, vmMetaData));

        AssertionMarshaller marshaller = new AssertionMarshaller();
        Element plaintextElement = marshaller.marshall(assertion);

        String originalAssertionString = XMLHelper.nodeToString(plaintextElement);
        System.out.println("Assertion String: " + originalAssertionString);

        // add signatures and/or encryption
        signAssertion(plaintextElement);

        samlAssertion.assertion = XMLHelper.nodeToString(plaintextElement);
        System.out.println("Signed Assertion String: " + samlAssertion.assertion);
        return samlAssertion;
    }

    private AttributeStatement createVMAttributes(TxtHost host, Map<String, String> vmMetaData) throws ConfigurationException {
        // Builder Attributes
        SAMLObjectBuilder attrStatementBuilder = (SAMLObjectBuilder) builderFactory.getBuilder(AttributeStatement.DEFAULT_ELEMENT_NAME);
        AttributeStatement attrStatement = (AttributeStatement) attrStatementBuilder.buildObject();

        attrStatement.getAttributes().add(createStringAttribute("Host_Name", host.getHostName().toString()));

        if (host.getAikCertificate() != null) {
            attrStatement.getAttributes().add(createStringAttribute("AIK_Certificate", host.getAikCertificate()));
            attrStatement.getAttributes().add(createStringAttribute("AIK_SHA1", host.getAikSha1()));
        } else if (host.getAikPublicKey() != null) {
            attrStatement.getAttributes().add(createStringAttribute("AIK_PublicKey", host.getAikPublicKey()));
            attrStatement.getAttributes().add(createStringAttribute("AIK_SHA1", host.getAikSha1()));
        }

        if (vmMetaData != null && !vmMetaData.isEmpty()) {
            for (Map.Entry<String, String> entry : vmMetaData.entrySet()) {
                attrStatement.getAttributes().add(createStringAttribute(entry.getKey(), entry.getValue()));
            }
        }

        return attrStatement;

    }
}
