/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.tag.rest.v2.repository;

import com.intel.mtwilson.jersey.resource.SimpleRepository;
import com.intel.mtwilson.tag.dao.TagJdbi;
import com.intel.mtwilson.tag.dao.jdbi.SelectionKvAttributeDAO;
import com.intel.mtwilson.tag.rest.v2.model.SelectionKvAttribute;
import com.intel.mtwilson.tag.rest.v2.model.SelectionKvAttributeCollection;
import com.intel.mtwilson.tag.rest.v2.model.SelectionKvAttributeFilterCriteria;
import com.intel.mtwilson.tag.rest.v2.model.SelectionKvAttributeLocator;
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
        return null;
        /*SelectionCollection objCollection = new SelectionCollection();
        KvAttributeDAO attrDao = null;
        DSLContext jooq = null;
        SelectionDAO selectionDao = null;
        SelectionKvAttributeDAO selectionTagDao = null;
        
        try {
                    
            SelectQuery sql = jooq.select()
                    .from(SELECTION.join(SELECTION_TAG_VALUE)
                    .on(SELECTION_TAG_VALUE.SELECTIONID.equal(SELECTION.ID))).getQuery();
            if( criteria.tagValueEqualTo != null || criteria.tagValueContains != null ) {
                log.debug("Selecting from tag-value");
                SelectQuery valueQuery = jooq.select(SELECTION_TAG_VALUE.ID)
                        .from(SELECTION_TAG_VALUE.join(TAG_VALUE).on(TAG_VALUE.ID.equal(SELECTION_TAG_VALUE.TAGVALUEID)))
                        .getQuery();
                if( criteria.tagValueEqualTo != null && criteria.tagValueEqualTo.length() > 0 ) {
                    valueQuery.addConditions(TAG_VALUE.VALUE.equal(criteria.tagValueEqualTo));
                }
                if( criteria.tagValueContains != null  && criteria.tagValueContains.length() > 0 ) {
                    valueQuery.addConditions(TAG_VALUE.VALUE.contains(criteria.tagValueContains));
                }
                sql.addConditions(SELECTION_TAG_VALUE.ID.in(valueQuery));
            }
            if( criteria.tagNameContains != null || criteria.tagNameEqualTo != null || criteria.tagOidContains != null || criteria.tagOidEqualTo != null ) {
                log.debug("Selecting from tag");
                SelectQuery tagQuery = jooq.select(SELECTION_TAG_VALUE.ID)
                        .from(SELECTION_TAG_VALUE.join(TAG).on(TAG.ID.equal(SELECTION_TAG_VALUE.TAGID)))
                        .getQuery();
                if( criteria.tagNameEqualTo != null  && criteria.tagNameEqualTo.length() > 0 ) {
                    tagQuery.addConditions(TAG.NAME.equal(criteria.tagNameEqualTo));
                }
                if( criteria.tagNameContains != null  && criteria.tagNameContains.length() > 0 ) {
                    tagQuery.addConditions(TAG.NAME.contains(criteria.tagNameContains));
                }
                if( criteria.tagOidEqualTo != null  && criteria.tagOidEqualTo.length() > 0 ) {
                    tagQuery.addConditions(TAG.OID.equal(criteria.tagOidEqualTo));
                }
                if( criteria.tagOidContains != null  && criteria.tagOidContains.length() > 0 ) {
                    tagQuery.addConditions(TAG.OID.contains(criteria.tagOidContains));
                }
                sql.addConditions(SELECTION_TAG_VALUE.ID.in(tagQuery));            
            }
            if( criteria.id != null ) {
                sql.addConditions(SELECTION.UUID.equal(criteria.id.toString())); // when uuid is stored in database as the standard UUID string format (36 chars)
            }
            if( criteria.nameEqualTo != null  && criteria.nameEqualTo.length() > 0 ) {
                sql.addConditions(SELECTION.NAME.equal(criteria.nameEqualTo));
            }
            if( criteria.nameContains != null  && criteria.nameContains.length() > 0  ) {
                sql.addConditions(SELECTION.NAME.contains(criteria.nameContains));
            }
            sql.addOrderBy(SELECTION.ID);
            Result<Record> result = sql.fetch();
            HashMap<Long,Selection> selections = new HashMap<Long,Selection>();
            log.debug("Got {} selection records", result.size());
            for(Record r : result) {
                if( selections.get(r.getValue(SELECTION.ID)) == null ) {
                    Selection selection = new Selection();
                    selection.setId(r.getValue(SELECTION.ID));
                    selection.setUuid(UUID.valueOf(r.getValue(SELECTION.UUID)));
                    selection.setName(r.getValue(SELECTION.NAME));
                    selection.setTags(new ArrayList<SelectionKvAttribute>());
                    selections.put(r.getValue(SELECTION.ID), selection);
                    log.debug("Created new selection object: {}", selection.getId());
                }

                SelectionKvAttribute crtv = new SelectionKvAttribute();
                crtv.setId(r.getValue(SELECTION_TAG_VALUE.ID));
                crtv.setSelectionId(r.getValue(SELECTION.ID));
                crtv.setAttributeId(r.getValue(SELECTION_TAG_VALUE.TAGID));
                crtv.setAttributeValueId(r.getValue(SELECTION_TAG_VALUE.TAGVALUEID));
                
                // XXX TODO inefficient to make two extra queries for the tag name and tag value... probably better to move this up to the big criteria but need to design the join properly
                KvAttribute attr = attrDao.findById(crtv.getAttributeId());
                AttributeValue attrValue = attrValueDao.findById(crtv.getAttributeValueId());
                if( attr != null && attrValue != null ) {
                    crtv.setAttributeName(attr.getName());
                    crtv.setAttributeOid(attr.getOid());
                    crtv.setAttributeValue(attrValue.getValue());
                    Selection selection = selections.get(r.getValue(SELECTION.ID));
                    selection.getTags().add(crtv); // XXX inefficient to look up the selection each time in the map but we want to avoid relying on the order of fields in the resultset... we're not assuming that all related records would be grouped together
                    log.debug("Added tag to selection object: {}", selection.getId());
                }
                else {
                    log.debug("tag is null? {}", attr == null);
                    log.debug("tag-value is null? {}", attrValue == null);
                }
            }
            sql.close();
            log.debug("Closed jooq sql statement");
            objCollection.getSelections().addAll(selections.values());
        } catch (ResourceException aex) {
            throw aex;            
        } catch (Exception ex) {
            log.error("Error during attribute search.", ex);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Please see the server log for more details.");
        } finally {
            if (attrDao != null)
                attrDao.close();
            if (attrValueDao != null)
                attrValueDao.close();
            if (selectionDao != null)
                selectionDao.close();
            if (selectionTagDao != null)
                selectionTagDao.close();
        }
        return objCollection;*/
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
