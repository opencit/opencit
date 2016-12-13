/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.agent.citrix;


import com.intel.mountwilson.as.common.ASException;
import com.intel.mountwilson.ta.data.hostinfo.HostInfo;
import com.intel.mtwilson.agent.HostAgent;
import com.intel.dcsg.cpg.crypto.RsaUtil;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.model.Aik;
import com.intel.mtwilson.model.Nonce;
import com.intel.mtwilson.model.Pcr;
import com.intel.mtwilson.model.PcrIndex;
import com.intel.mtwilson.model.PcrManifest;
import com.intel.dcsg.cpg.crypto.Sha1Digest;
import com.intel.mtwilson.model.PcrFactory;
import com.intel.mtwilson.model.TpmQuote;
import com.intel.mtwilson.trustagent.model.VMAttestationRequest;
import com.intel.mtwilson.trustagent.model.VMAttestationResponse;
import com.intel.mtwilson.trustagent.model.VMQuoteResponse;
import com.xensource.xenapi.Types;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.apache.xmlrpc.XmlRpcException;


/**
 *
 * @author stdalex
 */
public class CitrixHostAgent implements HostAgent{
    private CitrixClient client;
    private Logger log = LoggerFactory.getLogger(getClass());
    
    public CitrixHostAgent(CitrixClient client) {
     this.client = client;
    }
    
    
    @Override
    public boolean isTpmEnabled() {
        return true;
    }

    @Override
    public boolean isEkAvailable() {
        return true;
    }

    @Override
    public boolean isAikAvailable() {
        return true;
    }

    @Override
    public boolean isAikCaAvailable() {
        return false;
    }

    @Override
    public boolean isDaaAvailable() {
        return true;
    }

    @Override
    public X509Certificate getAikCertificate() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public X509Certificate getAikCaCertificate() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public String getHostInformation() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getVendorHostReport() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public TpmQuote getTpmQuote(Aik aik, Nonce nonce, Set<PcrIndex> pcr) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public TxtHostRecord getHostDetails() throws IOException {
        long getHostInfoStart = System.currentTimeMillis();
        //throw new UnsupportedOperationException("Not supported yet.");
        TxtHostRecord record = new TxtHostRecord();
        HostInfo info;
        try {
            info = this.client.getHostInfo();
        } catch(NoSuchAlgorithmException | KeyManagementException | MalformedURLException | Types.XenAPIException | XmlRpcException ex){
            log.error("getHostDetails getHostInfo caught: " + ex.getMessage());
            throw new IOException("Cannot get Citrix host info: "+ex.getMessage(), ex);
       }
        
        record.HostName = client.hostIpAddress;
        record.IPAddress = client.hostIpAddress;
        record.Port = client.port;
        record.BIOS_Name = info.getBiosOem();
        record.BIOS_Version = info.getBiosVersion();
        record.BIOS_Oem = info.getBiosOem();
        record.VMM_Name = info.getVmmName();
        record.VMM_Version = info.getVmmVersion();
        record.VMM_OSName = info.getOsName();
        record.VMM_OSVersion = info.getOsVersion();
        record.AddOn_Connection_String = client.connectionString;
        record.Processor_Info = info.getProcessorInfo();
        record.AIK_Certificate = null;
        
        long getHostInfoStart2 = System.currentTimeMillis();
        log.debug("CitrixClient: Time taken to get host information - " + (getHostInfoStart2 - getHostInfoStart) + " milliseconds");
        // Nov 19, 1013: Since AIK Cert is not needed by functions that call getHostDetails and also there is a separate function to
        // retrieve the AIK Cert, we will comment this out.
        /*
        try {
            record.AIK_Certificate = client.getAIKCertificate();
        }  catch(Exception ex){
            log.error("getHostDetails getAikCert caught: " + ex.getMessage());
       }
        long getHostInfoStart3 = System.currentTimeMillis();
        log.debug("CitrixClient: Time taken to get AIK Cert - " + (getHostInfoStart3 - getHostInfoStart2) + " milliseconds");*/
        return record;
    }
    
    @Override
    public String getHostAttestationReport(String pcrList) throws IOException {
        return getHostAttestationReport(pcrList, null);
    }
    
