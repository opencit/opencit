/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.policy.impl;

import com.intel.mountwilson.as.common.ASException;
import java.util.HashSet;
import com.intel.mtwilson.agent.Vendor;
import com.intel.mtwilson.as.business.HostBO;
import com.intel.mtwilson.as.controller.TblLocationPcrJpaController;
import com.intel.mtwilson.as.controller.TblMleJpaController;
import com.intel.mtwilson.as.controller.TblModuleManifestJpaController;
import com.intel.mtwilson.as.controller.TblPcrManifestJpaController;
import com.intel.mtwilson.as.data.TblHostSpecificManifest;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mtwilson.as.data.TblLocationPcr;
import com.intel.mtwilson.as.data.TblMle;
import com.intel.mtwilson.as.data.TblModuleManifest;
import com.intel.mtwilson.as.data.TblPcrManifest;
import com.intel.mtwilson.datatypes.ErrorCode;
import com.intel.mtwilson.model.Bios;
import com.intel.mtwilson.model.Measurement;
import com.intel.mtwilson.model.Pcr;
import com.intel.mtwilson.model.PcrEventLog;
import com.intel.mtwilson.model.PcrIndex;
import com.intel.mtwilson.model.PcrManifest;
import com.intel.mtwilson.model.Sha1Digest;
import com.intel.mtwilson.model.Vmm;
import com.intel.mtwilson.policy.*;
import com.intel.mtwilson.policy.impl.vendor.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example for setting up a whitelist:
 * 
 * HostTrustPolicyFactory hostTrustPolicyFactory = new HostTrustPolicyFactory(entityManagerFactory);
 * List<TrustPolicy> trustPolicy = createTrustPolicyWhitelistFromHost(tblHostsRecord, hostReport);
 * add general or non-vendor-specific trust policies, then:
 * saveTrustPolicyForMle(....);
 * 
 * 
 * Example for loading and using the trust policy "whitelist" for a host:
 * HostTrustPolicyFactory hostTrustPolicyFactory = new HostTrustPolicyFactory(entityManagerFactory);
 * TrustPolicy trustPolicy = loadTrustPolicyForHost(tblHostsRecord)
 * PolicyEngine policyEngine = new PolicyEngine();
 * 
 * @author jbuhacoff
 */
public class HostTrustPolicyFactory {
 
    private Logger log = LoggerFactory.getLogger(getClass());
    
    private EntityManagerFactory entityManagerFactory;
    private TblMleJpaController mleJpaController;
    private TblPcrManifestJpaController pcrManifestJpaController;
    private TblLocationPcrJpaController locationPcrJpaController;

    private Map<Vendor,VendorHostTrustPolicyFactory> vendorFactoryMap = new EnumMap<Vendor,VendorHostTrustPolicyFactory>(Vendor.class);
    //private Logger log = LoggerFactory.getLogger(getClass());
    public HostTrustPolicyFactory(EntityManagerFactory entityManagerFactory) {
        // we initialize the map with the known vendors; but this could also be done through IoC
        vendorFactoryMap.put(Vendor.INTEL, new IntelHostTrustPolicyFactory());
        vendorFactoryMap.put(Vendor.CITRIX, new CitrixHostTrustPolicyFactory());
        vendorFactoryMap.put(Vendor.VMWARE, new VmwareHostTrustPolicyFactory());
        
        this.entityManagerFactory = entityManagerFactory;
        mleJpaController = new TblMleJpaController(entityManagerFactory);
        pcrManifestJpaController = new TblPcrManifestJpaController(entityManagerFactory);
        locationPcrJpaController = new TblLocationPcrJpaController(entityManagerFactory);
    }
    
