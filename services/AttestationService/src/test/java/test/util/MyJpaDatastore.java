/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.util;

import com.intel.mtwilson.MyPersistenceManager;
import com.intel.mtwilson.as.business.HostBO;
import com.intel.mtwilson.as.controller.MwMleSourceJpaController;
import com.intel.mtwilson.as.controller.TblEventTypeJpaController;
import com.intel.mtwilson.as.controller.TblHostsJpaController;
import com.intel.mtwilson.as.controller.TblMleJpaController;
import com.intel.mtwilson.as.controller.TblModuleManifestJpaController;
import com.intel.mtwilson.as.controller.TblOemJpaController;
import com.intel.mtwilson.as.controller.TblOsJpaController;
import com.intel.mtwilson.as.controller.TblPackageNamespaceJpaController;
import com.intel.mtwilson.as.controller.TblPcrManifestJpaController;
import com.intel.mtwilson.as.controller.TblHostSpecificManifestJpaController;
import com.intel.mtwilson.as.controller.TblSamlAssertionJpaController;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mtwilson.crypto.Aes128;
import com.intel.mtwilson.crypto.CryptographyException;
import com.intel.mtwilson.policy.impl.HostTrustPolicyManager;

/**
 * Convenience class to use in JUnit tests... one stop shop to get an instance of any JPA Controller that you need.
 * It extends MyPersistenceManager which means the database connection settings come from your local Java Preferences.
 * See test.util.MyPersistenceManager for more information on how to check and update your local preferences.
 * 
 * HOW TO USE IT:
 * 
 * MyJpaDatastore pm = new MyJpaDatastore();
 * TblHosts host = pm.getHostsJpa().findHostByName(...);
 * 
 * @deprecated please use My.jpa() instead  (added in feature-autotest branch)
 * @author jbuhacoff
 */
public class MyJpaDatastore  {
    private MyPersistenceManager pm;
    
    public MyJpaDatastore(MyPersistenceManager pm) {
        this.pm = pm;
    }
    
        private TblHostsJpaController hostsJpa = null;
        private TblMleJpaController mleJpa = null;
        private TblOsJpaController osJpa = null;
        private TblOemJpaController oemJpa = null;
        private TblPcrManifestJpaController pcrJpa = null;
        private TblModuleManifestJpaController moduleJpa = null;
        private TblHostSpecificManifestJpaController hostSpecificModuleJpa = null;
        private TblEventTypeJpaController eventTypeJpa = null; // XXX we really don't need this, it's too specific to vmware and tehre's nothing configurable about it.
        private TblPackageNamespaceJpaController packageNamespaceJpa = null;
        private HostTrustPolicyManager hostTrustFactory = null;
        private MwMleSourceJpaController mleSourceJpa = null;
        private TblSamlAssertionJpaController samlJpa = null;
        
        public TblHostsJpaController getHostsJpa() throws CryptographyException {
            if( hostsJpa == null ) {
                /*
                hostsJpa = new TblHostsJpaController(pm.getEntityManagerFactory("ASDataPU"), pm.getDek());
                */
                hostsJpa = new TblHostsJpaController(pm.getEntityManagerFactory("ASDataPU"));
                TblHosts.dataCipher = new HostBO.Aes128DataCipher(new Aes128(pm.getDek()));
            }
            return hostsJpa;
        }
        public TblMleJpaController getMleJpa() {
            if( mleJpa == null ) {
                mleJpa = new TblMleJpaController(pm.getEntityManagerFactory("ASDataPU"));
            }
            return mleJpa;
        }
        public TblOsJpaController getOsJpa() {
            if( osJpa == null ) {
                osJpa = new TblOsJpaController(pm.getEntityManagerFactory("ASDataPU"));
            }
            return osJpa;
        }
        public TblOemJpaController getOemJpa() {
            if( oemJpa == null ) {
                oemJpa = new TblOemJpaController(pm.getEntityManagerFactory("ASDataPU"));
            }
            return oemJpa;
        }
        public TblPcrManifestJpaController getPcrJpa() {
            if( pcrJpa == null ) {
                pcrJpa = new TblPcrManifestJpaController(pm.getEntityManagerFactory("ASDataPU"));
            }
            return pcrJpa;
        }
        public TblModuleManifestJpaController getModuleJpa() {
            if( moduleJpa == null ) {
                moduleJpa = new TblModuleManifestJpaController(pm.getEntityManagerFactory("ASDataPU"));
            }
            return moduleJpa;
        }
        public TblHostSpecificManifestJpaController getHostSpecificModuleJpa() {
            if( hostSpecificModuleJpa == null ) {
                hostSpecificModuleJpa = new TblHostSpecificManifestJpaController(pm.getEntityManagerFactory("ASDataPU"));
            }
            return hostSpecificModuleJpa;
        }
        public TblEventTypeJpaController getEventTypeJpa() {
            if( eventTypeJpa == null ) {
                eventTypeJpa = new TblEventTypeJpaController(pm.getEntityManagerFactory("ASDataPU"));
            }
            return eventTypeJpa;
        }
        public TblPackageNamespaceJpaController getPackageNamespaceJpa() {
            if( packageNamespaceJpa == null ) {
                packageNamespaceJpa = new TblPackageNamespaceJpaController(pm.getEntityManagerFactory("ASDataPU"));
            }
            return packageNamespaceJpa;
        }
        public MwMleSourceJpaController getMleSourceJpa() {
            if( mleSourceJpa == null ) {
                mleSourceJpa = new MwMleSourceJpaController(pm.getEntityManagerFactory("ASDataPU"));
            }
            return mleSourceJpa;
        }
        public TblSamlAssertionJpaController getSamlJpa() {
            if( samlJpa == null ) {
                samlJpa = new TblSamlAssertionJpaController(pm.getEntityManagerFactory("ASDataPU"));
            }
            return samlJpa;
        }
        public HostTrustPolicyManager getHostTrustFactory() {
            if( hostTrustFactory == null ) {
                hostTrustFactory = new HostTrustPolicyManager(pm.getEntityManagerFactory("ASDataPU"));
            }
            return hostTrustFactory;
        }

    }