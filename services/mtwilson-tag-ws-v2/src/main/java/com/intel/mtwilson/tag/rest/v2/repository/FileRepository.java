/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.rest.v2.repository;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.tag.dao.jdbi.FileDAO;
import static com.intel.mtwilson.tag.dao.jooq.generated.Tables.MW_FILE;
import com.intel.mtwilson.jaxrs2.server.resource.DocumentRepository;
import com.intel.mtwilson.jooq.util.JooqContainer;
import com.intel.mtwilson.tag.dao.TagJdbi;
import com.intel.mtwilson.tag.model.File;
import com.intel.mtwilson.tag.model.FileCollection;
import com.intel.mtwilson.tag.model.FileFilterCriteria;
import com.intel.mtwilson.tag.model.FileLocator;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SelectQuery;

/**
 *
 * @author ssbangal
 */
public class FileRepository implements DocumentRepository<File, FileCollection, FileFilterCriteria, FileLocator> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FileRepository.class);
    

    @Override
    @RequiresPermissions("files:search")     
    public FileCollection search(FileFilterCriteria criteria) {
        FileCollection objCollection = new FileCollection();
        try(JooqContainer jc = TagJdbi.jooq()) {
            DSLContext jooq = jc.getDslContext();
            
            SelectQuery sql = jooq.select().from(MW_FILE).getQuery();
            if (criteria.filter) {
                if( criteria.id != null ) {
                    sql.addConditions(MW_FILE.ID.equalIgnoreCase(criteria.id.toString())); // when uuid is stored in database as the standard UUID string format (36 chars)
                }
                if( criteria.nameEqualTo != null && criteria.nameEqualTo.length() > 0 ) {
                    sql.addConditions(MW_FILE.NAME.equalIgnoreCase(criteria.nameEqualTo));
                }
                if( criteria.nameContains != null && criteria.nameContains.length() > 0 ) {
                    sql.addConditions(MW_FILE.NAME.lower().contains(criteria.nameContains.toLowerCase()));
                }
                if( criteria.contentTypeEqualTo != null && criteria.contentTypeEqualTo.length() > 0 ) {
                    sql.addConditions(MW_FILE.CONTENTTYPE.equalIgnoreCase(criteria.contentTypeEqualTo));
                }
                if( criteria.contentTypeContains != null && criteria.contentTypeContains.length() > 0 ) {
                    sql.addConditions(MW_FILE.CONTENTTYPE.lower().startsWith(criteria.contentTypeContains.toLowerCase()));
                }
            }
            Result<Record> result = sql.fetch();
            log.debug("Got {} records", result.size());
            for(Record r : result) {
                File obj = new File();
                obj.setId(UUID.valueOf(r.getValue(MW_FILE.ID)));
                obj.setName(r.getValue(MW_FILE.NAME));
                obj.setContentType(r.getValue(MW_FILE.CONTENTTYPE));
                obj.setContent(r.getValue(MW_FILE.CONTENT));
            }
            sql.close();
        } catch (WebApplicationException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during file search.", ex);
            throw new WebApplicationException("Please see the server log for more details.", Response.Status.INTERNAL_SERVER_ERROR);
        }        
        return objCollection;
    }

    @Override
    @RequiresPermissions("files:retrieve")     
    public File retrieve(FileLocator locator) {
        if (locator == null || locator.id == null ) { return null;}
        
        try(FileDAO dao = TagJdbi.fileDao()) {
            
            File obj = dao.findById(locator.id);
            if (obj != null)
                return obj;
                                    
        } catch (WebApplicationException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during file search.", ex);
            throw new WebApplicationException("Please see the server log for more details.", Response.Status.INTERNAL_SERVER_ERROR);
        }        
        return null;
    }

    @Override
    @RequiresPermissions("files:store")     
    public void store(File item) {
        
        try(FileDAO dao = TagJdbi.fileDao()) {
            
            File obj = dao.findById(item.getId());
            if (obj != null)
                dao.update(item.getId(), item.getName(), item.getContentType(), item.getContent());
            else {
                throw new WebApplicationException("Object not found.", Response.Status.NOT_FOUND);
            }
                                    
        } catch (WebApplicationException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during file update.", ex);
            throw new WebApplicationException("Please see the server log for more details.", Response.Status.INTERNAL_SERVER_ERROR);
        }        
    }

    @Override
    @RequiresPermissions("files:create")     
    public void create(File item) {

        try(FileDAO dao = TagJdbi.fileDao()) {

            File obj = dao.findById(item.getId());
            if (obj == null) {
                if (item.getName() == null || item.getName().isEmpty() || item.getContentType()== null 
                        || item.getContentType().isEmpty() || item.getContent().length == 0) {
                    log.error("Invalid input specified by the user.");
                    throw new WebApplicationException("Invalid input specified by the user.", Response.Status.PRECONDITION_FAILED);
                }
                obj = dao.findByName(item.getName());
                if (obj == null)
                    dao.insert(item.getId(), item.getName(), item.getContentType(), item.getContent());   
                else {
                    log.error("The file name already exists.");
                    throw new WebApplicationException("The file name is already used.", Response.Status.CONFLICT);
                }
            } else {
                throw new WebApplicationException("Object with specified id already exists.", Response.Status.CONFLICT);
            }
                        
        } catch (WebApplicationException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during file creation.", ex);
            throw new WebApplicationException("Please see the server log for more details.", Response.Status.INTERNAL_SERVER_ERROR);
        }         
    }

    @Override
    @RequiresPermissions("files:delete")     
    public void delete(FileLocator locator) {
        if( locator == null || locator.id == null ) { return; }
        try(FileDAO dao = TagJdbi.fileDao()) {
            
            File obj = dao.findById(locator.id);
            if (obj != null)
                dao.delete(locator.id);           
            
        } catch (WebApplicationException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during attribute deletion.", ex);
            throw new WebApplicationException("Please see the server log for more details.", Response.Status.INTERNAL_SERVER_ERROR);
        }        
    }
    
    @Override
    @RequiresPermissions("files:delete,search")     
    public void delete(FileFilterCriteria criteria) {
        log.debug("File:Delete - Got request to delete file by search criteria.");        
        FileCollection objCollection = search(criteria);
        try { 
            for (File obj : objCollection.getFiles()) {
                FileLocator locator = new FileLocator();
                locator.id = obj.getId();
                delete(locator);
            }
        } catch (Exception ex) {
            log.error("Error during File deletion.", ex);
            throw new WebApplicationException("Please see the server log for more details.", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
        
}
