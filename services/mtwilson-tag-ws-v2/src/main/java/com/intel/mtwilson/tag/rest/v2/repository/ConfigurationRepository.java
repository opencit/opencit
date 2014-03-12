/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.rest.v2.repository;

import com.intel.dcsg.cpg.io.UUID;
import static com.intel.mtwilson.tag.dao.jooq.generated.Tables.MW_CONFIGURATION;
import com.intel.mtwilson.tag.dao.jdbi.ConfigurationDAO;
import com.intel.mtwilson.jersey.resource.SimpleRepository;
import com.intel.mtwilson.tag.common.Global;
import com.intel.mtwilson.tag.dao.TagJdbi;
import com.intel.mtwilson.tag.model.Configuration;
import com.intel.mtwilson.tag.model.ConfigurationCollection;
import com.intel.mtwilson.tag.model.ConfigurationFilterCriteria;
import com.intel.mtwilson.tag.model.ConfigurationLocator;
import java.io.IOException;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SelectQuery;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
public class ConfigurationRepository extends ServerResource implements SimpleRepository<Configuration, ConfigurationCollection, ConfigurationFilterCriteria, ConfigurationLocator> {

    private Logger log = LoggerFactory.getLogger(getClass().getName());

    @Override
    public ConfigurationCollection search(ConfigurationFilterCriteria criteria) {
        ConfigurationCollection objCollection = new ConfigurationCollection();
        DSLContext jooq = null;
        
        try (ConfigurationDAO dao = TagJdbi.configurationDao()) {
            jooq = TagJdbi.jooq();
            
            SelectQuery sql = jooq.select().from(MW_CONFIGURATION).getQuery();
            if( criteria.id != null ) {
    //            sql.addConditions(TAG.UUID.equal(query.id.toByteArray().getBytes())); // when uuid is stored in database as binary
                sql.addConditions(MW_CONFIGURATION.ID.equal(criteria.id.toString())); // when uuid is stored in database as the standard UUID string format (36 chars)
            }
            if( criteria.nameEqualTo != null && criteria.nameEqualTo.length() > 0 ) {
                sql.addConditions(MW_CONFIGURATION.NAME.equal(criteria.nameEqualTo));
            }
            if( criteria.nameContains != null && criteria.nameContains.length() > 0 ) {
                sql.addConditions(MW_CONFIGURATION.NAME.contains(criteria.nameContains));
            }/*
            if( query.contentTypeEqualTo != null && query.contentTypeEqualTo.length() > 0 ) {
                sql.addConditions(MW_CONFIGURATION.CONTENTTYPE.equal(query.contentTypeEqualTo));
            }*/
            Result<Record> result = sql.fetch();
            com.intel.mtwilson.atag.model.Configuration[] configurations = new com.intel.mtwilson.atag.model.Configuration[result.size()];
            log.debug("Got {} records", configurations.length);
            for(Record r : result) {
                Configuration configObj = new Configuration();
                configObj.setId(UUID.valueOf(r.getValue(MW_CONFIGURATION.ID)));
                configObj.setName(r.getValue(MW_CONFIGURATION.NAME));
                try {
                    configObj.setXmlContent(r.getValue(MW_CONFIGURATION.CONTENT));
                }
                catch(IOException e) {
                    log.error("Failed to load configuration content for {}", configObj.getId().toString());
                }
                objCollection.getConfigurations().add(configObj);
            }
            sql.close();

        } catch (ResourceException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during configuration search.", ex);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Please see the server log for more details.");
        }        
        return objCollection;
    }

    @Override
    public Configuration retrieve(ConfigurationLocator locator) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void store(Configuration item) {
        if (item == null) {return;}
        try (ConfigurationDAO dao = TagJdbi.configurationDao()) {

            Configuration existingConfiguration = dao.findById(item.getId());
            if( existingConfiguration == null ) {
                setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Specified configuration does not exist in the system.");                
            }
            
            dao.update(item.getId(), item.getName(), item.getXmlContent());
            Global.reset(); // new configuration will take effect next time it is needed (if it's the active one)
                                    
        } catch (ResourceException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during configuration update.", ex);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Please see the server log for more details.");
        }       
    }

    @Override
    public void create(Configuration item) {
        
        try (ConfigurationDAO dao = TagJdbi.configurationDao()) {

            dao.insert(item.getId(), item.getName(), item.getXmlContent());                        
            
        } catch (ResourceException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during configuration creation.", ex);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Please see the server log for more details.");
        }        
    }

    @Override
    public void delete(ConfigurationLocator locator) {
        if( locator == null || locator.id == null ) { return; }
        ConfigurationDAO dao = null;
        
        try {
            dao = TagJdbi.configurationDao();
            dao.delete(locator.id);
        } catch (ResourceException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during configuration deletion.", ex);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Please see the server log for more details.");
        } finally {
            if (dao != null)
                dao.close();
        }        
    }
    
    @Override
    public void delete(ConfigurationFilterCriteria criteria) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
        
}
