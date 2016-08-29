package com.intel.mtwilson.agent.intel;

import com.intel.dcsg.cpg.crypto.AbstractDigest;
import com.intel.dcsg.cpg.crypto.DigestAlgorithm;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.dcsg.cpg.tls.policy.TlsConnection;
import com.intel.dcsg.cpg.tls.policy.TlsPolicy;
import com.intel.dcsg.cpg.io.ByteArray;
import com.intel.dcsg.cpg.net.IPv4Address;
import com.intel.dcsg.cpg.net.InternetAddress;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.IOUtils;

import com.intel.mountwilson.as.common.ASConfig;
import com.intel.mountwilson.as.common.ASException;
import com.intel.mountwilson.as.helper.CommandUtil;
import com.intel.mountwilson.ta.data.ClientRequestType;
import com.intel.mountwilson.ta.data.daa.response.DaaResponse;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.dcsg.cpg.x509.X509Util;
import com.intel.mtwilson.i18n.ErrorCode;
import com.intel.mtwilson.model.Measurement;
import com.intel.mtwilson.model.Pcr;
import com.intel.mtwilson.model.PcrEventLog;
import com.intel.mtwilson.model.PcrIndex;
import com.intel.mtwilson.model.PcrManifest;
import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.dcsg.cpg.crypto.Sha256Digest;
import com.intel.dcsg.cpg.io.Platform;
import com.intel.mtwilson.Folders;
import com.intel.mtwilson.My;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.model.MeasurementSha1;
import com.intel.mtwilson.model.MeasurementSha256;
import com.intel.mtwilson.model.Nonce;
import com.intel.mtwilson.model.PcrEventLogFactory;
import com.intel.mtwilson.model.PcrFactory;
import com.intel.mtwilson.tls.policy.factory.V1TlsPolicyFactory;
import com.intel.mtwilson.trustagent.client.jaxrs.TrustAgentClient;
import com.intel.mtwilson.trustagent.model.HostInfo;
import com.intel.mtwilson.trustagent.model.TpmQuoteResponse;
import com.intel.mtwilson.util.exec.EscapeUtil;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Map;
import javax.xml.bind.PropertyException;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.shiro.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * In order to use the TAHelper, you need to have attestation-service.properties
 * on your machine.
 *
 * Here are example properties that Jonathan has at
 * C:/Intel/CloudSecurity/attestation-service.properties: *
 * com.intel.mountwilson.as.home=C:/Intel/CloudSecurity/AttestationServiceData/aikverifyhome
 * com.intel.mountwilson.as.aikqverify.cmd=aikqverify.exe
 * com.intel.mountwilson.as.openssl.cmd=openssl.bat
 *
 * The corresponding files must exist. From the above example:
 *
 * C:/Intel/CloudSecurity/AttestationServiceData/aikverifyhome
 * C:/Intel/CloudSecurity/AttestationServiceData/aikverifyhome/data (can be
 * empty, TAHelper will save files there)
 * C:/Intel/CloudSecurity/AttestationServiceData/aikverifyhome/bin contains:
 * aikqverify.exe, cygwin1.dll
 *
 * @author dsmagadx
 */
public class TAHelper {

    private Logger log = LoggerFactory.getLogger(getClass());
//    private String aikverifyhome;
    private String aikverifyhomeData;
    private String aikverifyhomeBin;
    private String aikverifyCmd;
    private Pattern pcrNumberPattern = Pattern.compile("[0-9]|[0-1][0-9]|2[0-3]"); // integer 0-23 with optional zero-padding (00, 01, ...)
    private Pattern pcrValuePattern = Pattern.compile("[0-9a-fA-F]+"); // 40-character hex string
    private String pcrNumberUntaint = "[^0-9]";
    private String pcrValueUntaint = "[^0-9a-fA-F]";
    private boolean quoteWithIPAddress = true; // to fix issue #1038 we use this secure default
//	private EntityManagerFactory entityManagerFactory;
    private String trustedAik = null; // host's AIK in PEM format, for use in verifying quotes (caller retrieves it from database and provides it to us)
    private boolean deleteTemporaryFiles = true;  // normally we don't need to keep them around but during debugging it's helpful to set this to false
    private String[] openSourceHostSpecificModules = {"initrd","vmlinuz"};
    private TxtHostRecord host = null;
    boolean isHostWindows = false;


    public TAHelper(/*EntityManagerFactory entityManagerFactory*/) throws IOException {

        // check mtwilson 2.0 configuration first
        String binPath = Folders.features("aikqverify") + File.separator + "bin"; //MyFilesystem.getApplicationFilesystem().getBootstrapFilesystem().getBinPath();
        String varPath = Folders.features("aikqverify") + File.separator + "data"; //Folders.application() + File.separator + "repository" + File.separator + "aikqverify";//MyFilesystem.getApplicationFilesystem().getBootstrapFilesystem().getVarPath() + File.separator + "aikqverify";
        File bin = new File(binPath);
        File var = new File(varPath);
        if (bin.exists() && var.exists()) {
            aikverifyhomeBin = binPath;
            aikverifyhomeData = varPath;
//            opensslCmd = aikverifyhomeBin + File.separator + (Platform.isUnix() ? "openssl" : "openssl.bat"); //My.configuration().getConfiguration().getString("com.intel.mountwilson.as.openssl.cmd", "openssl.bat"));
            aikverifyCmd = aikverifyhomeBin + File.separator + (Platform.isUnix() ? "aikqverify" : "aikqverify.exe");
        } else {
            // mtwilson 1.2 configuration
            Configuration config = ASConfig.getConfiguration();
            String aikverifyhome = config.getString("com.intel.mountwilson.as.home", "C:/work/aikverifyhome");
            aikverifyhomeData = aikverifyhome + File.separator + "data";
            aikverifyhomeBin = aikverifyhome + File.separator + "bin";
//            opensslCmd = aikverifyhomeBin + File.separator + config.getString("com.intel.mountwilson.as.openssl.cmd", "openssl.bat");
            aikverifyCmd = aikverifyhomeBin + File.separator + config.getString("com.intel.mountwilson.as.aikqverify.cmd", "aikqverify.exe");
        }
        quoteWithIPAddress = My.configuration().getConfiguration().getBoolean("mtwilson.tpm.quote.ipv4", true); // issue #1038
        boolean foundAllRequiredFiles = true;
        String required[] = new String[]{aikverifyCmd, aikverifyhomeData};
        for (String filename : required) {
            File file = new File(filename);
            if (!file.exists()) {
                log.warn(String.format("Invalid service configuration: Cannot find %s", filename));
                foundAllRequiredFiles = false;
            }
        }
        if (!foundAllRequiredFiles) {
            throw new ASException(ErrorCode.AS_CONFIGURATION_ERROR, "Cannot find aikverify files");
        }

        // we must be able to write to the data folder in order to save certificates, nones, public keys, etc.
        File datafolder = new File(aikverifyhomeData);
        if (!datafolder.canWrite()) {
            throw new ASException(ErrorCode.AS_CONFIGURATION_ERROR, String.format(" Cannot write to %s", aikverifyhomeData));
        }

        //        this.setEntityManagerFactory(entityManagerFactory);
    }

