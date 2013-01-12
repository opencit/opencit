/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson;
import com.intel.mountwilson.as.hostmanifestreport.data.HostManifestReportType;
import com.intel.mountwilson.as.hosttrustreport.data.HostsTrustReportType;
import com.intel.mtwilson.crypto.CryptographyException;
import com.intel.mtwilson.crypto.HmacCredential;
import com.intel.mtwilson.crypto.Password;
import com.intel.mtwilson.crypto.RsaCredential;
import com.intel.mtwilson.crypto.RsaCredentialX509;
import com.intel.mtwilson.crypto.RsaUtil;
import com.intel.mtwilson.crypto.SimpleKeystore;
import com.intel.mtwilson.crypto.X509Util;
import com.intel.mtwilson.datatypes.*;
import com.intel.mtwilson.datatypes.xml.HostTrustXmlResponse;
import com.intel.mtwilson.datatypes.xml.HostTrustXmlResponseList;
import com.intel.mtwilson.io.ConfigurationUtil;
import com.intel.mtwilson.security.http.*;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import static javax.ws.rs.core.MediaType.*;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class has many constructors to provide convenience for developers. 
 * However, too many options may be confusing.
 * Based on developer feedback, we should retain the most useful constructors
 * and deprecate the rest. 
 * Two candidates for KEEPING are (File) and (URL,Hmac/RsaCredential,SimpleKeystore).
 * Those two constructors provide the two extremes: with (File), all properties in a file,
 * developer specifies the path for easy integration into any system); with
 * (URL,Hmac/RsaCredential,SimpleKeystore) a developer is able to instantiate a secure
 * ApiClient completely in Java without requiring a  configuration file (it will enable
 * requireTrustedCertificate and verifyHostname).
 * @since 0.5.2
 * @author jbuhacoff
 */
public class ApiClient implements AttestationService, WhitelistService, ManagementService {
    private static Logger log = LoggerFactory.getLogger(ApiClient.class);
//    private JerseyHttpClient httpClient;
    private ApacheHttpClient httpClient;
    private URL baseURL; // https://attestationservice.local:443
    private String attestationServicePath = "/AttestationService/resources"; // or /AttestationHandler/resources
    private String whitelistServicePath = "/WLMService/resources";
    private String managementServicePath = "/ManagementService/resources";
//    private Credential credential;
//    private HmacCredential hmacCredential;
//    private RsaCredential rsaCredential;
    protected static final ObjectMapper mapper = new ObjectMapper();
    private static final String defaultConfigurationFilename = "mtwilson.properties";
    private ClassLoader jaxbClassLoader = null;
    
    private SimpleKeystore keystore;
        
    /**
     * Loads configuration from the specified file. 
     * 
     * The configuration file must include the web service base URL and 
     * authentication information.
     * 
     * @param configurationFilename 
     * @throws ClientException that may wrap NoSuchAlgorithmException, KeyManagementException, MalformedURLException, UnsupportedEncodingException, KeyStoreException, IOException, UnrecoverableEntryException, or CertificateException
     * @throws IOException if there was a problem reading the specified file
     */
    public ApiClient(File configurationFile) throws ClientException, IOException {
        this(ConfigurationUtil.fromPropertiesFile(configurationFile));
        log.info("Initialized with configuration file: "+configurationFile.getAbsolutePath());
    }
    
    /**
     * Instantiates an ApiClient using the provided configuration. It must
     * include the base URL and authentication credentials.
     * 
     * @param config
     * @throws ClientException that may wrap NoSuchAlgorithmException, KeyManagementException, MalformedURLException, UnsupportedEncodingException, KeyStoreException, IOException, UnrecoverableEntryException, or CertificateException
     */
    public ApiClient(Configuration config) throws ClientException {
        try {
        setBaseURL(config.getString("mtwilson.api.baseurl"));
        log.debug("Base URL: "+baseURL.toExternalForm());
        /*
        httpClient = new JerseyHttpClient(baseURL.toExternalForm(), config.getString("mtwilson.api.clientId"), config.getString("mtwilson.api.secretKey"));
        */
        setKeystore(config);
        setHttpClientWithConfig(config);
        }
        catch(Exception e) {
            throw new ClientException("Cannot initialize client", e);
        }
    }
    

    /**
     * Instantiates an ApiClient using the provided base URL and authentication
     * credential, and using configuration information from the provided properties.
     * 
     * @param baseURL
     * @param credential
     * @param properties
     * @throws ClientException that may wrap NoSuchAlgorithmException, KeyManagementException, MalformedURLException, UnsupportedEncodingException, KeyStoreException, IOException, or CertificateException
     */
    public ApiClient(URL baseURL, HmacCredential credential, Properties properties) throws ClientException {
        try {
        setBaseURL(baseURL);
        Configuration config = new MapConfiguration(properties);
        setKeystore(config);
        log.debug("Base URL: "+baseURL.toExternalForm());
        httpClient = new ApacheHttpClient(baseURL, new ApacheHmacHttpAuthorization(credential), keystore, config);
        log.debug("HMAC-256 Identity: "+new String(credential.identity(), "UTF-8"));
        }
        catch(Exception e) {
            throw new ClientException("Cannot initialize client", e);
        }
    }

    /**
     * Instantiates an ApiClient using the provided base URL and authentication
     * credential, and using configuration information from the provided properties.
     * 
     * @param baseURL
     * @param credential
     * @param properties
     * @throws ClientException that may wrap NoSuchAlgorithmException, KeyManagementException, MalformedURLException, UnsupportedEncodingException, KeyStoreException, IOException, or CertificateException
     */
    public ApiClient(URL baseURL, RsaCredential credential, Properties properties) throws ClientException {
        try {
        setBaseURL(baseURL);
        Configuration config = new MapConfiguration(properties);
        setKeystore(config);
        log.debug("Base URL: "+baseURL.toExternalForm());
        httpClient = new ApacheHttpClient(baseURL, new ApacheRsaHttpAuthorization(credential), keystore, config);
        log.debug("RSA Identity: "+new String(credential.identity(), "UTF-8"));
        }
        catch(Exception e) {
            throw new ClientException("Cannot initialize client", e);
        }
    }
    

    /**
     * This constructor automatically enables requireTrustedCertificate and verifyHostname 
     * because a keystore is specified. If you want to specify a keystore yet not
     * require trusted certificates or verify hostnames, use the Configuration constructor.
     * @param baseURL
     * @param credential
     * @param keystore
     * @throws ClientException that may wrap NoSuchAlgorithmException, KeyManagementException, MalformedURLException, UnsupportedEncodingException, KeyStoreException, IOException, or CertificateException
     */
    public ApiClient(URL baseURL, HmacCredential credential, SimpleKeystore keystore, Configuration config) throws ClientException {
        try {
        setBaseURL(baseURL);
        setKeystore(keystore);
        log.debug("Base URL: "+baseURL.toExternalForm());
        httpClient = new ApacheHttpClient(baseURL, new ApacheHmacHttpAuthorization(credential), keystore, config);
        log.debug("HMAC-256 Identity: "+new String(credential.identity(), "UTF-8"));
        }
        catch(Exception e) {
            throw new ClientException("Cannot initialize client", e);
        }
    }
    /**
     * This constructor automatically enables requireTrustedCertificate and verifyHostname 
     * because a keystore is specified. If you want to specify a keystore yet not
     * require trusted certificates or verify hostnames, use the Configuration constructor.
     * @param baseURL
     * @param credential
     * @param keystore
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     * @throws MalformedURLException
     * @throws UnsupportedEncodingException
     * @throws KeyStoreException
     * @throws IOException
     * @throws CertificateException 
     */
    public ApiClient(URL baseURL, RsaCredential credential, SimpleKeystore keystore, Configuration config) throws ClientException {
        try {
        setBaseURL(baseURL);
        setKeystore(keystore);
        log.debug("Base URL: "+baseURL.toExternalForm());
        httpClient = new ApacheHttpClient(baseURL, new ApacheRsaHttpAuthorization(credential), keystore, config);
        log.debug("RSA Identity: "+new String(credential.identity(), "UTF-8"));
        }
        catch(Exception e) {
            throw new ClientException("Cannot initialize client", e);
        }
    }
    
