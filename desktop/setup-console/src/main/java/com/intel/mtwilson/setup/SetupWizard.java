/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.setup;

import com.intel.mtwilson.My;
import com.intel.mtwilson.MyPersistenceManager;
import com.intel.mtwilson.crypto.Aes128;
import com.intel.dcsg.cpg.crypto.CryptographyException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import javax.crypto.SecretKey;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jbuhacoff
 */
public class SetupWizard {
    private Logger log = LoggerFactory.getLogger(getClass());    
    private Configuration conf;
    
    public SetupWizard(Configuration conf) {
        this.conf = conf;
    }
    
    public Connection getDatabaseConnection() throws SetupException, IOException {
        try {
            Properties p = MyPersistenceManager.getASDataJpaProperties(My.configuration());
            Class.forName(p.getProperty("javax.persistence.jdbc.driver"));
            String url =  p.getProperty("javax.persistence.jdbc.url");
            String user =  p.getProperty("javax.persistence.jdbc.user");
            String pass =  p.getProperty("javax.persistence.jdbc.password");
            
//            System.out.println(String.format("SetupWizard.java db url: %s", url));
//            System.out.println(String.format("SetupWizard.java db user: %s", user));
//            System.out.println(String.format("SetupWizard.java db pass: %s", pass));
            
            Connection conn = DriverManager.getConnection(url, user, pass);
            
            return conn;
        }
        catch (ClassNotFoundException e) {
            throw new SetupException("Cannot connect to database", e);
        }
        catch (SQLException e) {
            throw new SetupException("Cannot connect to database", e);
        }
    }

    public Connection getMSDatabaseConnection() throws SetupException, IOException {
        try {
            Class.forName(conf.getString("mountwilson.ms.db.driver", My.configuration().getDatabaseDriver()));
            /*
             * Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(
                    String.format("jdbc:mysql://%s:%d/%s",
                        conf.getString("mountwilson.ms.db.host", conf.getString("mtwilson.db.host", "127.0.0.1")),
                        conf.getInteger("mountwilson.ms.db.port", conf.getInteger("mtwilson.db.port", 3306)),
                        conf.getString("mountwilson.ms.db.schema", conf.getString("mtwilson.db.schema"))),
                    conf.getString("mountwilson.ms.db.user", conf.getString("mtwilson.db.user")),
                    conf.getString("mountwilson.ms.db.password", conf.getString("mtwilson.db.password")));
                    */
            String dbms = (conf.getString("mountwilson.ms.db.driver", conf.getString("mtwilson.db.driver", "com.mysql.jdbc.Driver")).contains("mysql")) ? "mysql" : "postgresql";
            String url =conf.getString("mountwilson.ms.db.url",
                    conf.getString("mtwilson.db.url",
                    String.format("jdbc:"+dbms+"://%s:%s/%s?autoReconnect=true",
                    conf.getString("mountwilson.ms.db.host", My.configuration().getDatabaseHost()),
                    conf.getString("mountwilson.ms.db.port", My.configuration().getDatabasePort()),
                    conf.getString("mountwilson.ms.db.schema", My.configuration().getDatabaseSchema()))));
            String user = conf.getString("mountwilson.ms.db.user", My.configuration().getDatabaseUsername());
            String pass = conf.getString("mountwilson.ms.db.password", My.configuration().getDatabasePassword());
            Connection conn = DriverManager.getConnection(url, user, pass);
            return conn;        }
        catch (ClassNotFoundException e) {
            throw new SetupException("Cannot connect to database", e);
        }
        catch (SQLException e) {
            throw new SetupException("Cannot connect to database", e);
        }
    }
    
    /***** UNUSED
    public void closeConnection(Connection c) throws SetupException {
        try {
            if( c != null ) {
                c.close();
            }
        }
        catch (SQLException e) {
            throw new SetupException("Error while closing database connection", e);
        }
    }*/
    
    public void encryptVmwareConnectionStrings() throws SetupException, IOException {
        try {
            try (Connection c = getDatabaseConnection()) {
                /*
                 if( !allNonEmptyFieldsInTableBeginWith(c, "tbl_hosts", "http") ) {
                 throw new SetupException("Not all non-empty fields in tbl_hosts are valid connection strings");
                 }
                 */
                String dekBase64 = loadOrCreateSecretKeyAes128("mtwilson.as.dek");
                encryptAllNonEmptyFieldsInTableWithKey(c, "mw_hosts", "AddOn_Connection_Info", dekBase64); 
            }
        } catch (SQLException e) {
            throw new SetupException("Error while closing database connection", e);
        }
    }
    
