/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.agent.citrix;

import com.intel.mountwilson.manifest.data.IManifest;
import com.intel.mountwilson.manifest.data.PcrManifest;
import com.intel.mountwilson.ta.data.hostinfo.HostInfo;
import com.intel.mtwilson.agent.HostAgent;
import com.intel.mtwilson.agent.vmware.VCenterHost;
import com.intel.mtwilson.crypto.X509Util;
import com.intel.mtwilson.datatypes.Aik;
import com.intel.mtwilson.datatypes.Nonce;
import com.intel.mtwilson.datatypes.Pcr;
import com.intel.mtwilson.datatypes.PcrIndex;
import com.intel.mtwilson.datatypes.TpmQuote;
import com.intel.mtwilson.datatypes.TxtHostRecord;
import com.intel.mtwilson.model.PcrManifest;
import com.xensource.xenapi.Types.BadServerResponse;
import com.xensource.xenapi.Types.XenAPIException;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.apache.commons.codec.binary.Base64;
import org.apache.xmlrpc.XmlRpcException;


/**
 *
 * @author stdalex
 */
public class CitrixHostAgent implements HostAgent{
    private CitrixClient client;
    
    
    public CitrixHostAgent(CitrixClient client) {
     this.client = client;
     
    }
    
    @Override
    public boolean isTpmAvailable() {
        return true;
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
        X509Certificate cert = null;
        try {
            String crt = client.getAIKCertificate().replaceAll("\n", "").replaceAll("\r","");
            System.out.println("decodeding pem == \n"+crt);
             cert = X509Util.decodePemCertificate(crt);  
             
        }  catch(Exception ex){
            System.out.println("getAikCert caught: " + ex.getMessage());
            
        }
        return cert;
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
    public List<Pcr> getPcrValues() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<String> getModuleManifest() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public List<Pcr> getPcrHistory(PcrIndex number) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public HashMap<String, ? extends IManifest> getManifest(VCenterHost postProcessing) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public TxtHostRecord getHostDetails() throws IOException {
        //throw new UnsupportedOperationException("Not supported yet.");
        TxtHostRecord record = new TxtHostRecord();
        HostInfo info = null;
        try {
            info = this.client.getHostInfo();
        } catch(Exception ex){
            System.out.println("getHostDetails getHostInfo caught: " + ex.getMessage());
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
        
        try {
            record.AIK_Certificate = client.getAIKCertificate();
        }  catch(Exception ex){
            System.out.println("getHostDetails getAikCert caught: " + ex.getMessage());
       }
        
        return record;
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
    public String getHostAttestationReport(String pcrList) throws IOException {
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
        
            HashMap<String, PcrManifest> pcrMap = client.getQuoteInformationForHost(pcrList);
        
            Iterator it = pcrMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pairs = (Map.Entry)it.next();
                xtw.writeStartElement("PCRInfo");
                PcrManifest pcr = (PcrManifest)pairs.getValue();
                xtw.writeAttribute("ComponentName",Integer.toString(pcr.getPcrNumber()));
                xtw.writeAttribute("DigestValue", pcr.getPcrValue());
                xtw.writeEndElement();
               
                it.remove(); // avoids a ConcurrentModificationException
            }
            xtw.writeEndElement();
            xtw.writeEndDocument();
            xtw.flush();
            xtw.close(); 
            attestationReport = sw.toString();
        
        } catch (XMLStreamException ex) {
            Logger.getLogger(CitrixHostAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        System.err.println("stdalex-error getHostAttestationReport report:" + attestationReport);
        return attestationReport;
    }
    
}