    /* We need host info to be passed so we can verify the host quote based on the OS and TPM version
     * Based on the host information, the command to call for quote verification will be different
     * These are the 4 combination: Linux/TPM 1.2, Linux/TPM2.0, Windows/TPM1.2, Windows/TPM2.0
     *    
    */
    public TAHelper(TxtHostRecord hostBeingVerified) throws IOException {

        this.host = hostBeingVerified;
        
        log.debug("TA Helper getOsName: " + host.VMM_OSName);
        
        //check if the host is Microsoft Windows
        isHostWindows = host.VMM_OSName.toLowerCase().contains("microsoft");

        
        // check mtwilson 2.0 configuration first
        String binPath = Folders.features("aikqverify") + File.separator + "bin"; //MyFilesystem.getApplicationFilesystem().getBootstrapFilesystem().getBinPath();
        String varPath = Folders.features("aikqverify") + File.separator + "data"; //Folders.application() + File.separator + "repository" + File.separator + "aikqverify";//MyFilesystem.getApplicationFilesystem().getBootstrapFilesystem().getVarPath() + File.separator + "aikqverify";
        File bin = new File(binPath);
        File var = new File(varPath);
        if (bin.exists() && var.exists()) {
            aikverifyhomeBin = binPath;
            aikverifyhomeData = varPath;
//            opensslCmd = aikverifyhomeBin + File.separator + (Platform.isUnix() ? "openssl" : "openssl.bat"); //My.configuration().getConfiguration().getString("com.intel.mountwilson.as.openssl.cmd", "openssl.bat"));
            if (isHostWindows)
                if (host.TpmVersion.equals("2.0"))
                    aikverifyCmd = aikverifyhomeBin + File.separator + "aikqverifywin2";
                else
                    aikverifyCmd = aikverifyhomeBin + File.separator + "aikqverifywin";
            else {
                if (host.TpmVersion.equals("2.0"))
                    aikverifyCmd = aikverifyhomeBin + File.separator + "aikqverify2";
                else
                    aikverifyCmd = aikverifyhomeBin + File.separator + "aikqverify";
            }
        } else {
            // mtwilson 1.2 configuration
            Configuration config = ASConfig.getConfiguration();
            String aikverifyhome = config.getString("com.intel.mountwilson.as.home", "C:/work/aikverifyhome");
            aikverifyhomeData = aikverifyhome + File.separator + "data";
            aikverifyhomeBin = aikverifyhome + File.separator + "bin";
//            opensslCmd = aikverifyhomeBin + File.separator + config.getString("com.intel.mountwilson.as.openssl.cmd", "openssl.bat");
            aikverifyCmd = aikverifyhomeBin + File.separator + config.getString("com.intel.mountwilson.as.aikqverify.cmd", "aikqverify.exe");
        }
        quoteWithIPAddress = My.configuration().getConfiguration().getBoolean("mtwilson.tpm.quote.ipv4", true); // issue #1038
        boolean foundAllRequiredFiles = true;
        String required[] = new String[]{aikverifyCmd, aikverifyhomeData};
        for (String filename : required) {
            File file = new File(filename);
            if (!file.exists()) {
                log.warn(String.format("Invalid service configuration: Cannot find %s", filename));
                foundAllRequiredFiles = false;
            }
        }
        if (!foundAllRequiredFiles) {
            throw new ASException(ErrorCode.AS_CONFIGURATION_ERROR, "Cannot find aikverify files");
        }

        // we must be able to write to the data folder in order to save certificates, nones, public keys, etc.
        File datafolder = new File(aikverifyhomeData);
        if (!datafolder.canWrite()) {
            throw new ASException(ErrorCode.AS_CONFIGURATION_ERROR, String.format(" Cannot write to %s", aikverifyhomeData));
        }

        //        this.setEntityManagerFactory(entityManagerFactory);
    }
    
    public void setTrustedAik(String pem) {
        trustedAik = pem;
    }

    /**
     * The default value of deleteTemporaryFiles is true.
     *
     * @param deleteTemporaryFiles true to delete them, false to keep them after
     * processing
     */
    public void setDeleteTemporaryFiles(boolean deleteTemporaryFiles) {
        this.deleteTemporaryFiles = deleteTemporaryFiles;
    }