    private void setBaseURL(URL url) {
        if( url == null ) {
            throw new IllegalArgumentException("Base URL must not be null");
        }
        baseURL = url;
    }
    private void setBaseURL(String url) throws MalformedURLException {
        if( url == null ) {
            throw new IllegalArgumentException("Base URL must not be null");
        }
        baseURL = new URL(url);
    }
    
    /**
     * Prefers RSA-SHA256 authentication (mtwilson.api.keystore and mtwilson.api.key.alias),
     * then HMAC-SHA256 authentication (mtwilson.api.clientId and mtwilson.api.secretKey),
     * then no authentication.
     * 
     * @param config
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     * @throws UnrecoverableEntryException
     * @throws IOException
     * @throws KeyManagementException 
     */
    private void setHttpClientWithConfig(Configuration config) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, UnrecoverableEntryException, IOException, KeyManagementException {
//        if( config.containsKey("mtwilson.api.keystore") && config.containsKey("mtwilson.api.keystore.password") && config.containsKey("mtwilson.api.key.alias") ) {        
        if( keystore != null ) {
            RsaCredentialX509 rsaCredential = keystore.getRsaCredentialX509(config.getString("mtwilson.api.key.alias"), config.getString("mtwilson.api.key.password"));
//            RsaCredential rsaCredential = RsaUtil.fromKeystore(config);
            setKeystore(config);
            httpClient = new ApacheHttpClient(baseURL, new ApacheRsaHttpAuthorization(rsaCredential), keystore, config);
            Hex hex = new Hex();
            log.debug("RSA Identity: "+hex.encode(rsaCredential.identity()));
        }
        else if( config.containsKey("mtwilson.api.clientId") && config.containsKey("mtwilson.api.secretKey") ) {
            HmacCredential hmacCredential = new HmacCredential(config.getString("mtwilson.api.clientId"), config.getString("mtwilson.api.secretKey"));
            setKeystore(config);
            httpClient = new ApacheHttpClient(baseURL, new ApacheHmacHttpAuthorization(hmacCredential), keystore, config);
            log.debug("HMAC-256 Identity: "+new String(hmacCredential.identity(), "UTF-8"));
        }
        else {
            // no authentication
            setKeystore(config);
            httpClient = new ApacheHttpClient(baseURL, null, keystore, config);
            log.debug("No identity configured");
        }        
    }
    
    private void setKeystore(Configuration config) throws KeyManagementException {
        if( config != null && config.containsKey("mtwilson.api.keystore") && config.containsKey("mtwilson.api.keystore.password") ) {
            keystore = new SimpleKeystore(new File(config.getString("mtwilson.api.keystore")), config.getString("mtwilson.api.keystore.password"));
        }
        else if( config != null && config.containsKey("javax.net.ssl.keyStore") && config.containsKey("javax.net.ssl.keyStorePassword") ) {
            keystore = new SimpleKeystore(new File(config.getString("javax.net.ssl.keyStore")), config.getString("javax.net.ssl.keyStorePassword"));            
        }
    }
    
    public final void setKeystore(SimpleKeystore keystore) {
        this.keystore = keystore;
    }

    /**
     * Some environments such as OSGi require the use of their own ClassLoader
     * when using JAXB. Use this method to set the ClassLoader that should be
     * used when deserializing XML responses with JAXB.
     * The default is to use the system class loader.
     * @param classLoader to use with JAXB, or null to use the default class loader
     */
    public void setJaxbClassLoader(ClassLoader classLoader) {
        jaxbClassLoader = classLoader;
    }
        
    /**
     * Call this to ensure that all HTTP connections and files are closed
     * when your are done using the API Client.
     */
    public void close() {
//        connectionManager.shutdown();
    }
    
    private String querystring(MultivaluedMap<String,String> query) {
        URLCodec urlsafe = new URLCodec("UTF-8");
        String queryString = "";
        ArrayList<String> params = new ArrayList<String>();
        for( String key : query.keySet() ) {
            if( query.get(key) == null ) {
                params.add(key+"=");
            }
            else {
                for( String value : query.get(key) ) {
                    try {
                        params.add(key+"="+urlsafe.encode(value)); // XXX assumes that the keys don't have any special characters
                    } catch (EncoderException ex) {
                        log.error("Cannot encode query parameter: {}", value, ex);
                    }
                }
            }
            queryString = StringUtils.join(params, "&");
        }
        return queryString;
    }
    
    private String asurl(String apiPath) {
        return baseURL.toExternalForm().concat(attestationServicePath).concat(apiPath);
    }
    private String asurl(String apiPath, MultivaluedMap<String,String> query) {
        return baseURL.toExternalForm().concat(attestationServicePath).concat(apiPath).concat("?").concat(querystring(query));
    }

    private String wlmurl(String apiPath) {
        return baseURL.toExternalForm().concat(whitelistServicePath).concat(apiPath);
    }
    
    private String wlmurl(String apiPath, MultivaluedMap<String,String> query) {
        return baseURL.toExternalForm().concat(whitelistServicePath).concat(apiPath).concat("?").concat(querystring(query));
    }

    private String msurl(String apiPath) {
        return baseURL.toExternalForm().concat(managementServicePath).concat(apiPath);
    }
    private String msurl(String apiPath, MultivaluedMap<String,String> query) {
        return baseURL.toExternalForm().concat(managementServicePath).concat(apiPath).concat("?").concat(querystring(query));
    }

    private ApiResponse httpGet(String path) throws IOException, ApiException, SignatureException {
        return httpClient.get(path);
    }
    private ApiResponse httpDelete(String path) throws IOException, ApiException, SignatureException {
        return httpClient.delete(path);
    }
    private ApiResponse httpPut(String path, ApiRequest body) throws IOException, ApiException, SignatureException {
        return httpClient.put(path, body);
    }
    private ApiResponse httpPost(String path, ApiRequest body) throws IOException, ApiException, SignatureException {
        return httpClient.post(path, body);
    }
    
