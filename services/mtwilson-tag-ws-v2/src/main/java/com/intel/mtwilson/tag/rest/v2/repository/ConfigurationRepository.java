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
import com.intel.mtwilson.tag.common.Global;
import com.intel.mtwilson.tag.dao.TagJdbi;
import com.intel.mtwilson.tag.model.Configuration;
import com.intel.mtwilson.tag.model.ConfigurationCollection;
import com.intel.mtwilson.tag.model.ConfigurationFilterCriteria;
import com.intel.mtwilson.tag.model.ConfigurationLocator;
import java.io.IOException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SelectQuery;
//import org.restlet.data.Status;
//import org.restlet.resource.ResourceException;
//import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ssbangal
 */
public class ConfigurationRepository implements DocumentRepository<Configuration, ConfigurationCollection, ConfigurationFilterCriteria, ConfigurationLocator> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConfigurationRepository.class);
    

    @Override
    @RequiresPermissions("configurations:search")     
    public ConfigurationCollection search(ConfigurationFilterCriteria criteria) {
        ConfigurationCollection objCollection = new ConfigurationCollection();
        try(JooqContainer jc = TagJdbi.jooq()) {
            DSLContext jooq = jc.getDslContext();
            
            SelectQuery sql = jooq.select().from(MW_CONFIGURATION).getQuery();
            if( criteria.id != null ) {
                sql.addConditions(MW_CONFIGURATION.ID.equalIgnoreCase(criteria.id.toString())); // when uuid is stored in database as the standard UUID string format (36 chars)
            }
            if( criteria.nameEqualTo != null && criteria.nameEqualTo.length() > 0 ) {
                sql.addConditions(MW_CONFIGURATION.NAME.equalIgnoreCase(criteria.nameEqualTo));
            }
            if( criteria.nameContains != null && criteria.nameContains.length() > 0 ) {
                sql.addConditions(MW_CONFIGURATION.NAME.lower().contains(criteria.nameContains.toLowerCase()));
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

        } catch (WebApplicationException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during configuration search.", ex);
            throw new WebApplicationException("Please see the server log for more details.", Response.Status.INTERNAL_SERVER_ERROR);
        }        
        return objCollection;
    }

    @Override
    @RequiresPermissions("configurations:retrieve")     
    public Configuration retrieve(ConfigurationLocator locator) {
        if( locator == null || locator.id == null ) { return null; }
        
        try (ConfigurationDAO dao = TagJdbi.configurationDao()) {            
            Configuration obj = dao.findById(locator.id);
            if (obj != null) {
                return obj;
            }
        } catch (WebApplicationException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during configuration deletion.", ex);
            throw new WebApplicationException("Please see the server log for more details.", Response.Status.INTERNAL_SERVER_ERROR);
        }        
        return null;
    }

    @Override
    @RequiresPermissions("configurations:store")     
    public void store(Configuration item) {
        if (item == null) {return;}
        try (ConfigurationDAO dao = TagJdbi.configurationDao()) {

            Configuration existingConfiguration = dao.findById(item.getId());
            if( existingConfiguration == null ) {
                Response.status(Response.Status.NOT_FOUND);
                throw new WebApplicationException("Specified configuration does not exist in the system.", Response.Status.NOT_FOUND);
            }
            
            dao.update(item.getId(), item.getName(), item.getXmlContent());
            Global.reset(); // new configuration will take effect next time it is needed (if it's the active one)
                                    
        } catch (WebApplicationException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during configuration update.", ex);
            throw new WebApplicationException("Please see the server log for more details.", Response.Status.INTERNAL_SERVER_ERROR);
        }       
    }

    @Override
    @RequiresPermissions("configurations:create")     
    public void create(Configuration item) {
        
        try (ConfigurationDAO dao = TagJdbi.configurationDao()) {
            Configuration obj = dao.findById(item.getId());
            if (obj == null) {
                dao.insert(item.getId(), item.getName(), item.getXmlContent());                        
            }
            
        } catch (WebApplicationException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during configuration creation.", ex);
            throw new WebApplicationException("Please see the server log for more details.", Response.Status.INTERNAL_SERVER_ERROR);
        }        
    }

    @Override
    @RequiresPermissions("configurations:delete")     
    public void delete(ConfigurationLocator locator) {
        if( locator == null || locator.id == null ) { return; }
        
        try (ConfigurationDAO dao = TagJdbi.configurationDao()) {            
            Configuration obj = dao.findById(locator.id);
            if (obj != null) {
                dao.delete(locator.id);
            }
        } catch (WebApplicationException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during configuration deletion.", ex);
            throw new WebApplicationException("Please see the server log for more details.", Response.Status.INTERNAL_SERVER_ERROR);
        }        
    }
    
    @Override
    @RequiresPermissions("configurations:delete,search")     
    public void delete(ConfigurationFilterCriteria criteria) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
        
}
