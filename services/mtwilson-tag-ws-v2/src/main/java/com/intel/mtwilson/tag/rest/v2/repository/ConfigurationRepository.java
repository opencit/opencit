/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.rest.v2.repository;

import com.intel.dcsg.cpg.io.UUID;
import static com.intel.mtwilson.tag.dao.jooq.generated.Tables.MW_CONFIGURATION;
import com.intel.mtwilson.tag.dao.jdbi.ConfigurationDAO;
import com.intel.mtwilson.jaxrs2.server.resource.DocumentRepository;
import com.intel.mtwilson.jooq.util.JooqContainer;
import com.intel.mtwilson.repository.RepositoryCreateConflictException;
import com.intel.mtwilson.repository.RepositoryCreateException;
import com.intel.mtwilson.repository.RepositoryDeleteException;
import com.intel.mtwilson.repository.RepositoryException;
import com.intel.mtwilson.repository.RepositoryRetrieveException;
import com.intel.mtwilson.repository.RepositorySearchException;
import com.intel.mtwilson.repository.RepositoryStoreConflictException;
import com.intel.mtwilson.repository.RepositoryStoreException;
import com.intel.mtwilson.tag.common.Global;
import com.intel.mtwilson.tag.dao.TagJdbi;
import com.intel.mtwilson.tag.model.CertificateLocator;
import com.intel.mtwilson.tag.model.Configuration;
import com.intel.mtwilson.tag.model.ConfigurationCollection;
import com.intel.mtwilson.tag.model.ConfigurationFilterCriteria;
import com.intel.mtwilson.tag.model.ConfigurationLocator;
import java.io.IOException;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SelectQuery;

/**
 *
 * @author ssbangal
 */