    // only call this if the Http Status is NOT OK in order to convert the response to an ApiException
    private ApiException error(ApiResponse response) throws IOException, ApiException {
        if( response.contentType.isCompatible(APPLICATION_JSON_TYPE) ) {
            // a json error response from the web application. we need to provide the error message to the user.
            ErrorResponse errorResponse;
            try {
                log.debug("Parsing JSON error response: "+new String(response.content, "UTF-8"));
                errorResponse = json(new String(response.content, "UTF-8"), ErrorResponse.class);
            }
            catch(Exception e) {
                // cannot parse the json response, so include the entire response for the user. we ignore the exception "e" because it just means we couldn't parse the response.
                return new ApiException(response, "Cannot parse response: "+e.getMessage(), ErrorCode.UNKNOWN_ERROR);
            }
            return new ApiException(response, errorResponse.getErrorMessage(), ErrorCode.valueOf(errorResponse.getErrorCode()));
        }
        else if( response.contentType.isCompatible(TEXT_HTML_TYPE) ) {
            // typically html error message generated by web application container; we can ignore the html content because its generic
            String errorMessage = response.httpReasonPhrase;
            HtmlErrorParser errorParser = new HtmlErrorParser(new String(response.content, "UTF-8"));
            if( errorParser.getRootCause() != null ) {
                errorMessage = errorMessage.concat(": "+errorParser.getRootCause());
            }
            return new ApiException(response, errorMessage, httpErrorCode(response.httpStatusCode));
        }
        else {
            // a non-json, non-html error response from the web application: so we include the response in the exception message. http 401 unauthorized responses are included here.
            return new ApiException(response, new String(response.content, "UTF-8"), httpErrorCode(response.httpStatusCode));
        }
    }
    
    private ErrorCode httpErrorCode(int httpErrorCode) {
        ErrorCode e;
        switch(httpErrorCode) {
            case 200: e = ErrorCode.OK; break;
            case 400: e = ErrorCode.HTTP_INVALID_REQUEST; break;
            case 401: e = ErrorCode.HTTP_UNAUTHORIZED; break;
            case 403: e = ErrorCode.HTTP_FORBIDDEN; break;
            case 404: e = ErrorCode.HTTP_NOT_FOUND; break;
            case 500: e = ErrorCode.HTTP_INTERNAL_SERVER_ERROR; break;                
            default: e = ErrorCode.UNKNOWN_ERROR; break;
        }
        return e;
    }
    
    private byte[] content(ApiResponse response) throws IOException, ApiException {
        log.trace("Status: {} {}", response.httpStatusCode, response.httpReasonPhrase);
        log.trace("Content-Type: {}", response.contentType.toString());
        log.trace("Content: {}", response.content);
        if( response.httpStatusCode == HttpStatus.SC_OK ) {
            return response.content;
        }
        else {
            throw error(response);
        }
    }
    
    // xxx: don't know if it's better to define just one serialize method or to have each api call choose what form its response should come in...
    // one call is more flexible and easier to maintain...
    // this gets called only for successful responses; error codes 400, 500, etc. are thrown as ApiException by the http client so we'd never get to this.
    /*
    private <T> T deserialize(HttpResponse response, Class<T> valueType) throws IOException, ApiException {
        String body = responsebody(response);
        String contentType = response.getEntity() != null && response.getEntity().getContentType() != null ? response.getEntity().getContentType().getValue() : "";
        if( "application/json".equals(contentType) ) {
            return fromJSON(body, valueType);
        }
        else {
            return null;
        }
    }*/
    
    private String text(ApiResponse response) throws IOException, ApiException {
        return new String(content(response), "UTF-8");
    }

    private byte[] binary(ApiResponse response) throws IOException, ApiException {
        return content(response);
    }
    
    private <T> T json(ApiResponse response, Class<T> valueType) throws IOException, ApiException {
        if( response.httpStatusCode == HttpStatus.SC_OK && response.contentType.isCompatible(APPLICATION_JSON_TYPE) ) {
            return json(new String(response.content, "UTF-8"), valueType);
        }
        else if( response.httpStatusCode == HttpStatus.SC_OK ) {
            log.error("Unexpected content type {} in response", response.contentType.toString());
            throw new ApiException(response, "Unexpected content type in response: "+response.contentType.toString());
        }
        else {
            throw error(response);
        }
    }
    
    
    private <T> T json(String document, Class<T> valueType) throws IOException, ApiException {
        if( document == null ) {
            throw new ApiException("Response from server has no content");
        }
        try {
            return mapper.readValue(document, valueType);
        }
        catch(org.codehaus.jackson.JsonParseException e) {
            log.error("Cannot parse response: "+document);
            throw new ApiException("Cannot parse response: "+document, e);
        }        
    }
    
    private <T> T fromJSON(ApiResponse response, Class<T> valueType) throws IOException, ApiException {
        return json(response, valueType);
    }
    
    private ApiRequest toJSON(Object value) throws IOException {
        return new ApiRequest(APPLICATION_JSON_TYPE, mapper.writeValueAsString(value));
    }

    private <T> T xml(ApiResponse response, Class<T> valueType) throws IOException, ApiException {
        if( response.httpStatusCode == HttpStatus.SC_OK && response.contentType.isCompatible(APPLICATION_XML_TYPE) ) { // XXX or isCompatible(TEXT_XML_TYPE)
            return xml(new String(response.content, "UTF-8"), valueType);                
        }
        else if( response.httpStatusCode == HttpStatus.SC_OK ) {
            log.error("Unexpected content type {} in response", response.contentType.toString());
            throw new ApiException(response, "Unexpected content type in response: "+response.contentType.toString());
        }
        else {
            throw error(response);
        }
    }
    
    private <T> T xml(String document, Class<T> valueType) throws IOException, ApiException {
        try {
            JAXBContext jc;
            if( jaxbClassLoader != null ) {
                jc = JAXBContext.newInstance( valueType.getPackage().getName(), jaxbClassLoader );
            }
            else {
                jc = JAXBContext.newInstance( valueType.getPackage().getName() );
            }
            Unmarshaller u = jc.createUnmarshaller();
            JAXBElement<T> doc = (JAXBElement<T>)u.unmarshal( new StreamSource( new StringReader( document ) ) );
            return doc.getValue();
        }
        catch(JAXBException e) {
            throw new ApiException("Cannot parse response: "+document, e);
        }
    }
    
    private <T> T fromXML(ApiResponse response, Class<T> valueType) throws IOException, ApiException, JAXBException {
        return xml(response, valueType);
    }
    
    /**
     * 
     * @param response an HttpResponse from GET, PUT, POST, or DELETE request
     * @return String content from server response
     * @throws IOException 
     */
    /*
    private String plaintext(HttpResponse response) throws IOException {
        return IOUtils.toString(response.getEntity().getContent(), "UTF-8");
    }
      */  
    // Attestation Service API
    
    /**
     * javax.ws.rs.core.MediaType.APPLICATION_JSON  application/json
     * @param hostname 
     */
    @Override
    public HostLocation getHostLocation(Hostname hostname) throws IOException, ApiException, SignatureException {
        MultivaluedMap<String,String> query = new MultivaluedMapImpl();
        query.add("hostName", hostname.toString());
        HostLocation location = fromJSON(httpGet(asurl("/hosts/location", query)), HostLocation.class);
        return location;
    }
    
    @Override
    public boolean addHostLocation(HostLocation hostLocObj) throws IOException, ApiException, SignatureException {
        String result = text(httpPost(asurl("/hosts/location"), toJSON(hostLocObj)));
        return "true".equals(result);          
    }

