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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.jooq.DSLContext;
import org.jooq.JoinType;
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
public class SelectionKvAttributeRepository implements SimpleRepository<SelectionKvAttribute, SelectionKvAttributeCollection, SelectionKvAttributeFilterCriteria, SelectionKvAttributeLocator> {

    private Logger log = LoggerFactory.getLogger(getClass().getName());

    @Override
    public SelectionKvAttributeCollection search(SelectionKvAttributeFilterCriteria criteria) {
        SelectionKvAttributeCollection objCollection = new SelectionKvAttributeCollection();
        DSLContext jooq = null;
        
        try {
            jooq = TagJdbi.jooq();
            
            SelectQuery sql = jooq.select()
                    .from(MW_TAG_KVATTRIBUTE.join(MW_TAG_SELECTION_KVATTRIBUTE, JoinType.JOIN)
                    .on(MW_TAG_KVATTRIBUTE.ID.equal(MW_TAG_SELECTION_KVATTRIBUTE.KVATTRIBUTEID))
                    .join(MW_TAG_SELECTION, JoinType.JOIN).on(MW_TAG_SELECTION_KVATTRIBUTE.SELECTIONID.equal(MW_TAG_SELECTION.ID)))                    
                    .getQuery();
            if( criteria.attrNameEqualTo != null  && criteria.attrNameEqualTo.length() > 0 ) {
                sql.addConditions(MW_TAG_KVATTRIBUTE.NAME.equal(criteria.attrNameEqualTo));
            }
            if( criteria.attrNameContains != null  && criteria.attrNameContains.length() > 0 ) {
                sql.addConditions(MW_TAG_KVATTRIBUTE.NAME.contains(criteria.attrNameContains));
            }
            if( criteria.attrValueEqualTo != null  && criteria.attrValueEqualTo.length() > 0 ) {
                sql.addConditions(MW_TAG_KVATTRIBUTE.VALUE.equal(criteria.attrValueEqualTo));
            }
            if( criteria.attrValueContains != null  && criteria.attrValueContains.length() > 0 ) {
                sql.addConditions(MW_TAG_KVATTRIBUTE.VALUE.contains(criteria.attrValueContains));
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
            sql.addOrderBy(MW_TAG_SELECTION.NAME);
            Result<Record> result = sql.fetch();
            log.debug("Got {} selection records", result.size());
            for(Record r : result) {
                SelectionKvAttribute sAttr = new SelectionKvAttribute();
                sAttr.setId(UUID.valueOf(r.getValue(MW_TAG_SELECTION_KVATTRIBUTE.ID)));
                sAttr.setSelectionId(UUID.valueOf(r.getValue(MW_TAG_SELECTION.ID)));
                sAttr.setSelectionName(r.getValue(MW_TAG_SELECTION.NAME));
                sAttr.setKvAttributeId(UUID.valueOf(r.getValue(MW_TAG_KVATTRIBUTE.ID)));
                sAttr.setKvAttributeName(r.getValue(MW_TAG_KVATTRIBUTE.NAME));
                sAttr.setKvAttributeValue(r.getValue(MW_TAG_KVATTRIBUTE.VALUE));
                
                objCollection.getSelectionKvAttributeValues().add(sAttr);
            }
            sql.close();
            log.debug("Closed jooq sql statement");
        } catch (WebApplicationException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during attribute search.", ex);
            throw new WebApplicationException("Please see the server log for more details.", Response.Status.INTERNAL_SERVER_ERROR);
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
                                    
        } catch (WebApplicationException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during Selection search.", ex);
            throw new WebApplicationException("Please see the server log for more details.", Response.Status.INTERNAL_SERVER_ERROR);
        }        
        return null;
    }

    @Override
    public void store(SelectionKvAttribute item) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void create(SelectionKvAttribute item) {
        try(SelectionKvAttributeDAO dao = TagJdbi.selectionKvAttributeDao();
                SelectionDAO selectionDao = TagJdbi.selectionDao();
                KvAttributeDAO attrDao = TagJdbi.kvAttributeDao()) {
            
            Selection selectionObj = null;
            SelectionKvAttribute obj = dao.findById(item.getId());
            if (obj == null) {
                if (item.getSelectionName() == null || item.getKvAttributeId() == null) {
                    log.error("Invalid input specified by the user.");
                    throw new WebApplicationException("Invalid input specified by the user.", Response.Status.PRECONDITION_FAILED);
                }
                
                selectionObj = selectionDao.findByName(item.getSelectionName());
                if (selectionObj == null) {
                    log.error("Invalid input specified by the user. Specified selection is not configured.");
                    throw new WebApplicationException("Invalid input specified by the user. Specified selection is not configured.", Response.Status.PRECONDITION_FAILED);                    
                }
                
                KvAttribute attrObj = attrDao.findById(item.getKvAttributeId());
                if (attrObj == null) {
                    log.error("Invalid input specified by the user. Specified attribute is not configured.");
                    throw new WebApplicationException("Invalid input specified by the user. Specified attribute is not configured.", Response.Status.PRECONDITION_FAILED);                                        
                }
                dao.insert(item.getId(), selectionObj.getId(), item.getKvAttributeId());
            }
                        
        } catch (WebApplicationException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during selection attribute creation.", ex);
            throw new WebApplicationException("Please see the server log for more details.", Response.Status.INTERNAL_SERVER_ERROR);
        }         
    }

    @Override
    public void delete(SelectionKvAttributeLocator locator) {
        if (locator == null || locator.id == null) { return; }
        
        try(SelectionKvAttributeDAO dao = TagJdbi.selectionKvAttributeDao()) {

            dao.delete(locator.id);
                        
        } catch (WebApplicationException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during selection attribute deletion.", ex);
            throw new WebApplicationException("Please see the server log for more details.", Response.Status.INTERNAL_SERVER_ERROR);
        }         
    }
    
    @Override
    public void delete(SelectionKvAttributeFilterCriteria criteria) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
        
}
