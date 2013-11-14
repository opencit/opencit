/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.controller;

import com.intel.mtwilson.as.controller.exceptions.ASDataException;
import com.intel.mtwilson.as.controller.exceptions.IllegalOrphanException;
import com.intel.mtwilson.as.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.as.data.TblEventType;
import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import com.intel.mtwilson.as.data.TblModuleManifest;
import com.intel.mtwilson.as.data.TblPcrManifest;
import java.util.ArrayList;
import java.util.Collection;
import org.eclipse.persistence.config.CacheUsage;
import org.eclipse.persistence.config.HintValues;
import org.eclipse.persistence.config.QueryHints;

/**
 *
 * @author dsmagadx
 */
public class TblEventTypeJpaController implements Serializable {

    public TblEventTypeJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(TblEventType tblEventType) {
        if (tblEventType.getTblModuleManifestCollection() == null) {
            tblEventType.setTblModuleManifestCollection(new ArrayList<TblModuleManifest>());
        }
        EntityManager em = getEntityManager();
        try {
            
            em.getTransaction().begin();
            Collection<TblModuleManifest> attachedTblModuleManifestCollection = new ArrayList<TblModuleManifest>();
            for (TblModuleManifest tblModuleManifestCollectionTblModuleManifestToAttach : tblEventType.getTblModuleManifestCollection()) {
                tblModuleManifestCollectionTblModuleManifestToAttach = em.getReference(tblModuleManifestCollectionTblModuleManifestToAttach.getClass(), tblModuleManifestCollectionTblModuleManifestToAttach.getId());
                attachedTblModuleManifestCollection.add(tblModuleManifestCollectionTblModuleManifestToAttach);
            }
            tblEventType.setTblModuleManifestCollection(attachedTblModuleManifestCollection);
            em.persist(tblEventType);
            for (TblModuleManifest tblModuleManifestCollectionTblModuleManifest : tblEventType.getTblModuleManifestCollection()) {
                TblEventType oldEventIDOfTblModuleManifestCollectionTblModuleManifest = tblModuleManifestCollectionTblModuleManifest.getEventID();
                tblModuleManifestCollectionTblModuleManifest.setEventID(tblEventType);
                tblModuleManifestCollectionTblModuleManifest = em.merge(tblModuleManifestCollectionTblModuleManifest);
                if (oldEventIDOfTblModuleManifestCollectionTblModuleManifest != null) {
                    oldEventIDOfTblModuleManifestCollectionTblModuleManifest.getTblModuleManifestCollection().remove(tblModuleManifestCollectionTblModuleManifest);
                    em.merge(oldEventIDOfTblModuleManifestCollectionTblModuleManifest);
                }
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public void edit(TblEventType tblEventType) throws IllegalOrphanException, NonexistentEntityException, ASDataException {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            TblEventType persistentTblEventType = em.find(TblEventType.class, tblEventType.getId());
            Collection<TblModuleManifest> tblModuleManifestCollectionOld = persistentTblEventType.getTblModuleManifestCollection();
            Collection<TblModuleManifest> tblModuleManifestCollectionNew = tblEventType.getTblModuleManifestCollection();
            List<String> illegalOrphanMessages = null;
            for (TblModuleManifest tblModuleManifestCollectionOldTblModuleManifest : tblModuleManifestCollectionOld) {
                if (!tblModuleManifestCollectionNew.contains(tblModuleManifestCollectionOldTblModuleManifest)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain TblModuleManifest " + tblModuleManifestCollectionOldTblModuleManifest + " since its eventID field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Collection<TblModuleManifest> attachedTblModuleManifestCollectionNew = new ArrayList<TblModuleManifest>();
            for (TblModuleManifest tblModuleManifestCollectionNewTblModuleManifestToAttach : tblModuleManifestCollectionNew) {
                tblModuleManifestCollectionNewTblModuleManifestToAttach = em.getReference(tblModuleManifestCollectionNewTblModuleManifestToAttach.getClass(), tblModuleManifestCollectionNewTblModuleManifestToAttach.getId());
                attachedTblModuleManifestCollectionNew.add(tblModuleManifestCollectionNewTblModuleManifestToAttach);
            }
            tblModuleManifestCollectionNew = attachedTblModuleManifestCollectionNew;
            tblEventType.setTblModuleManifestCollection(tblModuleManifestCollectionNew);
            tblEventType = em.merge(tblEventType);
            for (TblModuleManifest tblModuleManifestCollectionNewTblModuleManifest : tblModuleManifestCollectionNew) {
                if (!tblModuleManifestCollectionOld.contains(tblModuleManifestCollectionNewTblModuleManifest)) {
                    TblEventType oldEventIDOfTblModuleManifestCollectionNewTblModuleManifest = tblModuleManifestCollectionNewTblModuleManifest.getEventID();
                    tblModuleManifestCollectionNewTblModuleManifest.setEventID(tblEventType);
                    tblModuleManifestCollectionNewTblModuleManifest = em.merge(tblModuleManifestCollectionNewTblModuleManifest);
                    if (oldEventIDOfTblModuleManifestCollectionNewTblModuleManifest != null && !oldEventIDOfTblModuleManifestCollectionNewTblModuleManifest.equals(tblEventType)) {
                        oldEventIDOfTblModuleManifestCollectionNewTblModuleManifest.getTblModuleManifestCollection().remove(tblModuleManifestCollectionNewTblModuleManifest);
                        em.merge(oldEventIDOfTblModuleManifestCollectionNewTblModuleManifest);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = tblEventType.getId();
                if (findTblEventType(id) == null) {
                    throw new NonexistentEntityException("The tblEventType with id " + id + " no longer exists.");
                }
            }
            throw new ASDataException(ex);
        } finally {
                em.close();
        }
    }

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            TblEventType tblEventType;
            try {
                tblEventType = em.getReference(TblEventType.class, id);
                tblEventType.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The tblEventType with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Collection<TblModuleManifest> tblModuleManifestCollectionOrphanCheck = tblEventType.getTblModuleManifestCollection();
            for (TblModuleManifest tblModuleManifestCollectionOrphanCheckTblModuleManifest : tblModuleManifestCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This TblEventType (" + tblEventType + ") cannot be destroyed since the TblModuleManifest " + tblModuleManifestCollectionOrphanCheckTblModuleManifest + " in its tblModuleManifestCollection field has a non-nullable eventID field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(tblEventType);
            em.getTransaction().commit();
        } finally {
                em.close();
        }
    }

    public List<TblEventType> findTblEventTypeEntities() {
        return findTblEventTypeEntities(true, -1, -1);
    }

    public List<TblEventType> findTblEventTypeEntities(int maxResults, int firstResult) {
        return findTblEventTypeEntities(false, maxResults, firstResult);
    }

    private List<TblEventType> findTblEventTypeEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(TblEventType.class));
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

    public TblEventType findTblEventType(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(TblEventType.class, id);
        } finally {
            em.close();
        }
    }

    public int getTblEventTypeCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<TblEventType> rt = cq.from(TblEventType.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    

    /**
     * Added By: Sudhir on June 21, 2012
     * 
     * Retrieves the table row having the identity for the event name specified.
     * 
     * @param eventName : Name of the event
     * @return : Result set for the query.
     */
    public TblEventType findEventTypeByName(String eventName) {
        EntityManager em = getEntityManager();
        try {

            Query query = em.createNamedQuery("TblEventType.findByName");
            query.setParameter("name", eventName);

            // Nov 14, 2013: Commenting out the below setting for better performance and updating the cacheusage to check cache and then DB            
            //query.setHint(QueryHints.REFRESH, HintValues.TRUE);
            query.setHint(QueryHints.CACHE_USAGE, CacheUsage.CheckCacheThenDatabase);

            TblEventType eventType = (TblEventType) query.getSingleResult();
            return eventType;

        } finally {
            em.close();
        }               
    }
}