    // DAA challenge
    //    public void verifyAikWithDaa(String hostIpAddress, int port) {
    public void verifyAikWithDaa(TblHosts tblHosts) throws XMLStreamException {
        try {
            //            TrustAgentSecureClient client = new TrustAgentSecureClient(hostIpAddress, port); // bug #497
            com.intel.mtwilson.tls.policy.factory.TlsPolicyFactory tlsPolicyFactory = com.intel.mtwilson.tls.policy.factory.TlsPolicyFactory.createFactory(tblHosts);//getTlsPolicyWithKeystore(tlsPolicyName, tlsKeystore);
            TlsPolicy tlsPolicy = tlsPolicyFactory.getTlsPolicy();

            String connectionString = tblHosts.getAddOnConnectionInfo();
            if (connectionString == null || connectionString.isEmpty()) {
                if (tblHosts.getName() != null) {
                    connectionString = String.format("https://%s:%d", tblHosts.getName(), tblHosts.getPort()); // without vendor scheme because we are passing directly to TrustAgentSEcureClient  (instead of to HOstAgentFactory)
                }
            }

            URL url = new URL(connectionString);
            TrustAgentSecureClient client = new TrustAgentSecureClient(new TlsConnection(url, tlsPolicy));

            String sessionId = generateSessionId();

            // request AIK certificate and CA chain (the AIK Proof File)
            log.info("DAA requesting AIK proof");
            String aikproof = client.getAIKCertificate(); // <identity_request></identity_request>
            try(FileOutputStream outAikProof = new FileOutputStream(new File(getDaaAikProofFileName(sessionId)))) {
            IOUtils.write(aikproof, outAikProof);
            }

            // create DAA challenge secret
            SecureRandom random = new SecureRandom();
            byte[] secret = new byte[20];
            random.nextBytes(secret);
            try(FileOutputStream outSecret = new FileOutputStream(new File(getDaaSecretFileName(sessionId)))) {
            IOUtils.write(secret, outSecret);
            }

            // encrypt DAA challenge secret using AIK public key so only TPM can read it
            CommandUtil.runCommand(String.format("aikchallenge %s %s %s %s",
                    EscapeUtil.doubleQuoteEscapeShellArgument(getDaaSecretFileName(sessionId)),
                    EscapeUtil.doubleQuoteEscapeShellArgument(getDaaAikProofFileName(sessionId)),
                    EscapeUtil.doubleQuoteEscapeShellArgument(getDaaChallengeFileName(sessionId)),
                    EscapeUtil.doubleQuoteEscapeShellArgument(getRSAPubkeyFileName(sessionId))), false, "Aik Challenge");

            // send DAA challenge to Trust Agent and validate the response
            try(FileInputStream in = new FileInputStream(new File(getDaaChallengeFileName(sessionId)))) {
            String challenge = IOUtils.toString(in);
            DaaResponse response = client.sendDaaChallenge(challenge);
            byte[] responseContentDecoded = Base64.decodeBase64(response.getContent());
            if (responseContentDecoded.length != secret.length) {
                throw new ASException(ErrorCode.AS_TRUST_AGENT_DAA_ERROR, "Incorrect challenge response");
            }
            for (int i = 0; i < secret.length; i++) {
                if (responseContentDecoded[i] != secret[i]) {
                    throw new ASException(ErrorCode.AS_TRUST_AGENT_DAA_ERROR, "Incorrect challenge response");
                }
            }
            }

        } catch (KeyManagementException | JAXBException | NoSuchAlgorithmException | IOException ex) {
            log.error("Cannot verify AIK: " + ex.getMessage(), ex);
        } catch (ASException ex) {
            throw ex;
        }
    }

    // BUG #497 see  the other getQuoteInformationForHost which is called from IntelHostAgent
    //    public HashMap<String, PcrManifest> getQuoteInformationForHost(String hostIpAddress, String pcrList, String name, int port) {
    public PcrManifest getQuoteInformationForHost(TblHosts tblHosts) {

        try {
            // going to IntelHostAgent directly because 1) we are TAHelper so we know we need intel trust agents,  2) the HostAgent interface isn't ready yet for full generic usage,
            // 3) one day this entire function will be in the IntelHostAgent or that agent will call THIS function instaed of the othe way around
//            HostAgentFactory factory = new HostAgentFactory();

            TlsPolicy tlsPolicy = V1TlsPolicyFactory.getInstance().getTlsPolicyWithKeystore(tblHosts.getTlsPolicyName(), tblHosts.getTlsKeystoreResource());

            String connectionString = tblHosts.getAddOnConnectionInfo();
            if (connectionString == null || connectionString.isEmpty()) {
                if (tblHosts.getName() != null) {
                    // without vendor scheme because we are passing directly to TrustAgentSEcureClient  (instead of to HOstAgentFactory)
                    connectionString = String.format("https://%s:%d", tblHosts.getName(), tblHosts.getPort());
                    log.debug("getQuoteInformationForHost called with ip address and port {}", connectionString);
                }
            } else if (connectionString.startsWith("intel:")) {
                //log.debug("getQuoteInformationForHost called with intel connection string: {}", connectionString);
                connectionString = connectionString.substring(6);
            }


            URL url = new URL(connectionString);
            TrustAgentSecureClient client = new TrustAgentSecureClient(new TlsConnection(url, tlsPolicy));
            //  IntelHostAgent agent = new IntelHostAgent(client, new InternetAddress(tblHosts.getIPAddress().toString()));
            return getQuoteInformationForHost(tblHosts.getName(), client);

        } catch (ASException e) {
            throw e;
        } catch (UnknownHostException e) {
            throw new ASException(e, ErrorCode.AS_HOST_COMMUNICATION_ERROR, "Unknown host: " + (tblHosts.getName() == null ? "missing IP Address" : tblHosts.getName()));
        } catch (Exception e) {
            throw new ASException(e);
        }
    }

    public byte[] getIPAddress(String hostname) throws UnknownHostException {
        byte[] ipaddress;
        InternetAddress address = new InternetAddress(hostname);
        if (address.isIPv4()) {
            IPv4Address ipv4address = new IPv4Address(hostname);
            ipaddress = ipv4address.toByteArray();
            if (ipaddress == null) {
                throw new UnknownHostException(hostname); // throws UnknownHostException
            }
            assert ipaddress.length == 4;
        } else if (address.isIPv6() || address.isHostname()) {
            // resolve it to find the ipv4 address
            InetAddress inetAddress = InetAddress.getByName(hostname); // throws UnknownHostException
            log.info("Resolved hostname {} to address {}", hostname, inetAddress.getHostAddress());
            if (inetAddress instanceof Inet4Address) {
                ipaddress = inetAddress.getAddress();
                assert ipaddress.length == 4;
            } else if (inetAddress instanceof Inet6Address) {
                if (((Inet6Address) inetAddress).isIPv4CompatibleAddress()) {
                    ipaddress = ByteArray.subarray(inetAddress.getAddress(), 12, 4); // the last 4 bytes of of an ipv4-compatible ipv6 address are the ipv4 address (first 12 bytes are zero)
                } else {
                    throw new IllegalArgumentException("mtwilson.tpm.quote.ipv4 is enabled and requires an IPv4-compatible address but host address is IPv6: " + hostname);
                }
            } else {
                throw new IllegalArgumentException("mtwilson.tpm.quote.ipv4 is enabled and requires an IPv4-compatible address but host address is unknown type: " + hostname);
            }
        } else {
            throw new IllegalArgumentException("mtwilson.tpm.quote.ipv4 is enabled and requires an IPv4-compatible address but host address is unknown type: " + hostname);
        }
        return ipaddress;
    }

    public PcrManifest getQuoteInformationForHost(String hostname, TrustAgentSecureClient client) throws NoSuchAlgorithmException, PropertyException, JAXBException,
            UnknownHostException, IOException, KeyManagementException, CertificateException, XMLStreamException {
        return getQuoteInformationForHost(hostname, client, null);
    }
    
