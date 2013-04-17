/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.util;

import com.intel.mtwilson.as.controller.MwMleSourceJpaController;
import com.intel.mtwilson.as.controller.TblHostsJpaController;
import com.intel.mtwilson.as.controller.TblMleJpaController;
import com.intel.mtwilson.as.controller.TblModuleManifestJpaController;
import com.intel.mtwilson.as.controller.TblOemJpaController;
import com.intel.mtwilson.as.controller.TblOsJpaController;
import com.intel.mtwilson.as.controller.TblPcrManifestJpaController;
import com.intel.mtwilson.as.controller.TblHostSpecificManifestJpaController;
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
 * @author jbuhacoff
 */
public class MyJpaDatastore extends MyPersistenceManager {

        TblHostsJpaController hostsJpa = null;
        TblMleJpaController mleJpa = null;
        TblOsJpaController osJpa = null;
        TblOemJpaController oemJpa = null;
        TblPcrManifestJpaController pcrJpa = null;
        TblModuleManifestJpaController moduleJpa = null;
        TblHostSpecificManifestJpaController hostSpecificModuleJpa = null;
        HostTrustPolicyManager hostTrustFactory = null;
        MwMleSourceJpaController mleSourceJpa = null;
        
        public TblHostsJpaController getHostsJpa() throws CryptographyException {
            if( hostsJpa == null ) {
                hostsJpa = new TblHostsJpaController(getEntityManagerFactory("ASDataPU"), getDek());
            }
            return hostsJpa;
        }
        public TblMleJpaController getMleJpa() {
            if( mleJpa == null ) {
                mleJpa = new TblMleJpaController(getEntityManagerFactory("ASDataPU"));
            }
            return mleJpa;
        }
        public TblOsJpaController getOsJpa() {
            if( osJpa == null ) {
                osJpa = new TblOsJpaController(getEntityManagerFactory("ASDataPU"));
            }
            return osJpa;
        }
        public TblOemJpaController getOemJpa() {
            if( oemJpa == null ) {
                oemJpa = new TblOemJpaController(getEntityManagerFactory("ASDataPU"));
            }
            return oemJpa;
        }
        public TblPcrManifestJpaController getPcrJpa() {
            if( pcrJpa == null ) {
                pcrJpa = new TblPcrManifestJpaController(getEntityManagerFactory("ASDataPU"));
            }
            return pcrJpa;
        }
        public TblModuleManifestJpaController getModuleJpa() {
            if( moduleJpa == null ) {
                moduleJpa = new TblModuleManifestJpaController(getEntityManagerFactory("ASDataPU"));
            }
            return moduleJpa;
        }
        public TblHostSpecificManifestJpaController getHostSpecificModuleJpa() {
            if( hostSpecificModuleJpa == null ) {
                hostSpecificModuleJpa = new TblHostSpecificManifestJpaController(getEntityManagerFactory("ASDataPU"));
            }
            return hostSpecificModuleJpa;
        }
        public MwMleSourceJpaController getMleSourceJpa() {
            if( mleSourceJpa == null ) {
                mleSourceJpa = new MwMleSourceJpaController(getEntityManagerFactory("ASDataPU"));
            }
            return mleSourceJpa;
        }
        public HostTrustPolicyManager getHostTrustFactory() {
            if( hostTrustFactory == null ) {
                hostTrustFactory = new HostTrustPolicyManager(getEntityManagerFactory("ASDataPU"));
            }
            return hostTrustFactory;
        }

    }