/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.rest.v2.repository;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.tag.dao.jdbi.FileDAO;
import static com.intel.mtwilson.tag.dao.jooq.generated.Tables.MW_FILE;
import com.intel.mtwilson.jersey.resource.SimpleRepository;
import com.intel.mtwilson.tag.dao.TagJdbi;
import com.intel.mtwilson.tag.model.File;
import com.intel.mtwilson.tag.model.FileCollection;
import com.intel.mtwilson.tag.model.FileFilterCriteria;
import com.intel.mtwilson.tag.model.FileLocator;
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
public class FileRepository implements SimpleRepository<File, FileCollection, FileFilterCriteria, FileLocator> {

    private Logger log = LoggerFactory.getLogger(getClass().getName());

    @Override
    public FileCollection search(FileFilterCriteria criteria) {
        FileCollection objCollection = new FileCollection();
        DSLContext jooq = null;
        
        try (FileDAO dao = TagJdbi.fileDao()) {
            jooq = TagJdbi.jooq();
            
            SelectQuery sql = jooq.select().from(MW_FILE).getQuery();
            if( criteria.id != null ) {
    //            sql.addConditions(TAG.UUID.equal(query.id.toByteArray().getBytes())); // when uuid is stored in database as binary
                sql.addConditions(MW_FILE.ID.equal(criteria.id.toString())); // when uuid is stored in database as the standard UUID string format (36 chars)
            }
            if( criteria.nameEqualTo != null && criteria.nameEqualTo.length() > 0 ) {
                sql.addConditions(MW_FILE.NAME.equal(criteria.nameEqualTo));
            }
            if( criteria.nameContains != null && criteria.nameContains.length() > 0 ) {
                sql.addConditions(MW_FILE.NAME.contains(criteria.nameContains));
            }
            if( criteria.contentTypeEqualTo != null && criteria.contentTypeEqualTo.length() > 0 ) {
                sql.addConditions(MW_FILE.CONTENTTYPE.equal(criteria.contentTypeEqualTo));
            }
            if( criteria.contentTypeContains != null && criteria.contentTypeContains.length() > 0 ) {
                sql.addConditions(MW_FILE.CONTENTTYPE.startsWith(criteria.contentTypeContains));
            }
            Result<Record> result = sql.fetch();
            log.debug("Got {} records", result.size());
            int i = 0;
            for(Record r : result) {
                File obj = new File();
                obj.setId(UUID.valueOf(r.getValue(MW_FILE.ID)));
                obj.setName(r.getValue(MW_FILE.NAME));
                obj.setContentType(r.getValue(MW_FILE.CONTENTTYPE));
                obj.setContent(r.getValue(MW_FILE.CONTENT));
            }
            sql.close();
        } catch (ResourceException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during file search.", ex);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Please see the server log for more details.");
        }        
        return objCollection;
    }

    @Override
    public File retrieve(FileLocator locator) {
        if (locator == null || locator.id == null ) { return null;}
        
        try(FileDAO dao = TagJdbi.fileDao()) {
            
            File obj = dao.findById(locator.id);
            if (obj != null)
                return obj;
                                    
        } catch (ResourceException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during file search.", ex);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Please see the server log for more details.");
        }        
        return null;
    }

    @Override
    public void store(File item) {
        
        try(FileDAO dao = TagJdbi.fileDao()) {
            
            File obj = dao.findById(item.getId());
            if (obj != null)
                dao.update(item.getId(), item.getName(), item.getContentType(), item.getContent());
            else {
                throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Object not found.");
            }
                                    
        } catch (ResourceException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during file update.", ex);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Please see the server log for more details.");
        }        
    }

    @Override
    public void create(File item) {

        try(FileDAO dao = TagJdbi.fileDao()) {

            File obj = dao.findById(item.getId());
            if (obj == null) {
                if (item.getName() == null || item.getName().isEmpty() || item.getContentType()== null 
                        || item.getContentType().isEmpty() || item.getContent().length == 0) {
                    log.error("Invalid input specified by the user.");
                    throw new ResourceException(Status.CLIENT_ERROR_PRECONDITION_FAILED, "Invalid input specified by the user.");
                }
                obj = dao.findByName(item.getName());
                if (obj == null)
                    dao.insert(item.getId(), item.getName(), item.getContentType(), item.getContent());   
                else {
                    log.error("The file name already exists.");
                    throw new ResourceException(Status.CLIENT_ERROR_CONFLICT, "The file name is already used.");
                }
            } else {
                throw new ResourceException(Status.CLIENT_ERROR_CONFLICT, "Object with specified id already exists.");
            }
                        
        } catch (ResourceException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during file creation.", ex);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Please see the server log for more details.");
        }         
    }

    @Override
    public void delete(FileLocator locator) {
        if( locator == null || locator.id == null ) { return; }
        try(FileDAO dao = TagJdbi.fileDao()) {
            
            File obj = dao.findById(locator.id);
            if (obj != null)
                dao.delete(locator.id);           
            
        } catch (ResourceException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during attribute deletion.", ex);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Please see the server log for more details.");
        }        
    }
    
    @Override
    public void delete(FileFilterCriteria criteria) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
        
}