    private String loadOrCreateSecretKeyAes128(String name) throws SetupException {
        String dekBase64 = conf.getString(name);
        if( dekBase64 == null || dekBase64.isEmpty() ) {
            try {
                System.out.println(String.format("Generating Data Encryption Key %s...", name));
                SecretKey dek = Aes128.generateKey();
                dekBase64 = Base64.encodeBase64String(dek.getEncoded());
                //conf.setProperty(name, dekBase64); // this does not automatically save to the configuration file
                // save the new dek to configuration file.    the Properties object inserts backslash-escapes before punctuation like : , = , etc. which affects the values... not sure if they'll be read in properly!!!
                Properties xxxTodoSubclassConf = new Properties();
                try (FileInputStream in = new FileInputStream("/etc/intel/cloudsecurity/mtwilson.properties")) {
                    xxxTodoSubclassConf.load(in);
                }
                xxxTodoSubclassConf.setProperty(name, dekBase64);
                try (FileOutputStream out = new FileOutputStream("/etc/intel/cloudsecurity/mtwilson.properties")) {
                    xxxTodoSubclassConf.store(out, "auto-saved");
                }
                My.reset();
            } catch (CryptographyException e) {
                throw new SetupException(String.format("Cannot create Data Encryption Key %s", name), e);
            } catch (IOException e) {
                throw new SetupException(String.format("Cannot save Data Encryption Key %s", name), e);
            }
        }
        return dekBase64;
    }
    
    /**
     * @param c
     * @param tableName
     * @param beginWith
     * @return
     * @throws SetupException 
     */
    // commenting oput unused function (6/11 1.2)
    /*
    private boolean allNonEmptyFieldsInTableBeginWith(Connection c, String tableName, String beginWith) throws SetupException {
        try {
            boolean condition = true;
            Statement s = c.createStatement();
            ResultSet rs = s.executeQuery(String.format("SELECT ID,Name,AddOn_Connection_Info FROM %s", tableName));
            while(rs.next()) {
                String value = rs.getString("AddOn_Connection_Info");
                if( value != null && !value.isEmpty() && !value.contains(beginWith)) {
                    condition = false;
                    log.error(String.format("Host %s [record %d] connection string %s", rs.getString("Name"), rs.getInt("ID"), value));
                }
            }
            rs.close();
            s.close();
            return condition;
        }
        catch (SQLException e) {
            throw new SetupException(String.format("Cannot check contents of table %s", tableName), e);
        }
    }
    */
    
    /**
     * @param c
     * @param tableName
     * @param fieldName
     * @param dekBase64
     * @throws SetupException 
     */
    private void encryptAllNonEmptyFieldsInTableWithKey(Connection c, String tableName, String fieldName, String dekBase64) throws SetupException {
        try {
            Aes128 aes = new Aes128(Base64.decodeBase64(dekBase64));
            try (PreparedStatement update = c.prepareStatement(String.format("UPDATE %s SET %s=? WHERE ID=?", tableName, fieldName));Statement query = c.createStatement();ResultSet rs = query.executeQuery(String.format("SELECT ID,%s FROM %s", fieldName, tableName))) {
                while (rs.next()) {
                    String value = rs.getString(fieldName);
                    if (value != null && !value.isEmpty() && value.startsWith("http")) { 
                        log.debug(String.format("Encrypting record %d field %s", rs.getInt("ID"), fieldName)); // do not log the value being encrypted because that leaks sensitive information to log
                        String encrypted = aes.encryptString(value);
                        update.setString(1, encrypted);
                        update.setInt(2, rs.getInt("ID"));
                        update.executeUpdate();
                    }
                }
            }
        }
        catch (CryptographyException e) {
            throw new SetupException(String.format("Cannot encrypt field %s in table %s", tableName), e);
        }
        catch (SQLException e) {
            throw new SetupException(String.format("Cannot check contents of table %s", tableName), e);
        }
    }
}
