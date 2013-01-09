/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mountwilson.manifest.strategy;

import com.intel.mountwilson.as.common.ASException;
import com.intel.mtwilson.as.controller.TblMleJpaController;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mtwilson.as.data.TblMle;
import com.intel.mountwilson.manifest.IManifestStrategy;
import com.intel.mountwilson.manifest.data.IManifest;
import com.intel.mountwilson.manifest.strategy.helper.VMWare50Esxi50;
import com.intel.mountwilson.manifest.strategy.helper.VMWare51Esxi51;
import com.intel.mtwilson.agent.vmware.VCenterHost;
import com.intel.mtwilson.agent.vmware.VMwareClient;
import com.intel.mtwilson.agent.HostAgentFactory;
import com.intel.mtwilson.datatypes.ErrorCode;
import com.vmware.vim25.HostTpmAttestationReport;
import com.vmware.vim25.HostTpmDigestInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import javax.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * XXX needs to move to a vmware-specific package, and see also notes about
 * IManifestStrategy interface 
 * @author dsmagadx
 */
public class VMWareManifestStrategy implements
        IManifestStrategy {

    private Logger log = LoggerFactory.getLogger(getClass());
    private EntityManagerFactory entityManagerFactory;

    public EntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory;
    }

    /**
     * XXX the EntityManagerFactory belongs to a specific data storage implementation JPA,
     * and should be replaced with an appropriate Repository object.
     */
    public VMWareManifestStrategy(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    // BUG #497 moving to VmwareHostAgent
    @Override
    public HashMap<String, ? extends IManifest> getManifest(TblHosts host) {
        return getQuoteInformationForHost(host);
    }

    private List<String> getRequestedPcrs(TblHosts tblHosts) {
        ArrayList<String> pcrs = new ArrayList<String>();

        TblMle biosMle = new TblMleJpaController(entityManagerFactory).findMleById(tblHosts.getBiosMleId().getId()); // XXX don't know why we are doing another database lookup, the tblHosts.getBiosMleId() is not an Id it's the full record and it has the same information we are looking up here

        String biosPcrList = biosMle.getRequiredManifestList();

        if (biosPcrList.isEmpty()) {
            throw new ASException(
                    ErrorCode.AS_MISSING_MLE_REQD_MANIFEST_LIST,
                    tblHosts.getBiosMleId().getName(), tblHosts.getBiosMleId().getVersion());
        }


        // Get the Vmm MLE without accessing cache
        TblMle vmmMle = new TblMleJpaController(getEntityManagerFactory()).findMleById(tblHosts.getVmmMleId().getId()); // XXX don't know why we are doing another database lookup, the tblHosts.getVmmMleId() is not an Id it's the full record and it has the same information we are looking up here


        String vmmPcrList = vmmMle.getRequiredManifestList();

        if (vmmPcrList == null || vmmPcrList.isEmpty()) {
            throw new ASException(
                    ErrorCode.AS_MISSING_MLE_REQD_MANIFEST_LIST,
                    tblHosts.getVmmMleId().getName(), tblHosts.getVmmMleId().getVersion());
        }

        pcrs.addAll(Arrays.asList(biosPcrList.split(",")));
        pcrs.addAll(Arrays.asList(vmmPcrList.split(",")));

        return pcrs;
    }

    /**
     * BUG #497
     * This method creates a new vcenterhost object with the database record
     * for the host, and that new object connects to vcenter and gets the information.
     * XXX TODO this needs to be rewritten to fit into HostAgentFactory and HostAgent.
     * It's public but currently ONLY called from getManifest() in this class.
     * getManifest() itself is called from ReportsBO and HostTrustBO.
     * @param host
     * @return 
     */
    public HashMap<String, ? extends IManifest> getQuoteInformationForHost(
            final TblHosts host) {

        // BUG #497 the VCenterHost class is now completely abstract and here we are providing post-processing for the manifest map
        VCenterHost postProcessing = new VCenterHost() {
            @Override
            public HashMap<String, ? extends IManifest> processReport(String esxVersion,
                    HostTpmAttestationReport report) {
                log.info("Processing Attestation Report for ESX version " + esxVersion);
                if(esxVersion.contains("5.1"))
                    return new VMWare51Esxi51().getPcrModuleManiFest(report,
                        getRequestedPcrs(host));
                else
                    return new VMWare50Esxi50().getPcrManiFest(report, 
                            getRequestedPcrs(host));
            }

            @Override
            public HashMap<String, ? extends IManifest> processDigest(String esxVersion,
                    List<HostTpmDigestInfo> htdis) {


                
                return new VMWare50Esxi50().getPcrManiFest(htdis, getRequestedPcrs(host));

            }            
        };
        HostAgentFactory hostAgentFactory = new HostAgentFactory();
        HashMap<String, ? extends IManifest> manifestMap = hostAgentFactory.getManifest(host, postProcessing);
        
        log.info("PCR map {}", manifestMap);

        return manifestMap;
    }
}
