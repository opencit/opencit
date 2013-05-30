/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jpa;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;
//import org.apache.commons.dbcp.PoolingDataSource;

/**
 * This class represents the contents of persistence.xml
 * @author jbuhacoff
 */
public class CustomPersistenceUnitInfoImpl implements PersistenceUnitInfo {
    protected URL url;
    protected DataSource ds;
    protected Properties jpaProperties;
    protected String persistenceUnitName; // ex: ASDataPU
    protected String persistenceUnitProvider; // ex: org.eclipse.persistence.jpa.PersistenceProvider
    protected String transactionType; // ex: RESOURCE_LOCAL, JTA (enum PersistenceUnitTransactionType)
    protected List<String> classList; // ex: com.mtwilson.as.data.MwCertificate, com.mtwilson.as.data.MwOem
//    protected String jdbcDriver; // ex: com.mysql.jdbc.Driver
//    protected String jdbcUrl; // ex: jdbc:mysql://127.0.0.1:3306/mw_as
//    protected String jdbcUsername; // ex: root
//    protected String jdbcPassword; // ex: password
            
    @Override
    public String getPersistenceUnitName() {
        return persistenceUnitName;
    }

    @Override
    public String getPersistenceProviderClassName() {
        return persistenceUnitProvider;
    }

    @Override
    public PersistenceUnitTransactionType getTransactionType() {
        return PersistenceUnitTransactionType.valueOf(transactionType);
    }
    
    @Override
    public DataSource getJtaDataSource() {
//        throw new UnsupportedOperationException("Not supported yet.");
//        return mds; // XXX TODO need to create the transaction-managed jta/jpa data source
        return ds;
    }

    @Override
    public DataSource getNonJtaDataSource() {
//        throw new UnsupportedOperationException("Not supported yet.");
        return ds;
    }

    @Override
    public List<String> getMappingFileNames() {
//        throw new UnsupportedOperationException("Not supported yet.");
        return Collections.EMPTY_LIST;
    }

    @Override
    public List<URL> getJarFileUrls() {
//        throw new UnsupportedOperationException("Not supported yet.");
        return Collections.EMPTY_LIST;
    }

    @Override
    public URL getPersistenceUnitRootUrl() {
        if( url != null ) { return url; }
        try {
            return new URL("http://localhost");
        }
        catch(MalformedURLException e) {
            throw new IllegalArgumentException("Invalid persistence unit root url: "+e.getLocalizedMessage());
        }
    }

    @Override
    public List<String> getManagedClassNames() {
        return classList;
    }

    @Override
    public boolean excludeUnlistedClasses() {
//        throw new UnsupportedOperationException("Not supported yet.");
        return true;
    }

    @Override
    public SharedCacheMode getSharedCacheMode() {
//        throw new UnsupportedOperationException("Not supported yet.");
        return SharedCacheMode.NONE;
    }

    @Override
    public ValidationMode getValidationMode() {
//        throw new UnsupportedOperationException("Not supported yet.");
        return ValidationMode.NONE;
    }

    @Override
    public Properties getProperties() {
//        throw new UnsupportedOperationException("Not supported yet.");
        return jpaProperties;
    }

    @Override
    public String getPersistenceXMLSchemaVersion() {
//        throw new UnsupportedOperationException("Not supported yet.");
        return "2.0";
    }

    @Override
    public ClassLoader getClassLoader() {
//        throw new UnsupportedOperationException("Not supported yet.");
//        return ClassLoader.getSystemClassLoader();
        return getClass().getClassLoader();
    }

    /**
     * XXX currently we do not support this feature; our usage of EclipseLink
     * appears to be working well without it. 
     * @param ct 
     */
    @Override
    public void addTransformer(ClassTransformer ct) {
//        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ClassLoader getNewTempClassLoader() {
//        throw new UnsupportedOperationException("Not supported yet.");
//        return ClassLoader.getSystemClassLoader();
        return getClass().getClassLoader();
    }
    
}
