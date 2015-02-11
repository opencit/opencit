/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.x509;

import com.intel.dcsg.cpg.crypto.RsaCredentialX509;
import com.intel.dcsg.cpg.net.InternetAddress;
import com.intel.dcsg.cpg.validation.BuilderModel;
import com.intel.dcsg.cpg.x500.DNBuilder;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.asn1.DEROctetString;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.AlgorithmId;
import sun.security.x509.BasicConstraintsExtension;
import sun.security.x509.CertificateAlgorithmId;
import sun.security.x509.CertificateExtensions;
import sun.security.x509.CertificateIssuerName;
import sun.security.x509.CertificateSerialNumber;
import sun.security.x509.CertificateSubjectName;
import sun.security.x509.CertificateValidity;
import sun.security.x509.CertificateVersion;
import sun.security.x509.CertificateX509Key;
import sun.security.x509.DNSName;
import sun.security.x509.ExtendedKeyUsageExtension;
import sun.security.x509.GeneralName;
import sun.security.x509.GeneralNames;
import sun.security.x509.IPAddressName;
import sun.security.x509.KeyUsageExtension;
import sun.security.x509.SubjectAlternativeNameExtension;
import sun.security.x509.X500Name;
import sun.security.x509.X509CertImpl;
import sun.security.x509.X509CertInfo;

/**
 * This convenience class implements the Builder pattern in order to simplify
 * creation of X509 certificates.
 * 
 * You should create a new instance of this class for every certificate.
 *
 * XXX This class uses Sun internal APIs, which may be removed in a future
 * release.
 * 
 * @since 0.1
 * @author jbuhacoff
 */
public class X509Builder extends BuilderModel {
    private X509CertInfo info = new X509CertInfo();
    private CertificateValidity certificateValidity = null;
    private CertificateSerialNumber certificateSerialNumber = null;
    private String commonName = null;
    private String organizationUnit = null;
    private String organizationName = null;
    private String country = null;
    private CertificateSubjectName certificateSubjectName = null;
    private CertificateIssuerName certificateIssuerName = null;
    private CertificateX509Key subjectPublicKey = null;
    private PrivateKey issuerPrivateKey = null;
    private CertificateVersion certificateVersion = null;
    private GeneralNames alternativeNames = null;
    private AlgorithmId algorithm = null;
    private KeyUsageExtension keyUsageExtension = null;
    private CertificateExtensions certificateExtensions = null;
    private ExtendedKeyUsageExtension extendedKeyUsageExtension = null;
    private Vector<ObjectIdentifier> extendedKeyUsageExtensionList = null; // suppress warning, the corresponding Sun API requires Vector<ObjectIdentifier>
    private boolean extendedKeyUsageExtensionIsCritical = false;
    
    // the following OID's are copied from sun.security.x509.ExtendedKeyUsageExtension... don't know why they didn't make them public:
    private static final int[] anyExtendedKeyUsageOidData = {2, 5, 29, 37, 0};   
    private static final int[] serverAuthOidData = {1, 3, 6, 1, 5, 5, 7, 3, 1};     
    private static final int[] clientAuthOidData = {1, 3, 6, 1, 5, 5, 7, 3, 2};  
    private static final int[] codeSigningOidData = {1, 3, 6, 1, 5, 5, 7, 3, 3};   
    private static final int[] emailProtectionOidData = {1, 3, 6, 1, 5, 5, 7, 3, 4};  
    private static final int[] ipsecEndSystemOidData = {1, 3, 6, 1, 5, 5, 7, 3, 5}; 
    private static final int[] ipsecTunnelOidData = {1, 3, 6, 1, 5, 5, 7, 3, 6};   
    private static final int[] ipsecUserOidData = {1, 3, 6, 1, 5, 5, 7, 3, 7};  
    private static final int[] timeStampingOidData = {1, 3, 6, 1, 5, 5, 7, 3, 8};   
    private static final int[] OCSPSigningOidData = {1, 3, 6, 1, 5, 5, 7, 3, 9};  

    public X509Builder() { }
    
    /**
     * Supports fluent writing:
     * X509Builder x509 = X509Builder.factory().subjectName(...).alternativeName(...);
     * if( x509.isValid() ) { X509Certificate cert = x509.build(); } // check for isValid() is optional, but you will get null result from build if it's not valid
     * @return 
     */
    public static X509Builder factory() { return new X509Builder(); } 
    
