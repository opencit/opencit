/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.trustagent.client.jaxrs;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.dcsg.cpg.tls.policy.TlsConnection;
import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
import com.intel.mtwilson.jaxrs2.mediatype.CryptoMediaType;
import com.intel.mtwilson.trustagent.model.VMAttestationRequest;
import com.intel.mtwilson.trustagent.model.VMAttestationResponse;
import com.intel.mtwilson.trustagent.model.*;
import java.security.cert.X509Certificate;
import java.util.Properties;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author ssbangal
 */
public class TrustAgentClient extends MtWilsonClient {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrustAgentClient.class);
    
    public TrustAgentClient(Properties properties, TlsConnection tlsConnection) throws Exception {
        super(properties, tlsConnection);
    }
    
     /**
     * Retrieves the Attestation Identity Key (AIK) certificate for the host. The required content type can also be specified
     * as an extension in the URL.
     * @return AIK x509 certificate
     * @since Mt.Wilson 2.0
     * @mtwContentTypeReturned application/pkix-cert or application/x-pem-file
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:1443/v2/aik
     * https://server.com:1443/v2/aik.pem
     * https://server.com:1443/v2/aik.cer
     * 
     * Output: -----BEGIN CERTIFICATE-----
     * MIIDMjCCAhqgAwIBAgIGAU92j4SVMA0GCSqGSIb3DQEBBQUAMBsxGTAXBgNVBAMTEG10d2lsc29u
     * LXBjYS1haWswHhcNMTUwODI4MjMwNjAxWhcNMjUwODI3MjMwNjAxWjAbMRkwFwYDVQQDExBtdHdp
     * bHNvbi1wY2EtYWlrMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArRUrsziH8nIJWPtA
     * CAXbugYI9yX/KmwtG2vdBFCon+FcT6zidynaUtUqTLPmMigVEsWiEhbVxNDPr+rKponkjDmeSn/w
     * WGFp/dETtKLYLUTW1Aij7DFmz6+draAB6k4m0JcVvCM+Xevs2VG1kBOxC94GtKtO9ycLFzTGlxTJ
     * FlRkoyd4qM45O8Xc/qS3xF2gNLNqhWzzQNWG/rJXK1o8k/7EIcvW9tRvGTBj+STKZiAG/gomSY8b
     * 0avhrtOIgFeV8oYbolPu7RaxuPbfXBoEpw7fnDwiCowm9dxAOQpJ02ZP5cj4ZbVHWULcBL/gY4T6
     * AZvQ2EZAqRIJ3LX/7fsSewIDAQABo3wwejAdBgNVHQ4EFgQUW7eXsmNIQ4buvbJlWuOoTau3Pykw
     * DwYDVR0TAQH/BAUwAwEB/zBIBgNVHSMEQTA/gBRbt5eyY0hDhu69smVa46hNq7c/KaEfpB0wGzEZ
     * MBcGA1UEAxMQbXR3aWxzb24tcGNhLWFpa4IGAU92j4SVMA0GCSqGSIb3DQEBBQUAA4IBAQCUgor4
     * oNnnqukBT0B8C+zAPUm0w0yrvxM8YmaAIodKOhFIF9OuR/gWzAi2lzxsGoaPKqYEeZFQpMlQ8AvK
     * fZj6tBK7iUy0zFcuMqdvwMhXX2h3ryaw0Qslspy7HY3CIX6Qck5G2zAJBlHBd7ZXLVWcoTWa56o1
     * mNqUhftOBLi+DlB8klD7Z6/Un+XVlBTk5uimgT42WF0XupHJrOF0tx767JcopZQSeYbdiugQEztz
     * IKmdGysVyg+7F7hkhrQfLZsohLJ54Zvgrq5+nKF0Rj2zzoImlPtYUKV5EnQm2+SsLxr3GP1flm6M
     * sHIC30ht3TBDoVw8vh80jxsu75afi4Al
     * -----END CERTIFICATE-----
     * </pre>
     */        
    public X509Certificate getAik() {
        log.debug("target: {}", getTarget().getUri().toString());
        X509Certificate aik = getTarget()
                .path("/aik")
                .request()
                .accept(CryptoMediaType.APPLICATION_PKIX_CERT)
                .get(X509Certificate.class);
        return aik;
    }

     /**
     * Retrieves the CA certificate that signed the Attestation Identity Key (AIK) certificate for the host. 
     * @return AIK CA x509 certificate
     * @since Mt.Wilson 2.0
     * @mtwContentTypeReturned application/pkix-cert or application/x-pem-file
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:1443/v2/aik/ca
     * https://server.com:1443/v2/aik/ca.pem
     * https://server.com:1443/v2/aik/ca.cer
     * 
     * Output: -----BEGIN CERTIFICATE-----
     * MIICvTCCAaWgAwIBAgIGAU+KI+kQMA0GCSqGSIb3DQEBBQUAMBsxGTAXBgNVBAMTEG10d2lsc29u
     * LXBjYS1haWswHhcNMTUwOTAxMTgyMDUzWhcNMjUwODMxMTgyMDUzWjAAMIIBIjANBgkqhkiG9w0B
     * AQEFAAOCAQ8AMIIBCgKCAQEAqnT+nx5W0c3Hm5yFIfXYbaYi86wC1LDqqVCHRzeFlO07moZw1oV/
     * ucwF/LOmepouxWRI7RVRdTZD6KV52O+Iu2kIHZ1UXWNmL+9BrGWufvByZy1f3u08TGl7WSuKVWFK
     * UPsQ+5XITMaknZlK+ldog2VbyNNwvty8yo/mFx2fnVrMmDz03E+pE1zUyIgqKSomlyS+rGlAl8ZD
     * 1cKKiZc8ZCRh38lLGjTalRXPGCnOTi3uK/P7wut3yynJM1ZEr9Vc6QYxcX8O3vd/RIkF0GqPJrh+
     * Xu0hWUPy1Eviz85NsHnQ2nZ79VC0VS0nqLIPKg5uqIyohGgppK41KWvC545nAQIDAQABoyIwIDAe
     * BgNVHREBAf8EFDASgRBISVMgSWRlbnRpdHkgS2V5MA0GCSqGSIb3DQEBBQUAA4IBAQA6qJLucSWy
     * dFb0BPvlsyYYFSdjPaGAFWFwh/lbHYI1Ouy3jw34gmZIR0xTSI/96NA5KO17bzhzvKg9+nsPIS5I
     * 81GBiIaPc4HPAuqi21jBCI/LZQIC61P1R6/Tmzosm8NrRX+VVn+NmBVp2rXFtBb6BmBmyx7D7cNZ
     * b6+C6DQ+gg2PlU8qAjAzF0iQUqzELL8LIzIMtVDJYSdHe4kgyFom3mnBwfhpUmsnv0U2YAsdgcH5
     * +uZPD/+j3en5u8O5rNY15onq+2pFIxA/F29DwWCuOlF4orc9ejPv5hdVqsHjUR0zPPj87gLeHUbj
     * vDTmD6JzA3PbuypM/bFZrELA7oT0
     * -----END CERTIFICATE-----
     * </pre>
     */        
    public X509Certificate getAikCa() {
        log.debug("target: {}", getTarget().getUri().toString());
        X509Certificate aik = getTarget()
                .path("/aik/ca")
                .request()
                .accept(CryptoMediaType.APPLICATION_PKIX_CERT)
                .get(X509Certificate.class);
        return aik;
    }
    
     /**
     * Retrieves the BIOS, OS and Hypervison information from the host.  
     * @return HostInfo object having the details of the host.
     * @since Mt.Wilson 2.0
     * @mtwContentTypeReturned JSON/XML
     * @mtwMethodType GET
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:1443/v2/host
     * https://server.com:1443/v2/host.json
     * Output: {"timestamp":1447060685640,"error_code":"OK","error_message":"OK","os_name":"Ubuntu","os_version":"14.04",
     * "bios_oem":"Intel Corp.","bios_version":"S5500.86B.01.00.0065.070920141009","vmm_name":"QEMU","vmm_version":"2.0.0",
     * "processor_info":" C2 06 02 00 FF FB EB BF","hardware_uuid":"9F9A9165-61EF-11E0-B0A5-001E671044D8"}
     * </pre>
     */        
    public HostInfo getHostInfo() {
        log.debug("target: {}", getTarget().getUri().toString());
        HostInfo hostInfo = getTarget()
                .path("/host")
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .get(HostInfo.class);
        return hostInfo;
    }
    
    
     /**
     * Writes the 20 byte asset tag into NVRAM that would be extended to PCR 22 by tBoot during the boot process. 
     * @param tag value is 20 bytes base64-encoded
     * @param hardwareUuid is the hardware UUID of the target host
     * @since Mt.Wilson 2.0
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:1443/v2/tag
     * Input: { tag: "YTBiMWMyZDNlNGY1ZzZoN2k4ajk=", hardware_uuid: "7a569dad-2d82-49e4-9156-069b0065b262" }
     * Output: N/A
     * </pre>
     */          
    public void writeTag(byte[] tag, UUID hardwareUuid) {
        TagWriteRequest tagWriteRequest = new TagWriteRequest();
        tagWriteRequest.setTag(tag);
        tagWriteRequest.setHardwareUuid(hardwareUuid);
        getTarget()
                .path("/tag")
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .post(Entity.json(tagWriteRequest));
    }
    
     /**
     * Retrieves the AIK signed quote from TPM.  
     * @param nonce - The nonce value is 20 bytes base64-encoded. The client chooses the nonce. The trust agent will 
     * automatically extend its IP address to the nonce before using it in the quote. The extend operation is 
     * nonce1 = sha1( nonce0 || ip-address ) where nonce0 is the original input nonce (20 bytes) and nonce1 
     * is the extended nonce used in the TPM quote (20 bytes), and the ip-address is the 4-byte encoding of 
     * the IP address.
     * @param pcrs - List of PCRs for which the quote is needed.
     * @return TpmQuoteResponse object having the details of the current status of the TPM and its PCR values.
     * The output is base64-encoded in both XML and JSON output formats. 
     * @since Mt.Wilson 2.0
     * @mtwContentTypeReturned JSON/XML
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:1443/v2/tpm/quote
     * https://server.com:1443/v2/tpm/quote.json
     * Input: {"nonce":"1aQNqvIIgf1kY2cO5DZ68FYE7H0=","pcrs":[0,17,18,19]}
     * Output: <tpm_quote_response>
     * <timestamp>1396871823858</timestamp>
     * <client_ip>192.168.122.1</client_ip>
     * <error_code>0</error_code>
     * <error_message>OK</error_message>
     * <quote>AAMBAA4AAABQhrEJN0kDQ5xzRQpHn9mNlKLwyYOpDjIz4zVdP4nGtOsNcgnTge8BT6RUcyMEMAsU
     * 0c2c1w5kUPFvOA/ay4TLE8VLe+ghBW5aKeleeCo9p+hKBSc4SPPipnPdQ9Tjr+FlzeBJx2ML8Auy
     * BptRLqDS3d2iuQn18PJ0nLChbqBrkbbj4+9vCEBk9Ybi6CNpgWcw+p5RgGMa0auB0Oxq4JvDaVAj
     * vvsgh6Kx5dyXtYRTCsS8Z5RRSJK8wv7d//wjzuFYqw/BNvksNU0pZ8wFuwsNdRV6EARTs5xIVjek
     * a1+kfuCyWYgm1LNwTW5iLNCjxg3zjdK5TnM4+QS454o6flH85picZSoJyYT1WLYmQEg9VK/7LF5/
     * YUInPLd89GLNKdYXGQdvkiPDQGZlAG8h087ycjY9NyPrcXtBm15nm/R7bxEic1E6fl9xw7MIw40w
     * DM21</quote>
     * <event_log>PG1lYXN1cmVMb2c+PHR4dD48dHh0U3RhdHVzPjE8L3R4dFN0YXR1cz48b3NTaW5pdERhdGFDYXBhYmlsaXRpZX
     * M+MDAwMDAwMDA8L29zU2luaXREYXRhQ2FwYWJpbGl0aWVzPjxzaW5pdE1sZURhdGE+PHZlcnNpb24+ODwvdmVyc2lvbj48c2l
     * uaXRIYXNoPmZlZTM2NTAyMzdlMjE2ZGEyMTU5ZDZmNGVmODVkMzNiM2I4ZDNiYmU8L3Npbml0SGFzaD48bWxlSGFzaD44Nzgw
     * NjNjYTQ5ODE2NjU1YTZjMWViNjhmODI3ZTI5MzQzZTc1NmQzPC9tbGVIYXNoPjxiaW9zQWNtSWQ+MDBjNDAwMDAwMDFhMDAwM
     * DAwMDAwMDAwZmZmZmZmZmZmZmZmZmZmZjwvYmlvc0FjbUlkPjxtc2VnVmFsaWQ+MDwvbXNlZ1ZhbGlkPjxzdG1IYXNoPjAwMD
     * AwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDA8L3N0bUhhc2g+PHBvbGljeUNvbnRyb2w+MDAwMDAwMDI8L3B
     * vbGljeUNvbnRyb2w+PGxjcFBvbGljeUhhc2g+MDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDwvbGNw
     * UG9saWN5SGFzaD48cHJvY2Vzc29yU0NSVE1TdGF0dXM+MDAwMDAwMDE8L3Byb2Nlc3NvclNDUlRNU3RhdHVzPjxlZHhTZW50Z
     * XJGbGFncz4wMDAwMDAwMDwvZWR4U2VudGVyRmxhZ3M+PC9zaW5pdE1sZURhdGE+PG1vZHVsZXM+PG1vZHVsZT48cGNyTnVtYm
     * VyPjE3PC9wY3JOdW1iZXI+PG5hbWU+dGJfcG9saWN5PC9uYW1lPjx2YWx1ZT45NzA0MzUzNjMwNjc0YmZlMjFiODZiNjRhN2I
     * wZjk5YzI5N2NmOTAyPC92YWx1ZT48L21vZHVsZT48bW9kdWxlPjxwY3JOdW1iZXI+MTg8L3Bjck51bWJlcj48bmFtZT54ZW4u
     * Z3o8L25hbWU+PHZhbHVlPjMyNTAwMmVmYjMzNjFhMDZmYTNlODA4YTQ5ZjQ2ODJlNDJiY2ZmYmQ8L3ZhbHVlPjwvbW9kdWxlP
     * jxtb2R1bGU+PHBjck51bWJlcj4xOTwvcGNyTnVtYmVyPjxuYW1lPnZtbGludXo8L25hbWU+PHZhbHVlPmZjMmI5YjcyN2RiNG
     * Q1ZmMyMGQzYjJhZTgyZDFjNjZmMjIwNmViMmE8L3ZhbHVlPjwvbW9kdWxlPjxtb2R1bGU+PHBjck51bWJlcj4xOTwvcGNyTnV
     * tYmVyPjxuYW1lPmluaXRyZDwvbmFtZT48dmFsdWU+Y2U5MGMxMTc1Yzc0OGJkYzJhNjUwM2ZjNDY5M2ExOTI5MjNiZDQ2ODwv
     * dmFsdWU+PC9tb2R1bGU+PC9tb2R1bGVzPjwvdHh0PjwvbWVhc3VyZUxvZz4K</event_log>
     * <aik>MIICuzCCAaOgAwIBAgIGAUU9UFFmMA0GCSqGSIb3DQEBBQUAMBkxFzAVBgNVBAMMDkhJU19Qcml2
     * YWN5X0NBMB4XDTE0MDQwNzE3NTA0NVoXDTI0MDQwNjE3NTA0NVowADCCASIwDQYJKoZIhvcNAQEB
     * BQADggEPADCCAQoCggEBANN3nC4v3/lQsPOEQ45k+n0MvnTXRmpLON4Ake9c0qaGz0e3mBBL3+/E
     * koCgvSX2zvsomsK/B3QKcf0HFl1GU3xReQL8nqvSiF5/D4Y32erbLguMcA0MpvGMgb57yToUBX1X
     * H7JNxg3n+AAvgdOuRnkk/ntieOmp76CQGd68EMU5EnOyxiCnHDqOc51TuQBzP+K1e6MLgcvewAP8
     * VJOrTWj3WRP/zQGS1/Mwc3Cpd7qV6pMWUo3NXxLc2TDltDpnD1Yrn1ex7r94JIrUnFtiEJ3tJGIs
     * mXE1EsTp5GVEL6qAgGCAN7Wxc57CrOFcCk4LDGfC2tIFixbx7GO/YFoE9fcCAwEAAaMiMCAwHgYD
     * VR0RAQH/BBQwEoEQSElTIElkZW50aXR5IEtleTANBgkqhkiG9w0BAQUFAAOCAQEAbC3c/8EZ8iBD
     * 8nBWDZLI/U0n8+zKrKrBPegM0p/pxkxnbm/IQyIUg4/mK0KgMPWwe6IufSdwFxiWlkIx2u0bg9d6
     * w/jUsONDR4idmmbCeCFerSd89KnBZzPM9txpm1mvcClvBwXK1a/e7ElLBBQWG4so7lh4jyuEwT1A
     * RaP54dmLEs6GSR8NXZ9rZrGWMrYgjrLhSjoWbmqSOcWyG1/xRRMLn5qMZaOG5rA0K+dKz1XcXUJw
     * MeMZbGB9gIXcHUX+GtsyTRPerd/81VmIWM3XHPh4SJfPoi8nYddWCIEVzS5DzujpzvSRfgGB8hbp
     * nLH3YgnKwWYLbQq46M60TB1P4Q==</aik>
     * </tpm_quote_response>
     * </pre>
     */      
    public TpmQuoteResponse getTpmQuote(byte[] nonce, int[] pcrs) {
        TpmQuoteRequest tpmQuoteRequest = new TpmQuoteRequest();
        tpmQuoteRequest.setNonce(nonce);
        tpmQuoteRequest.setPcrs(pcrs);
        log.debug("target: {}", getTarget().getUri().toString());
        TpmQuoteResponse tpmQuoteResponse = getTarget()
                .path("/tpm/quote")
                .request()
                .accept(MediaType.APPLICATION_XML)
                .post(Entity.json(tpmQuoteRequest), TpmQuoteResponse.class);
        return tpmQuoteResponse;
    }
    
    public TpmQuoteResponse getTpmQuote(byte[] nonce, int[] pcrs, String pcrBank) {
        TpmQuoteRequest tpmQuoteRequest = new TpmQuoteRequest();
        tpmQuoteRequest.setNonce(nonce);
        tpmQuoteRequest.setPcrs(pcrs);
        tpmQuoteRequest.setPcrbanks(pcrBank);
        log.debug("target: {}", getTarget().getUri().toString());
        TpmQuoteResponse tpmQuoteResponse = getTarget()
                .path("/tpm/quote")
                .request()
                .accept(MediaType.APPLICATION_XML)
                .post(Entity.json(tpmQuoteRequest), TpmQuoteResponse.class);
        return tpmQuoteResponse;
    }
    
    public TpmQuoteResponse getTpmQuote(byte[] nonce, int[] pcrs, String[] pcrBanks) {
        TpmQuoteRequest tpmQuoteRequest = new TpmQuoteRequest();
        tpmQuoteRequest.setNonce(nonce);
        tpmQuoteRequest.setPcrs(pcrs);
                
        tpmQuoteRequest.setPcrbanks(StringUtils.join(pcrBanks, " "));
        log.debug("target: {}", getTarget().getUri().toString());
        TpmQuoteResponse tpmQuoteResponse = getTarget()
                .path("/tpm/quote")
                .request()
                .accept(MediaType.APPLICATION_XML)
                .post(Entity.json(tpmQuoteRequest), TpmQuoteResponse.class);
        return tpmQuoteResponse;
    }

    public X509Certificate getBindingKeyCertificate() {
        log.debug("target: {}", getTarget().getUri().toString());
        X509Certificate aik = getTarget()
                .path("/binding-key-certificate")
                .request()
                .accept(CryptoMediaType.APPLICATION_PKIX_CERT)
                .get(X509Certificate.class);
        return aik;
    }

    
//     * This API retrieves the VM attestation status. It would just return either true or false.
//     * @param vmInstanceId
//     * @return 
    public VMAttestationResponse getVMAttestationStatus(String vmInstanceId) {        
        VMAttestationRequest vmAttestationRequest = new VMAttestationRequest();
        vmAttestationRequest.setVmInstanceId(vmInstanceId);
        log.debug("target: {}", getTarget().getUri().toString());

        VMAttestationResponse vmAttestationResponse = getTarget()
                .path("/vrtm/status")
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .post(Entity.json(vmAttestationRequest), VMAttestationResponse.class);
        return vmAttestationResponse;
    }
    
    
//     * This API retrieves the complete VM attestation report including the following:
//     * - Signed VM Quote having the nonce, vm instance id, and cumulative hash
//     * - Signed Trust Policy
//     * - Signing key certificate
//     * - Measurement log.
//     * @param obj
//     * @return 
    public VMQuoteResponse getVMAttestationReport(VMAttestationRequest obj) {
        
        log.debug("target: {}", getTarget().getUri().toString());
        VMQuoteResponse vmQuoteResponse = getTarget()
                .path("/vrtm/report")
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .post(Entity.json(obj), VMQuoteResponse.class);
                
        return vmQuoteResponse;
    }
}
