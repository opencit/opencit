/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.controller;

import com.intel.mtwilson.as.controller.exceptions.ASDataException;
import com.intel.mtwilson.as.controller.exceptions.IllegalOrphanException;
import com.intel.mtwilson.as.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.as.data.TblModuleManifest;
import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.eclipse.persistence.config.CacheUsage;
import org.eclipse.persistence.config.HintValues;
import org.eclipse.persistence.config.QueryHints;

//import com.intel.mtwilson.as.data.TblDbPortalUser;
import com.intel.mtwilson.as.data.TblMle;
import com.intel.mtwilson.as.data.TblEventType;
import com.intel.mtwilson.as.data.TblPackageNamespace;
import com.intel.mtwilson.as.data.TblHostSpecificManifest;
import java.util.ArrayList;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dsmagadx
 */
public class TblModuleManifestJpaController implements Serializable {
    private Logger log = LoggerFactory.getLogger(getClass());

    public TblModuleManifestJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(TblModuleManifest tblModuleManifest) {
        if (tblModuleManifest.getTblHostSpecificManifestCollection() == null) {
            tblModuleManifest.setTblHostSpecificManifestCollection(new ArrayList<TblHostSpecificManifest>());
        }
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            // @since 1.1 we are relying on the audit log for "created on", "created by", etc. type information
            /*
            TblDbPortalUser updatedBy = tblModuleManifest.getUpdatedBy();
            if (updatedBy != null) {
                updatedBy = em.getReference(updatedBy.getClass(), updatedBy.getId());
                tblModuleManifest.setUpdatedBy(updatedBy);
            }
            TblDbPortalUser createdBy = tblModuleManifest.getCreatedBy();
            if (createdBy != null) {
                createdBy = em.getReference(createdBy.getClass(), createdBy.getId());
                tblModuleManifest.setCreatedBy(createdBy);
            }
            */
            TblMle mleId = tblModuleManifest.getMleId();
            if (mleId != null) {
                mleId = em.getReference(mleId.getClass(), mleId.getId());
                tblModuleManifest.setMleId(mleId);
            }
            TblEventType eventID = tblModuleManifest.getEventID();
            if (eventID != null) {
                eventID = em.getReference(eventID.getClass(), eventID.getId());
                tblModuleManifest.setEventID(eventID);
            }
            TblPackageNamespace nameSpaceID = tblModuleManifest.getNameSpaceID();
            if (nameSpaceID != null) {
                nameSpaceID = em.getReference(nameSpaceID.getClass(), nameSpaceID.getId());
                tblModuleManifest.setNameSpaceID(nameSpaceID);
            }
            Collection<TblHostSpecificManifest> attachedTblHostSpecificManifestCollection = new ArrayList<TblHostSpecificManifest>();
            for (TblHostSpecificManifest tblHostSpecificManifestCollectionTblHostSpecificManifestToAttach : tblModuleManifest.getTblHostSpecificManifestCollection()) {
                tblHostSpecificManifestCollectionTblHostSpecificManifestToAttach = em.getReference(tblHostSpecificManifestCollectionTblHostSpecificManifestToAttach.getClass(), tblHostSpecificManifestCollectionTblHostSpecificManifestToAttach.getId());
                attachedTblHostSpecificManifestCollection.add(tblHostSpecificManifestCollectionTblHostSpecificManifestToAttach);
            }
            tblModuleManifest.setTblHostSpecificManifestCollection(attachedTblHostSpecificManifestCollection);
            em.persist(tblModuleManifest);
            // @since 1.1 we are relying on the audit log for "created on", "created by", etc. type information
            /*
            if (updatedBy != null) {
                updatedBy.getTblModuleManifestCollection().add(tblModuleManifest);
                em.merge(updatedBy);
            }
            if (createdBy != null) {
                createdBy.getTblModuleManifestCollection().add(tblModuleManifest);
                em.merge(createdBy);
            }*/
            if (mleId != null) {
                mleId.getTblModuleManifestCollection().add(tblModuleManifest);
                em.merge(mleId);
            }
            if (eventID != null) {
                eventID.getTblModuleManifestCollection().add(tblModuleManifest);
                em.merge(eventID);
            }
            if (nameSpaceID != null) {
                nameSpaceID.getTblModuleManifestCollection().add(tblModuleManifest);
                em.merge(nameSpaceID);
            }
            for (TblHostSpecificManifest tblHostSpecificManifestCollectionTblHostSpecificManifest : tblModuleManifest.getTblHostSpecificManifestCollection()) {
                TblModuleManifest oldModuleManifestIDOfTblHostSpecificManifestCollectionTblHostSpecificManifest = tblHostSpecificManifestCollectionTblHostSpecificManifest.getModuleManifestID();
                tblHostSpecificManifestCollectionTblHostSpecificManifest.setModuleManifestID(tblModuleManifest);
                tblHostSpecificManifestCollectionTblHostSpecificManifest = em.merge(tblHostSpecificManifestCollectionTblHostSpecificManifest);
                if (oldModuleManifestIDOfTblHostSpecificManifestCollectionTblHostSpecificManifest != null) {
                    oldModuleManifestIDOfTblHostSpecificManifestCollectionTblHostSpecificManifest.getTblHostSpecificManifestCollection().remove(tblHostSpecificManifestCollectionTblHostSpecificManifest);
                    em.merge(oldModuleManifestIDOfTblHostSpecificManifestCollectionTblHostSpecificManifest);
                }
            }
            em.getTransaction().commit();
        } finally {
                em.close();
        }
    }

    public void create_v2(TblModuleManifest tblModuleManifest, EntityManager em) {
        long createV2_1 = System.currentTimeMillis();
        if (tblModuleManifest.getTblHostSpecificManifestCollection() == null) {
            tblModuleManifest.setTblHostSpecificManifestCollection(new ArrayList<TblHostSpecificManifest>());
        }
        try {
            TblMle mleId = tblModuleManifest.getMleId();
            if (mleId != null) {
                mleId = em.getReference(mleId.getClass(), mleId.getId());
                tblModuleManifest.setMleId(mleId);
            }
            TblEventType eventID = tblModuleManifest.getEventID();
            if (eventID != null) {
                eventID = em.getReference(eventID.getClass(), eventID.getId());
                tblModuleManifest.setEventID(eventID);
            }
            TblPackageNamespace nameSpaceID = tblModuleManifest.getNameSpaceID();
            if (nameSpaceID != null) {
                nameSpaceID = em.getReference(nameSpaceID.getClass(), nameSpaceID.getId());
                tblModuleManifest.setNameSpaceID(nameSpaceID);
            }
            long createV2_2 = System.currentTimeMillis();
            log.debug("CREATE_V2 - Time taken to setup IDs :" + (createV2_2 - createV2_1) + "milliseconds.");
            
            Collection<TblHostSpecificManifest> attachedTblHostSpecificManifestCollection = new ArrayList<TblHostSpecificManifest>();
            for (TblHostSpecificManifest tblHostSpecificManifestCollectionTblHostSpecificManifestToAttach : tblModuleManifest.getTblHostSpecificManifestCollection()) {
                tblHostSpecificManifestCollectionTblHostSpecificManifestToAttach = em.getReference(tblHostSpecificManifestCollectionTblHostSpecificManifestToAttach.getClass(), tblHostSpecificManifestCollectionTblHostSpecificManifestToAttach.getId());
                attachedTblHostSpecificManifestCollection.add(tblHostSpecificManifestCollectionTblHostSpecificManifestToAttach);
            }
            tblModuleManifest.setTblHostSpecificManifestCollection(attachedTblHostSpecificManifestCollection);
            em.persist(tblModuleManifest);

            long createV2_3 = System.currentTimeMillis();
            log.debug("CREATE_V2 - Time taken to insert and persist :" + (createV2_3 - createV2_2) + "milliseconds.");

            if (mleId != null) {
                mleId.getTblModuleManifestCollection().add(tblModuleManifest);
                em.merge(mleId);
            }
            if (eventID != null) {
                eventID.getTblModuleManifestCollection().add(tblModuleManifest);
                em.merge(eventID);
            }
            if (nameSpaceID != null) {
                nameSpaceID.getTblModuleManifestCollection().add(tblModuleManifest);
                em.merge(nameSpaceID);
            }
            for (TblHostSpecificManifest tblHostSpecificManifestCollectionTblHostSpecificManifest : tblModuleManifest.getTblHostSpecificManifestCollection()) {
                TblModuleManifest oldModuleManifestIDOfTblHostSpecificManifestCollectionTblHostSpecificManifest = tblHostSpecificManifestCollectionTblHostSpecificManifest.getModuleManifestID();
                tblHostSpecificManifestCollectionTblHostSpecificManifest.setModuleManifestID(tblModuleManifest);
                tblHostSpecificManifestCollectionTblHostSpecificManifest = em.merge(tblHostSpecificManifestCollectionTblHostSpecificManifest);
                if (oldModuleManifestIDOfTblHostSpecificManifestCollectionTblHostSpecificManifest != null) {
                    oldModuleManifestIDOfTblHostSpecificManifestCollectionTblHostSpecificManifest.getTblHostSpecificManifestCollection().remove(tblHostSpecificManifestCollectionTblHostSpecificManifest);
                    em.merge(oldModuleManifestIDOfTblHostSpecificManifestCollectionTblHostSpecificManifest);
                }
            }
            long createV2_4 = System.currentTimeMillis();
            log.debug("CREATE_V2 - Time taken for merge operations :" + (createV2_4 - createV2_3) + "milliseconds.");

        } finally {
        }
    }

    public void edit(TblModuleManifest tblModuleManifest) throws IllegalOrphanException, NonexistentEntityException, ASDataException {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            TblModuleManifest persistentTblModuleManifest = em.find(TblModuleManifest.class, tblModuleManifest.getId());
            // @since 1.1 we are relying on the audit log for "created on", "created by", etc. type information
            /*
            TblDbPortalUser updatedByOld = persistentTblModuleManifest.getUpdatedBy();
            TblDbPortalUser updatedByNew = tblModuleManifest.getUpdatedBy();
            TblDbPortalUser createdByOld = persistentTblModuleManifest.getCreatedBy();
            TblDbPortalUser createdByNew = tblModuleManifest.getCreatedBy();
            */
            TblMle mleIdOld = persistentTblModuleManifest.getMleId();
            TblMle mleIdNew = tblModuleManifest.getMleId();
            TblEventType eventIDOld = persistentTblModuleManifest.getEventID();
            TblEventType eventIDNew = tblModuleManifest.getEventID();
            TblPackageNamespace nameSpaceIDOld = persistentTblModuleManifest.getNameSpaceID();
            TblPackageNamespace nameSpaceIDNew = tblModuleManifest.getNameSpaceID();
            Collection<TblHostSpecificManifest> tblHostSpecificManifestCollectionOld = persistentTblModuleManifest.getTblHostSpecificManifestCollection();
            Collection<TblHostSpecificManifest> tblHostSpecificManifestCollectionNew = tblModuleManifest.getTblHostSpecificManifestCollection();
            List<String> illegalOrphanMessages = null;
            for (TblHostSpecificManifest tblHostSpecificManifestCollectionOldTblHostSpecificManifest : tblHostSpecificManifestCollectionOld) {
                if (!tblHostSpecificManifestCollectionNew.contains(tblHostSpecificManifestCollectionOldTblHostSpecificManifest)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain TblHostSpecificManifest " + tblHostSpecificManifestCollectionOldTblHostSpecificManifest + " since its moduleManifestID field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            // @since 1.1 we are relying on the audit log for "created on", "created by", etc. type information
            /*
            if (updatedByNew != null) {
                updatedByNew = em.getReference(updatedByNew.getClass(), updatedByNew.getId());
                tblModuleManifest.setUpdatedBy(updatedByNew);
            }
            if (createdByNew != null) {
                createdByNew = em.getReference(createdByNew.getClass(), createdByNew.getId());
                tblModuleManifest.setCreatedBy(createdByNew);
            }*/
            if (mleIdNew != null) {
                mleIdNew = em.getReference(mleIdNew.getClass(), mleIdNew.getId());
                tblModuleManifest.setMleId(mleIdNew);
            }
            if (eventIDNew != null) {
                eventIDNew = em.getReference(eventIDNew.getClass(), eventIDNew.getId());
                tblModuleManifest.setEventID(eventIDNew);
            }
            if (nameSpaceIDNew != null) {
                nameSpaceIDNew = em.getReference(nameSpaceIDNew.getClass(), nameSpaceIDNew.getId());
                tblModuleManifest.setNameSpaceID(nameSpaceIDNew);
            }
            Collection<TblHostSpecificManifest> attachedTblHostSpecificManifestCollectionNew = new ArrayList<TblHostSpecificManifest>();
            for (TblHostSpecificManifest tblHostSpecificManifestCollectionNewTblHostSpecificManifestToAttach : tblHostSpecificManifestCollectionNew) {
                tblHostSpecificManifestCollectionNewTblHostSpecificManifestToAttach = em.getReference(tblHostSpecificManifestCollectionNewTblHostSpecificManifestToAttach.getClass(), tblHostSpecificManifestCollectionNewTblHostSpecificManifestToAttach.getId());
                attachedTblHostSpecificManifestCollectionNew.add(tblHostSpecificManifestCollectionNewTblHostSpecificManifestToAttach);
            }
            tblHostSpecificManifestCollectionNew = attachedTblHostSpecificManifestCollectionNew;
            tblModuleManifest.setTblHostSpecificManifestCollection(tblHostSpecificManifestCollectionNew);
            tblModuleManifest = em.merge(tblModuleManifest);
            // @since 1.1 we are relying on the audit log for "created on", "created by", etc. type information
            /*
            if (updatedByOld != null && !updatedByOld.equals(updatedByNew)) {
                updatedByOld.getTblModuleManifestCollection().remove(tblModuleManifest);
                updatedByOld = em.merge(updatedByOld);
            }
            if (updatedByNew != null && !updatedByNew.equals(updatedByOld)) {
                updatedByNew.getTblModuleManifestCollection().add(tblModuleManifest);
                em.merge(updatedByNew);
            }
            if (createdByOld != null && !createdByOld.equals(createdByNew)) {
                createdByOld.getTblModuleManifestCollection().remove(tblModuleManifest);
                createdByOld = em.merge(createdByOld);
            }
            if (createdByNew != null && !createdByNew.equals(createdByOld)) {
                createdByNew.getTblModuleManifestCollection().add(tblModuleManifest);
                em.merge(createdByNew);
            }
            */
            if (mleIdOld != null && !mleIdOld.equals(mleIdNew)) {
                mleIdOld.getTblModuleManifestCollection().remove(tblModuleManifest);
                mleIdOld = em.merge(mleIdOld);
            }
            if (mleIdNew != null && !mleIdNew.equals(mleIdOld)) {
                mleIdNew.getTblModuleManifestCollection().add(tblModuleManifest);
                em.merge(mleIdNew);
            }
            if (eventIDOld != null && !eventIDOld.equals(eventIDNew)) {
                eventIDOld.getTblModuleManifestCollection().remove(tblModuleManifest);
                eventIDOld = em.merge(eventIDOld);
            }
            if (eventIDNew != null && !eventIDNew.equals(eventIDOld)) {
                eventIDNew.getTblModuleManifestCollection().add(tblModuleManifest);
                em.merge(eventIDNew);
            }
            if (nameSpaceIDOld != null && !nameSpaceIDOld.equals(nameSpaceIDNew)) {
                nameSpaceIDOld.getTblModuleManifestCollection().remove(tblModuleManifest);
                nameSpaceIDOld = em.merge(nameSpaceIDOld);
            }
            if (nameSpaceIDNew != null && !nameSpaceIDNew.equals(nameSpaceIDOld)) {
                nameSpaceIDNew.getTblModuleManifestCollection().add(tblModuleManifest);
                em.merge(nameSpaceIDNew);
            }
            for (TblHostSpecificManifest tblHostSpecificManifestCollectionNewTblHostSpecificManifest : tblHostSpecificManifestCollectionNew) {
                if (!tblHostSpecificManifestCollectionOld.contains(tblHostSpecificManifestCollectionNewTblHostSpecificManifest)) {
                    TblModuleManifest oldModuleManifestIDOfTblHostSpecificManifestCollectionNewTblHostSpecificManifest = tblHostSpecificManifestCollectionNewTblHostSpecificManifest.getModuleManifestID();
                    tblHostSpecificManifestCollectionNewTblHostSpecificManifest.setModuleManifestID(tblModuleManifest);
                    tblHostSpecificManifestCollectionNewTblHostSpecificManifest = em.merge(tblHostSpecificManifestCollectionNewTblHostSpecificManifest);
                    if (oldModuleManifestIDOfTblHostSpecificManifestCollectionNewTblHostSpecificManifest != null && !oldModuleManifestIDOfTblHostSpecificManifestCollectionNewTblHostSpecificManifest.equals(tblModuleManifest)) {
                        oldModuleManifestIDOfTblHostSpecificManifestCollectionNewTblHostSpecificManifest.getTblHostSpecificManifestCollection().remove(tblHostSpecificManifestCollectionNewTblHostSpecificManifest);
                        em.merge(oldModuleManifestIDOfTblHostSpecificManifestCollectionNewTblHostSpecificManifest);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = tblModuleManifest.getId();
                if (findTblModuleManifest(id) == null) {
                    throw new NonexistentEntityException("The tblModuleManifest with id " + id + " no longer exists.");
                }
            }
            throw new ASDataException(ex);
        } finally {
                em.close();
        }
    }

    public void edit_v2(TblModuleManifest tblModuleManifest, EntityManager em) throws IllegalOrphanException, NonexistentEntityException, ASDataException {
        try {
            TblModuleManifest persistentTblModuleManifest = em.find(TblModuleManifest.class, tblModuleManifest.getId());

            TblMle mleIdOld = persistentTblModuleManifest.getMleId();
            TblMle mleIdNew = tblModuleManifest.getMleId();
            TblEventType eventIDOld = persistentTblModuleManifest.getEventID();
            TblEventType eventIDNew = tblModuleManifest.getEventID();
            TblPackageNamespace nameSpaceIDOld = persistentTblModuleManifest.getNameSpaceID();
            TblPackageNamespace nameSpaceIDNew = tblModuleManifest.getNameSpaceID();
            Collection<TblHostSpecificManifest> tblHostSpecificManifestCollectionOld = persistentTblModuleManifest.getTblHostSpecificManifestCollection();
            Collection<TblHostSpecificManifest> tblHostSpecificManifestCollectionNew = tblModuleManifest.getTblHostSpecificManifestCollection();
            List<String> illegalOrphanMessages = null;
            for (TblHostSpecificManifest tblHostSpecificManifestCollectionOldTblHostSpecificManifest : tblHostSpecificManifestCollectionOld) {
                if (!tblHostSpecificManifestCollectionNew.contains(tblHostSpecificManifestCollectionOldTblHostSpecificManifest)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain TblHostSpecificManifest " + tblHostSpecificManifestCollectionOldTblHostSpecificManifest + " since its moduleManifestID field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }

            if (mleIdNew != null) {
                mleIdNew = em.getReference(mleIdNew.getClass(), mleIdNew.getId());
                tblModuleManifest.setMleId(mleIdNew);
            }
            if (eventIDNew != null) {
                eventIDNew = em.getReference(eventIDNew.getClass(), eventIDNew.getId());
                tblModuleManifest.setEventID(eventIDNew);
            }
            if (nameSpaceIDNew != null) {
                nameSpaceIDNew = em.getReference(nameSpaceIDNew.getClass(), nameSpaceIDNew.getId());
                tblModuleManifest.setNameSpaceID(nameSpaceIDNew);
            }
            Collection<TblHostSpecificManifest> attachedTblHostSpecificManifestCollectionNew = new ArrayList<TblHostSpecificManifest>();
            for (TblHostSpecificManifest tblHostSpecificManifestCollectionNewTblHostSpecificManifestToAttach : tblHostSpecificManifestCollectionNew) {
                tblHostSpecificManifestCollectionNewTblHostSpecificManifestToAttach = em.getReference(tblHostSpecificManifestCollectionNewTblHostSpecificManifestToAttach.getClass(), tblHostSpecificManifestCollectionNewTblHostSpecificManifestToAttach.getId());
                attachedTblHostSpecificManifestCollectionNew.add(tblHostSpecificManifestCollectionNewTblHostSpecificManifestToAttach);
            }
            tblHostSpecificManifestCollectionNew = attachedTblHostSpecificManifestCollectionNew;
            tblModuleManifest.setTblHostSpecificManifestCollection(tblHostSpecificManifestCollectionNew);
            tblModuleManifest = em.merge(tblModuleManifest);

            if (mleIdOld != null && !mleIdOld.equals(mleIdNew)) {
                mleIdOld.getTblModuleManifestCollection().remove(tblModuleManifest);
                mleIdOld = em.merge(mleIdOld);
            }
            if (mleIdNew != null && !mleIdNew.equals(mleIdOld)) {
                mleIdNew.getTblModuleManifestCollection().add(tblModuleManifest);
                em.merge(mleIdNew);
            }
            if (eventIDOld != null && !eventIDOld.equals(eventIDNew)) {
                eventIDOld.getTblModuleManifestCollection().remove(tblModuleManifest);
                eventIDOld = em.merge(eventIDOld);
            }
            if (eventIDNew != null && !eventIDNew.equals(eventIDOld)) {
                eventIDNew.getTblModuleManifestCollection().add(tblModuleManifest);
                em.merge(eventIDNew);
            }
            if (nameSpaceIDOld != null && !nameSpaceIDOld.equals(nameSpaceIDNew)) {
                nameSpaceIDOld.getTblModuleManifestCollection().remove(tblModuleManifest);
                nameSpaceIDOld = em.merge(nameSpaceIDOld);
            }
            if (nameSpaceIDNew != null && !nameSpaceIDNew.equals(nameSpaceIDOld)) {
                nameSpaceIDNew.getTblModuleManifestCollection().add(tblModuleManifest);
                em.merge(nameSpaceIDNew);
            }
            for (TblHostSpecificManifest tblHostSpecificManifestCollectionNewTblHostSpecificManifest : tblHostSpecificManifestCollectionNew) {
                if (!tblHostSpecificManifestCollectionOld.contains(tblHostSpecificManifestCollectionNewTblHostSpecificManifest)) {
                    TblModuleManifest oldModuleManifestIDOfTblHostSpecificManifestCollectionNewTblHostSpecificManifest = tblHostSpecificManifestCollectionNewTblHostSpecificManifest.getModuleManifestID();
                    tblHostSpecificManifestCollectionNewTblHostSpecificManifest.setModuleManifestID(tblModuleManifest);
                    tblHostSpecificManifestCollectionNewTblHostSpecificManifest = em.merge(tblHostSpecificManifestCollectionNewTblHostSpecificManifest);
                    if (oldModuleManifestIDOfTblHostSpecificManifestCollectionNewTblHostSpecificManifest != null && !oldModuleManifestIDOfTblHostSpecificManifestCollectionNewTblHostSpecificManifest.equals(tblModuleManifest)) {
                        oldModuleManifestIDOfTblHostSpecificManifestCollectionNewTblHostSpecificManifest.getTblHostSpecificManifestCollection().remove(tblHostSpecificManifestCollectionNewTblHostSpecificManifest);
                        em.merge(oldModuleManifestIDOfTblHostSpecificManifestCollectionNewTblHostSpecificManifest);
                    }
                }
            }
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = tblModuleManifest.getId();
                if (findTblModuleManifest(id) == null) {
                    throw new NonexistentEntityException("The tblModuleManifest with id " + id + " no longer exists.");
                }
            }
            throw new ASDataException(ex);
        } finally {
        }
    }

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            TblModuleManifest tblModuleManifest;
            try {
                tblModuleManifest = em.getReference(TblModuleManifest.class, id);
                tblModuleManifest.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The tblModuleManifest with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Collection<TblHostSpecificManifest> tblHostSpecificManifestCollectionOrphanCheck = tblModuleManifest.getTblHostSpecificManifestCollection();
            for (TblHostSpecificManifest tblHostSpecificManifestCollectionOrphanCheckTblHostSpecificManifest : tblHostSpecificManifestCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This TblModuleManifest (" + tblModuleManifest + ") cannot be destroyed since the TblHostSpecificManifest " + tblHostSpecificManifestCollectionOrphanCheckTblHostSpecificManifest + " in its tblHostSpecificManifestCollection field has a non-nullable moduleManifestID field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            // @since 1.1 we are relying on the audit log for "created on", "created by", etc. type information
            /*
            TblDbPortalUser updatedBy = tblModuleManifest.getUpdatedBy();
            if (updatedBy != null) {
                updatedBy.getTblModuleManifestCollection().remove(tblModuleManifest);
                em.merge(updatedBy);
            }
            TblDbPortalUser createdBy = tblModuleManifest.getCreatedBy();
            if (createdBy != null) {
                createdBy.getTblModuleManifestCollection().remove(tblModuleManifest);
                em.merge(createdBy);
            }
            */
            TblMle mleId = tblModuleManifest.getMleId();
            if (mleId != null) {
                mleId.getTblModuleManifestCollection().remove(tblModuleManifest);
                em.merge(mleId);
            }
            TblEventType eventID = tblModuleManifest.getEventID();
            if (eventID != null) {
                eventID.getTblModuleManifestCollection().remove(tblModuleManifest);
                em.merge(eventID);
            }
            TblPackageNamespace nameSpaceID = tblModuleManifest.getNameSpaceID();
            if (nameSpaceID != null) {
                nameSpaceID.getTblModuleManifestCollection().remove(tblModuleManifest);
                em.merge(nameSpaceID);
            }
            em.remove(tblModuleManifest);
            em.getTransaction().commit();
        } finally {
                em.close();
        }
    }

    public List<TblModuleManifest> findTblModuleManifestEntities() {
        return findTblModuleManifestEntities(true, -1, -1);
    }

    public List<TblModuleManifest> findTblModuleManifestEntities(int maxResults, int firstResult) {
        return findTblModuleManifestEntities(false, maxResults, firstResult);
    }

    private List<TblModuleManifest> findTblModuleManifestEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(TblModuleManifest.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public TblModuleManifest findTblModuleManifest(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(TblModuleManifest.class, id);
        } finally {
            em.close();
        }
    }

    public int getTblModuleManifestCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<TblModuleManifest> rt = cq.from(TblModuleManifest.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }

    public List<TblModuleManifest> findByMleId(Integer mleId) {
        EntityManager em = getEntityManager();
        try {           
            Query query = em.createNamedQuery("TblModuleManifest.findByMleId");
            query.setParameter("mleId", mleId);
            
            query.setHint(QueryHints.REFRESH, HintValues.TRUE);
            query.setHint(QueryHints.CACHE_USAGE, CacheUsage.DoNotCheckCache);
            
            List<TblModuleManifest> tblModuleManifestList = query.getResultList();
            return tblModuleManifestList;

        } finally {
            em.close();
        }
        
    }


    /**
     * Modified By: Sudhir on June 21st to remove the throw of ASException. Instead the NoResultException is
     * being thrown, which is caught by the caller.
     * 
     * @param mleId
     * @param componentName
     * @param eventName
     * @return 
     */
    public List<TblModuleManifest> findByMleNameEventName(Integer mleId,String componentName, String eventName){
    
        EntityManager em = getEntityManager();
        try {
            log.debug(String.format("Module Manifest for MLE: %d Component: %s Event: %s", mleId,componentName, eventName));
            Query query = em.createNamedQuery("TblModuleManifest.findByMleNameEventName");
            query.setParameter("name", componentName);
            query.setParameter("eventName", eventName);
            query.setParameter("mleId", mleId);
            
            
            query.setHint(QueryHints.REFRESH, HintValues.TRUE);
            query.setHint(QueryHints.CACHE_USAGE, CacheUsage.DoNotCheckCache);
            
            
            return (List<TblModuleManifest>)query.getResultList();            
            
        } catch(NoResultException e){
        	log.error(String.format("Module Manifest for MLE %d Component %s Event %s  Not found in Database ", mleId,componentName, eventName), e);
        	return null;
        } finally {
            em.close();
        }            	
    }
    
    public TblModuleManifest findByMleNameEventNamePcrBank(Integer mleId,String componentName, String eventName, String pcrBank){
    
        EntityManager em = getEntityManager();
        try {
            log.debug(String.format("Module Manifest for MLE: %d Component: %s Event: %s", mleId,componentName, eventName));
            Query query = em.createNamedQuery("TblModuleManifest.findByMleNameEventNamePcrBank");
            query.setParameter("name", componentName);
            query.setParameter("eventName", eventName);
            query.setParameter("mleId", mleId);
            query.setParameter("pcrBank", pcrBank);
            
            
            query.setHint(QueryHints.REFRESH, HintValues.TRUE);
            query.setHint(QueryHints.CACHE_USAGE, CacheUsage.DoNotCheckCache);
            
            
            return (TblModuleManifest)query.getSingleResult();            
            
        } catch(NoResultException e){
        	log.error(String.format("Module Manifest for MLE %d Component %s Event %s PcrBank %s Not found in Database ", mleId,componentName, eventName, pcrBank), e);
        	return null;
        } finally {
            em.close();
        }            	
    }

    
    public TblModuleManifest findByMleIdEventIdPcrBank(Integer mleId, String componentName, Integer eventId, String pcrBank){
    
        EntityManager em = getEntityManager();
        try {
            log.debug(String.format("Module Manifest for MLE: %d Component: %s Event: %s", mleId,componentName, eventId));
            Query query = em.createNamedQuery("TblModuleManifest.findByMleIDEventIDPcrBank");
            query.setParameter("name", componentName);
            query.setParameter("eventId", eventId);
            query.setParameter("mleId", mleId);
            query.setParameter("pcrBank", pcrBank);
            
            
            query.setHint(QueryHints.REFRESH, HintValues.TRUE);
            query.setHint(QueryHints.CACHE_USAGE, CacheUsage.CheckCacheThenDatabase);
            
            return (TblModuleManifest) query.getSingleResult();                                    
            
        } catch(NoResultException e){
                //log.error(String.format("Module Manifest for MLE %d Component %s Event %s  Not found in Database ", mleId,componentName, eventId), e);            
        	log.info("Module Manifest for MLE {}, Component {} & Event {} & PcrBank {} not found in Database ", mleId, componentName, eventId, pcrBank);
        	return null;
        } finally {
            em.close();
        }
            	
    }
   
    public List<TblModuleManifest> findTblModuleManifestByMleUuid(String mleUuid) {
        
        EntityManager em = getEntityManager();
        try {

            Query query = em.createNamedQuery("TblModuleManifest.findByMleUuidHex");
            query.setParameter("mle_uuid_hex", mleUuid);

            List<TblModuleManifest> moduleList = query.getResultList();
            return moduleList;

        } catch(NoResultException e){
        	log.error(String.format("MLE information with UUID {} not found in the DB.", mleUuid));
        	return null;
        } finally {
            em.close();
        }               
    }    

    public TblModuleManifest findTblModuleManifestByUuid(String uuid) {
        
        EntityManager em = getEntityManager();
        try {

            Query query = em.createNamedQuery("TblModuleManifest.findByUuidHex");
            query.setParameter("uuid_hex", uuid);

            TblModuleManifest pcrObj = (TblModuleManifest) query.getSingleResult();
            return pcrObj;

        } catch(NoResultException e){
        	log.error(String.format("Module information with UUID {} not found in the DB.", uuid));
        	return null;
        } finally {
            em.close();
        }               
    }    

    public List<TblModuleManifest> findTblModuleManifestByComponentNameLike(String moduleName) {
        
        EntityManager em = getEntityManager();
        try {

            Query query = em.createNamedQuery("TblModuleManifest.findByComponentNameLike");
            query.setParameter("name", "%"+moduleName+"%");

            List<TblModuleManifest> pcrList = query.getResultList();
            return pcrList;

        } catch(NoResultException e){
        	log.error(String.format("Module information with name {} not found in the DB.", moduleName));
        	return null;
        } finally {
            em.close();
        }               
    }    

    public List<TblModuleManifest> findByComponentVlaue(String digestValue) {
        
        EntityManager em = getEntityManager();
        try {

            Query query = em.createNamedQuery("TblModuleManifest.findByModuleValue");
            query.setParameter("digestValue", digestValue);

            List<TblModuleManifest> pcrList = query.getResultList();
            return pcrList;

        } catch(NoResultException e){
        	log.error(String.format("Module information with name {} not found in the DB.", digestValue));
        	return null;
        } finally {
            em.close();
        }               
    }    
    
}