    /**
     * 
     * @param trustStatusString like "BIOS:1,VMM:1" from API version 0.5.1
     * @return 
     */
    private HostTrustStatus parseHostTrustStatusString(String trustStatusString) {
            HostTrustStatus trustStatus = new HostTrustStatus();        
            String[] parts = trustStatusString.split(",");
            for (String part : parts) {
                    String[] subParts = part.split(":");
                    if (subParts[0].equals("BIOS")) {
                            trustStatus.bios = subParts[1].equals("1");
                    }
                    else if(subParts[0].equals("VMM")) {
                            trustStatus.vmm = subParts[1].equals("1");
                    }
            }
            return trustStatus;
    }
    
    /**
     * /hosts/trust?hostname=1.2.3.4
     * Response is PLAINTEXT ("BIOS:1,VMM:1") but needs to be changed to JSON on server  (todo) so we support both.
     * @param hostname
     * @return 
     */
    @Override
    public HostTrustResponse getHostTrust(Hostname hostname) throws IOException, ApiException, SignatureException {
        MultivaluedMap<String,String> query = new MultivaluedMapImpl();
        query.add("hostName", hostname.toString());
        // need to support both formats:  "BIOS:1,VMM:1" from 0.5.1 and JSON from 0.5.2
        ApiResponse response = httpClient.get(asurl("/hosts/trust", query));
        HostTrustResponse trust;
        if( response.httpStatusCode == HttpStatus.SC_OK ) {            
            if( APPLICATION_JSON_TYPE.equals(response.contentType) ) {
                trust = json(response, HostTrustResponse.class);            
            }
            else if( TEXT_PLAIN_TYPE.equals(response.contentType) ) {
                trust = new HostTrustResponse(hostname, parseHostTrustStatusString(text(response)));
            }
            else {
                throw new ApiException(response, "Unexpected content type in response: "+response.contentType, ErrorCode.UNKNOWN_ERROR.getErrorCode());
            }
            return trust;
        }
        else {
            throw error(response);
        }
    }

    @Override
    public HostResponse addHost(TxtHost host) throws IOException, ApiException, SignatureException {
        HostResponse added = fromJSON(httpPost(asurl("/hosts"), toJSON(new TxtHostRecord(host))), HostResponse.class);
        return added;
    }

    @Override
    public HostResponse updateHost(TxtHost host) throws IOException, ApiException, SignatureException {
        HostResponse added = fromJSON(httpPut(asurl("/hosts"), toJSON(new TxtHostRecord(host))), HostResponse.class);
        return added;        
    }

    @Override
    public HostResponse deleteHost(Hostname hostname) throws IOException, ApiException, SignatureException {
        MultivaluedMap<String,String> query = new MultivaluedMapImpl();
        query.add("hostName", hostname.toString());
        HostResponse deleted = fromJSON(httpDelete(asurl("/hosts", query)), HostResponse.class);
        return deleted;        
    }

    @Override
    public AttestationReport getAttestationFailureReport(Hostname hostname) throws IOException, ApiException, SignatureException {
        MultivaluedMap<String,String> query = new MultivaluedMapImpl();
        query.add("hostName", hostname.toString());
        query.add("failure_only", Boolean.toString(true));
        AttestationReport report = fromJSON(httpGet(asurl("/hosts/reports/attestationreport", query)), AttestationReport.class);
        return report;        
        
    }

    @Override
    public AttestationReport getAttestationReport(Hostname hostname) throws IOException, ApiException, SignatureException {
        MultivaluedMap<String,String> query = new MultivaluedMapImpl();
        query.add("hostName", hostname.toString());
        AttestationReport report = fromJSON(httpGet(asurl("/hosts/reports/attestationreport", query)), AttestationReport.class);
        return report;        
    }

    @Override
    public X509Certificate getTlsCertificateForTrustedHost(Hostname hostname) throws IOException, ApiException, SignatureException {
        throw new UnsupportedOperationException("Not supported yet.");
        // TODO: getSamlForHost(), then verifyTrustAssertion(), then extract the host's TLS Certificate (RC3 feature) from the TrustAssertion object (Because it's included in the SAML)
    }

    /**
     * Returns a set of X509Certificate objects comprised of the Mt Wilson Root CA and any intermediate CA's that
     * are available. 
     * 
     * Note: this method returns only CA certs, which does NOT return the server's TLS certificate or SAML certificate.
     * 
     * @return
     * @throws IOException
     * @throws ApiException
     * @throws SignatureException Do 
     */
    /*
    @Override
    public Set<X509Certificate> getCaCertificates() throws IOException, ApiException, SignatureException {
        try {
            List<X509Certificate> rootCaCerts = X509Util.decodePemCertificates(rootca);
            List<X509Certificate> privacyCaCerts = X509Util.decodePemCertificates(privacyca);
            List<X509Certificate> samlCaCerts = X509Util.decodePemCertificates(samlca);
            List<X509Certificate> tlsCaCerts = X509Util.decodePemCertificates(tlsca);
            HashSet<X509Certificate> cacerts = new HashSet<X509Certificate>();
            cacerts.addAll(rootCaCerts);
            cacerts.addAll(privacyCaCerts);
            cacerts.addAll(samlCaCerts);
            cacerts.addAll(tlsCaCerts);
            for(X509Certificate cert : cacerts) {
                if( cert.getBasicConstraints() == -1 ) {  // -1 indicates the certificate is not a CA cert;  0 and above indicates a CA cert
                    cacerts.remove(cert);
                }
            }
            return cacerts;
        } catch (CertificateException ex) {
            throw new ApiException("Invalid certificate", ex);
        }
    }*/
    
    @Override
    public X509Certificate getSamlCertificate() throws IOException, ApiException, SignatureException {
        byte[] certificateBytes = binary(httpGet(msurl("/saml/certificate")));
        X509Certificate certificate;
        try {
            certificate = X509Util.decodeDerCertificate(certificateBytes);
        } catch (CertificateException ex) {
            throw new ApiException("Cannot read certificate from response", ex);
        }
        return certificate;
    }
    
    @Override
    public Set<X509Certificate> getRootCaCertificates() throws IOException, ApiException, SignatureException {
       String rootca = text(httpGet(msurl("/ca/certificate/rootca/current")));
        try {
            List<X509Certificate> rootCaCerts = X509Util.decodePemCertificates(rootca);
            HashSet<X509Certificate> cacerts = new HashSet<X509Certificate>();
            cacerts.addAll(rootCaCerts); 
            // we expect that these are all CA certs, so we are not specifically checking:
            /*
            for(X509Certificate cert : cacerts) {
                if( cert.getBasicConstraints() == -1 ) {  // -1 indicates the certificate is not a CA cert;  0 and above indicates a CA cert
                    cacerts.remove(cert);
                }
            }*/
            return cacerts;
        } catch (CertificateException ex) {
            throw new ApiException("Invalid certificate", ex);
        }         
    }

    @Override
    public Set<X509Certificate> getPrivacyCaCertificates() throws IOException, ApiException, SignatureException {
        String privacyca = text(httpGet(msurl("/ca/certificate/privacyca/current")));
        try {
            List<X509Certificate> privacyCaCerts = X509Util.decodePemCertificates(privacyca);
            HashSet<X509Certificate> cacerts = new HashSet<X509Certificate>();
            cacerts.addAll(privacyCaCerts);
            // we expect that these are all CA certs, so we are not specifically checking:
            /*
            for(X509Certificate cert : cacerts) {
                if( cert.getBasicConstraints() == -1 ) {  // -1 indicates the certificate is not a CA cert;  0 and above indicates a CA cert
                    cacerts.remove(cert);
                }
            }*/
            return cacerts;
        } catch (CertificateException ex) {
            throw new ApiException("Invalid certificate", ex);
        }
    }
    