    /**
     * Optional to call this -  the HostTrustPolicyFactory default constructor already
     * creates a map with Intel, Vmware, and Citrix vendor-specific factories.
     * It is recommended to supply an EnumMap instance
     * @param map of vendor host agent factories
     */
    public void setVendorFactoryMap(Map<Vendor,VendorHostTrustPolicyFactory> map) {
        vendorFactoryMap = map;
    }
        
    
    public EntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory;
    }
    
    
    // see the notepad notes
    // XXX TODO but this method needs to move to another class where we serialize the policies... 
    // in this class it's only deserialize!!
    public TrustPolicy createHostSpecificTrustPolicy(HostReport hostReport, TblMle biosMleId, TblMle vmmMleId) {
        // FOR VMWARE, WE NEED TO GET THE "COMMAND LINE" MODULE,  AND CREATE A HOST-SPECIFIC POLICY FOR IT
        
        // IF THERE IS NOT A HOST -SPECIFIC POLICY THAT IS CREATED, JUST RETURN NULL
        return null;
    }
    
    /**
     * XXX TODO should this go into a jpa controller ? or just USE the jpa controllers ?
     * The purpose of this method is to instantiate a list of policies that have been
     * saved in the database.
     * 
     * This method returns just one policy - so probably it's an instance of RequireAll or RequireAny and
     * it contains other policies within. 
     */
    public TrustPolicy loadTrustPolicyForHost(TblHosts host) {
        Bios bios = new Bios(host.getBiosMleId().getName(), host.getBiosMleId().getVersion(), host.getBiosMleId().getOemId().getName());
        Vmm vmm = new Vmm(host.getVmmMleId().getName(), host.getVmmMleId().getVersion(), host.getVmmMleId().getOsId().getName(), host.getVmmMleId().getOsId().getVersion());
        ArrayList<TrustPolicy> list = new ArrayList<TrustPolicy>();
        list.add(new TrustedBios(loadTrustPolicyListForBios(bios,host)));
        list.add(new TrustedVmm(loadTrustPolicyListForVmm(vmm,host)));
        list.add(new TrustedLocation(loadTrustPolicyListForLocation(host)));
        return new RequireAll(list);
    }
    
    /**
     * XXX TODO should this go into a jpa controller ? or just USE the jap controllers 
     * to serialize the policy ?   also this is for a SPECIFIC HOST , NOT for a whitelist.
     * @param host
     * @param trustPolicy 
     */
    public void saveTrustPolicyForHost(TblHosts host, TrustPolicy trustPolicy) {
        
    }
    
    /**
     * XXX TODO should this go into a jpa controller ? or just USE the jap controllers 
     * to serialize the policy ?   also this is for an MLE (whitelist) , NOT for a specific host
     * @param host
     * @param trustPolicy 
     */
    public void saveTrustPolicyForMle(TblMle host, TrustPolicy trustPolicy) {
        
    }
    
    // moved here from both VMWareManifestStrategy (returned List<String>) and TrustAgentManifestStrategy  (returned String with comma-separated list)
    // but ... XXX do we even need this?  is the list of indices useless?  because if we're going to query the whitelist, we may as well get ALL the
    // information and just ninstantiate policies out of it !!!
    private List<PcrIndex> getPcrIndexList(TblHosts tblHosts) {
        ArrayList<PcrIndex> pcrs = new ArrayList<PcrIndex>();

        TblMle biosMle = mleJpaController.findMleById(tblHosts.getBiosMleId().getId()); // XXX don't know why we are doing another database lookup, the tblHosts.getBiosMleId() is not an Id it's the full record and it has the same information we are looking up here

        String biosPcrList = biosMle.getRequiredManifestList();

        if (biosPcrList.isEmpty()) {
            throw new ASException(
                    ErrorCode.AS_MISSING_MLE_REQD_MANIFEST_LIST,
                    tblHosts.getBiosMleId().getName(), tblHosts.getBiosMleId().getVersion());
        }

        String[] biosPcrs = biosPcrList.split(",");
        for(String str : biosPcrs) {
            pcrs.add(new PcrIndex(Integer.valueOf(str)));
        }

        // Get the Vmm MLE without accessing cache
        TblMle vmmMle = mleJpaController.findMleById(tblHosts.getVmmMleId().getId()); // XXX don't know why we are doing another database lookup, the tblHosts.getVmmMleId() is not an Id it's the full record and it has the same information we are looking up here


        String vmmPcrList = vmmMle.getRequiredManifestList();

        if (vmmPcrList == null || vmmPcrList.isEmpty()) {
            throw new ASException(
                    ErrorCode.AS_MISSING_MLE_REQD_MANIFEST_LIST,
                    tblHosts.getVmmMleId().getName(), tblHosts.getVmmMleId().getVersion());
        }

        String[] vmmPcrs = vmmPcrList.split(",");
        
        for(String str : vmmPcrs) {
            pcrs.add(new PcrIndex(Integer.valueOf(str)));
        }

        return pcrs;
    }
    
    
    ///////////////////////////////////// BEYOND THIS POINT,  CODE TAKEN FROM THE OLD  "GKV FACTORY"  ///// NEED TO LOAD FROM DB, THEN TURN INTO POLICIES !!!
    
    /**
     * Creates a list of PcrMatchesConstant policies for the given bios.
     * XXX the hard-coded rule right now is that we don't check for modules in bios pcr's,
     * and there are no host-specific bios pcrs in the database... but we add the host parameter
     * anyway to prepare for a future where anything is possible, and to make a consistent
     * interface so that callers need only make ONE call and get the right set of policies, 
     * and not need to be concerned whether those policies are host-specific or not. we
     * take care of all of that here.
     * @param bios
     * @return 
     */
    public List<TrustPolicy> loadTrustPolicyListForBios(Bios bios, TblHosts tblHosts) {
        TblMle biosMle = mleJpaController.findBiosMle(bios.getName(), bios.getVersion(), bios.getOem());
        log.debug("HostTrustPolicyFactory found BIOS MLE: {}", biosMle.getName());
        Collection<TblPcrManifest> pcrInfoList = biosMle.getTblPcrManifestCollection();
        ArrayList<TrustPolicy> list = new ArrayList<TrustPolicy>();
        for(TblPcrManifest pcrInfo : pcrInfoList) {
            PcrIndex pcrIndex = new PcrIndex(Integer.valueOf(pcrInfo.getName()));
            Sha1Digest pcrValue = new Sha1Digest(pcrInfo.getValue());
            log.debug("... PCR {} value {}", pcrIndex.toString(), pcrValue.toString());
            list.add(new PcrMatchesConstant(new Pcr(pcrIndex, pcrValue)));
        }
        return list;
    }

    public List<TrustPolicy> loadTrustPolicyListForVmm(Vmm vmm, TblHosts tblHosts) {
        TblMle vmmMle = mleJpaController.findVmmMle(vmm.getName(), vmm.getVersion(), vmm.getOsName(), vmm.getOsVersion());
        log.debug("HostTrustPolicyFactory found VMM MLE: {}", vmmMle.getName());
        
        ArrayList<TrustPolicy> list = new ArrayList<TrustPolicy>();

        // first, get a list of all the pcr's in the whitelist for this vmm
        Collection<TblPcrManifest> pcrInfoList = vmmMle.getTblPcrManifestCollection();
        for(TblPcrManifest pcrInfo : pcrInfoList) {
            PcrIndex pcrIndex = new PcrIndex(Integer.valueOf(pcrInfo.getName()));
            Sha1Digest pcrValue = new Sha1Digest(pcrInfo.getValue());
            log.debug("... PCR {} value {}", pcrIndex.toString(), pcrValue.toString());
            list.add(new PcrMatchesConstant(new Pcr(pcrIndex, pcrValue)));
        }
        
        // second, get a list of any modules in the whitelist for this vmm  (remember if it doesn't apply, then it won't be in the database)
        // XXX we use the PcrEventLogIncludes policy so that if the host as any extra modules it's not an error ...   but it's a security risk because could be a new module that is malware!
        // XXX right now this mechanism is very rigid... straightforward adaptation of our existing database schema to policies.  but the policy mechanism
        // is a lot more flexible and probably needs changes to the schema to enable its full power. for example we could make a list of mandatory modules,
        // and a list of optional known-safe modules, and then combine them here,   and that would solve he use case of authorized optional modules while
        // still being secure against any new unknown modules.
        Collection<TblModuleManifest> pcrModuleInfoList = vmmMle.getTblModuleManifestCollection();
        for(TblModuleManifest moduleInfo : pcrModuleInfoList) {
            PcrIndex pcrIndex = new PcrIndex(Integer.valueOf(moduleInfo.getExtendedToPCR()));
            log.debug("... MODULE for PCR {}", pcrIndex.toString());
            list.add(new PcrEventLogIntegrity(pcrIndex)); // if we're going to look for things in the host's event log, it needs to have integrity
            HashSet<Measurement> measurements = new HashSet<Measurement>();
            
            HashMap<String,String> info = new HashMap<String,String>();
            // info.put("EventType", manifest.getEventType()); // XXX  we don't have an "EventType" field defined in the "mw_module_manifest" table ... should add it 
            info.put("EventName", moduleInfo.getEventID().getName());
            info.put("ComponentName", moduleInfo.getComponentName());

            if( moduleInfo.getUseHostSpecificDigestValue() ) {
                Collection<TblHostSpecificManifest> hostSpecificManifest = moduleInfo.getTblHostSpecificManifestCollection();
                for(TblHostSpecificManifest hostSpecificModule : hostSpecificManifest) {
                    Measurement m = new Measurement(new Sha1Digest(hostSpecificModule.getDigestValue()), moduleInfo.getDescription(), info); // XXX using the description, but maybe we need to add a helpr function so we can use something like vendor-modulename-moduleversion   or vendor-eventdesc
                    measurements.add(m);
                }
            }
            else {
                // XXX making assumptions about the nature of the module... due to what we store in the database when adding whitelist and host.  
                // XXX the only way to fix this is to change the schema so it can accomodate all the custom info we need w/o needing to know WHAT it is from here.
                info.put("PackageName", moduleInfo.getPackageName());
                info.put("PackageVersion", moduleInfo.getPackageVersion());
                info.put("PackageVendor", moduleInfo.getPackageVendor());
                Measurement m = new Measurement(new Sha1Digest(moduleInfo.getDigestValue()), moduleInfo.getDescription(), info); // XXX using the description, but maybe we need to add a helpr function so we can use something like vendor-modulename-moduleversion   or vendor-eventdesc
                measurements.add(m);
            }
            list.add(new PcrEventLogIncludes(pcrIndex, measurements));
        }
        return list;
    }
    
    // XXX FOR SUDHIR  ... IF YOU CONVERT HOST.LOCATION TO ID YOU CAN USE AS-IS... OTHERWISE NEED TO LOOK UP LOCATION BY STRING VALUE ... THAT METHOD ISN'T IN THE CONTROLLER RIGHT NOW
    public List<TrustPolicy> loadTrustPolicyListForLocation(TblHosts tblHosts) {
//        TblLocationPcr locationPcr = locationPcrJpaController.findTblLocationPcr(tblHosts.getLocationId());
        ArrayList<TrustPolicy> list = new ArrayList<TrustPolicy>();
//        PcrIndex pcrIndex = HostBO.LOCATION_PCR;
//        Sha1Digest pcrValue = new Sha1Digest(locationPcr.getPcrValue());
//        list.add(new PcrMatchesConstant(new Pcr(pcrIndex, pcrValue)));
        return list;
    }

}
