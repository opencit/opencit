/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.tag.setup.cmd;

import com.intel.mtwilson.tag.setup.TagCommand;
import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.tag.dao.TagJdbi;
import com.intel.mtwilson.tag.dao.jdbi.KvAttributeDAO;
import com.intel.mtwilson.tag.dao.jdbi.SelectionDAO;
import com.intel.mtwilson.tag.dao.jdbi.SelectionKvAttributeDAO;
import com.intel.mtwilson.tag.dao.jdbi.ConfigurationDAO;
import com.intel.mtwilson.tag.dao.jdbi.TpmPasswordDAO;
import com.intel.mtwilson.tag.model.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import org.apache.commons.configuration.MapConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This command creates a default "main" configuration
 *
 * @author jbuhacoff
 */
public class TagInitDatabase extends TagCommand {

    private static Logger log = LoggerFactory.getLogger(TagInitDatabase.class);

    @Override
    public void execute(String[] args) throws Exception {
        insertMainConfiguration();
    }

    public void insertMainConfiguration() throws SQLException, IOException {

        try (ConfigurationDAO configurationDao = TagJdbi.configurationDao();
                KvAttributeDAO kvAttrDao = TagJdbi.kvAttributeDao();
                SelectionDAO selectionDao = TagJdbi.selectionDao();
                SelectionKvAttributeDAO selectionKvAttrDao = TagJdbi.selectionKvAttributeDao();
                TpmPasswordDAO tpmPasswordDao = TagJdbi.tpmPasswordDao()) {
            
            Configuration main = configurationDao.findByName("main");
            if (main == null) {
                log.debug("Inserting sample attributes");
                kvAttrDao.insert(new UUID(), "country", "US");
                kvAttrDao.insert(new UUID(), "country", "CA");
                kvAttrDao.insert(new UUID(), "country", "MX");
                kvAttrDao.insert(new UUID(), "state", "CA");
                kvAttrDao.insert(new UUID(), "state", "AZ");
                kvAttrDao.insert(new UUID(), "state", "OR");
                kvAttrDao.insert(new UUID(), "state", "TX");
                kvAttrDao.insert(new UUID(), "state", "NY");
                kvAttrDao.insert(new UUID(), "city", "Folsom");
                kvAttrDao.insert(new UUID(), "city", "Santa Clara");
                kvAttrDao.insert(new UUID(), "city", "Hillsboro");
                kvAttrDao.insert(new UUID(), "city", "Austin");
                kvAttrDao.insert(new UUID(), "city", "New York");
                kvAttrDao.insert(new UUID(), "customer", "Coke");
                kvAttrDao.insert(new UUID(), "customer", "Pepsi");
                kvAttrDao.insert(new UUID(), "customer", "US Govt");
                kvAttrDao.insert(new UUID(), "building", "Bldg 100");
                kvAttrDao.insert(new UUID(), "building", "Bldg 200");
                kvAttrDao.insert(new UUID(), "building", "Bldg 300");

                // default tag selection
                log.debug("Inserting sample selections");
                UUID defaultSelectionUuid = new UUID();
                UUID otherSelectionUuid = new UUID();
                selectionDao.insert(defaultSelectionUuid, "default", "default selections");
                selectionDao.insert(otherSelectionUuid, "other", "second selection option");

                KvAttribute countryAttr1 = kvAttrDao.findByNameAndValue("country", "US");
                KvAttribute stateAttr1 = kvAttrDao.findByNameAndValue("state", "CA");
                KvAttribute cityAttr1 = kvAttrDao.findByNameAndValue("city", "Folsom");
                KvAttribute cityAttr2 = kvAttrDao.findByNameAndValue("city", "Santa Clara");
                KvAttribute custAttr1 = kvAttrDao.findByNameAndValue("customer", "Coke");
                KvAttribute custAttr2 = kvAttrDao.findByNameAndValue("customer", "Pepsi");

                // Insert the mapping of selection and kvattribute into the database for the default selection       
                selectionKvAttrDao.insert(new UUID(), defaultSelectionUuid, countryAttr1.getId());
                selectionKvAttrDao.insert(new UUID(), defaultSelectionUuid, stateAttr1.getId());
                selectionKvAttrDao.insert(new UUID(), defaultSelectionUuid, cityAttr1.getId());
                selectionKvAttrDao.insert(new UUID(), defaultSelectionUuid, cityAttr2.getId());
                selectionKvAttrDao.insert(new UUID(), defaultSelectionUuid, custAttr1.getId());
                selectionKvAttrDao.insert(new UUID(), defaultSelectionUuid, custAttr2.getId());

                // Insert the mapping of selection and kvattribute into the database for the second selection created
                countryAttr1 = kvAttrDao.findByNameAndValue("country", "US");
                stateAttr1 = kvAttrDao.findByNameAndValue("state", "AZ");
                KvAttribute stateAttr2 = kvAttrDao.findByNameAndValue("state", "OR");
                KvAttribute stateAttr3 = kvAttrDao.findByNameAndValue("state", "TX");
                cityAttr1 = kvAttrDao.findByNameAndValue("city", "Hillsboro");
                cityAttr2 = kvAttrDao.findByNameAndValue("city", "Austin");
                custAttr1 = kvAttrDao.findByNameAndValue("building", "Bldg 100");
                custAttr2 = kvAttrDao.findByNameAndValue("building", "Bldg 200");

                selectionKvAttrDao.insert(new UUID(), otherSelectionUuid, countryAttr1.getId());
                selectionKvAttrDao.insert(new UUID(), otherSelectionUuid, stateAttr1.getId());
                selectionKvAttrDao.insert(new UUID(), otherSelectionUuid, stateAttr2.getId());
                selectionKvAttrDao.insert(new UUID(), otherSelectionUuid, stateAttr3.getId());
                selectionKvAttrDao.insert(new UUID(), otherSelectionUuid, cityAttr1.getId());
                selectionKvAttrDao.insert(new UUID(), otherSelectionUuid, cityAttr2.getId());
                selectionKvAttrDao.insert(new UUID(), otherSelectionUuid, custAttr1.getId());
                selectionKvAttrDao.insert(new UUID(), otherSelectionUuid, custAttr2.getId());

                
//                tpmPasswordDao.insert(UUID.valueOf("AAAAAAAA-AAAA-AAAA-AAAA-AAAAAAAAAAAA"), "TPMPASSWORD");

                // configuration to allow automatic tag selection & approval
                log.debug("Inserting default configuration");
                Properties p = new Properties();
                // 2014-03-22 after integration with mtwilson the properties are currently being set in mtwilson.properties so commenting out from here to prevent confusion
//                p.setProperty("allowTagsInCertificateRequests", "true");
//                p.setProperty("allowAutomaticTagSelection", "true");
//                p.setProperty("automaticTagSelectionName", defaultSelectionUuid.toString());
//                p.setProperty("approveAllCertificateRequests", "true");

                Configuration configuration = new Configuration();
                configuration.setName("main");
                configuration.setContent(p);
                configurationDao.insert(new UUID(), configuration.getName(), configuration.getXmlContent());
            }
        } catch (Exception ex) {
            log.error("Error during initialization of the database.", ex);
        }

    }

    public static void main(String args[]) throws Exception {
        TagInitDatabase cmd = new TagInitDatabase();
        cmd.setOptions(new MapConfiguration(new Properties()));
        cmd.execute(new String[0]);

    }
}