public class ConfigurationRepository implements DocumentRepository<Configuration, ConfigurationCollection, ConfigurationFilterCriteria, ConfigurationLocator> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConfigurationRepository.class);
    

    @Override
    @RequiresPermissions("configurations:search")     
    public ConfigurationCollection search(ConfigurationFilterCriteria criteria) {
        log.debug("Configuration:Search - Got request to search for the Configurations.");        
        ConfigurationCollection objCollection = new ConfigurationCollection();
        try(JooqContainer jc = TagJdbi.jooq()) {
            DSLContext jooq = jc.getDslContext();
            
            SelectQuery sql = jooq.select().from(MW_CONFIGURATION).getQuery();
            if (criteria.filter) {
                if( criteria.id != null ) {
                    sql.addConditions(MW_CONFIGURATION.ID.equalIgnoreCase(criteria.id.toString())); // when uuid is stored in database as the standard UUID string format (36 chars)
                }
                if( criteria.nameEqualTo != null && criteria.nameEqualTo.length() > 0 ) {
                    sql.addConditions(MW_CONFIGURATION.NAME.equalIgnoreCase(criteria.nameEqualTo));
                }
                if( criteria.nameContains != null && criteria.nameContains.length() > 0 ) {
                    sql.addConditions(MW_CONFIGURATION.NAME.lower().contains(criteria.nameContains.toLowerCase()));
                }
            }
            Result<Record> result = sql.fetch();
            log.debug("Got {} records", result.size());
            for(Record r : result) {
                Configuration configObj = new Configuration();
                configObj.setId(UUID.valueOf(r.getValue(MW_CONFIGURATION.ID)));
                configObj.setName(r.getValue(MW_CONFIGURATION.NAME));
                try {
                    configObj.setXmlContent(r.getValue(MW_CONFIGURATION.CONTENT));
                }
                catch(IOException e) {
                    log.error("Failed to load configuration content for {}: {}", configObj.getId().toString(), e.getMessage());
                }
                objCollection.getConfigurations().add(configObj);
            }
            sql.close();

        } catch (Exception ex) {
            log.error("Configuration:Search - Error during configuration search.", ex);
            throw new RepositorySearchException(ex, criteria);
        }        
        log.debug("Configuration:Search - Returning back {} of results.", objCollection.getConfigurations().size());                        
        return objCollection;
    }

    @Override
    @RequiresPermissions("configurations:retrieve")     
    public Configuration retrieve(ConfigurationLocator locator) {
        if( locator == null || locator.id == null ) { return null; }
        log.debug("Configuration:Retrieve - Got request to retrieve Configuration with id {}.", locator.id);                        
        try (ConfigurationDAO dao = TagJdbi.configurationDao()) {            
            Configuration obj = dao.findById(locator.id);
            if (obj != null) {
                return obj;
            }
        } catch (Exception ex) {
            log.error("Configuration:Retrieve - Error during configuration retrieval.", ex);
            throw new RepositoryRetrieveException(ex, locator);
        }        
        return null;
    }

    @Override
    @RequiresPermissions("configurations:store")     
    public void store(Configuration item) {
        if (item == null) {return;}
        log.debug("Configuration:Store - Got request to update Configuration with id {}.", item.getId().toString());        
        ConfigurationLocator locator = new ConfigurationLocator();
        locator.id = item.getId();
        try (ConfigurationDAO dao = TagJdbi.configurationDao()) {

            Configuration existingConfiguration = dao.findById(item.getId());
            if( existingConfiguration == null ) {
                log.error("Configuration:Store - Configuration will not be updated since it does not exist.");
                throw new RepositoryStoreConflictException(locator);
            }
            
            dao.update(item.getId(), item.getName(), item.getXmlContent());
            Global.reset(); // new configuration will take effect next time it is needed (if it's the active one)
            log.debug("Configuration:Store - Updated the Configuration {} successfully.", item.getId().toString());                
                                    
        } catch (RepositoryException re) {
            throw re;            
        } catch (Exception ex) {
            log.error("Configuration:Store - Error during Configuration update.", ex);
            throw new RepositoryStoreException(ex, locator);
        }       
    }

    @Override
    @RequiresPermissions("configurations:create")     
    public void create(Configuration item) {
        log.debug("Configuration:Create - Got request to create a new Configuration {}.", item.getId().toString());
        CertificateLocator locator = new CertificateLocator();
        locator.id = item.getId();        
        try (ConfigurationDAO dao = TagJdbi.configurationDao()) {
            Configuration obj = dao.findById(item.getId());
            if (obj == null) {
                dao.insert(item.getId(), item.getName(), item.getXmlContent());                        
                log.debug("Configuration:Create - Created the Configuration {} successfully.", item.getId().toString());
            } else {
                log.error("Configuration:Create - Configuration {} will not be created since a duplicate Configuration already exists.", item.getId().toString());                
                throw new RepositoryCreateConflictException(locator);
            }
        } catch (RepositoryException re) {
            throw re;            
        } catch (Exception ex) {
            log.error("Configuration:Create - Error during Configuration creation.", ex);
            throw new RepositoryCreateException(ex, locator);
        }        
    }

    @Override
    @RequiresPermissions("configurations:delete")     
    public void delete(ConfigurationLocator locator) {
        if( locator == null || locator.id == null ) { return; }
        log.debug("Configuration:Delete - Got request to delete Configuration with id {}.", locator.id.toString());                        
        try (ConfigurationDAO dao = TagJdbi.configurationDao()) {            
            Configuration obj = dao.findById(locator.id);
            if (obj != null) {
                dao.delete(locator.id);
                log.debug("Configuration:Delete - Deleted the Configuration {} successfully.", locator.id.toString());                                
            }else {
                log.info("Configuration:Delete - Configuration does not exist in the system.");                
            }
        } catch (Exception ex) {
            log.error("Configuration:Delete - Error during configuration deletion.", ex);
            throw new RepositoryDeleteException(ex, locator);
        }        
    }
    
    @Override
    @RequiresPermissions("configurations:delete,search")     
    public void delete(ConfigurationFilterCriteria criteria) {
        log.debug("Configuration:Delete - Got request to delete configuration by search criteria.");        
        ConfigurationCollection objCollection = search(criteria);
        try { 
            for (Configuration obj : objCollection.getConfigurations()) {
                ConfigurationLocator locator = new ConfigurationLocator();
                locator.id = obj.getId();
                delete(locator);
            }
        } catch(RepositoryException re) {
            throw re;
        } catch (Exception ex) {
            log.error("Configuration:Delete - Error during Configuration deletion.", ex);
            throw new RepositoryDeleteException(ex);
        }
    }
        
}
