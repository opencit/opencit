/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.rest.v2.repository;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.jersey.resource.SimpleRepository;
import com.intel.mtwilson.tag.dao.TagJdbi;
import com.intel.mtwilson.tag.dao.jdbi.KvAttributeDAO;
import com.intel.mtwilson.tag.dao.jdbi.SelectionDAO;
import com.intel.mtwilson.tag.dao.jdbi.SelectionKvAttributeDAO;
import com.intel.mtwilson.tag.model.SelectionKvAttribute;
import com.intel.mtwilson.tag.model.SelectionKvAttributeCollection;
import com.intel.mtwilson.tag.model.SelectionKvAttributeFilterCriteria;
import com.intel.mtwilson.tag.model.SelectionKvAttributeLocator;
import static com.intel.mtwilson.tag.dao.jooq.generated.Tables.MW_TAG_SELECTION;
import static com.intel.mtwilson.tag.dao.jooq.generated.Tables.MW_TAG_SELECTION_KVATTRIBUTE;
import static com.intel.mtwilson.tag.dao.jooq.generated.Tables.MW_TAG_KVATTRIBUTE;
import com.intel.mtwilson.tag.model.KvAttribute;
import com.intel.mtwilson.tag.model.Selection;
import com.intel.mtwilson.tag.model.SelectionCollection;
import java.util.ArrayList;
import java.util.HashMap;
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
public class SelectionKvAttributeRepository extends ServerResource implements SimpleRepository<SelectionKvAttribute, SelectionKvAttributeCollection, SelectionKvAttributeFilterCriteria, SelectionKvAttributeLocator> {

    private Logger log = LoggerFactory.getLogger(getClass().getName());

