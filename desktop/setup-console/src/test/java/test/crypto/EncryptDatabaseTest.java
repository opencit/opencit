/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package test.crypto;

import com.intel.mtwilson.setup.SetupWizard;
import com.intel.mtwilson.crypto.Aes128;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Properties;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.MapConfiguration;
import org.junit.Before;
import org.junit.Test;


/**
 *
 * @author jbuhacoff
 */
public class EncryptDatabaseTest {
    
    private Aes128 aes;
    private Configuration conf;
    
    @Before
    public void setup() throws Exception {
        String dek = "IDYjLjDYxsRQBjbGt+UW8g==";
        aes = new Aes128(Base64.decodeBase64(dek));
        Properties p = new Properties();
        p.setProperty("mountwilson.as.db.host", "10.1.71.90"); // "10.1.71.103";
        p.setProperty("mountwilson.as.db.port", "3306");
        p.setProperty("mountwilson.as.db.schema", "mw_as");
        p.setProperty("mountwilson.as.db.user", "root");
        p.setProperty("mountwilson.as.db.password", "password");
        conf = new MapConfiguration(p);        
    }
    
//    @Test
//    public void create
    
    @Test
    public void testCheckAdequateFieldSize() throws Exception {
        SetupWizard wizard = new SetupWizard(conf);
        Connection c = wizard.getDatabaseConnection();
        ResultSet rs = c.getMetaData().getColumns("mw_as", null, "mw_hosts", "AddOn_Connection_Info");
        /*  this code snippet will show the available columns.  we're interested in DATA_TYPE and TYPE_NAME and COLUMN_SIZE and COLUMN_NAME, maybe SQL_DATA_TYPE
        ResultSetMetaData meta = rs.getMetaData();
        for(int i=1; i<=meta.getColumnCount(); i++) {
            System.out.println(meta.getColumnLabel(i));
        }
        */
        if( rs.next() ) {
        //System.out.println(rs.getString("COLUMN_NAME")+" "+rs.getString("TYPE_NAME")+" ("+rs.getInt("COLUMN_SIZE")+")");               // prints AddOn_Connection_Info VARCHAR (80)
            String columnType = rs.getString("TYPE_NAME");
            int varcharSize = rs.getInt("COLUMN_SIZE");
            if( columnType.equals("VARCHAR") && varcharSize < 240 ) {
                System.out.println("Size of column AddOn_Connection_Info "+columnType+"("+String.valueOf(varcharSize)+") is too small for encrypted data; attempting to increase size to 240...");
                Statement update = c.createStatement();
                update.executeUpdate("ALTER TABLE `mw_hosts` MODIFY COLUMN `AddOn_Connection_Info` varchar(240) DEFAULT NULL;");
                update.close();
            }
            else {
                System.out.println("Field looks ok");
            }
        }
        rs.close();
        
    }
    
    @Test
    public void testEncryptFields() throws Exception {
        SetupWizard wizard = new SetupWizard(conf);
        Connection c = wizard.getDatabaseConnection();
        PreparedStatement update = c.prepareStatement("UPDATE mw_hosts SET AddOn_Connection_Info=? WHERE ID=?");
        Statement query = c.createStatement();
        ResultSet rs = query.executeQuery("SELECT ID,AddOn_Connection_Info FROM mw_hosts");
        while(rs.next()) {
            String value = rs.getString("AddOn_Connection_Info");
            if( value != null && !value.isEmpty() ) {
                System.out.println(String.format("Encrypting record %d value: %s", rs.getInt("ID"), value));
                String encrypted = aes.encryptString(value);
                update.setString(1, encrypted);
                update.setInt(2, rs.getInt("ID"));
                update.executeUpdate();
            }
        }
        rs.close();
        query.close();
        update.close();
        
    }
    
    @Test
    public void testDecryptFields() throws Exception {
        SetupWizard wizard = new SetupWizard(conf);
        Connection c = wizard.getDatabaseConnection();
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery("SELECT ID,Name,AddOn_Connection_Info FROM mw_hosts");
        while(rs.next()) {
            String value = rs.getString("AddOn_Connection_Info");
            if( value != null && !value.isEmpty() ) {
                System.out.println(String.format("Record %d Name %s Connection Info: %s", rs.getInt("ID"), rs.getString("Name"), value));
                if( value.startsWith("http") ) {
                    System.out.println("   PLAINTEXT");
                }
                else {
                    System.out.println("   DECRYPTED: "+aes.decryptString(value));
                }
            }
        }
        rs.close();
        s.close();
        
    }
}