    /*
     * Format should look something like this
     * <?xml version='1.0' encoding='UTF-8'?>
     * <Host_Attestation_Report Host_Name="10.1.70.126" vCenterVersion="5.0" HostVersion="5.0">
     *      <PCRInfo ComponentName="0" DigestValue="1d670f2ae1dde52109b33a1f14c03e079ade7fea"/>
     *      <PCRInfo ComponentName="17" DigestValue="ca21b877fa54dff86ed5170bf4dd6536cfe47e4d"/>
     *      <PCRInfo ComponentName="18" DigestValue="8cbd66606433c8b860de392efb30d76990a3b1ed"/>
     * </Host_Attestation_Report>
     */
    @Override
    public String getHostAttestationReport(String pcrList, Nonce challenge) throws IOException {
        long getAttReportStart1 = System.currentTimeMillis();
       String attestationReport = "";
        XMLOutputFactory xof = XMLOutputFactory.newInstance();
        XMLStreamWriter xtw;
        StringWriter sw = new StringWriter();
        try {
            xtw = xof.createXMLStreamWriter(sw);
        
            xtw.writeStartDocument();
            xtw.writeStartElement("Host_Attestation_Report");
            xtw.writeAttribute("Host_Name",this.client.hostIpAddress);
            xtw.writeAttribute("vCenterVersion", "5.0");
            xtw.writeAttribute("HostVersion", "5.0");
            //xtw.writeAttribute("TXT_Support", tpmSupport.toString());
        
            long getAttReportStart2 = System.currentTimeMillis();
            log.debug("CitrixClient: before calling to get quote info - " + (getAttReportStart2 - getAttReportStart1) + " milliseconds");
            HashMap<String, Pcr> pcrMap = client.getQuoteInformationForHost(pcrList, challenge);
            long getAttReportStart3 = System.currentTimeMillis();
            log.debug("CitrixClient: Time taken to get quote info - " + (getAttReportStart3 - getAttReportStart2) + " milliseconds");
            
            Iterator it = pcrMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pairs = (Map.Entry)it.next();
                xtw.writeStartElement("PCRInfo");
                Pcr pcr = (Pcr)pairs.getValue();
                xtw.writeAttribute("ComponentName",pcr.getIndex().toString());
                xtw.writeAttribute("DigestValue", pcr.getValue().toString());
                xtw.writeEndElement();
               
                it.remove(); // avoids a ConcurrentModificationException
            }
            xtw.writeEndElement();
            xtw.writeEndDocument();
            xtw.flush();
            xtw.close(); 
            attestationReport = sw.toString();
            long getAttReportStart4 = System.currentTimeMillis();
            log.debug("CitrixClient: before sending the quote info - " + (getAttReportStart4 - getAttReportStart3) + " milliseconds");

        } catch (XMLStreamException ex) {
//            Logger.getLogger(CitrixHostAgent.class.getName()).log(Level.SEVERE, null, ex);
            log.error("Cannot get host attestation report", ex);
        }
        