    public PcrManifest getQuoteInformationForHost(String hostname, TrustAgentSecureClient client, Nonce challenge) throws NoSuchAlgorithmException, PropertyException, JAXBException,
            UnknownHostException, IOException, KeyManagementException, CertificateException, XMLStreamException {
        //  BUG #497  START CODE SNIPPET MOVED TO INTEL HOST AGENT
        File q;
        File n;
        File c;
        File r;
        byte[] nonce;
        if( challenge == null ) {
            nonce = generateNonce(); // 20 random bytes
        }
        else {
            nonce = challenge.toByteArray(); // issue #4978: use specified nonce, if available
        }

        // to fix issue #1038 we have a new option to put the host ip address in the nonce (we don't send this to the host - the hsot automatically would do the same thing)
        byte[] verifyNonce = nonce; // verifyNonce is what we save to verify against host's tpm quote response
        if (quoteWithIPAddress) {
            // is the hostname a dns name or an ip address?  if it's a dns name we have to resolve it to an ip address
            // see also corresponding code in TrustAgent CreateNonceFileCmd
            byte[] ipaddress = getIPAddress(hostname);
            if (ipaddress == null) {
                throw new IllegalArgumentException("mtwilson.tpm.quote.ipv4 is enabled but host address cannot be resolved: " + hostname);
            }
            verifyNonce = ByteArray.concat(ByteArray.subarray(nonce, 0, 16), ipaddress);
        }
        String verifyNonceBase64 = Base64.encodeBase64String(verifyNonce);

        String sessionId = generateSessionId();

        // FIrst let us ensure that we have an AIK cert created on the host before trying to retrieve the quote. The trust agent
        // would verify if a AIK is already present or not. If not it will create a new one.
        trustedAik = client.getAIKCertificate();

        // to fix issue #1038 trust agent relay we send 20 random bytes nonce to the host (base64-encoded) but if mtwilson.tpm.quote.ipaddress is enabled then in our copy we replace the last 4 bytes with the host's ip address, and when the host generates the quote it does the same thing, and we can verify it later
        String nonceBase64 = Base64.encodeBase64String(nonce);
        ClientRequestType clientRequestType = client.getQuote(nonceBase64, "0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23"); // pcrList used to be a comma-separated list passed to this method... but now we are returning a quote with ALL the PCR's ALL THE TIME.
        log.debug("got response from server [" + hostname + "] " + clientRequestType);

        String quote = clientRequestType.getQuote();
        log.debug("extracted quote from response: " + quote);

        q = saveQuote(quote, sessionId);
        log.debug("saved quote with session id: " + sessionId);

        // we only need to save the certificate when registring the host ... when we are just getting a quote we need to verify it using the previously saved AIK.
        if (trustedAik == null) {
            String aikCertificate = clientRequestType.getAikcert();
            log.debug("extracted aik cert from response: " + aikCertificate);

            c = saveCertificate(aikCertificate, sessionId);
            log.debug("saved host-provided AIK certificate with session id: " + sessionId);
        } else {
            c = saveCertificate(trustedAik, sessionId);
            log.debug("saved database-provided trusted AIK certificate with session id: " + sessionId);
        }

        n = saveNonce(verifyNonceBase64, sessionId);

        log.debug("saved nonce with session id: " + sessionId);

        r = createRSAKeyFile(sessionId);

        log.debug("created RSA key file for session id: " + sessionId);

        // Verify if there is TCBMeasurement Data. This data would be available if we are extending the root of trust to applications and data on the OS
        String tcbMeasurementString = clientRequestType.getTcbMeasurement();
        log.debug("TCB Measurement XML is {}", tcbMeasurementString);

        log.debug("Event log: {}", clientRequestType.getEventLog()); // issue #879
        byte[] eventLogBytes = Base64.decodeBase64(clientRequestType.getEventLog());// issue #879
        log.debug("Decoded event log length: {}", eventLogBytes == null ? null : eventLogBytes.length);// issue #879
        if (eventLogBytes != null) { // issue #879
            String decodedEventLog = new String(eventLogBytes);
            log.debug("Event log retrieved from the host consists of: " + decodedEventLog);

            /*
             * Example output:
             * <pre>
             2014-05-27 11:40:49,089 DEBUG [http-listener-2(15)] c.i.m.a.i.TAHelper [TAHelper.java:464] Event log retrieved from the host consists of: <measureLog><txt><txtStatus>1</txtStatus><osSinitDataCapabilities>00000000</osSinitDataCapabilities><sinitMleData><version>8</version><sinitHash>fee3650237e216da2159d6f4ef85d33b3b8d3bbe</sinitHash><mleHash>03721dc24915743302f9648b7e82c559d5bdfc6b</mleHash><biosAcmId>800000002010120400003400ffffffffffffffff</biosAcmId><msegValid>0</msegValid><stmHash>0000000000000000000000000000000000000000</stmHash><policyControl>00000000</policyControl><lcpPolicyHash>0000000000000000000000000000000000000000</lcpPolicyHash><processorSCRTMStatus>00000001</processorSCRTMStatus><edxSenterFlags>00000000</edxSenterFlags></sinitMleData><modules><module><pcrNumber>17</pcrNumber><name>tb_policy</name><value>c3438497fda827be3b321c5309a204f0c9e53943</value></module><module><pcrNumber>18</pcrNumber><name>xen.gz</name><value>7a7262b37b255ec484c274a6b2e6a94591e2fdce</value></module><module><pcrNumber>19</pcrNumber><name>vmlinuz</name><value>d70e9875afa574c58348f25ef1249671e396cbc6</value></module><module><pcrNumber>19</pcrNumber><name>initrd</name><value>06c2db1b1050a3347f3f69616f9735da2089effa</value></module><module><pcrNumber>22</pcrNumber><name>asset-tag</name><value>4fd9fdab06ce10c7360b87478232cc3fc1a45249</value></module></modules></txt></measureLog>]]
             *          * </pre>
             *
             */

            // Since we need to add the event log details into the pcrManifest, we will pass in that information to the below function
            PcrManifest pcrManifest = verifyQuoteAndGetPcr(sessionId, decodedEventLog);
            log.info("Got PCR map");
            if (tcbMeasurementString != null && !tcbMeasurementString.isEmpty())
                pcrManifest.setMeasurementXml(tcbMeasurementString);

            //log.log(Level.INFO, "PCR map = "+pcrMap); // need to untaint this first
            if (deleteTemporaryFiles) {
                q.delete();
                n.delete();
                c.delete();
                r.delete();
            }
            return pcrManifest;
        } else {
            PcrManifest pcrManifest = verifyQuoteAndGetPcr(sessionId, null); // verify the quote but don't add any event log info to the PcrManifest. // issue #879
            log.info("Got PCR map");
            if (tcbMeasurementString != null && !tcbMeasurementString.isEmpty())
                pcrManifest.setMeasurementXml(tcbMeasurementString);

            //log.log(Level.INFO, "PCR map = "+pcrMap); // need to untaint this first
            if (deleteTemporaryFiles) {
                q.delete();
                n.delete();
                c.delete();
                r.delete();
            }
            return pcrManifest;
        }

    }