    // includes the server's saml certificate and any root certificates if available
    @Override
    public Set<X509Certificate> getSamlCertificates() throws IOException, ApiException, SignatureException {
        String samlca = text(httpGet(msurl("/ca/certificate/saml/current")));
        try {
            List<X509Certificate> samlCaCerts = X509Util.decodePemCertificates(samlca);
            HashSet<X509Certificate> certs = new HashSet<X509Certificate>();
            certs.addAll(samlCaCerts);
            return certs;
        } catch (CertificateException ex) {
            throw new ApiException("Invalid certificate", ex);
        }
    }
    
    // includes the server's tls certificate and any root certificates if available
    @Override
    public Set<X509Certificate> getTlsCertificates() throws IOException, ApiException, SignatureException {
        String tlsca = text(httpGet(msurl("/ca/certificate/tls/current")));
        try {
            List<X509Certificate> tlsCaCerts = X509Util.decodePemCertificates(tlsca);
            HashSet<X509Certificate> certs = new HashSet<X509Certificate>();
            certs.addAll(tlsCaCerts);
            return certs;
        } catch (CertificateException ex) {
            throw new ApiException("Invalid certificate", ex);
        }        
    }
    
    

    @Override
    public CaInfo getCaStatus() throws IOException, ApiException, SignatureException {
        throw new UnsupportedOperationException("Not supported yet.");
        // TODO: create a CaInfo object, which combines data from two sources:  1) the Mt Wilson CA Certificate, 2) the "ca" user in the HMAC users table
    }

    /**
     * XXX needs a rename, because we're talking about the host provisioning ca specifically
     * @param newPasswordString to authorize new hosts
     * @throws IOException
     * @throws ApiException
     * @throws SignatureException 
     */
    @Override
    public void enableCaWithPassword(String newPasswordString) throws IOException, ApiException, SignatureException {
        try {
            Password newPassword = new Password(newPasswordString, new byte[0]); // XXX currently using an empty salt, so the user doesn't need to also copy the salt, but maybe we should use a generated salt and return it to the user to paste when using the password....
            String result = text(httpPost(msurl("/ca/enable"), toJSON(newPassword.toString())));
            //return "true".equals(result);          
        }
        catch(CryptographyException e) {
            throw new ApiException("Cannot hash password", e);
        }
    }

    /**
     * XXX needs a rename, because we're talking about the host provisioning ca specifically
     * @throws IOException
     * @throws ApiException
     * @throws SignatureException 
     */
    @Override
    public void disableCa() throws IOException, ApiException, SignatureException {
            String result = text(httpPost(msurl("/ca/disable"), null));
        // TODO:  an update on the "ca" user in the HMAC users table,  set enabled=false
    }

    @Override
    public List<AuditLogEntry> searchAuditLog(AuditLogSearchCriteria criteria) throws IOException, ApiException, SignatureException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    
    // this is required so that the jackson mapper will create an instance of ListMleData (List<MleData>) instead of creating an instance of List<LinkedHashMap>
    public static class ListHostData extends ArrayList<TxtHostRecord> { };
    
    @Override
    public List<TxtHostRecord> queryForHosts(String searchCriteria) throws IOException, ApiException, SignatureException {
        MultivaluedMap<String,String> query = new MultivaluedMapImpl();
        query.add("searchCriteria", searchCriteria);        
        ListHostData results = fromJSON(httpGet(asurl("/hosts", query)), ListHostData.class);
        return results;                
    }

    /**
     * javax.ws.rs.core.MediaType.APPLICATION_XML   application/xml
     * @param hostnames 
     */
    @Override
    public OpenStackHostTrustLevelReport pollHosts(List<Hostname> hostnames) throws IOException, ApiException, SignatureException {
        OpenStackHostTrustLevelQuery input = new OpenStackHostTrustLevelQuery();
        input.hosts = hostnames.toArray(new Hostname[0]);
        OpenStackHostTrustLevelReport output = fromJSON(httpPost(asurl("/PollHosts"), toJSON(input)), OpenStackHostTrustLevelReport.class);
        return output;
    }

    @Override
    public HostsTrustReportType getHostTrustReport (List<Hostname> hostnames) throws IOException, ApiException, SignatureException, JAXBException {
        String hostNamesCSV = StringUtils.join(hostnames, ",");
        MultivaluedMap<String,String> query = new MultivaluedMapImpl();
        query.add("hostNames", hostNamesCSV);
        HostsTrustReportType report = fromXML(httpGet(asurl("/hosts/reports/trust", query)), HostsTrustReportType.class);
        return report;
    }

    @Override
    public HostManifestReportType getHostManifestReport (Hostname hostname) throws IOException, ApiException, SignatureException, JAXBException {
        MultivaluedMap<String,String> query = new MultivaluedMapImpl();
        query.add("hostName", hostname.toString());
        HostManifestReportType report = fromXML(httpGet(asurl("/hosts/reports/trust", query)), HostManifestReportType.class);        
        return report;
    }
    


    /**
     * application/samlassertion+xml
     * 
     * @param hostname 
     */
    @Override
    public String getSamlForHost(Hostname hostname) throws IOException, ApiException, SignatureException {
        MultivaluedMap<String,String> query = new MultivaluedMapImpl();
        query.add("ID", hostname.toString());
        String saml = text(httpGet(asurl("/saml/assertions/host", query))); // NOTE: we are returning the raw XML document, we don't try to instantiate any Java object via the xml() funciton. The client can create a TrustAssertion object using this XML string in order to parse it.
        return saml;
    }

    public TrustAssertion verifyTrustAssertion(String saml) throws IOException, ApiException, SignatureException {
        X509Certificate[] trustedSamlCertificates;
        try {
            trustedSamlCertificates = keystore.getTrustedCertificates(SimpleKeystore.SAML);
        }
        catch(KeyStoreException e) {
            throw new ApiException("Cannot load trusted SAML certificates", e);
        }
        catch(NoSuchAlgorithmException e) {
            throw new ApiException("Cannot load trusted SAML certificates", e);
        }
        catch(UnrecoverableEntryException e) {
            throw new ApiException("Cannot load trusted SAML certificates", e);
        }
        catch(CertificateEncodingException e) {
            throw new ApiException("Cannot load trusted SAML certificates", e);
        }
        TrustAssertion trustAssertion = new TrustAssertion(trustedSamlCertificates, saml);
        return trustAssertion;
    }
    