        log.debug("getHostAttestationReport report:" + attestationReport);       
        return attestationReport;
    }

    @Override
    public boolean isIntelTxtSupported() {
        return true;
    }

    @Override
    public boolean isIntelTxtEnabled() {
        return true;
    }

    @Override
    public boolean isTpmPresent() {
        return true;
    }

    @Override
    public X509Certificate getEkCertificate() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /*  BEFORE
     * -----BEGIN PUBLIC KEY-----
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvNEz3+TStAAndHTc1qwT
NGvZYyB7DD1FshQf+mbQUGJ9HccOXNn5oHB7fWQjODjlDrYyCs7FclSMTLxA3lHX
98QWeWHL2O8t8qrJQQEUWZITmr/ddiNJOOvMeYF0K5if4m84vjgx/pTwwAVyU0Yo
MMXPnRozO8o7zSyRsH4jixALDugrsveEjLQI/cIEFvNjqlhyfumHyJKywNkMH1oJ
4e/f89FkpeDV694lsLs1jguuLLnvroXYJ5Uzeos+F0Pj1zFDUvhWrjVwxsUfAxS8
5uFGTUm6EEl9XiKwi+mgg8ODrY5dh3uE2yKB2T1Qj8BfK55zB8cYbORSsm6/f6Bi
BwIDAQAB
-----END PUBLIC KEY-----
*   AFTER
* -----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvNEz3+TStAAndHTc1qwTNGvZYyB7DD1FshQf+mbQUGJ9HccOXNn5oHB7fWQjODjlDrYyCs7FclSMTLxA3lHX98QWeWHL2O8t8qrJQQEUWZITmr/ddiNJOOvMeYF0K5if4m84vjgx/pTwwAVyU0YoMMXPnRozO8o7zSyRsH4jixALDugrsveEjLQI/cIEFvNjqlhyfumHyJKywNkMH1oJ4e/f89FkpeDV694lsLs1jguuLLnvroXYJ5Uzeos+F0Pj1zFDUvhWrjVwxsUfAxS85uFGTUm6EEl9XiKwi+mgg8ODrY5dh3uE2yKB2T1Qj8BfK55zB8cYbORSsm6/f6BiBwIDAQAB-----END PUBLIC KEY-----
     */
    @Override
    public PublicKey getAik() {
        PublicKey pk = null;
         try {
            String crt  = client.getAIKCertificate();
            log.debug(" crt == " + crt);
            pk = RsaUtil.decodePemPublicKey(crt);
            //client.getAIKCertificate().replace(X509Util.BEGIN_PUBLIC_KEY, "").replace(X509Util.END_PUBLIC_KEY, "").replaceAll("\n","").replaceAll("\r","");  
        }  catch(Exception ex){
            log.error("getAik caught: " + ex.getMessage()); 
            
        }  
        return pk;
    }
    
    @Override
    public PcrManifest getPcrManifest(Nonce challenge) throws IOException {
        log.error("citrix does not support client-specified nonce; ignoring challenge nonce: {}", challenge);
        return getPcrManifest();
    }    

    @Override
    public PcrManifest getPcrManifest() throws IOException {
        PcrManifest pcrManifest = new PcrManifest();
        String pcrList = "0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24";
         HashMap<String, Pcr> pcrMap = client.getQuoteInformationForHost(pcrList);
         log.info("CitrixHostAgent: getQuoteInformationForHost done");
         Iterator it = pcrMap.entrySet().iterator();
         while (it.hasNext()) {
                Map.Entry pairs = (Map.Entry)it.next();
                Pcr pcr = (Pcr)pairs.getValue();
                pcrManifest.setPcr(PcrFactory.newInstance(pcr.getPcrBank(), pcr.getIndex(), pcr.getValue().toByteArray()));                
                //it.remove(); // avoids a ConcurrentModificationException
        }
         log.info("CitrixHostAgent: created PcrManifest");
       return pcrManifest;
    }

    @Override
    public Map<String, String> getHostAttributes()  {
        HashMap<String,String> hm = new HashMap<>();
        // Retrieve the data from the host and add it into the hashmap
        HostInfo hostInfo;
        try {
            hostInfo = client.getHostInfo();
            log.debug("Successfully retrieved the details of the host ruuing OS {}.", hostInfo.getOsName());            
        } catch (NoSuchAlgorithmException | KeyManagementException | MalformedURLException | Types.XenAPIException | XmlRpcException ex) {
            log.error("Unexpected error during retrieval of the host properties. Details : {}", ex.getMessage());
        }
        try {
            // Currently we are just adding the UUID of th host. Going ahead we can add additional details
            hm.put("Host_UUID", client.getSystemUUID());
        } catch(NoSuchAlgorithmException | KeyManagementException | Types.XenAPIException | XmlRpcException ex){
            throw new ASException(ex);
        }
        return hm;
    }
    
    @Override
    public void setAssetTag(com.intel.dcsg.cpg.crypto.Sha1Digest tag) throws IOException {
        try {
            client.setAssetTag(tag);
        }
        catch(Types.XenAPIException | XmlRpcException | NoSuchAlgorithmException | KeyManagementException e) {
            log.error("Unexpected error while setting asset tag", e);
            throw new IOException(e);
        }
    }

    @Override
    public VMAttestationResponse getVMAttestationStatus(String vmInstanceId) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
	@Override
    public X509Certificate getBindingKeyCertificate() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public VMQuoteResponse getVMAttestationReport(VMAttestationRequest obj) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


}
