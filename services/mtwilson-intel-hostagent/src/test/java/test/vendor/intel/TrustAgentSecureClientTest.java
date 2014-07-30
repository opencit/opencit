package test.vendor.intel;


import com.intel.dcsg.cpg.tls.policy.TlsConnection;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.dcsg.cpg.xml.JAXB;
import com.intel.mountwilson.as.common.ASConfig;
import com.intel.mountwilson.ta.data.ClientRequestType;
import com.intel.mtwilson.agent.*;
import com.intel.mtwilson.agent.intel.TAHelper;
import com.intel.mtwilson.agent.intel.TrustAgentSecureClient;
import com.intel.dcsg.cpg.io.ByteArrayResource;
import com.intel.dcsg.cpg.tls.policy.impl.InsecureTlsPolicy;
import com.intel.mtwilson.model.PcrManifest;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import javax.xml.bind.JAXBException;
import javax.xml.bind.PropertyException;
import javax.xml.stream.XMLStreamException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
//import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

/**
 *
* 
 * @author jbuhacoff
 */
public class TrustAgentSecureClientTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrustAgentSecureClientTest.class);

    private JAXB jaxb = new JAXB();
    public TrustAgentSecureClientTest() {
        
    }

    private void sendIdentityRequest(String hostname, int port) throws UnknownHostException, IOException, JAXBException, KeyManagementException, NoSuchAlgorithmException, XMLStreamException {
        System.out.println("Sending Generate Identity");
        //byte[] data = "<identity_request></identity_request>".getBytes();
        HostAgentFactory factory = new HostAgentFactory();
        TlsPolicy tlsPolicy = new InsecureTlsPolicy(); //factory.getTlsPolicy("INSECURE", new ByteArrayResource());
        TrustAgentSecureClient client = new TrustAgentSecureClient(new TlsConnection(new URL(String.format("https://%s:%d",hostname,port)), tlsPolicy));
//        TrustAgentSecureClient client = new TrustAgentSecureClient(hostname, port, data);
 // TODO ... need client.setData(data)
                client.sendQuoteRequest();
    }
    
    /**
     * For example, this nonce:  M3/53nFiX1+epM+pEmdExIPVxKM=
     * prints like this:
     * Nonce: 51 127 249 222 113 98 95 95 158 164 207 169 18 103 68 196 10 1 71 167
     * 
     * Notice last 4 bytes are 10.1.71.167 the ip address of the host
     */
    private void printNonce(byte[] nonce) {
        System.out.print("Nonce: ");
        for(int i=0; i<nonce.length; i++) {
            System.out.print(String.format("%d ", nonce[i] < 0 ? nonce[i]+256 : nonce[i]));
        }
        System.out.println();
    }
    
    /**
     * Sample output:
     * 
Sending Generate Quote
20:15:55.086 [main] DEBUG com.intel.mtwilson.MyConfiguration - Loaded configuration keys from system: java.vm.version, java.vendor.url, sun.jnu.encoding, test, java.vm.info, user.dir, sun.cpu.isalist, java.awt.graphicsenv, sun.os.patch.level, java.io.tmpdir, user.home, java.awt.printerjob, java.version, file.encoding.pkg, java.vendor.url.bug, file.encoding, line.separator, sun.java.command, java.vm.specification.vendor, java.vm.vendor, java.class.path, sun.io.unicode.encoding, user.variant, os.arch, user.name, user.language, java.runtime.version, sun.boot.class.path, sun.desktop, sun.cpu.endian, awt.toolkit, sun.boot.library.path, surefire.real.class.path, java.vm.name, java.home, java.endorsed.dirs, basedir, sun.management.compiler, java.runtime.name, java.library.path, file.separator, java.specification.vendor, java.vm.specification.version, sun.java.launcher, user.timezone, os.name, path.separator, java.ext.dirs, sun.arch.data.model, java.specification.name, os.version, user.script, user.country, java.class.version, java.vendor, java.vm.specification.name, localRepository, surefire.test.class.path, java.specification.version
20:15:55.096 [main] DEBUG com.intel.mtwilson.MyConfiguration - Loaded configuration keys from environment: USERPROFILE, PROGRAMDATA, JAVA_HOME, MAVEN_CMD_LINE_ARGS, VS110COMNTOOLS, COMMONPROGRAMFILES, DEFLOGDIR, PROCESSOR_REVISION, USERDOMAIN, ALLUSERSPROFILE, ECCLIENT, PROGRAMW6432, VBOX_INSTALL_PATH, OPENSSL_CONF, SESSIONNAME, TMP, PSMODULEPATH, NB_EXEC_MAVEN_PROCESS_UUID, M2_HOME, =::, LOGONSERVER, PATH, PROMPT, PROCESSOR_LEVEL, COMMONPROGRAMW6432, USERDOMAIN_ROAMINGPROFILE, LOCALAPPDATA, COMPUTERNAME, CLASSWORLDS_LAUNCHER, USERNAME, WINDOWS_TRACING_FLAGS, WSSITE, INTELLOGS, APPDATA, WINDIR, PATHEXT, USERDNSDOMAIN, MAVEN_HOME, PROGRAMFILES(X86), WINDOWS_TRACING_LOGFILE, TEMP, HOMEDRIVE, SYSTEMDRIVE, =C:, UATDATA, COMMONPROGRAMFILES(X86), ERROR_CODE, CLASSWORLDS_JAR, VSEDEFLOGDIR, PROCESSOR_IDENTIFIER, GLASSFISH_HOME, PROCESSOR_ARCHITECTURE, MAVEN_JAVA_EXE, OS, FP_NO_HOST_CHECK, PROCDIRLOG, HOMEPATH, COMSPEC, ANT_HOME, PROGRAMFILES, HOME, NUMBER_OF_PROCESSORS, PUBLIC, SYSTEMROOT
20:15:55.106 [main] DEBUG com.intel.mtwilson.MyConfiguration - FILE C:\Users\jbuhacof\.mtwilson\mtwilson.properties IS IN REGULAR PROPERTIES FORMAT
20:15:55.130 [main] DEBUG com.intel.mtwilson.MyConfiguration - Loaded configuration keys from file:C:\Users\jbuhacof\.mtwilson\mtwilson.properties: mtwilson.as.dek, mtwilson.db.port, mtwilson.api.username, mtwilson.db.user, mtwilson.api.password, mtwilson.db.schema, mtwilson.api.url, mtwilson.db.password, mtwilson.api.roles, mtwilson.db.host, mtwilson.db.protocol, mtwilson.api.ssl.policy, mtwilson.default.tls.policy.id, mtwilson.tls.keystore.password, mtwilson.locales, mtwilson.auto.refresh.trust.interval, mtwilson.atag.html5.dir, mtwilson.atag.url, mtwilson.atag.keystore, mtwilson.atag.keystore.password, mtwilson.atag.key.password, mtwilson.atag.api.username, mtwilson.atag.api.password, mtwilson.dev.html5
20:15:55.131 [main] DEBUG com.intel.mtwilson.MyConfiguration - FILE C:\Intel\CloudSecurity\management-service.properties IS IN REGULAR PROPERTIES FORMAT
20:15:55.134 [main] DEBUG com.intel.mtwilson.MyConfiguration - Loaded configuration keys from file:C:\Intel\CloudSecurity\management-service.properties: mountwilson.ms.db.host, mountwilson.ms.db.port, mountwilson.ms.db.schema, mountwilson.ms.db.user, mountwilson.ms.db.password, mtwilson.ssl.required, mountwilson.ms.saml.certificate
20:15:55.135 [main] DEBUG com.intel.mtwilson.MyConfiguration - FILE C:\Intel\CloudSecurity\attestation-service.properties IS IN REGULAR PROPERTIES FORMAT
20:15:55.138 [main] DEBUG com.intel.mtwilson.MyConfiguration - Loaded configuration keys from file:C:\Intel\CloudSecurity\attestation-service.properties: com.intel.mountwilson.as.home, com.intel.mountwilson.as.aikqverify.cmd, com.intel.mountwilson.as.openssl.cmd, saml.issuer, keystore-path, keystore, storepass, alias, keypass, keyalg, keysize, saml.keystore.file, saml.keystore.password, saml.key.alias, saml.key.password, saml.validity.seconds, mtwilson.ssl.required, mtwilson.as.dek
20:15:55.167 [main] DEBUG c.i.m.a.intel.TrustAgentSecureClient - TrustAgentSecureClient  hostname(10.1.71.167) port(9999)
20:15:55.224 [main] DEBUG c.i.mtwilson.agent.intel.TAHelper - Nonce Generated oenSL665gy9J8mZdLfJ/4BrufdE=
20:15:55.259 [main] DEBUG c.i.mtwilson.agent.intel.TAHelper - Session Id Generated [2059533862]
20:15:55.259 [main] INFO  c.i.m.a.intel.TrustAgentSecureClient - Sending Generate Identity
20:15:55.529 [main] INFO  c.i.m.a.intel.TrustAgentSecureClient - About to start reading/writing to/from socket.
20:15:55.530 [main] DEBUG c.i.m.a.intel.TrustAgentSecureClient - Writing: <identity_request></identity_request>
20:16:00.483 [main] DEBUG c.i.m.a.intel.TrustAgentSecureClient - Received 1219 bytes to server and received them back again, msg = <client_request> <timestamp>Tue Dec 03 20:22:24 PST 2013</timestamp><clientIp>192.168.122.1</clientIp><error_code>0</error_code><error_message>OK</error_message><aikcert>-----BEGIN CERTIFICATE-----
  MIICuzCCAaOgAwIBAgIGAUKYF5HrMA0GCSqGSIb3DQEBBQUAMBkxFzAVBgNVBAMMDkhJU19Qcml2
  YWN5X0NBMB4XDTEzMTEyNzA1NDU1MFoXDTIzMTEyNzA1NDU1MFowADCCASIwDQYJKoZIhvcNAQEB
  BQADggEPADCCAQoCggEBALxcv6QbIwaC29A6iC6TCymjHRMsZouM5njsQH19exG58V+fDUrIp1xa
  Lged2X5RJZsiJP8IoiqFYgdXa76PEIZrG/3t1S+D7GZX4/acvLduqCc9+692xBGzEtktlEHLm4la
  kFlUNsK8UXcT1twe78D7cuEnkgIfSkh3mMyt4PhDYzAMdHOj9AccIPD9F3vFN3/0DBpCzwJolw/v
  a4/Gwn0LOiuluvGW9q+9l9xjGFL6NxRF7LzU/J8yJvzl7NUFNE4oLnQr3FMn5rJKIXOfPQ911+Yt
  TI1hvV/2aH0RWxcozt6SQAO2iYLvbmOIv0E5unWWNMnPc0wa5jYMO8X1mbcCAwEAAaMiMCAwHgYD
  VR0RAQH/BBQwEoEQSElTIElkZW50aXR5IEtleTANBgkqhkiG9w0BAQUFAAOCAQEAcC0P4E+mw7JX
  i9JTroDi2T6oTZ13UhuokkXMmyeBO/wZys+K4UIfpDAoJOHZ239fhm7vRHaN5ouobLQhg4Zp4izi
  HQMOLOlnBtyFLGZuI4AJzdhhOhgxRDBy88B6UyqpxQtw8Jk2EgZfb5iKspbqistD6lq1c1p8hXdr
  f/fJ6tivWTMyH2A6KqN1pIWgIhKIvep+OpyujjAd49CFkFRjZNUeby98rlu6EhiuzLorTjx7cS97
  IHVmtcpM/3Tg94RAbJIcHRu+6TT28vLTkNPa3qLpnRYUy7asPh/IEZSKvsjZsPkrPuVNIqysc1el
  aUikHZJg1iCHO5uxDag9xETiJQ==
  -----END CERTIFICATE-----
  </aikcert></client_request>                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     
20:16:00.598 [main] INFO  c.i.m.a.intel.TrustAgentSecureClient - Unmarshalling to Jaxb object.
20:16:00.954 [main] ERROR c.i.m.a.intel.TrustAgentSecureClient - Trust Agent Error 0 [192.168.122.1]: OK
20:16:00.954 [main] INFO  c.i.m.a.intel.TrustAgentSecureClient - Done reading/writing to/from socket, closing socket.
20:16:00.993 [main] DEBUG c.i.m.a.intel.TrustAgentSecureClient - Quote request XML <?xml version="1.0" encoding="UTF-8" standalone="yes"?><quote_request><nonce>oenSL665gy9J8mZdLfJ/4BrufdE=</nonce><pcr_list>0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23</pcr_list></quote_request>
20:16:01.026 [main] INFO  c.i.m.a.intel.TrustAgentSecureClient - About to start reading/writing to/from socket.
20:16:01.028 [main] DEBUG c.i.m.a.intel.TrustAgentSecureClient - Writing: <?xml version="1.0" encoding="UTF-8" standalone="yes"?><quote_request><nonce>oenSL665gy9J8mZdLfJ/4BrufdE=</nonce><pcr_list>0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23</pcr_list></quote_request>
20:16:09.056 [main] DEBUG c.i.m.a.intel.TrustAgentSecureClient - Received 2747 bytes to server and received them back again, msg = <client_request> <timestamp>Tue Dec 03 20:22:33 PST 2013</timestamp><clientIp>192.168.122.1</clientIp><error_code>0</error_code><error_message>OK</error_message><aikcert>-----BEGIN CERTIFICATE-----
  MIICuzCCAaOgAwIBAgIGAUKYF5HrMA0GCSqGSIb3DQEBBQUAMBkxFzAVBgNVBAMMDkhJU19Qcml2
  YWN5X0NBMB4XDTEzMTEyNzA1NDU1MFoXDTIzMTEyNzA1NDU1MFowADCCASIwDQYJKoZIhvcNAQEB
  BQADggEPADCCAQoCggEBALxcv6QbIwaC29A6iC6TCymjHRMsZouM5njsQH19exG58V+fDUrIp1xa
  Lged2X5RJZsiJP8IoiqFYgdXa76PEIZrG/3t1S+D7GZX4/acvLduqCc9+692xBGzEtktlEHLm4la
  kFlUNsK8UXcT1twe78D7cuEnkgIfSkh3mMyt4PhDYzAMdHOj9AccIPD9F3vFN3/0DBpCzwJolw/v
  a4/Gwn0LOiuluvGW9q+9l9xjGFL6NxRF7LzU/J8yJvzl7NUFNE4oLnQr3FMn5rJKIXOfPQ911+Yt
  TI1hvV/2aH0RWxcozt6SQAO2iYLvbmOIv0E5unWWNMnPc0wa5jYMO8X1mbcCAwEAAaMiMCAwHgYD
  VR0RAQH/BBQwEoEQSElTIElkZW50aXR5IEtleTANBgkqhkiG9w0BAQUFAAOCAQEAcC0P4E+mw7JX
  i9JTroDi2T6oTZ13UhuokkXMmyeBO/wZys+K4UIfpDAoJOHZ239fhm7vRHaN5ouobLQhg4Zp4izi
  HQMOLOlnBtyFLGZuI4AJzdhhOhgxRDBy88B6UyqpxQtw8Jk2EgZfb5iKspbqistD6lq1c1p8hXdr
  f/fJ6tivWTMyH2A6KqN1pIWgIhKIvep+OpyujjAd49CFkFRjZNUeby98rlu6EhiuzLorTjx7cS97
  IHVmtcpM/3Tg94RAbJIcHRu+6TT28vLTkNPa3qLpnRYUy7asPh/IEZSKvsjZsPkrPuVNIqysc1el
  aUikHZJg1iCHO5uxDag9xETiJQ==
  -----END CERTIFICATE-----
  </aikcert><quote>AAP///8AAAHgiR6wtVa4P87xwQ8/pkZDReNPj5E6P3gPEaS0mWn8qoDNbjlXwzsida6OANejVsuKNFboNmiEafsEaZZlOj94DxGktJlp/KqAzW45V8M7InUWTzxnux1QwGt6v3fJMMcbbXLFErBCM6468Qe1YYc7jUkaqKS5yW/MOj94DxGktJlp/KqAzW45V8M7InU6P3gPEaS0mWn8qoDNbjlXwzsidQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAL/D/9eUDpKBo+v9+k4EEoaaP1XYvZy2t8YVUv9vHaMAWBfY7lh+fuj+Tx4Ny4+jjA7n7/V4/4Rf9UPARgAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAVO1rDufjy1qHPcQ7510AMqdPlCBD7HZ0PXbHDbM5qUV97yMvqvMBnnZuRaiX+jligFLvChXnlCY6LntNCIoQMX6KrcYX9YsS04TJ3+z6sWVRcm2LWwUDhR9R6cTzvx/zMz0VkkVEG6hRGXTxcZONmlBUWAgIOp9Uugs3KFyL4zX+xthYSDFKZKWYXVIRLUi8mOnPrp24enzwXIZh+Y2WqVCKROhIlf2A1gg3/HwagXcT3kau999pE0vGC0fMwfneEH0WcL0DGAaa6Vj4IcskXfOHXD966U7W8/muqJt98HtD7E5A+cRGK3tatOy58PtHlzMjejHEZ31sJ34b+PcRJw==</quote><eventLog>PG1vZHVsZXM+PG1vZHVsZT48cGNyTnVtYmVyPjE3PC9wY3JOdW1iZXI+PG5hbWU+dGJfcG9saWN5PC9uYW1lPjx2YWx1ZT45NzA0MzUzNjMwNjc0YmZlMjFiODZiNjRhN2IwZjk5YzI5N2NmOTAyPC92YWx1ZT48L21vZHVsZT48bW9kdWxlPjxwY3JOdW1iZXI+MTg8L3Bjck51bWJlcj48bmFtZT52bWxpbnV6PC9uYW1lPjx2YWx1ZT5kMDYwMDcwNzUwNmYxYzY3ODRlNGNjNDFiY2RlMjk4ODAzODA4NmZiPC92YWx1ZT48L21vZHVsZT48bW9kdWxlPjxwY3JOdW1iZXI+MTk8L3Bjck51bWJlcj48bmFtZT5pbml0cmQ8L25hbWU+PHZhbHVlPmNiNjk1MTBlODVmZDgxOWVjZmVkYmQyY2VlYzIxOGNlYTk5Zjk4Mzc8L3ZhbHVlPjwvbW9kdWxlPjwvbW9kdWxlcz4=</eventLog></client_request>                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             
20:16:09.062 [main] INFO  c.i.m.a.intel.TrustAgentSecureClient - Unmarshalling to Jaxb object.
20:16:09.087 [main] ERROR c.i.m.a.intel.TrustAgentSecureClient - Trust Agent Error 0 [192.168.122.1]: OK
20:16:09.087 [main] INFO  c.i.m.a.intel.TrustAgentSecureClient - Done reading/writing to/from socket, closing socket.
20:16:09.087 [main] INFO  c.i.m.a.intel.TrustAgentSecureClient - Got quote from server
20:16:09.087 [main] DEBUG c.i.mtwilson.agent.intel.TAHelper - got response from server [10.1.71.167] com.intel.mountwilson.ta.data.ClientRequestType@456a973f
20:16:09.088 [main] DEBUG c.i.mtwilson.agent.intel.TAHelper - extracted quote from response: AAP///8AAAHgiR6wtVa4P87xwQ8/pkZDReNPj5E6P3gPEaS0mWn8qoDNbjlXwzsida6OANejVsuKNFboNmiEafsEaZZlOj94DxGktJlp/KqAzW45V8M7InUWTzxnux1QwGt6v3fJMMcbbXLFErBCM6468Qe1YYc7jUkaqKS5yW/MOj94DxGktJlp/KqAzW45V8M7InU6P3gPEaS0mWn8qoDNbjlXwzsidQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAL/D/9eUDpKBo+v9+k4EEoaaP1XYvZy2t8YVUv9vHaMAWBfY7lh+fuj+Tx4Ny4+jjA7n7/V4/4Rf9UPARgAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAVO1rDufjy1qHPcQ7510AMqdPlCBD7HZ0PXbHDbM5qUV97yMvqvMBnnZuRaiX+jligFLvChXnlCY6LntNCIoQMX6KrcYX9YsS04TJ3+z6sWVRcm2LWwUDhR9R6cTzvx/zMz0VkkVEG6hRGXTxcZONmlBUWAgIOp9Uugs3KFyL4zX+xthYSDFKZKWYXVIRLUi8mOnPrp24enzwXIZh+Y2WqVCKROhIlf2A1gg3/HwagXcT3kau999pE0vGC0fMwfneEH0WcL0DGAaa6Vj4IcskXfOHXD966U7W8/muqJt98HtD7E5A+cRGK3tatOy58PtHlzMjejHEZ31sJ34b+PcRJw==
20:16:09.089 [main] DEBUG c.i.mtwilson.agent.intel.TAHelper - saving file quote_2059533862.data to [C:/Intel/CloudSecurity/AttestationServiceData/aikverifyhome\data]
20:16:09.105 [main] DEBUG c.i.mtwilson.agent.intel.TAHelper - saved quote with session id: 2059533862
20:16:09.136 [main] DEBUG c.i.mtwilson.agent.intel.TAHelper - saved database-provided trusted AIK certificate with session id: 2059533862
20:16:09.137 [main] DEBUG c.i.mtwilson.agent.intel.TAHelper - saving file nonce_2059533862.data to [C:/Intel/CloudSecurity/AttestationServiceData/aikverifyhome\data]
20:16:09.153 [main] DEBUG c.i.mtwilson.agent.intel.TAHelper - saved nonce with session id: 2059533862
20:16:09.177 [main] DEBUG c.i.mtwilson.agent.intel.TAHelper - created RSA key file for session id: 2059533862
20:16:09.178 [main] DEBUG c.i.mtwilson.agent.intel.TAHelper - Event log: PG1vZHVsZXM+PG1vZHVsZT48cGNyTnVtYmVyPjE3PC9wY3JOdW1iZXI+PG5hbWU+dGJfcG9saWN5PC9uYW1lPjx2YWx1ZT45NzA0MzUzNjMwNjc0YmZlMjFiODZiNjRhN2IwZjk5YzI5N2NmOTAyPC92YWx1ZT48L21vZHVsZT48bW9kdWxlPjxwY3JOdW1iZXI+MTg8L3Bjck51bWJlcj48bmFtZT52bWxpbnV6PC9uYW1lPjx2YWx1ZT5kMDYwMDcwNzUwNmYxYzY3ODRlNGNjNDFiY2RlMjk4ODAzODA4NmZiPC92YWx1ZT48L21vZHVsZT48bW9kdWxlPjxwY3JOdW1iZXI+MTk8L3Bjck51bWJlcj48bmFtZT5pbml0cmQ8L25hbWU+PHZhbHVlPmNiNjk1MTBlODVmZDgxOWVjZmVkYmQyY2VlYzIxOGNlYTk5Zjk4Mzc8L3ZhbHVlPjwvbW9kdWxlPjwvbW9kdWxlcz4=
20:16:09.179 [main] DEBUG c.i.mtwilson.agent.intel.TAHelper - Decoded event log length: 371
20:16:09.179 [main] DEBUG c.i.mtwilson.agent.intel.TAHelper - Event log retrieved from the host consists of: <modules><module><pcrNumber>17</pcrNumber><name>tb_policy</name><value>9704353630674bfe21b86b64a7b0f99c297cf902</value></module><module><pcrNumber>18</pcrNumber><name>vmlinuz</name><value>d0600707506f1c6784e4cc41bcde2988038086fb</value></module><module><pcrNumber>19</pcrNumber><name>initrd</name><value>cb69510e85fd819ecfedbd2ceec218cea99f9837</value></module></modules>
20:16:09.205 [main] DEBUG c.i.mtwilson.agent.intel.TAHelper - verifyQuoteAndGetPcr for session 2059533862
20:16:09.206 [main] DEBUG c.i.mtwilson.agent.intel.TAHelper - Command: C:/Intel/CloudSecurity/AttestationServiceData/aikverifyhome\bin\aikqverify.exe -c C:/Intel/CloudSecurity/AttestationServiceData/aikverifyhome\data\nonce_2059533862.data C:/Intel/CloudSecurity/AttestationServiceData/aikverifyhome\data\rsapubkey_2059533862.key C:/Intel/CloudSecurity/AttestationServiceData/aikverifyhome\data\quote_2059533862.data
Exception during testIdentityRequest: TPM quote verification failed.Command error code 2
     * 
     * 
     */
    private void sendQuoteRequest(String hostname, int port) throws UnknownHostException, IOException, JAXBException, KeyManagementException, NoSuchAlgorithmException, XMLStreamException, PropertyException, CertificateException {
        System.out.println("Sending Generate Quote");
        //byte[] data = "<quote_request><nonce>Iamnonce</nonce><pcr_list>3,19</pcr_list></quote_request>".getBytes();
        //             data = "<quote_request><nonce>+nao5lHKxcMoqIGY3LuAYQ==</nonce><pcr_list>3-5,4-8</pcr_list></quote_request>".getBytes();
        HostAgentFactory factory = new HostAgentFactory();
        TlsPolicy tlsPolicy = new InsecureTlsPolicy(); //factory.getTlsPolicy("INSECURE", new ByteArrayResource());
        TrustAgentSecureClient client = new TrustAgentSecureClient(new TlsConnection(new URL(String.format("https://%s:%d",hostname,port)), tlsPolicy));
        /* this works to get the quote response, but doesn't verify it */
        /*
        TAHelper helper = new TAHelper();
        byte[] nonce = helper.generateNonce();
        printNonce(nonce);
        String nonceBase64 = Base64.encodeBase64String(nonce);
        ClientRequestType response = client.getQuote(nonceBase64, "0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23");
        log.debug("Response: {}", jaxb.write(response));
        */
        TAHelper helper = new TAHelper();
        helper.setDeleteTemporaryFiles(false); // keep them so we can check them out after processing
        PcrManifest manifest = helper.getQuoteInformationForHost(hostname, client);
        ObjectMapper mapper = new ObjectMapper();
        log.debug("Response: {}", mapper.writeValueAsString(manifest));
    }
    
    @Test
    public void testPrintQuoteInfo() throws FileNotFoundException, IOException {
        String sessionId = "295415201"; // get it from the output of the sendQuoteRequest test
        String homedir = ASConfig.getConfiguration().getString("com.intel.mountwilson.as.home", "C:/work/aikverifyhome"); // just like in TAHelper
        File nonceFile = new File(homedir +File.separator + "data" + File.separator + "nonce_" +  sessionId + ".data");
        FileInputStream nonceInput = new FileInputStream(nonceFile);
        byte[] nonce = IOUtils.toByteArray(nonceInput);
        nonceInput.close();
        printNonce(nonce);
    }
    
    @Test
    public void testIdentityRequest() {
        try {
            sendIdentityRequest("10.1.71.145", 9999);
        }
        catch(Exception e) {
            System.err.println("Exception during testIdentityRequest: "+e.toString());
        }
    }

    /**
     * Sample request:
     * <?xml version="1.0" encoding="UTF-8" standalone="yes"?><quote_request><nonce>EchN01e1q2+DTRouiHZaT6Q2t6o=</nonce><pcr_list>0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23</pcr_list></quote_request>
     * 
     * Sample response:
<client_request> <timestamp>Tue Dec 03 20:09:16 PST 2013</timestamp><clientIp>192.168.122.1</clientIp><error_code>0</error_code><error_message>OK</error_message><aikcert>-----BEGIN CERTIFICATE-----
  MIICuzCCAaOgAwIBAgIGAUKYF5HrMA0GCSqGSIb3DQEBBQUAMBkxFzAVBgNVBAMMDkhJU19Qcml2
  YWN5X0NBMB4XDTEzMTEyNzA1NDU1MFoXDTIzMTEyNzA1NDU1MFowADCCASIwDQYJKoZIhvcNAQEB
  BQADggEPADCCAQoCggEBALxcv6QbIwaC29A6iC6TCymjHRMsZouM5njsQH19exG58V+fDUrIp1xa
  Lged2X5RJZsiJP8IoiqFYgdXa76PEIZrG/3t1S+D7GZX4/acvLduqCc9+692xBGzEtktlEHLm4la
  kFlUNsK8UXcT1twe78D7cuEnkgIfSkh3mMyt4PhDYzAMdHOj9AccIPD9F3vFN3/0DBpCzwJolw/v
  a4/Gwn0LOiuluvGW9q+9l9xjGFL6NxRF7LzU/J8yJvzl7NUFNE4oLnQr3FMn5rJKIXOfPQ911+Yt
  TI1hvV/2aH0RWxcozt6SQAO2iYLvbmOIv0E5unWWNMnPc0wa5jYMO8X1mbcCAwEAAaMiMCAwHgYD
  VR0RAQH/BBQwEoEQSElTIElkZW50aXR5IEtleTANBgkqhkiG9w0BAQUFAAOCAQEAcC0P4E+mw7JX
  i9JTroDi2T6oTZ13UhuokkXMmyeBO/wZys+K4UIfpDAoJOHZ239fhm7vRHaN5ouobLQhg4Zp4izi
  HQMOLOlnBtyFLGZuI4AJzdhhOhgxRDBy88B6UyqpxQtw8Jk2EgZfb5iKspbqistD6lq1c1p8hXdr
  f/fJ6tivWTMyH2A6KqN1pIWgIhKIvep+OpyujjAd49CFkFRjZNUeby98rlu6EhiuzLorTjx7cS97
  IHVmtcpM/3Tg94RAbJIcHRu+6TT28vLTkNPa3qLpnRYUy7asPh/IEZSKvsjZsPkrPuVNIqysc1el
  aUikHZJg1iCHO5uxDag9xETiJQ==
  -----END CERTIFICATE-----
  </aikcert><quote>AAP///8AAAHgiR6wtVa4P87xwQ8/pkZDReNPj5E6P3gPEaS0mWn8qoDNbjlXwzsida6OANejVsuKNFboNmiEafsEaZZlOj94DxGktJlp/KqAzW45V8M7InUWTzxnux1QwGt6v3fJMMcbbXLFErBCM6468Qe1YYc7jUkaqKS5yW/MOj94DxGktJlp/KqAzW45V8M7InU6P3gPEaS0mWn8qoDNbjlXwzsidQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAL/D/9eUDpKBo+v9+k4EEoaaP1XYvZy2t8YVUv9vHaMAWBfY7lh+fuj+Tx4Ny4+jjA7n7/V4/4Rf9UPARgAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAXLpz7VPHNABBbucSX6owbwXlJs9cpYqAiVpoUeAaL9CxTTNbDcJCtBYTUnMybLCCEW7y5ValvVzj/FbhR+Id7IdelUSqt9CfA6DOCycfYP0LsQeH7jclEnn1wAiX9sE1LdPTpyejR2/P7y6fxVvvsW47kmCemoesz0jexSmUyHzaj88XePemoM7PSiyCv0mdKNjuoorrCfUbrCmaY0PLjwwk1BaZ+ENcaapbt1uYAFS8T2W1KSJN4C5sTdCWLL/uJktNJHKInj34P3m3kvZ3Mq0ee1BgTA8f2y9eIyMkYdSxHuLOHIqkEXCtPRmj2JAvIswZBH+O4HQINKOAYjZyUA==</quote><eventLog>PG1vZHVsZXM+PG1vZHVsZT48cGNyTnVtYmVyPjE3PC9wY3JOdW1iZXI+PG5hbWU+dGJfcG9saWN5PC9uYW1lPjx2YWx1ZT45NzA0MzUzNjMwNjc0YmZlMjFiODZiNjRhN2IwZjk5YzI5N2NmOTAyPC92YWx1ZT48L21vZHVsZT48bW9kdWxlPjxwY3JOdW1iZXI+MTg8L3Bjck51bWJlcj48bmFtZT52bWxpbnV6PC9uYW1lPjx2YWx1ZT5kMDYwMDcwNzUwNmYxYzY3ODRlNGNjNDFiY2RlMjk4ODAzODA4NmZiPC92YWx1ZT48L21vZHVsZT48bW9kdWxlPjxwY3JOdW1iZXI+MTk8L3Bjck51bWJlcj48bmFtZT5pbml0cmQ8L25hbWU+PHZhbHVlPmNiNjk1MTBlODVmZDgxOWVjZmVkYmQyY2VlYzIxOGNlYTk5Zjk4Mzc8L3ZhbHVlPjwvbW9kdWxlPjwvbW9kdWxlcz4=</eventLog></client_request>                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             
     * 
     */
    @Test
    public void testQuoteRequest() {
        try {
            sendQuoteRequest("10.1.71.167", 9999);
        }
        catch(Exception e) {
            System.err.println("Exception during testIdentityRequest: "+e.toString());
        }
    }
    
    @Test
    public void testTASecureClient() {

//        String hostname = "10.1.71.96"; // ubuntu 
//        String hostname = "10.1.130.152"; // trust agent, seems to work fine
        String hostname = "10.1.71.145";
        int port = 9999;
        try {

//            System.out.println("Sending BAD request");
//            byte[] data = "<client_request></client_request>".getBytes();
//            TrustAgentSecureClient client = new TrustAgentSecureClient(hostname, port, data);
//            client.sendRequest();
//
//            System.out.println("Sending Generate Identity");
//            data = "<identity_request></identity_request>".getBytes();
//            client = new TrustAgentSecureClient(hostname, port, data);
//            client.sendRequest();
//
            System.out.println("Sending Generate Quote");
            //byte[] data;
//             data = "<quote_request><nonce>+nao5lHKxcMoqIGY3LuAYQ==</nonce><pcr_list>3-5,4-8</pcr_list></quote_request>".getBytes();
             //data = "<quote_request><nonce>Iamnonce</nonce><pcr_list>3,19</pcr_list></quote_request>".getBytes();
        HostAgentFactory factory = new HostAgentFactory();
        TlsPolicy tlsPolicy = new InsecureTlsPolicy(); //factory.getTlsPolicy("INSECURE", new ByteArrayResource());
        TrustAgentSecureClient client = new TrustAgentSecureClient(new TlsConnection(new URL(String.format("https://%s:%d",hostname,port)), tlsPolicy));
//            TrustAgentSecureClient client = new TrustAgentSecureClient(hostname, port, data);
        // TODO ... need client.setData(data)
            client.sendQuoteRequest();

//            System.out.println("Result " + new TrustAgentSecureClient(hostname, port, null).getAIKCertificate());

        } catch (Throwable e) {
//            log.info("Error while contacting Trust Agent " + e.getMessage());
            e.printStackTrace();
        }
    }
}