    /**
     * XXX TODO inputs to this are the same as HostTrustStatusRequest in the datatypes project but that class requires username and password in the constructor
     * so we need to clean it up before using it. Also because the parameters here are distinct (Collection<Hostname>, boolean) the change is a low priority since there
     * cannot be any confusion about the parameters to this method.
     * @param hostnames
     * @param forceVerify
     * @return
     * @throws IOException
     * @throws ApiException
     * @throws SignatureException 
     */
    @Override
    public List<HostTrustXmlResponse> getSamlForMultipleHosts(Set<Hostname> hostnames, boolean forceVerify) throws IOException, ApiException, SignatureException {
        // prepare the request
        String hostnamesCSV = StringUtils.join(hostnames, ","); // calls toString() on each hostname
        MultivaluedMap<String,String> query = new MultivaluedMapImpl();
        query.add("hosts", hostnamesCSV);
        query.add("force_verify", Boolean.toString(forceVerify));
        // make the request and parse the xml response
        HostTrustXmlResponseList list = xml(httpGet(asurl("/hosts/bulk/trust/saml", query)), HostTrustXmlResponseList.class);
        return list.getHost(); // get the list of <Host> elements inside the root <Hosts> element... it's an automatically generated method name. would have been nice if they named it getHostList()
    }
    
    /**
     * @deprecated this method is used only by OpenSourceVMMHelper which is being replaced by IntelHostAgent; also the service implementation of this method only supports hosts with trust agents (even though vmware hosts also have their own attestation report)
     * @param hostname
     * @return
     * @throws IOException
     * @throws ApiException
     * @throws SignatureException 
     */
    @Override
    public String getHostAttestationReport(Hostname hostname) throws IOException, ApiException, SignatureException {
        MultivaluedMap<String,String> query = new MultivaluedMapImpl();
        query.add("hostName", hostname.toString());
        String attReport = text(httpGet(asurl("/hosts/reports/attestation", query)));
        return attReport;
    }

    // Whitelist Management API
    @Override
    public boolean addMLE(MleData mle) throws IOException, ApiException, SignatureException {
        String result = text(httpPost(wlmurl("/mles"), toJSON(mle)));
        return "true".equals(result);
    }

    @Override
    public boolean updateMLE(MleData mle) throws IOException, ApiException, SignatureException {
        String result = text(httpPut(wlmurl("/mles"), toJSON(mle)));
        return "true".equals(result);        
    }

    @Override
    public BulkHostTrustResponse getTrustForMultipleHosts(Set<Hostname> hostnames, boolean forceVerify) throws IOException, ApiException, SignatureException {
        // prepare the request
        String hostnamesCSV = StringUtils.join(hostnames, ","); // calls toString() on each hostname
        MultivaluedMap<String,String> query = new MultivaluedMapImpl();
        query.add("hosts", hostnamesCSV);
        query.add("force_verify", Boolean.toString(forceVerify));
        // make the request and parse the xml response
        return json(httpGet(asurl("/hosts/bulk/trust", query)), BulkHostTrustResponse.class);
    }


    // this is required so that the jackson mapper will create an instance of ListMleData (List<MleData>) instead of creating an instance of List<LinkedHashMap>
    public static class ListMleData extends ArrayList<MleData> { };
    
    @Override
    public List<MleData> searchMLE(String name) throws IOException, ApiException, SignatureException {
        MultivaluedMap<String,String> query = new MultivaluedMapImpl();
        query.add("searchCriteria", name);        
        ListMleData results = fromJSON(httpGet(wlmurl("/mles", query)), ListMleData.class);
        return results;        
    }

    /**
     * Also known as GetMLEDetails
     * 
     * @param criteria 
     */
    @Override
    public MleData getMLEManifest(MLESearchCriteria criteria) throws IOException, ApiException, SignatureException {
        MultivaluedMap<String,String> query = new MultivaluedMapImpl();
        query.add("mleName", criteria.mleName);        
        query.add("mleVersion", criteria.mleVersion);        
        query.add("osName", criteria.osName);        
        query.add("osVersion", criteria.osVersion);        
        query.add("oemName", criteria.oemName);        
        MleData mle = fromJSON(httpGet(wlmurl("/mles/manifest", query)), MleData.class);
        return mle;
    }

    @Override
    public boolean deleteMLE(MLESearchCriteria criteria) throws IOException, ApiException, SignatureException {
        MultivaluedMap<String,String> query = new MultivaluedMapImpl();
        query.add("mleName", criteria.mleName);        
        query.add("mleVersion", criteria.mleVersion);        
        query.add("osName", criteria.osName);        
        query.add("osVersion", criteria.osVersion);        
        query.add("oemName", criteria.oemName);        
        String result = fromJSON(httpDelete(wlmurl("/mles", query)), String.class);        
        return "true".equals(result);        
    }

    // this is required so that the jackson mapper will create an instance of ListOemData (List<OemData>) instead of creating an instance of List<LinkedHashMap>
    public static class ListOemData extends ArrayList<OemData> { };
    
    @Override
    public List<OemData> listAllOEM() throws IOException, ApiException, SignatureException {
        ListOemData results = fromJSON(httpGet(wlmurl("/oem")), ListOemData.class);
        return results;                
    }

    @Override
    public boolean addOEM(OemData oem) throws IOException, ApiException, SignatureException {
        String result = text(httpPost(wlmurl("/oem"), toJSON(oem)));
        return "true".equals(result);                
    }

    @Override
    public boolean updateOEM(OemData oem) throws IOException, ApiException, SignatureException {
        String result = text(httpPut(wlmurl("/oem"), toJSON(oem)));
        return "true".equals(result);                
        
    }

    @Override
    public boolean deleteOEM(String name) throws IOException, ApiException, SignatureException {
        MultivaluedMap<String,String> query = new MultivaluedMapImpl();
        query.add("Name", name);        
        String result = text(httpDelete(wlmurl("/oem", query))); 
        return "true".equals(result);                
    }

    // this is required so that the jackson mapper will create an instance of ListOsData (List<OsData>) instead of creating an instance of List<LinkedHashMap>
    public static class ListOsData extends ArrayList<OsData> { };
   
    @Override
    public List<OsData> listAllOS() throws IOException, ApiException, SignatureException {
        //ArrayList<OsData> results = fromJSON(GET(wlmurl("/os")), ArrayList.class);
        ListOsData results = fromJSON(httpGet(wlmurl("/os")), ListOsData.class);
        return results;                        
    }

    @Override
    public boolean updateOS(OsData os) throws IOException, ApiException, SignatureException {
        String result = text(httpPut(wlmurl("/os"), toJSON(os)));
        return "true".equals(result);                        
    }

    @Override
    public boolean addOS(OsData os) throws IOException, ApiException, SignatureException {
        String result = text(httpPost(wlmurl("/os"), toJSON(os)));
        return "true".equals(result);                
        
    }

    @Override
    public boolean deleteOS(OsData os) throws IOException, ApiException, SignatureException {
        MultivaluedMap<String,String> query = new MultivaluedMapImpl();
        query.add("Name", os.getName());        
        query.add("Version", os.getVersion());        
        String result = text(httpDelete(wlmurl("/os", query))); 
        return "true".equals(result);        
    }

    
    // Registration API
    public void register(ApiClientCreateRequest apiClient) throws IOException, ApiException, SignatureException {
        httpPost(msurl("/apiclient/register"), toJSON(apiClient));
        //return "true".equals(result);        
    }

    
    // Credential Management API
    
    // this is required so that the jackson mapper will create an instance of ListOsData (List<OsData>) instead of creating an instance of List<LinkedHashMap>
    public static class ListApiClientInfo extends ArrayList<ApiClientInfo> { };
    