    public PcrManifest getQuoteInformationForHost(String hostname, TrustAgentClient client) throws NoSuchAlgorithmException, PropertyException, JAXBException,
            UnknownHostException, IOException, KeyManagementException, CertificateException, XMLStreamException {
        return getQuoteInformationForHost(hostname, client, null);
    }
    
    // NOTE:  this v2 client method is a little different from the getQuoteInformationForHost for the v1 trust agent because
    //        it hashes the nonce and the ip address together  (instead of replacing the last 4 bytes of the nonce
    //        with the ip address like the v1 does)
    public PcrManifest getQuoteInformationForHost(String hostname, TrustAgentClient client, Nonce challenge) throws NoSuchAlgorithmException, PropertyException, JAXBException,
            UnknownHostException, IOException, KeyManagementException, CertificateException, XMLStreamException {
        //  BUG #497  START CODE SNIPPET MOVED TO INTEL HOST AGENT
        File q;
        File n;
        File c;
        File r;
        byte[] nonce;        
        if( challenge == null ) {
            nonce = generateNonce(); // 20 random bytes
        }
        else {
            nonce = challenge.toByteArray(); // issue #4978: use specified nonce, if available
        }

        // to fix issue #1038 we have a new option to put the host ip address in the nonce (we don't send this to the host - the hsot automatically would do the same thing)
        byte[] verifyNonce = nonce; // verifyNonce is what we save to verify against host's tpm quote response
        if (quoteWithIPAddress) {
            // is the hostname a dns name or an ip address?  if it's a dns name we have to resolve it to an ip address
            // see also corresponding code in TrustAgent CreateNonceFileCmd
            byte[] ipaddress = getIPAddress(hostname);
            if (ipaddress == null) {
                throw new IllegalArgumentException("mtwilson.tpm.quote.ipv4 is enabled but host address cannot be resolved: " + hostname);
            }
            verifyNonce = Sha1Digest.digestOf(nonce).extend(ipaddress).toByteArray();
        }
//        String verifyNonceBase64 = Base64.encodeBase64String(verifyNonce);

        String sessionId = generateSessionId();

        // FIrst let us ensure that we have an AIK cert created on the host before trying to retrieve the quote. The trust agent
        // would verify if a AIK is already present or not. If not it will create a new one.
        trustedAik = X509Util.encodePemCertificate(client.getAik());

        // to fix issue #1038 trust agent relay we send 20 random bytes nonce to the host (base64-encoded) but if mtwilson.tpm.quote.ipaddress is enabled then in our copy we replace the last 4 bytes with the host's ip address, and when the host generates the quote it does the same thing, and we can verify it later
        // we select best PCR bank but we will change to all PCR banks once it's supported
        TpmQuoteResponse tpmQuoteResponse = client.getTpmQuote(nonce, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23}, host.PcrBanks); // pcrList used to be a comma-separated list passed to this method... but now we are returning a quote with ALL the PCR's ALL THE TIME.
        log.debug("got response from server [" + hostname + "] ");

        log.debug("extracted quote from response: {}", Base64.encodeBase64String(tpmQuoteResponse.quote));

        q = saveQuote(tpmQuoteResponse.quote, sessionId);
        log.debug("saved quote with session id: " + sessionId);

        // we only need to save the certificate when registring the host ... when we are just getting a quote we need to verify it using the previously saved AIK.
        if (trustedAik == null) {
            String aikCertificate = X509Util.encodePemCertificate(tpmQuoteResponse.aik);
            log.debug("extracted aik cert from response: " + aikCertificate);

            c = saveCertificate(aikCertificate, sessionId);
            log.debug("saved host-provided AIK certificate with session id: " + sessionId);
        } else {
            c = saveCertificate(trustedAik, sessionId);
            log.debug("saved database-provided trusted AIK certificate with session id: " + sessionId);
        }

        // for Windows host, we generate a new nonce by sha1(nonce | tag)
        // Now is done for ALL hosts, not only Windows
        if (tpmQuoteResponse.isTagProvisioned) {
            log.debug("tpmQuoteResponse.isTagProvisioned is true");
            verifyNonce = Sha1Digest.digestOf(verifyNonce).extend(tpmQuoteResponse.assetTag).toByteArray();
        }
        
        n = saveNonce(verifyNonce, sessionId);

        log.debug("saved nonce with session id: " + sessionId);

        r = createRSAKeyFile(sessionId);

        log.debug("created RSA key file for session id: " + sessionId);

        // Verify if there is TCBMeasurement Data. This data would be available if we are extending the root of trust to applications and data on the OS
        String tcbMeasurementString = tpmQuoteResponse.tcbMeasurement;
        log.debug("TCB Measurement XML is {}", tcbMeasurementString);

