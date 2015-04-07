package com.intel.mtwilson.v2.vm.attestation.resource;

/**
 *
 * @author boskisha
 */
import java.io.File;
import javax.xml.crypto.*;
import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dom.*;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * This is a simple example of validating an XML Signature using the JSR 105
 * API. It assumes the key needed to validate the signature is contained in a
 * KeyValue KeyInfo.
 */
public class ValidateSignature {
    
    public static boolean isValid(String xmlData) throws Exception {

        // Instantiate the document to be validated
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document doc = dbf.newDocumentBuilder().parse(xmlData); //(new FileInputStream(new File(TrustPolicyLocation)));
        
        // Find Signature element
        NodeList nl = doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
        if (nl.getLength() == 0) {
            throw new Exception("Cannot find Signature element");
        }

        // Create a DOM XMLSignatureFactory that will be used to unmarshal the
        // document containing the XMLSignature
        XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");
        // Create a DOMValidateContext and specify a KeyValue KeySelector
        // and document context
        DOMValidateContext valContext = new DOMValidateContext(new KeyValueKeySelector(), nl.item(0));

        // unmarshal the XMLSignature
        XMLSignature signature = fac.unmarshalXMLSignature(valContext);

        // ValidateSignature the XMLSignature (generated above)
        boolean coreValidity = signature.validate(valContext);
 
        // Check core validation status
        if (coreValidity == false) {
            System.err.println("Signature failed core validation");
            boolean sv = signature.getSignatureValue().validate(valContext);
            System.out.println("signature validation status: " + sv);
            // check the validation status of each Reference
            Iterator i = signature.getSignedInfo().getReferences().iterator();
            for (int j = 0; i.hasNext(); j++) {
                boolean refValid = ((Reference) i.next()).validate(valContext);
                System.out.println("ref[" + j + "] validity status: " + refValid);
            }
            return false;
        } else {
            System.out.println("Signature passed core validation");
            return true;
        }
    }
    
    /**
     * KeySelector which retrieves the public key out of the KeyValue element
     * and returns it. NOTE: If the key algorithm doesn't match signature
     * algorithm, then the public key will be ignored.
     */
    private static class KeyValueKeySelector extends KeySelector {

        public KeySelectorResult select(KeyInfo keyInfo,KeySelector.Purpose purpose,AlgorithmMethod method,XMLCryptoContext context)throws KeySelectorException {
            Iterator ki = keyInfo.getContent().iterator();
            while (ki.hasNext()) {
                XMLStructure info = (XMLStructure) ki.next();
                if (!(info instanceof X509Data)) {
                    continue;
                }
                X509Data x509Data = (X509Data) info;
                Iterator xi = x509Data.getContent().iterator();
                while (xi.hasNext()) {
                    Object o = xi.next();
                    if (!(o instanceof X509Certificate)) {
                        continue;
                    }
//                    if(!isTrustedCertificate((X509Certificate) o)){
//                        continue;
//                    }
                    final PublicKey key = ((X509Certificate) o).getPublicKey();
                    // Make sure the algorithm is compatible
                    // with the method.
                    if (algEquals(method.getAlgorithm(), key.getAlgorithm())) {
                        return new KeySelectorResult() {
                            public Key getKey() {
                                return key;
                            }
                        };
                    }
                }
            }
            throw new KeySelectorException("No key found!");
        }
    }

	/**
	 * If certificate is available in trusted keystore than certificate is said to be 
	 * a trusted certificate	
	*/
    private static boolean isTrustedCertificate(X509Certificate cert) {
        FileInputStream fIn = null;
        boolean trusted = false;
        char[] password=new char[30];
        try {
            String keystoreFilename = "/opt/trustagent/configuration/trustagent.jks";
       
            //Parse the file to retrieve the trust agent keystore password
            String filepath="/opt/trustagent/configuration/trustagent.properties";
            String keyword="password";
            final Scanner scanner = new Scanner(new File(filepath));
            while (scanner.hasNextLine()) {
                   final String readLine = scanner.nextLine();
                   if(readLine.contains(keyword)) { 
                      String delimiter="=";
                      int startindex=readLine.indexOf(delimiter);
                      int endindex=readLine.length();
                      password=readLine.substring(startindex+1, endindex).toCharArray();
                      break;
                    }
            }
            
            fIn = new FileInputStream(keystoreFilename);
            KeyStore trustedStore = KeyStore.getInstance("JKS");
            trustedStore.load(fIn, password);
            if (trustedStore == null) {
                return trusted;
            }            
           // try {
                
                if (cert != null) {
                    // Only returns null if cert is NOT in keystore.
                    String alias = trustedStore.getCertificateAlias(cert);
//                    String alias="saml (ca)";
                    System.out.println("Alias value is:" + alias);
                    
                    if (alias != null) {
                        trusted = true;
                    }
                }
           // } catch (KeyStoreException e) {
             //   System.out.println(e.toString()+ e);
           // }            
        }catch (FileNotFoundException ex) {
            Logger.getLogger(ValidateSignature.class.getName()).log(Level.SEVERE, null, ex);
        } catch (KeyStoreException ex) {
            Logger.getLogger(ValidateSignature.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ValidateSignature.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(ValidateSignature.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CertificateException ex) {
            Logger.getLogger(ValidateSignature.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fIn.close();
            } catch (IOException ex) {
                Logger.getLogger(ValidateSignature.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
       System.out.println("Value of trusted: " + trusted);
        return trusted;
    }

	/**
	 *  Verifies if given two algorithms are same
	*/
    static boolean algEquals(String algURI, String algName) {
        if ((algName.equalsIgnoreCase("DSA")
                && algURI.equalsIgnoreCase(SignatureMethod.DSA_SHA1))
                || (algName.equalsIgnoreCase("RSA")
                && algURI.equalsIgnoreCase(SignatureMethod.RSA_SHA1))) {
            return true;
        } else {
            return false;
        }
    }

}