    @Override
    public List<ApiClientInfo> searchApiClients(ApiClientSearchCriteria criteria) throws IOException, ApiException, SignatureException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        ArrayList<BasicNameValuePair> queryList = new ArrayList<BasicNameValuePair>();
        if( criteria.enabledEqualTo != null ) {
            queryList.add(new BasicNameValuePair("enabledEqualTo", String.valueOf(criteria.enabledEqualTo))); // boolean will serialize to "true" or "false"
        }
        if( criteria.expiresAfter != null ) {
            queryList.add(new BasicNameValuePair("expiresAfter", dateFormat.format(criteria.expiresAfter)));
        }
        if( criteria.expiresBefore != null ) {
            queryList.add(new BasicNameValuePair("expiresBefore", dateFormat.format(criteria.expiresBefore)));
        }
        if( criteria.fingerprintEqualTo != null ) {
            queryList.add(new BasicNameValuePair("fingerprintEqualTo", new String(Hex.encodeHex(criteria.fingerprintEqualTo))));
        }
        if( criteria.issuerEqualTo != null ) {
            queryList.add(new BasicNameValuePair("issuerEqualTo", criteria.issuerEqualTo));
        }
        if( criteria.nameContains != null ) {
            queryList.add(new BasicNameValuePair("nameContains", criteria.nameContains));
        }
        if( criteria.nameEqualTo != null ) {
            queryList.add(new BasicNameValuePair("nameEqualTo", criteria.nameEqualTo));
        }
        if( criteria.serialNumberEqualTo != null ) {
            queryList.add(new BasicNameValuePair("serialNumberEqualTo", String.valueOf(criteria.serialNumberEqualTo)));
        }
        if( criteria.statusEqualTo != null ) {
            queryList.add(new BasicNameValuePair("statusEqualTo", criteria.statusEqualTo));
        }
        if( criteria.commentContains != null ) {
            queryList.add(new BasicNameValuePair("commentContains", criteria.commentContains));
        }
        String queryString = URLEncodedUtils.format(queryList, "UTF-8");
        ListApiClientInfo results = fromJSON(httpGet(msurl("/apiclient/search?"+queryString)), ListApiClientInfo.class);
        return results;
    }

    @Override
    public List<ApiClientInfo> listPendingAccessRequests() throws IOException, ApiException, SignatureException {        
        ApiClientSearchCriteria criteria = new ApiClientSearchCriteria();
        criteria.enabledEqualTo = false;
        criteria.statusEqualTo = "Pending"; // XXX TODO status codes should be in enum in datatypes project
        return searchApiClients(criteria);
    }

    @Override
    public boolean updateApiClient(ApiClientUpdateRequest apiClient) throws IOException, ApiException, SignatureException {
        httpPut(msurl("/apiclient"), toJSON(apiClient));
        return true; // XXX TODO need actual return code here, but the web service currently does not return anything, it's either success or we'll get an ApiExceptoin
    }

    @Override
    public boolean deleteApiClient(byte[] fingerprint) throws IOException, ApiException, SignatureException {
//        String fingerprintBase64 = new String(Base64.encodeBase64(fingerprint));
        String fingerprintHex = new String(Hex.encodeHex(fingerprint));
        httpDelete(msurl(String.format("/apiclient?fingerprint=%s", fingerprintHex)));
        return true;
    }

    @Override
    public ApiClientInfo getApiClientInfo(byte[] fingerprint) throws IOException, ApiException, SignatureException {
//        String fingerprintBase64 = new String(Base64.encodeBase64(fingerprint));
        String fingerprintHex = new String(Hex.encodeHex(fingerprint));
        ApiClientInfo result = fromJSON(httpGet(msurl(String.format("/apiclient?fingerprint=%s", fingerprintHex))), ApiClientInfo.class);
        return result;
    }
    
    @Override
    public Role[] listAvailableRoles() throws IOException, ApiException, SignatureException {
        Role[] roles = fromJSON(httpGet(msurl("/apiclient/availableRoles")), Role[].class);
        return roles;
    }
    
    /**
     * Added By: Sudhir on June 26, 2012
     * 
     * Process the add request into the PCR manifest table.
     * 
     * @param pcrObj : White List data to be added to the PCR Manifest table
     * NOTE: OS Details need to be provided only when the associated MLE is of VMM type. 
     * For MLEs of BIOS type OEM Details have to be provided
     * @return : "true" if success or else exception.
     * @throws IOException
     * @throws ApiException
     * @throws SignatureException 
     */    
    @Override
    public boolean addPCRWhiteList(PCRWhiteList pcrObj) throws IOException, ApiException, SignatureException {
        String result = text(httpPost(wlmurl("/mles/whitelist/pcr"), toJSON(pcrObj)));
        return "true".equals(result);                
    }

    /**
     * Added By: Sudhir on June 26, 2012
     * 
     * Processes the update request into the PCR manifest table.
     * 
     * @param pcrObj : White List data to be updated in the PCR Manifest table.
     * NOTE: OS Details need to be provided only when the associated MLE is of VMM type. 
     * For MLEs of BIOS type OEM Details have to be provided
     * @return : "true" if success or else exception.
     * @throws IOException
     * @throws ApiException
     * @throws SignatureException 
     */
    @Override
    public boolean updatePCRWhiteList(PCRWhiteList pcrObj) throws IOException, ApiException, SignatureException {
        String result = text(httpPut(wlmurl("/mles/whitelist/pcr"), toJSON(pcrObj)));
        return "true".equals(result);                    
    }

    /**
     * Added By: Sudhir on June 26, 2012
     * 
     * Processes the delete request from the PCR manifest table.
     * 
     * @param pcrObj : White List data to be from the PCR Manifest table.
     * NOTE: PCR name along with MLE details need to be provided. No need to
     * specify PCR Digest value. OS Details need to be provided only when the 
     * associated MLE is of VMM type. For MLEs of BIOS type OEM Details have to be provided.
     * @return : "true" if success or else exception.
     * @throws IOException
     * @throws ApiException
     * @throws SignatureException 
     */
    @Override
    public boolean deletePCRWhiteList(PCRWhiteList pcrObj) throws IOException, ApiException, SignatureException {
        MultivaluedMap<String,String> query = new MultivaluedMapImpl();
        query.add("pcrName", pcrObj.getPcrName());        
        query.add("mleName", pcrObj.getMleName());        
        query.add("mleVersion", pcrObj.getMleVersion());        
        query.add("osName", pcrObj.getOsName());        
        query.add("osVersion", pcrObj.getOsVersion());        
        query.add("oemName", pcrObj.getOemName());        
        String result = text(httpDelete(wlmurl("/mles/whitelist/pcr", query))); 
        return "true".equals(result);                
    }
    
    /**
     * Added By: Sudhir on June 26, 2012
     * 
     * Process the add request into the Module manifest table.
     * 
     * @param moduleObj : White List data to be added to the Module Manifest table
     * NOTE: OS Details need to be provided only when the associated MLE is of VMM type. 
     * For MLEs of BIOS type OEM Details have to be provided
     * @return : "true" if success or else exception.
     * @throws IOException
     * @throws ApiException
     * @throws SignatureException 
     */    
    @Override
    public boolean addModuleWhiteList(ModuleWhiteList moduleObj) throws IOException, ApiException, SignatureException {
        String result = text(httpPost(wlmurl("/mles/whitelist/module"), toJSON(moduleObj)));
        return "true".equals(result);                
    }

    /**
     * Added By: Sudhir on June 26, 2012
     * 
     * Processes the update request into the Module manifest table.
     * 
     * @param moduleObj : White List data to be updated in the Module Manifest table.
     * NOTE: OS Details need to be provided only when the associated MLE is of VMM type. 
     * For MLEs of BIOS type OEM Details have to be provided
     * @return : "true" if success or else exception.
     * @throws IOException
     * @throws ApiException
     * @throws SignatureException 
     */
    @Override
    public boolean updateModuleWhiteList(ModuleWhiteList moduleObj) throws IOException, ApiException, SignatureException {
        String result = text(httpPut(wlmurl("/mles/whitelist/module"), toJSON(moduleObj)));
        return "true".equals(result);                    
    }

    /**
     * Added By: Sudhir on June 26, 2012
     * 
     * Processes the delete request from the Module manifest table.
     * 
     * @param moduleObj : White List data to be deleted from the Module Manifest table.
     * NOTE: Component & Event names along with MLE details need to be provided. 
     * OS Details need to be provided only when the associated MLE is of VMM type. 
     * For MLEs of BIOS type OEM Details have to be provided.
     * @return : "true" if success or else exception.
     * @throws IOException
     * @throws ApiException
     * @throws SignatureException 
     */
    @Override
    public boolean deleteModuleWhiteList(ModuleWhiteList moduleObj) throws IOException, ApiException, SignatureException {
        MultivaluedMap<String,String> query = new MultivaluedMapImpl();
        query.add("componentName", moduleObj.getComponentName());        
        query.add("eventName", moduleObj.getEventName());        
        query.add("mleName", moduleObj.getMleName());        
        query.add("mleVersion", moduleObj.getMleVersion());        
        query.add("osName", moduleObj.getOsName());        
        query.add("osVersion", moduleObj.getOsVersion());        
        query.add("oemName", moduleObj.getOemName());        
        String result = text(httpDelete(wlmurl("/mles/whitelist/module", query))); 
        return "true".equals(result);                
    }
    
    // 
    /**
     * This is required so that the jackson mapper will create an instance of ListModuleWhiteList 
     * (List<ModuleWhiteList>) instead of creating an instance of List<LinkedHashMap>
     */
    public static class ListModuleWhiteList extends ArrayList<ModuleWhiteList> { };

    /**
     * Added By: Sudhir on June 26, 2012
     * 
     * Retrieves the list of all the Module white list for the specified MLE.
     * 
     * @param mleName : Name of the measured launch environment (MLE) associated with the white list.
     * @param mleVersion : Version of the MLE or Hypervisor
     * @param osName : Name of the OS running the hypervisor. OS Details need to be provided only
     * when the associated MLE is of VMM type.
     * @param osVersion : Version of the OS
     * @param oemName : OEM vendor of the hardware system. OEM Details have to be provided only 
     * when the associated MLE is of BIOS type.
     * @return : List of module white lists.
     * @throws IOException
     * @throws ApiException
     * @throws SignatureException 
     */
    @Override
    public List<ModuleWhiteList> listModuleWhiteListForMLE(String mleName, String mleVersion, 
            String osName, String osVersion, String oemName) throws IOException, ApiException, SignatureException {
        MultivaluedMap<String,String> query = new MultivaluedMapImpl();
        query.add("mleName", mleName);        
        query.add("mleVersion", mleVersion);        
        query.add("osName", osName);        
        query.add("osVersion", osVersion);        
        query.add("oemName", oemName);          
        ListModuleWhiteList results = fromJSON(httpGet(wlmurl("/mles/whitelist/module", query)), ListModuleWhiteList.class);
        return results;                        
    }
    
    /**
     * Author: Sudhir
     * 
     * This function registers the specified host with mount wilson. The main difference between this and the addHost
     * function of the Attestation service is that this automatically creates the required OEM, OS, BIOS MLE and VMM MLE
     * configurations without any user intervention. 
     * 
     * @param hostObj
     * @return
     * @throws IOException
     * @throws ApiException
     * @throws SignatureException 
     */
    @Override
    public boolean registerHost(TxtHostRecord hostObj) throws IOException, ApiException, SignatureException {
        String result = text(httpPost(msurl("/host"), toJSON(hostObj)));
        return "true".equals(result);                
    }
    
    @Override
    public boolean registerHost(HostConfigData hostConfigObj) throws IOException, ApiException, SignatureException {
        String result = text(httpPost(msurl("/host/custom"), toJSON(hostConfigObj)));
        return "true".equals(result);                
    }

    /**
     * Author : Sudhir
     * 
     * This function automates the white list database configuration using the details of the host provided. If
     * need this function also configures the OEM, OS, BIOS MLE, VMM MLE and also registers the host. The reason
     * why the registration happens is because unless the host is registered, the Trust Agent cannot take the
     * ownership of the TPM and retrieve the boot time measurements. Whereas this is not the case with VMware. But
     * since we want both the implementation to be similar even for VMware hosts, we will register the host if it is 
     * not already done so.
     * 
     * @param hostObj
     * @return
     * @throws IOException
     * @throws ApiException
     * @throws SignatureException 
     */
    @Override
    public boolean configureWhiteList(TxtHostRecord hostObj) throws IOException, ApiException, SignatureException {
        String result = text(httpPost(msurl("/host/whitelist"), toJSON(hostObj)));
        return "true".equals(result);                
    }
    
    @Override
    public boolean configureWhiteList(HostConfigData hostConfigObj) throws IOException, ApiException, SignatureException {
        String result = text(httpPost(msurl("/host/whitelist/custom"), toJSON(hostConfigObj)));
        return "true".equals(result);                
    }
    
    @Override
    public boolean addMleSource(MleSource mleSourceObj) throws IOException, ApiException, SignatureException {
        String result = text(httpPost(wlmurl("/mles/source"), toJSON(mleSourceObj)));
        return "true".equals(result);                
    }

    @Override
    public boolean updateMleSource(MleSource mleSourceObj) throws IOException, ApiException, SignatureException {
        String result = text(httpPut(wlmurl("/mles/source"), toJSON(mleSourceObj)));
        return "true".equals(result);                    
    }

    @Override
    public boolean deleteMleSource(MleData mleDataObj) throws IOException, ApiException, SignatureException {
        MultivaluedMap<String,String> query = new MultivaluedMapImpl();
        query.add("mleName", mleDataObj.getName());        
        query.add("mleVersion", mleDataObj.getVersion());        
        query.add("osName", mleDataObj.getOsName());        
        query.add("osVersion", mleDataObj.getOsVersion());        
        query.add("oemName", mleDataObj.getOemName());        
        String result = text(httpDelete(wlmurl("/mles/source", query))); 
        return "true".equals(result);                
    }
    
    @Override
    public String getMleSource(MleData mleDataObj) throws IOException, ApiException, SignatureException {
        MultivaluedMap<String,String> query = new MultivaluedMapImpl();
        query.add("mleName", mleDataObj.getName());        
        query.add("mleVersion", mleDataObj.getVersion());        
        query.add("osName", mleDataObj.getOsName());        
        query.add("osVersion", mleDataObj.getOsVersion());        
        query.add("oemName", mleDataObj.getOemName());        
        String result = text(httpGet(wlmurl("/mles/source", query)));
        return result;                        
    }
    
}