        log.debug("Event log: {}", tpmQuoteResponse.eventLog); // issue #879
        byte[] eventLogBytes = Base64.decodeBase64(tpmQuoteResponse.eventLog);// issue #879
        log.debug("Decoded event log length: {}", eventLogBytes == null ? null : eventLogBytes.length);// issue #879
        if (eventLogBytes != null) { // issue #879
            String decodedEventLog = new String(eventLogBytes);
            log.debug("Event log retrieved from the host consists of: " + decodedEventLog);

            // Since we need to add the event log details into the pcrManifest, we will pass in that information to the below function
            PcrManifest pcrManifest = verifyQuoteAndGetPcr(sessionId, decodedEventLog);
            if (tcbMeasurementString != null && !tcbMeasurementString.isEmpty())
                pcrManifest.setMeasurementXml(tcbMeasurementString);
            log.info("Got PCR map");
            //log.log(Level.INFO, "PCR map = "+pcrMap); // need to untaint this first
            if (deleteTemporaryFiles) {
                q.delete();
                n.delete();
                c.delete();
                r.delete();
            }
            pcrManifest.setProvisionedTag(tpmQuoteResponse.assetTag);
            return pcrManifest;
        } else {
            PcrManifest pcrManifest = verifyQuoteAndGetPcr(sessionId, null); // verify the quote but don't add any event log info to the PcrManifest. // issue #879
            log.info("Got PCR map");
            if (tcbMeasurementString != null && !tcbMeasurementString.isEmpty())
                pcrManifest.setMeasurementXml(tcbMeasurementString);

            //log.log(Level.INFO, "PCR map = "+pcrMap); // need to untaint this first
            if (deleteTemporaryFiles) {
                q.delete();
                n.delete();
                c.delete();
                r.delete();
            }
            pcrManifest.setProvisionedTag(tpmQuoteResponse.assetTag);
            return pcrManifest;
        }

    }

    // hostName == internetAddress.toString() or Hostname.toString() or IPAddress.toString()
    // vmmName == tblHosts.getVmmMleId().getName()
    public String getHostAttestationReport(String hostName, PcrManifest pcrManifest, String vmmName) throws XMLStreamException {
        XMLOutputFactory xof = XMLOutputFactory.newInstance();
        XMLStreamWriter xtw;
        StringWriter sw = new StringWriter();

        /*
         // We need to check if the host supports TPM or not. Only way we can do it
         // using the host table contents is by looking at the AIK Certificate. Based
         // on this flag we generate the attestation report.
         boolean tpmSupport = true;
         String hostType = "";

         if (tblHosts.getAIKCertificate() == null || tblHosts.getAIKCertificate().isEmpty()) {
         tpmSupport = false;
         }
         * */

//        boolean tpmSupport = true;


        // xtw = xof.createXMLStreamWriter(new FileWriter("c:\\temp\\nb_xml.xml"));
        xtw = xof.createXMLStreamWriter(sw);
        xtw.writeStartDocument();
        xtw.writeStartElement("Host_Attestation_Report");
        xtw.writeAttribute("Host_Name", hostName);
        xtw.writeAttribute("Host_VMM", vmmName);
        xtw.writeAttribute("TXT_Support", String.valueOf(true)); //String.valueOf(tpmSupport));

//        if (tpmSupport == true) {
            
            // Note: Map should be insertion sorted by insertion order
            Map<DigestAlgorithm, List<Pcr>> pcrs = pcrManifest.getPcrsMap();
            for(Map.Entry<DigestAlgorithm, List<Pcr>> e : pcrs.entrySet()) {
                for(Pcr p : e.getValue()) {
                    xtw.writeStartElement("PCRInfo");
                    xtw.writeAttribute("ComponentName", p.getIndex().toString());
                    xtw.writeAttribute("DigestValue", p.getValue().toString().toUpperCase());
                    xtw.writeAttribute("DigestAlgorithm", e.getKey().toString());                            
                }                
            }                       
//        } else {
//            xtw.writeStartElement("PCRInfo");
//            xtw.writeAttribute("Error", "Host does not support TPM.");
//            xtw.writeEndElement();
//        }

        // Now we need to traverse through the PcrEventLogs and write that also into the Attestation Report.
        Map<DigestAlgorithm, List<PcrEventLog>> logs = pcrManifest.getPcrEventLogMap();
        for(Map.Entry<DigestAlgorithm, List<PcrEventLog>> e : logs.entrySet()) {
            for(PcrEventLog pel : e.getValue()) {
                List<Measurement> eventLogs = pel.getEventLog();
                for(Measurement m : eventLogs) {
                    xtw.writeStartElement("EventDetails");
                    xtw.writeAttribute("EventName", "OpenSource.EventName");
                    xtw.writeAttribute("ComponentName", m.getLabel());                    
                    xtw.writeAttribute("DigestValue", m.getValue().toString().toUpperCase());
                    xtw.writeAttribute("DigestAlgorithm", pel.getPcrBank().toString().toUpperCase());
                    xtw.writeAttribute("ExtendedToPCR", String.valueOf(pel.getPcrIndex()));
                    xtw.writeAttribute("PackageName", "");
                    xtw.writeAttribute("PackageVendor", "");
                    xtw.writeAttribute("PackageVersion", "");
                    if (ArrayUtils.contains(openSourceHostSpecificModules, m.getLabel())) {
                        // For Xen, these modules would be vmlinuz and initrd and for KVM it would just be initrd.
                        xtw.writeAttribute("UseHostSpecificDigest", "true");
                    } else {
                        xtw.writeAttribute("UseHostSpecificDigest", "false");
                    }
                    xtw.writeEndElement();
                }
            }
        }
        xtw.writeEndElement();
        xtw.writeEndDocument();
        xtw.flush();
        xtw.close();

        String attestationReport = sw.toString();
        return attestationReport;
    }

    public byte[] generateNonce() {
        try {
            // Create a secure random number generator
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            // Get 1024 random bits
            byte[] bytes = new byte[20]; // bug #1038  nonce should be 20 random bytes;  even though we send 20 random bytes to the host, both we and the host will replace the last 4 bytes with the host's primary IP address
            sr.nextBytes(bytes);

//            nonce = new BASE64Encoder().encode( bytes);
//            String nonce = Base64.encodeBase64String(bytes);

            log.debug("Nonce Generated {}", Base64.encodeBase64String(bytes));
            return bytes;
        } catch (NoSuchAlgorithmException e) {
            throw new ASException(e);
        }
    }

    private String generateSessionId() throws NoSuchAlgorithmException {

        // Create a secure random number generator
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        // Get 1024 random bits
        byte[] seed = new byte[1];
        sr.nextBytes(seed);

        sr = SecureRandom.getInstance("SHA1PRNG");
        sr.setSeed(seed);

        int nextInt = sr.nextInt();
        String sessionId = "" + ((nextInt < 0) ? nextInt * -1 : nextInt);
        log.debug("Session Id Generated [{}]", sessionId);

        return sessionId;
    }

    // for DAA
    private String getDaaAikProofFileName(String sessionId) {
        return "daaaikproof_" + sessionId + ".data";
    }

    private String getDaaSecretFileName(String sessionId) {
        return "daasecret_" + sessionId + ".data";
    }

    private String getDaaChallengeFileName(String sessionId) {
        return "daachallenge_" + sessionId + ".data";
    }
    /*
     private String getDaaResponseFileName(String sessionId) {
     return "daaresponse_"+sessionId+".data";
     }
     */

    private String getNonceFileName(String sessionId) {
        return "nonce_" + sessionId + ".data";
    }

    private String getQuoteFileName(String sessionId) {
        return "quote_" + sessionId + ".data";
    }

    private File saveCertificate(String aikCertificate, String sessionId) throws IOException, CertificateException {

        /*
         // first get a consistent newline character
         aikCertificate = aikCertificate.replace('\r', '\n').replace("\n\n", "\n");
         if( aikCertificate.indexOf("-----BEGIN CERTIFICATE-----\n") < 0 && aikCertificate.indexOf("-----BEGIN CERTIFICATE-----") >= 0 ) {
         log.info( "adding newlines to certificate BEGIN tag");
         aikCertificate = aikCertificate.replace("-----BEGIN CERTIFICATE-----", "-----BEGIN CERTIFICATE-----\n");
         }
         if( aikCertificate.indexOf("\n-----END CERTIFICATE-----") < 0 && aikCertificate.indexOf("-----END CERTIFICATE-----") >= 0 ) {
         log.info( "adding newlines to certificate END tag");
         aikCertificate = aikCertificate.replace("-----END CERTIFICATE-----", "\n-----END CERTIFICATE-----");
         }

         saveFile(getCertFileName(sessionId), aikCertificate.getBytes());
         */
        File file = new File(aikverifyhomeData + File.separator + getCertFileName(sessionId));
        X509Certificate aikcert = X509Util.decodePemCertificate(aikCertificate);
        String pem = X509Util.encodePemCertificate(aikcert);
        try (FileOutputStream out = new FileOutputStream(file)) {
            IOUtils.write(pem, out);
        }
        return file;
    }

    private String getCertFileName(String sessionId) {
        return "aikcert_" + sessionId + ".cer";
    }

    private File saveFile(String fileName, byte[] contents) throws IOException {
        log.debug(String.format("saving file %s to [%s]", fileName, aikverifyhomeData));
        File file = new File(aikverifyhomeData + File.separator + fileName);
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            fileOutputStream.write(contents);
            fileOutputStream.flush();
            return file;
        } catch (FileNotFoundException e) {
            log.debug(String.format("cannot save to file %s in [%s]: %s", fileName, aikverifyhomeData, e.getMessage()));
            throw e;
        }
    }

    private File saveQuote(String quote, String sessionId) throws IOException {
//          byte[] quoteBytes = new BASE64Decoder().decodeBuffer(quote);
        byte[] quoteBytes = Base64.decodeBase64(quote);
        File file = saveFile(getQuoteFileName(sessionId), quoteBytes);
        return file;
    }

    private File saveQuote(byte[] quoteBytes, String sessionId) throws IOException {
        File file = saveFile(getQuoteFileName(sessionId), quoteBytes);
        return file;
    }

    private File saveNonce(String nonce, String sessionId) throws IOException {
        byte[] nonceBytes = Base64.decodeBase64(nonce);
        File file = saveFile(getNonceFileName(sessionId), nonceBytes);
        return file;
    }

    private File saveNonce(byte[] nonceBytes, String sessionId) throws IOException {
        File file = saveFile(getNonceFileName(sessionId), nonceBytes);
        return file;
    }

    private File createRSAKeyFile(String sessionId) throws IOException, CertificateException {
        // 20130409 replacing external openssl command with equivalent java code, see below
        /*
         String command = String.format("%s %s %s",opensslCmd,aikverifyhomeData + File.separator + getCertFileName(sessionId),aikverifyhomeData + File.separator+getRSAPubkeyFileName(sessionId));
         log.info( "RSA Key Command {}", command);
         CommandUtil.runCommand(command, false, "CreateRsaKey" );
         //log.log(Level.INFO, "Result - {0} ", result);
         */
        try (FileInputStream in = new FileInputStream(new File(aikverifyhomeData + File.separator + getCertFileName(sessionId)))) {
            String x509cert = IOUtils.toString(in);
            X509Certificate aikcert = X509Util.decodePemCertificate(x509cert);
            String aikpubkey = RsaUtil.encodePemPublicKey(aikcert.getPublicKey());
            File file = new File(aikverifyhomeData + File.separator + getRSAPubkeyFileName(sessionId));
            try (FileOutputStream out = new FileOutputStream(file)) {
                IOUtils.write(aikpubkey, out);
                return file;
            }
        }
    }

    private String getRSAPubkeyFileName(String sessionId) {
        return "rsapubkey_" + sessionId + ".key";
    }

    private PcrManifest verifyQuoteAndGetPcr(String sessionId, String eventLog) {
//        HashMap<String,PcrManifest> pcrMp = new HashMap<String,PcrManifest>();        
        PcrManifest pcrManifest = new PcrManifest();
        log.debug("verifyQuoteAndGetPcr for session {}", sessionId);
        String command = String.format("%s -c %s %s %s",
                EscapeUtil.doubleQuoteEscapeShellArgument(aikverifyCmd),
                EscapeUtil.doubleQuoteEscapeShellArgument(aikverifyhomeData + File.separator + getNonceFileName(sessionId)),
                EscapeUtil.doubleQuoteEscapeShellArgument(aikverifyhomeData + File.separator + getRSAPubkeyFileName(sessionId)),
                EscapeUtil.doubleQuoteEscapeShellArgument(aikverifyhomeData + File.separator + getQuoteFileName(sessionId)));

        log.debug("Command: {}", command);
        List<String> result = CommandUtil.runCommand(command, true, "VerifyQuote");

        // Sample output from command:
        //  1 3a3f780f11a4b49969fcaa80cd6e3957c33b2275
        //  17 bfc3ffd7940e9281a3ebfdfa4e0412869a3f55d8
        //log.log(Level.INFO, "Result - {0} ", result); // need to untaint this first

        //List<String> pcrs = getPcrsList(); // replaced with regular expression that checks 0-23

        for (String pcrString : result) {
            String[] parts = pcrString.trim().split(" ");
            if (parts.length == 2) {
                /* parts[0] contains pcr index and the bank algorithm
                 * in case of SHA1, the bank algorithm is not attached. so the format is just the pcr number same as before
                 * in case of SHA256 or other algorithms, the format is "pcrNumber_SHA256"
                 */
                String[] pcrIndexParts = parts[0].trim().split("_");
                String pcrNumber = pcrIndexParts[0].trim().replaceAll(pcrNumberUntaint, "").replaceAll("\n", "");
                String pcrBank;
                if (pcrIndexParts.length ==2)
                    pcrBank = pcrIndexParts[1].trim();
                else
                    pcrBank = "SHA1";
                String pcrValue = parts[1].trim().replaceAll(pcrValueUntaint, "").replaceAll("\n", "");

                boolean validPcrNumber = pcrNumberPattern.matcher(pcrNumber).matches();
                boolean validPcrValue = pcrValuePattern.matcher(pcrValue).matches();
                if (validPcrNumber && validPcrValue) {
                    log.debug("Result PCR " + pcrNumber + ": " + pcrValue);
//                	pcrMp.put(pcrNumber, new PcrManifest(Integer.parseInt(pcrNumber),pcrValue));
                    // TODO: structure returned by this will be different, so we can actually select the algorithm by type and not length
                    // if(pcrValue.length() == 32 * 2) {
                    if(pcrBank.equals("SHA256")) {
                        pcrManifest.setPcr(PcrFactory.newInstance(DigestAlgorithm.SHA256, PcrIndex.valueOf(pcrNumber), pcrValue));
                    //} else if(pcrValue.length() == 20 * 2) {
                    } else if(pcrBank.equals("SHA1")) {
                        pcrManifest.setPcr(PcrFactory.newInstance(DigestAlgorithm.SHA1, PcrIndex.valueOf(pcrNumber), pcrValue));
                    } 
                }
            } else {
                log.warn("Result PCR invalid");
            }
            /*
             if(pcrs.contains(parts[0].trim()))
             pcrMp.put(parts[0].trim(), new PcrManifest(Integer.parseInt(parts[0]),parts[1]));
             */
        }

        // Now that we captured the PCR details, we need to capture the module information also into the PcrManifest object
        // Sample Format:
        // <modules>
        //<module><pcrNumber>17</pcrNumber><name>tb_policy</name><value>9704353630674bfe21b86b64a7b0f99c297cf902</value></module>
        //<module><pcrNumber>18</pcrNumber><name>xen.gz</name><value>dfdffe5d3bdff697c4d7447115440e34fa27c1a4</value></module>
        //<module><pcrNumber>19</pcrNumber><name>vmlinuz</name><value>d3f525b0dc6f7d7c9a3af165bcf6c3e3e02b2599</value></module>
        //<module><pcrNumber>19</pcrNumber><name>initrd</name><value>3dfa5762c78623ccfc778498ab4cb7136bb3f5ab</value></module>
        //</modules>
        if (eventLog != null) { // issue #879
            try {
                XMLInputFactory xif = XMLInputFactory.newInstance();
                //FileInputStream fis = new FileInputStream("c:\\temp\\nbtest.txt");
                StringReader sr = new StringReader(eventLog);
                XMLStreamReader reader = xif.createXMLStreamReader(sr);

                int extendedToPCR = -1;
                String digestValue = "";
                String componentName = "";
                String pcrBank = "SHA1";

                while (reader.hasNext()) {
                    if (reader.getEventType() == XMLStreamConstants.START_ELEMENT
                            && reader.getLocalName().equalsIgnoreCase("module")) {
                        reader.next();
                        
                        if(reader.getLocalName().equalsIgnoreCase("pcrBank")) {
                            pcrBank = reader.getElementText().toUpperCase();
                            reader.next();
                        }                       
                        
                        // Get the PCR Number to which the module is extended to
                        if (reader.getLocalName().equalsIgnoreCase("pcrNumber")) {
                            extendedToPCR = Integer.parseInt(reader.getElementText());
                        }

                        reader.next();
                        // Get the Module name
                        if (reader.getLocalName().equalsIgnoreCase("name")) {                            
                            componentName = reader.getElementText();
                        }

                        reader.next();
                        // Get the Module hash value
                        if (reader.getLocalName().equalsIgnoreCase("value")) {
                            digestValue = reader.getElementText();
                        }                                                

                        log.debug("Process module " + componentName + " getting extended to " + extendedToPCR);

                        // Attach the PcrEvent logs to the corresponding pcr indexes.
                        // Note: Since we will not be processing the even logs for 17 & 18, we will ignore them for now.                        
                        Measurement m = convertHostTpmEventLogEntryToMeasurement(extendedToPCR, componentName, digestValue, pcrBank);
                        if (pcrManifest.containsPcrEventLog(pcrBank, PcrIndex.valueOf(extendedToPCR))) {
                            pcrManifest.getPcrEventLog(pcrBank, extendedToPCR).getEventLog().add(m);
                        } else {
                            ArrayList<Measurement> list = new ArrayList<Measurement>();
                            list.add(m);
                            pcrManifest.setPcrEventLog(PcrEventLogFactory.newInstance(pcrBank, PcrIndex.valueOf(extendedToPCR), list));
                        }
                    }
                    reader.next();
                }
            } catch (FactoryConfigurationError | XMLStreamException | NumberFormatException ex) {
                // bug #2171 we need to throw an exception to prevent the host from being registered with an error manifest
                //log.error(ex.getMessage(), ex);
                throw new IllegalStateException("Invalid measurement log", ex);
            }
        }

        return pcrManifest;

    }

    /**
     * Helper method to create the Measurement Object.
     *
     * @param extendedToPcr
     * @param moduleName
     * @param moduleHash
     * @return
     */
    private static Measurement convertHostTpmEventLogEntryToMeasurement(int extendedToPcr, String moduleName, String moduleHash, String pcrBank) {
        HashMap<String, String> info = new HashMap<String, String>();
        info.put("EventName", "OpenSource.EventName");  // For OpenSource since we do not have any events associated, we are creating a dummy one.
        // Removing the prefix of "OpenSource" as it is being captured in the event type
        info.put("ComponentName", moduleName);
        info.put("PackageName", "");
        info.put("PackageVendor", "");
        info.put("PackageVersion", "");

        DigestAlgorithm da = DigestAlgorithm.valueOf(pcrBank);        
        switch(da) {
            case SHA1:
                return new MeasurementSha1(new Sha1Digest(moduleHash), moduleName, info);                
            case SHA256:                
                return new MeasurementSha256(new Sha256Digest(moduleHash), moduleName, info);                
            default:
                throw new UnsupportedOperationException("PCRBank: " + pcrBank + " not supported");
        }        
    }
    /*
     public EntityManagerFactory getEntityManagerFactory() {
     return entityManagerFactory;
     }

     public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
     this.entityManagerFactory = entityManagerFactory;
     }*/

    /*
     private List<String> getPcrsList() {
     List<String> pcrs = new ArrayList<String>() ;

     for(int i = 0 ; i< 24 ; i++)
     pcrs.add(String.valueOf(i));

     return pcrs;
     }
     */
        
}