    public X509Builder certificateValidity(CertificateValidity certificateValidity) {
        try {
            this.certificateValidity = certificateValidity;
            info.set(X509CertInfo.VALIDITY, certificateValidity); // CertificateException, IOException
        }
        catch(CertificateException | IOException e) {
            fault(e, "certificateValidity(%s)", certificateValidity==null?"null":certificateValidity.toString());
        }
        return this;
    }
    
    public X509Builder expires(long expiration, TimeUnit units) {
        try {
            Date from = new Date();
            Date to = new Date(from.getTime() + TimeUnit.MILLISECONDS.convert(expiration, units));
            certificateValidity = new CertificateValidity(from, to);
            info.set(X509CertInfo.VALIDITY, certificateValidity); // CertificateException, IOException
        }
        catch(CertificateException | IOException e) {
            fault(e, "expires(%d,%s)", expiration, units==null?"null":units.name());
        }
        return this;
    }
    
    public X509Builder valid(Date from, Date to) {
        try {
            certificateValidity = new CertificateValidity(from, to);
            info.set(X509CertInfo.VALIDITY, certificateValidity); // CertificateException, IOException
        }
        catch(CertificateException | IOException e) {
            fault(e, "valid(%s,%s)", from==null?"null":from.toString(), to==null?"null":to.toString());
        }
        return this;
    }
    
    public X509Builder randomSerial() {
        try {
            BigInteger sn = new BigInteger(64, new SecureRandom());
            certificateSerialNumber = new CertificateSerialNumber(sn);
            info.set(X509CertInfo.SERIAL_NUMBER, certificateSerialNumber);
        }
        catch(CertificateException | IOException e) {
            fault(e, "randomSerial");
        }
        return this;
    }
    
    public X509Builder subjectName(X500Name subjectName) {
        try {
            certificateSubjectName = new CertificateSubjectName(subjectName);
            info.set(X509CertInfo.SUBJECT, certificateSubjectName); // CertificateException, IOException
        }
        catch(CertificateException | IOException e) {
            fault(e, "subjectName(%s)", subjectName==null?"null":subjectName.getRFC2253Name());
        }
        return this;
    }
    
    /**
     * 
     * @param dn like "CN=Dave, OU=JavaSoft, O=Sun Microsystems, C=US"
     * @return 
     */
    public X509Builder subjectName(String dn) {
        try {
            certificateSubjectName = new CertificateSubjectName(new X500Name(dn));
            info.set(X509CertInfo.SUBJECT, certificateSubjectName); // CertificateException, IOException
        }
        catch(IOException | CertificateException e) {
            fault(e, "subjectName(%s)", dn);
        }
        return this;        
    }

    public X509Builder commonName(String text) { commonName = text; return this; }
    public X509Builder organizationUnit(String text) { organizationUnit = text; return this; }
    public X509Builder organizationName(String text) { organizationName = text; return this; }
    public X509Builder country(String text) { country = text; return this; }
    
    public X509Builder issuerName(CertificateIssuerName certificateIssuerName) {
        try {
            this.certificateIssuerName = certificateIssuerName;
            info.set(X509CertInfo.ISSUER, certificateIssuerName); // CertificateException, IOException
        }
        catch(CertificateException | IOException e) {
            fault(e, "issuerName(%s)", certificateIssuerName==null?"null":certificateIssuerName.toString());
        }
        return this;
    }

    public X509Builder issuerName(X500Name issuerName) {
        try {
            certificateIssuerName = new CertificateIssuerName(issuerName);
            info.set(X509CertInfo.ISSUER, certificateIssuerName); // CertificateException, IOException
        }
        catch(CertificateException | IOException e) {
            fault(e, "issuerName(%s)", issuerName.getRFC2253Name());
        }
        return this;
    }
    
    public X509Builder issuerName(X509Certificate issuerCertificate) {
        X500Name issuerName = X500Name.asX500Name(issuerCertificate.getSubjectX500Principal());
        issuerName(issuerName);
        return this;
    }

    public X509Builder issuerName(String dn) {
        try {
            certificateIssuerName = new CertificateIssuerName(new X500Name(dn));
            info.set(X509CertInfo.ISSUER, certificateIssuerName); // CertificateException, IOException
        }
        catch(IOException | CertificateException e) {
            fault(e, "issuerName(%s)", dn);
        }
        return this;
    }
    
