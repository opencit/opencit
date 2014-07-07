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
import com.intel.mtwilson.repository.RepositoryCreateConflictException;
import com.intel.mtwilson.repository.RepositoryCreateException;
import com.intel.mtwilson.repository.RepositoryDeleteException;
import com.intel.mtwilson.repository.RepositoryException;
import com.intel.mtwilson.repository.RepositoryRetrieveException;
import com.intel.mtwilson.repository.RepositorySearchException;
import com.intel.mtwilson.repository.RepositoryStoreConflictException;
import com.intel.mtwilson.repository.RepositoryStoreException;
import com.intel.mtwilson.tag.dao.TagJdbi;
import com.intel.mtwilson.tag.model.File;
import com.intel.mtwilson.tag.model.FileCollection;
import com.intel.mtwilson.tag.model.FileFilterCriteria;
import com.intel.mtwilson.tag.model.FileLocator;
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
        log.debug("File:Search - Got request to search for the Files.");        
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
        } catch (Exception ex) {
            log.error("Certificate:Search - Error during file search.", ex);
            throw new RepositorySearchException(ex, criteria);
        }        
        log.debug("Certificate:Search - Returning back {} of results.", objCollection.getFiles().size());                                
        return objCollection;
    }

    @Override
    @RequiresPermissions("files:retrieve")     
    public File retrieve(FileLocator locator) {
        if (locator == null || locator.id == null ) { return null;}
        log.debug("File:Retrieve - Got request to retrieve file with id {}.", locator.id);                        
        try(FileDAO dao = TagJdbi.fileDao()) {            
            File obj = dao.findById(locator.id);
            if (obj != null)
                return obj;                                    
        } catch (Exception ex) {
            log.error("File:Retrieve - Error during file search.", ex);
            throw new RepositoryRetrieveException(ex, locator);
        }        
        return null;
    }

    @Override
    @RequiresPermissions("files:store")     
    public void store(File item) {
        if (item == null) {return;}
        log.debug("File:Store - Got request to update File with id {}.", item.getId().toString());        
        FileLocator locator = new FileLocator();
        locator.id = item.getId();
        
        try(FileDAO dao = TagJdbi.fileDao()) {            
            File obj = dao.findById(item.getId());
            if (obj != null) {
                dao.update(item.getId(), item.getName(), item.getContentType(), item.getContent());
                log.debug("File:Store - Updated the File {} successfully.", item.getId().toString());                            
            } else {
                log.error("File:Store - File will not be updated since it does not exist.");
                throw new RepositoryStoreConflictException(locator);
            }                                    
        } catch (RepositoryException re) {
            throw re;            
        } catch (Exception ex) {
            log.error("File:Store - Error during File update.", ex);
            throw new RepositoryStoreException(ex, locator);
        }        
    }

    @Override
    @RequiresPermissions("files:create")     
    public void create(File item) {
        log.debug("File:Create - Got request to create a new File {}.", item.getId().toString());
        FileLocator locator = new FileLocator();
        locator.id = item.getId();

        try(FileDAO dao = TagJdbi.fileDao()) {

            File obj = dao.findById(item.getId());
            if (obj == null) {
                if (item.getName() == null || item.getName().isEmpty() || item.getContentType()== null 
                        || item.getContentType().isEmpty() || item.getContent().length == 0) {
                    log.error("File:Create - Invalid input specified by the user.");
                    throw new RepositoryCreateException();
                }
                obj = dao.findByName(item.getName());
                if (obj == null) {
                    dao.insert(item.getId(), item.getName(), item.getContentType(), item.getContent()); 
                    log.debug("File:Create - Created the File {} successfully.", item.getId().toString());                   
                } else {
                    log.error("File:Create - File {} will not be created since a duplicate File already exists.", item.getId().toString());                
                    throw new RepositoryCreateConflictException(locator);
                }
            } else {
                log.error("File:Create - File {} will not be created since a duplicate File already exists.", item.getId().toString());                
                throw new RepositoryCreateConflictException(locator);
            }
        } catch (RepositoryException re) {
            throw re;            
        } catch (Exception ex) {
            log.error("File:Create - Error during File creation.", ex);
            throw new RepositoryCreateException(ex, locator);
        }        
    }

    @Override
    @RequiresPermissions("files:delete")     
    public void delete(FileLocator locator) {
        if( locator == null || locator.id == null ) { return; }
        log.debug("File:Delete - Got request to delete File with id {}.", locator.id.toString());                

        try(FileDAO dao = TagJdbi.fileDao()) {            
            File obj = dao.findById(locator.id);
            if (obj != null) {
                dao.delete(locator.id);           
                log.debug("File:Delete - Deleted the File {} successfully.", locator.id.toString());                
            }else {
                log.info("File:Delete - File does not exist in the system.");                
            }
        } catch (Exception ex) {
            log.error("File:Delete - Error during attribute deletion.", ex);
            throw new RepositoryDeleteException(ex, locator);
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
        } catch(RepositoryException re) {
            throw re;
        } catch (Exception ex) {
            log.error("File:Delete - Error during File deletion.", ex);
            throw new RepositoryDeleteException(ex);
        }
    }
        
}