    @Override
    public SelectionKvAttributeCollection search(SelectionKvAttributeFilterCriteria criteria) {
        SelectionKvAttributeCollection objCollection = new SelectionKvAttributeCollection();
        DSLContext jooq = null;
        
        try(KvAttributeDAO attrDao = TagJdbi.kvAttributeDao()) {
                    
            SelectQuery sql = jooq.select()
                    .from(MW_TAG_SELECTION.join(MW_TAG_SELECTION_KVATTRIBUTE)
                    .on(MW_TAG_SELECTION_KVATTRIBUTE.SELECTIONID.equal(MW_TAG_SELECTION.ID))).getQuery();
            if( criteria.attrValueEqualTo != null || criteria.attrNameContains != null ) {
                log.debug("Selecting from tag-value");
                SelectQuery valueQuery = jooq.select(MW_TAG_SELECTION_KVATTRIBUTE.ID)
                        .from(MW_TAG_SELECTION_KVATTRIBUTE.join(MW_TAG_KVATTRIBUTE).on(MW_TAG_KVATTRIBUTE.ID.equal(MW_TAG_SELECTION_KVATTRIBUTE.KVATTRIBUTEID)))
                        .getQuery();
                if( criteria.attrValueEqualTo != null && criteria.attrValueEqualTo.length() > 0 ) {
                    valueQuery.addConditions(MW_TAG_KVATTRIBUTE.VALUE.equal(criteria.attrValueEqualTo));
                }
                if( criteria.attrValueContains != null  && criteria.attrValueContains.length() > 0 ) {
                    valueQuery.addConditions(MW_TAG_KVATTRIBUTE.VALUE.contains(criteria.attrValueContains));
                }
                sql.addConditions(MW_TAG_SELECTION_KVATTRIBUTE.ID.in(valueQuery));
            }
            if( criteria.attrNameContains != null || criteria.attrNameEqualTo != null || criteria.attrValueContains != null || criteria.attrValueEqualTo != null ) {
                log.debug("Selecting from tag");
                SelectQuery tagQuery = jooq.select(MW_TAG_SELECTION_KVATTRIBUTE.ID)
                        .from(MW_TAG_SELECTION_KVATTRIBUTE.join(MW_TAG_KVATTRIBUTE).on(MW_TAG_KVATTRIBUTE.ID.equal(MW_TAG_SELECTION_KVATTRIBUTE.KVATTRIBUTEID)))
                        .getQuery();
                if( criteria.attrNameEqualTo != null  && criteria.attrNameEqualTo.length() > 0 ) {
                    tagQuery.addConditions(MW_TAG_KVATTRIBUTE.NAME.equal(criteria.attrNameEqualTo));
                }
                if( criteria.attrNameContains != null  && criteria.attrNameContains.length() > 0 ) {
                    tagQuery.addConditions(MW_TAG_KVATTRIBUTE.NAME.contains(criteria.attrNameContains));
                }
                if( criteria.attrValueEqualTo != null  && criteria.attrValueEqualTo.length() > 0 ) {
                    tagQuery.addConditions(MW_TAG_KVATTRIBUTE.VALUE.equal(criteria.attrValueEqualTo));
                }
                if( criteria.attrValueContains != null  && criteria.attrValueContains.length() > 0 ) {
                    tagQuery.addConditions(MW_TAG_KVATTRIBUTE.VALUE.contains(criteria.attrValueContains));
                }
                sql.addConditions(MW_TAG_SELECTION_KVATTRIBUTE.ID.in(tagQuery));            
            }
            if( criteria.id != null ) {
                sql.addConditions(MW_TAG_SELECTION.ID.equal(criteria.id.toString())); // when uuid is stored in database as the standard UUID string format (36 chars)
            }
            if( criteria.nameEqualTo != null  && criteria.nameEqualTo.length() > 0 ) {
                sql.addConditions(MW_TAG_SELECTION.NAME.equal(criteria.nameEqualTo));
            }
            if( criteria.nameContains != null  && criteria.nameContains.length() > 0  ) {
                sql.addConditions(MW_TAG_SELECTION.NAME.contains(criteria.nameContains));
            }
            sql.addOrderBy(MW_TAG_SELECTION.ID);
            Result<Record> result = sql.fetch();
            log.debug("Got {} selection records", result.size());
            for(Record r : result) {
                SelectionKvAttribute sAttr = new SelectionKvAttribute();
                sAttr.setSelectionId(UUID.valueOf(r.getValue(MW_TAG_SELECTION.ID)));
                sAttr.setSelectionName(r.getValue(MW_TAG_SELECTION.NAME));
                sAttr.setKvAttributeId(UUID.valueOf(r.getValue(MW_TAG_SELECTION_KVATTRIBUTE.KVATTRIBUTEID)));
                KvAttribute attr = attrDao.findById(sAttr.getKvAttributeId());
                if (attr != null) {
                    sAttr.setKvAttributeName(attr.getName());
                    sAttr.setKvAttributeValue(attr.getValue());
                }
                
                objCollection.getSelectionTagValues().add(sAttr);
            }
            sql.close();
            log.debug("Closed jooq sql statement");
        } catch (ResourceException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during attribute search.", ex);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Please see the server log for more details.");
        }
        return objCollection;
    }

    @Override
    public SelectionKvAttribute retrieve(SelectionKvAttributeLocator locator) {
        if (locator == null || locator.id == null ) { return null;}
        try(SelectionKvAttributeDAO dao = TagJdbi.selectionKvAttributeDao()) {
            
            SelectionKvAttribute obj = dao.findById(locator.id);
            if (obj != null)
                return obj;
                                    
        } catch (ResourceException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during Selection search.", ex);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Please see the server log for more details.");
        }        
        return null;
    }

    @Override
    public void store(SelectionKvAttribute item) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void create(SelectionKvAttribute item) {
        try(SelectionKvAttributeDAO dao = TagJdbi.selectionKvAttributeDao()) {

            dao.insert(item.getId(), item.getSelectionId(), item.getKvAttributeId());
                        
        } catch (ResourceException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during selection attribute creation.", ex);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Please see the server log for more details.");
        }         
    }

    @Override
    public void delete(SelectionKvAttributeLocator locator) {
        if (locator == null || locator.id == null) { return; }
        
        try(SelectionKvAttributeDAO dao = TagJdbi.selectionKvAttributeDao()) {

            dao.delete(locator.id);
                        
        } catch (ResourceException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during selection attribute deletion.", ex);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Please see the server log for more details.");
        }         
    }
    
    @Override
    public void delete(SelectionKvAttributeFilterCriteria criteria) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
        
}