    /**
     * Sets the issuerPrivateKey and issuerName using the provided credential.
     * @param issuerCredential
     * @return 
     */
    public X509Builder issuer(RsaCredentialX509 issuerCredential) {
        try {
            issuerPrivateKey(issuerCredential.getPrivateKey());
            issuerName(issuerCredential.getCertificate());
        }
        catch(Exception e) {
            fault(e, "issuer(%s)", issuerCredential==null?"null":issuerCredential.getCertificate().getIssuerX500Principal().getName());
        }
        return this;
    }
    
    public X509Builder subjectPublicKey(PublicKey publicKey) {
        try {
            subjectPublicKey = new CertificateX509Key(publicKey);
            info.set(X509CertInfo.KEY, subjectPublicKey); // CertificateException, IOException
        }
        catch(CertificateException | IOException e) {
            fault(e, "subjectPublicKey(%s)", publicKey==null?"null":String.format("%d bytes, %s %s",publicKey.getEncoded().length,publicKey.getAlgorithm(),publicKey.getFormat()));
        }
        return this;
    }

    public X509Builder issuerPrivateKey(PrivateKey privateKey) {
        this.issuerPrivateKey = privateKey;
        return this;
    }
    
    /**
     * For self-signed certificates you can call this single method instead of
     * subjectName, subjectPublicKey, issuerName, issuerPrivateKey.
     * 
     * @param dn like "CN=John Doe,O=Company,OU=Division,C=US" to use for both subject and issuer
     * @param keyPair private and public keys for subject
     * @return 
     */
    public X509Builder selfSigned(String dn, KeyPair keyPair) {
        subjectName(dn);
        subjectPublicKey(keyPair.getPublic());
        issuerName(dn);
        issuerPrivateKey(keyPair.getPrivate());
        return this;
    }
    
    /**
     * For self-signed certificates you can call this single method instead of
     * subjectName, subjectPublicKey, issuerName, issuerPrivateKey.
     * 
     * @param name to use for both subject and issuer
     * @param keyPair private and public keys for subject
     * @return 
     */
    public X509Builder selfSigned(X500Name name, KeyPair keyPair) {
        subjectName(name);
        subjectPublicKey(keyPair.getPublic());
        issuerName(name);
        issuerPrivateKey(keyPair.getPrivate());
        return this;
    }
    
    
    public X509Builder certificateVersion(CertificateVersion version) {
        try {
            if( certificateVersion == null ) {
                this.certificateVersion = version;
                info.set(X509CertInfo.VERSION, certificateVersion); // CertificateException, IOException                
            }
            else {
                if( version != null && !certificateVersion.toString().equals(version.toString()) ) {
                    fault("certificateVersion(%s) conflicts with previously set certificateVersion(%s)", version.toString(), certificateVersion.toString());
                }
            }
        }
        catch(CertificateException | IOException e) {
            fault(e, "certificateVersion(%d)", version==null?"null":version.toString());
        }
        return this;
    }
    
    public X509Builder v3() {
        try {
            certificateVersion(new CertificateVersion(CertificateVersion.V3));
        }
        catch(Exception e) {
            fault(e, "v3");
        }
        return this;
    }
    
    public X509Builder noncriticalExtension(String oid, byte[] value) {
        try {
            v3();
//            X509CertificateExtension extension = new X509CertificateExtension(oid, false, value);
            if( certificateExtensions == null ) { certificateExtensions = new CertificateExtensions(); }
//            certificateExtensions.set(extension.getId(), /*extension*/ new sun.security.x509.Extension(new ObjectIdentifier(extension.getId()), extension.isCritical(), extension.getDEREncoded()));
            DEROctetString octetString = new DEROctetString(value);
            certificateExtensions.set(oid, new sun.security.x509.Extension(new ObjectIdentifier(oid), false, octetString.getDEREncoded()));
            info.set(X509CertInfo.EXTENSIONS, certificateExtensions);
        }
        catch(IOException | CertificateException e) {
            fault(e, "noncriticalExtension(%s,%s)", oid, Base64.encodeBase64String(value));
        }
        return this;
    }
    
    public X509Builder criticalExtension(String oid, byte[] value) {
        try {
            v3();
//            Extension extension = new X509CertificateExtension(oid, true, value);
            if( certificateExtensions == null ) { certificateExtensions = new CertificateExtensions(); }
            DEROctetString octetString = new DEROctetString(value);
            certificateExtensions.set(oid, new sun.security.x509.Extension(new ObjectIdentifier(oid), true, octetString.getDEREncoded()));
//            certificateExtensions.set(extension.getId(), extension);
            info.set(X509CertInfo.EXTENSIONS, certificateExtensions);
        }
        catch(IOException | CertificateException e) {
            fault(e, "criticalExtension(%s,%s)", oid, Base64.encodeBase64String(value));
        }
        return this;
    }
    
    public X509Builder ipAlternativeName(String ip) {
        try {
            v3();
            String alternativeName = ip;
            if (ip.startsWith("ip:")) {
                alternativeName = ip.substring(3);
            }
            //                InetAddress ipAddress = new InetAddress.getByName(alternativeName.substring(3));
            //                IPAddressName ipAddressName = new IPAddressName(ipAddress.getAddress());
            IPAddressName ipAddressName = new IPAddressName(alternativeName);
            if( alternativeNames == null ) { alternativeNames = new GeneralNames(); }
            alternativeNames.add(new GeneralName(ipAddressName));
            SubjectAlternativeNameExtension san = new SubjectAlternativeNameExtension(alternativeNames);
            if( certificateExtensions == null ) { certificateExtensions = new CertificateExtensions(); }
            certificateExtensions.set(san.getExtensionId().toString(), san);
            info.set(X509CertInfo.EXTENSIONS, certificateExtensions);
            //   ObjectIdentifier("2.5.29.17") , false, "ipaddress".getBytes()                            
            
        }
        catch(IOException | CertificateException e) {
            fault(e, "ipAlternativeName(%s)", ip);
        }
        return this;
    }
    
    public X509Builder dnsAlternativeName(String dns) {
        try {
            v3();
            String alternativeName = dns;
            if (dns.startsWith("dns:")) {
                alternativeName = dns.substring(4);
            }
            DNSName dnsName = new DNSName(alternativeName);
            if( alternativeNames == null ) { alternativeNames = new GeneralNames(); }
            alternativeNames.add(new GeneralName(dnsName));
            SubjectAlternativeNameExtension san = new SubjectAlternativeNameExtension(alternativeNames);
            if( certificateExtensions == null ) { certificateExtensions = new CertificateExtensions(); }
            certificateExtensions.set(san.getExtensionId().toString(), san);
            info.set(X509CertInfo.EXTENSIONS, certificateExtensions);
        }        
        catch(IOException | CertificateException e) {
            fault(e, "dnsAlternativeName(%s)", dns);
        }
        return this;
    }
    
    public X509Builder alternativeName(String alternativeName) {
        try {
            if(alternativeName.startsWith("ip:")) {
                ipAlternativeName(alternativeName);
            }
            else if(alternativeName.startsWith("dns:")) {
                dnsAlternativeName(alternativeName);
            }
            else {
                // caller did not provide a hint, so try to automatically detect
                InternetAddress address = new InternetAddress(alternativeName);
                if( address.isIPv4() || address.isIPv6() ) {
                    ipAlternativeName(alternativeName);
                }
                else if( address.isHostname() ) {
                    dnsAlternativeName(alternativeName);
                }
                else {
                    fault("alternativeName(%s) not a valid InternetAddress", alternativeName); // cannot figure out what it is, must alert user
                }
            }
        }
        catch(Exception e) {
            fault(e, "alternativeName(%s)", alternativeName);
        }
        return this;
    }
    
    public X509Builder algorithm(AlgorithmId algorithmId) {
        try {
            this.algorithm = algorithmId; // new AlgorithmId(AlgorithmId.sha256WithRSAEncryption_oid); // md5WithRSAEncryption_oid
            info.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(algorithm));
//                info.set(CertificateAlgorithmId.NAME + "." + CertificateAlgorithmId.ALGORITHM, algorithm); // was present in older monolith version of the certificate factory, but it seems we don't really need it
        }
        catch(CertificateException | IOException e) {
            fault(e, "algorithm(%s)", algorithmId.getName());
        }
        return this;
    }
    
    public X509Builder keyUsageDigitalSignature() { // other than CA or CRL;  so this applies to API clients
        try {
            v3();
             if( keyUsageExtension == null ) { keyUsageExtension = new KeyUsageExtension(); }
             keyUsageExtension.set(KeyUsageExtension.DIGITAL_SIGNATURE, true);
            if( certificateExtensions == null ) { certificateExtensions = new CertificateExtensions(); }
            certificateExtensions.set(keyUsageExtension.getExtensionId().toString(), keyUsageExtension);
            info.set(X509CertInfo.EXTENSIONS, certificateExtensions);
        }
        catch(IOException | CertificateException e) {
            fault(e, "keyUsageDigitalSignature");
        }
        return this;
    }

    public X509Builder keyUsageNonRepudiation() { // other than CA or CRL; this applies to API clients
        try {
            v3();
             if( keyUsageExtension == null ) { keyUsageExtension = new KeyUsageExtension(); }
             keyUsageExtension.set(KeyUsageExtension.NON_REPUDIATION, true);
            if( certificateExtensions == null ) { certificateExtensions = new CertificateExtensions(); }
            certificateExtensions.set(keyUsageExtension.getExtensionId().toString(), keyUsageExtension);
            info.set(X509CertInfo.EXTENSIONS, certificateExtensions);
        }
        catch(IOException | CertificateException e) {
            fault(e, "keyUsageNonRepudiation");
        }
        return this;
    }
    
    public X509Builder keyUsageKeyEncipherment() { // for encrypting and transporting other keys
        try {
            v3();
             if( keyUsageExtension == null ) { keyUsageExtension = new KeyUsageExtension(); }
             keyUsageExtension.set(KeyUsageExtension.KEY_ENCIPHERMENT, true);
            if( certificateExtensions == null ) { certificateExtensions = new CertificateExtensions(); }
            certificateExtensions.set(keyUsageExtension.getExtensionId().toString(), keyUsageExtension);
            info.set(X509CertInfo.EXTENSIONS, certificateExtensions);
        }
        catch(IOException | CertificateException e) {
            fault(e, "keyUsageKeyEncipherment");
        }
        return this;
    }

    public X509Builder keyUsageDataEncipherment() { // for encrypting data
        try {
            v3();
             if( keyUsageExtension == null ) { keyUsageExtension = new KeyUsageExtension(); }
             keyUsageExtension.set(KeyUsageExtension.DATA_ENCIPHERMENT, true);
            if( certificateExtensions == null ) { certificateExtensions = new CertificateExtensions(); }
            certificateExtensions.set(keyUsageExtension.getExtensionId().toString(), keyUsageExtension);
            info.set(X509CertInfo.EXTENSIONS, certificateExtensions);
        }
        catch(IOException | CertificateException e) {
            fault(e, "keyUsageDataEncipherment");
        }
        return this;
    }

    public X509Builder keyUsageCertificateAuthority() {
        try {
            v3();
            // certificate authority basic constraint
            BasicConstraintsExtension constraintsExtension = new BasicConstraintsExtension(true,-1); // true indicates this is a CA;  -1 means no restriction on path length;  0 or more to set a restriction on max number of certs under this one in the chain
            // certificate signing extension
            if( keyUsageExtension == null ) { keyUsageExtension = new KeyUsageExtension(); }
            keyUsageExtension.set(KeyUsageExtension.KEY_CERTSIGN, true);
            // add both
            if( certificateExtensions == null ) { certificateExtensions = new CertificateExtensions(); }
            certificateExtensions.set(keyUsageExtension.getExtensionId().toString(), keyUsageExtension);
            certificateExtensions.set(constraintsExtension.getExtensionId().toString(), constraintsExtension);
            info.set(X509CertInfo.EXTENSIONS, certificateExtensions);
        }
        catch(IOException | CertificateException e) {
            fault(e, "keyUsageCertificateAuthority");
        }
        return this;
    }
    
    public X509Builder keyUsageCRLSign() {
        try {
            v3();
            if( keyUsageExtension == null ) { keyUsageExtension = new KeyUsageExtension(); }
            keyUsageExtension.set(KeyUsageExtension.CRL_SIGN, true);
            if( certificateExtensions == null ) { certificateExtensions = new CertificateExtensions(); }
            certificateExtensions.set(keyUsageExtension.getExtensionId().toString(), keyUsageExtension);
            info.set(X509CertInfo.EXTENSIONS, certificateExtensions);             
        }
        catch(IOException | CertificateException e) {
            fault(e, "keyUsageCRLSign");
        }
        return this;
    }
    
    public X509Builder extKeyUsageIsCritical() {
        extendedKeyUsageExtensionIsCritical = true;
        try {
            v3();
            if( extendedKeyUsageExtensionList != null ) { 
                extendedKeyUsageExtension = new ExtendedKeyUsageExtension(extendedKeyUsageExtensionIsCritical, extendedKeyUsageExtensionList);
                if( certificateExtensions == null ) { certificateExtensions = new CertificateExtensions(); }
                certificateExtensions.set(extendedKeyUsageExtension.getExtensionId().toString(), extendedKeyUsageExtension);
                info.set(X509CertInfo.EXTENSIONS, certificateExtensions);             
            }
        }
        catch(IOException | CertificateException e) {
            fault(e, "extKeyUsageIsCritical");
        }
        return this;
    }
    
    public X509Builder extKeyUsageServerAuth() {
        try {
            extKeyUsage(new ObjectIdentifier(serverAuthOidData));
        }
        catch(Exception e) {
            fault(e, "extKeyUsageServerAuth");
        }
        return this;
    }
    
    public X509Builder extKeyUsageClientAuth() {
        try {
            extKeyUsage(new ObjectIdentifier(clientAuthOidData));
        }
        catch(Exception e) {
            fault(e, "extKeyUsageClientAuth");
        }
        return this;
    }
    
    public X509Builder extKeyUsage(ObjectIdentifier oid) {
        try {
            v3();
            if( extendedKeyUsageExtensionList == null ) { extendedKeyUsageExtensionList = new Vector<ObjectIdentifier>(); }
            extendedKeyUsageExtensionList.add(oid);
            extendedKeyUsageExtension = new ExtendedKeyUsageExtension(extendedKeyUsageExtensionIsCritical, extendedKeyUsageExtensionList);
            if( certificateExtensions == null ) { certificateExtensions = new CertificateExtensions(); }
            certificateExtensions.set(extendedKeyUsageExtension.getExtensionId().toString(), extendedKeyUsageExtension);
            info.set(X509CertInfo.EXTENSIONS, certificateExtensions);             
        }
        catch(IOException | CertificateException e) {
            fault(e, "extKeyUsage(%s)", oid.toString());
        }
        return this;
    }
    

    public X509Certificate build() {
        if( certificateVersion == null ) {
            v3();
        }
        if( certificateValidity == null ) {
            expires(365, TimeUnit.DAYS); // 1 year default
        }
        if( certificateSerialNumber == null ) {
            randomSerial();
        }
        if( certificateSubjectName == null ) {
            String dn = DNBuilder.factory()
                    .commonName(commonName)
                    .organizationUnit(organizationUnit)
                    .organizationName(organizationName)
                    .country(country)
                    .toString();
            try {
                subjectName(new X500Name(dn));
            }
            catch(Exception e) {
                fault(e, "commonName(%s) organizationUnit(%s) organizationName(%s) country(%s)", commonName, organizationUnit, organizationName, country);
            }
        }
        if( certificateIssuerName == null ) {
            //if( certificateSubjectName != null ) { // assume self-signed ?? or check if we have an issuer cert first ??? 
            // XXX TODO 
            //}
            if( commonName != null || organizationUnit != null || organizationName != null || country != null ) {
                try {
                    issuerName(new X500Name(commonName, organizationUnit, organizationName, country));
                }
                catch(Exception e) {
                    fault(e, "commonName(%s) organizationUnit(%s) organizationName(%s) country(%s)", commonName, organizationUnit, organizationName, country);
                }
            }
            
        }
        if( subjectPublicKey == null ) {
            fault("missing subject public key");
        }
        // Note: alternativeName is optional so we don't have any defaults or errors for it here
        if( algorithm == null ) {
            algorithm(new AlgorithmId(AlgorithmId.sha256WithRSAEncryption_oid)); // algorithm.getName() == SHA256withRSA
        }
        
        //if( keyUsageExtension != null ) {
        //   
        //}
        try {
            if( getFaults().isEmpty() ) {
                // Sign the cert to identify the algorithm that's used.
                X509CertImpl cert = new X509CertImpl(info);
                cert.sign(issuerPrivateKey, algorithm.getName()); // NoSuchAlgorithMException, InvalidKeyException, NoSuchProviderException, , SignatureException

                /*
                 * for some unknown reason, if we return the "cert" now then all 
                 * the optioanl fields such as getBasicConstraints() and 
                 * getKeyUsage() are missing even though they are included if you 
                 * call getEncoded() ... but if you re-create the certificate
                 * then those fields are present in the re-created certificate.
                 */
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                X509Certificate cert2 = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(cert.getEncoded()));
                
                return cert2;            
            }
            return null;
        }
        catch(CertificateException | NoSuchAlgorithmException | InvalidKeyException | NoSuchProviderException | SignatureException e) {
            fault(e, "cannot sign certificate");
            return null;
        }
        finally {
            done();
        }
    }
    
}
