/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.controller;

import com.intel.mtwilson.as.controller.exceptions.IllegalOrphanException;
import com.intel.mtwilson.as.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.as.data.TblDbPortalUser;
import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import com.intel.mtwilson.as.data.TblPcrManifest;
import java.util.ArrayList;
import java.util.Collection;
import com.intel.mtwilson.as.data.TblModuleManifest;

/**
 *
 * @author dsmagadx
 */
public class TblDbPortalUserJpaController implements Serializable {

    public TblDbPortalUserJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(TblDbPortalUser tblDbPortalUser) {
        if (tblDbPortalUser.getTblPcrManifestCollection() == null) {
            tblDbPortalUser.setTblPcrManifestCollection(new ArrayList<TblPcrManifest>());
        }
        if (tblDbPortalUser.getTblPcrManifestCollection1() == null) {
            tblDbPortalUser.setTblPcrManifestCollection1(new ArrayList<TblPcrManifest>());
        }
        if (tblDbPortalUser.getTblModuleManifestCollection() == null) {
            tblDbPortalUser.setTblModuleManifestCollection(new ArrayList<TblModuleManifest>());
        }
        if (tblDbPortalUser.getTblModuleManifestCollection1() == null) {
            tblDbPortalUser.setTblModuleManifestCollection1(new ArrayList<TblModuleManifest>());
        }
        EntityManager em = getEntityManager();
        try {
            
            em.getTransaction().begin();
            Collection<TblPcrManifest> attachedTblPcrManifestCollection = new ArrayList<TblPcrManifest>();
            for (TblPcrManifest tblPcrManifestCollectionTblPcrManifestToAttach : tblDbPortalUser.getTblPcrManifestCollection()) {
                tblPcrManifestCollectionTblPcrManifestToAttach = em.getReference(tblPcrManifestCollectionTblPcrManifestToAttach.getClass(), tblPcrManifestCollectionTblPcrManifestToAttach.getId());
                attachedTblPcrManifestCollection.add(tblPcrManifestCollectionTblPcrManifestToAttach);
            }
            tblDbPortalUser.setTblPcrManifestCollection(attachedTblPcrManifestCollection);
            Collection<TblPcrManifest> attachedTblPcrManifestCollection1 = new ArrayList<TblPcrManifest>();
            for (TblPcrManifest tblPcrManifestCollection1TblPcrManifestToAttach : tblDbPortalUser.getTblPcrManifestCollection1()) {
                tblPcrManifestCollection1TblPcrManifestToAttach = em.getReference(tblPcrManifestCollection1TblPcrManifestToAttach.getClass(), tblPcrManifestCollection1TblPcrManifestToAttach.getId());
                attachedTblPcrManifestCollection1.add(tblPcrManifestCollection1TblPcrManifestToAttach);
            }
            tblDbPortalUser.setTblPcrManifestCollection1(attachedTblPcrManifestCollection1);
            Collection<TblModuleManifest> attachedTblModuleManifestCollection = new ArrayList<TblModuleManifest>();
            for (TblModuleManifest tblModuleManifestCollectionTblModuleManifestToAttach : tblDbPortalUser.getTblModuleManifestCollection()) {
                tblModuleManifestCollectionTblModuleManifestToAttach = em.getReference(tblModuleManifestCollectionTblModuleManifestToAttach.getClass(), tblModuleManifestCollectionTblModuleManifestToAttach.getId());
                attachedTblModuleManifestCollection.add(tblModuleManifestCollectionTblModuleManifestToAttach);
            }
            tblDbPortalUser.setTblModuleManifestCollection(attachedTblModuleManifestCollection);
            Collection<TblModuleManifest> attachedTblModuleManifestCollection1 = new ArrayList<TblModuleManifest>();
            for (TblModuleManifest tblModuleManifestCollection1TblModuleManifestToAttach : tblDbPortalUser.getTblModuleManifestCollection1()) {
                tblModuleManifestCollection1TblModuleManifestToAttach = em.getReference(tblModuleManifestCollection1TblModuleManifestToAttach.getClass(), tblModuleManifestCollection1TblModuleManifestToAttach.getId());
                attachedTblModuleManifestCollection1.add(tblModuleManifestCollection1TblModuleManifestToAttach);
            }
            tblDbPortalUser.setTblModuleManifestCollection1(attachedTblModuleManifestCollection1);
            em.persist(tblDbPortalUser);
            for (TblPcrManifest tblPcrManifestCollectionTblPcrManifest : tblDbPortalUser.getTblPcrManifestCollection()) {
                TblDbPortalUser oldUpdatedByOfTblPcrManifestCollectionTblPcrManifest = tblPcrManifestCollectionTblPcrManifest.getUpdatedBy();
                tblPcrManifestCollectionTblPcrManifest.setUpdatedBy(tblDbPortalUser);
                tblPcrManifestCollectionTblPcrManifest = em.merge(tblPcrManifestCollectionTblPcrManifest);
                if (oldUpdatedByOfTblPcrManifestCollectionTblPcrManifest != null) {
                    oldUpdatedByOfTblPcrManifestCollectionTblPcrManifest.getTblPcrManifestCollection().remove(tblPcrManifestCollectionTblPcrManifest);
                    em.merge(oldUpdatedByOfTblPcrManifestCollectionTblPcrManifest);
                }
            }
            for (TblPcrManifest tblPcrManifestCollection1TblPcrManifest : tblDbPortalUser.getTblPcrManifestCollection1()) {
                TblDbPortalUser oldCreatedByOfTblPcrManifestCollection1TblPcrManifest = tblPcrManifestCollection1TblPcrManifest.getCreatedBy();
                tblPcrManifestCollection1TblPcrManifest.setCreatedBy(tblDbPortalUser);
                tblPcrManifestCollection1TblPcrManifest = em.merge(tblPcrManifestCollection1TblPcrManifest);
                if (oldCreatedByOfTblPcrManifestCollection1TblPcrManifest != null) {
                    oldCreatedByOfTblPcrManifestCollection1TblPcrManifest.getTblPcrManifestCollection1().remove(tblPcrManifestCollection1TblPcrManifest);
                    em.merge(oldCreatedByOfTblPcrManifestCollection1TblPcrManifest);
                }
            }
            for (TblModuleManifest tblModuleManifestCollectionTblModuleManifest : tblDbPortalUser.getTblModuleManifestCollection()) {
                TblDbPortalUser oldUpdatedByOfTblModuleManifestCollectionTblModuleManifest = tblModuleManifestCollectionTblModuleManifest.getUpdatedBy();
                tblModuleManifestCollectionTblModuleManifest.setUpdatedBy(tblDbPortalUser);
                tblModuleManifestCollectionTblModuleManifest = em.merge(tblModuleManifestCollectionTblModuleManifest);
                if (oldUpdatedByOfTblModuleManifestCollectionTblModuleManifest != null) {
                    oldUpdatedByOfTblModuleManifestCollectionTblModuleManifest.getTblModuleManifestCollection().remove(tblModuleManifestCollectionTblModuleManifest);
                    em.merge(oldUpdatedByOfTblModuleManifestCollectionTblModuleManifest);
                }
            }
            for (TblModuleManifest tblModuleManifestCollection1TblModuleManifest : tblDbPortalUser.getTblModuleManifestCollection1()) {
                TblDbPortalUser oldCreatedByOfTblModuleManifestCollection1TblModuleManifest = tblModuleManifestCollection1TblModuleManifest.getCreatedBy();
                tblModuleManifestCollection1TblModuleManifest.setCreatedBy(tblDbPortalUser);
                tblModuleManifestCollection1TblModuleManifest = em.merge(tblModuleManifestCollection1TblModuleManifest);
                if (oldCreatedByOfTblModuleManifestCollection1TblModuleManifest != null) {
                    oldCreatedByOfTblModuleManifestCollection1TblModuleManifest.getTblModuleManifestCollection1().remove(tblModuleManifestCollection1TblModuleManifest);
                    em.merge(oldCreatedByOfTblModuleManifestCollection1TblModuleManifest);
                }
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public void edit(TblDbPortalUser tblDbPortalUser) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            TblDbPortalUser persistentTblDbPortalUser = em.find(TblDbPortalUser.class, tblDbPortalUser.getId());
            if( persistentTblDbPortalUser == null ) {
            	throw new NonexistentEntityException("The tblDbPortalUser with id " + tblDbPortalUser.getId() + " no longer exists.");
            }
            Collection<TblPcrManifest> tblPcrManifestCollectionOld = persistentTblDbPortalUser.getTblPcrManifestCollection();
            Collection<TblPcrManifest> tblPcrManifestCollectionNew = tblDbPortalUser.getTblPcrManifestCollection();
            Collection<TblPcrManifest> tblPcrManifestCollection1Old = persistentTblDbPortalUser.getTblPcrManifestCollection1();
            Collection<TblPcrManifest> tblPcrManifestCollection1New = tblDbPortalUser.getTblPcrManifestCollection1();
            Collection<TblModuleManifest> tblModuleManifestCollectionOld = persistentTblDbPortalUser.getTblModuleManifestCollection();
            Collection<TblModuleManifest> tblModuleManifestCollectionNew = tblDbPortalUser.getTblModuleManifestCollection();
            Collection<TblModuleManifest> tblModuleManifestCollection1Old = persistentTblDbPortalUser.getTblModuleManifestCollection1();
            Collection<TblModuleManifest> tblModuleManifestCollection1New = tblDbPortalUser.getTblModuleManifestCollection1();
            List<String> illegalOrphanMessages = null;
            for (TblPcrManifest tblPcrManifestCollectionOldTblPcrManifest : tblPcrManifestCollectionOld) {
                if (!tblPcrManifestCollectionNew.contains(tblPcrManifestCollectionOldTblPcrManifest)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain TblPcrManifest " + tblPcrManifestCollectionOldTblPcrManifest + " since its updatedBy field is not nullable.");
                }
            }
            for (TblPcrManifest tblPcrManifestCollection1OldTblPcrManifest : tblPcrManifestCollection1Old) {
                if (!tblPcrManifestCollection1New.contains(tblPcrManifestCollection1OldTblPcrManifest)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain TblPcrManifest " + tblPcrManifestCollection1OldTblPcrManifest + " since its createdBy field is not nullable.");
                }
            }
            for (TblModuleManifest tblModuleManifestCollectionOldTblModuleManifest : tblModuleManifestCollectionOld) {
                if (!tblModuleManifestCollectionNew.contains(tblModuleManifestCollectionOldTblModuleManifest)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain TblModuleManifest " + tblModuleManifestCollectionOldTblModuleManifest + " since its updatedBy field is not nullable.");
                }
            }
            for (TblModuleManifest tblModuleManifestCollection1OldTblModuleManifest : tblModuleManifestCollection1Old) {
                if (!tblModuleManifestCollection1New.contains(tblModuleManifestCollection1OldTblModuleManifest)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain TblModuleManifest " + tblModuleManifestCollection1OldTblModuleManifest + " since its createdBy field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Collection<TblPcrManifest> attachedTblPcrManifestCollectionNew = new ArrayList<TblPcrManifest>();
            for (TblPcrManifest tblPcrManifestCollectionNewTblPcrManifestToAttach : tblPcrManifestCollectionNew) {
                tblPcrManifestCollectionNewTblPcrManifestToAttach = em.getReference(tblPcrManifestCollectionNewTblPcrManifestToAttach.getClass(), tblPcrManifestCollectionNewTblPcrManifestToAttach.getId());
                attachedTblPcrManifestCollectionNew.add(tblPcrManifestCollectionNewTblPcrManifestToAttach);
            }
            tblPcrManifestCollectionNew = attachedTblPcrManifestCollectionNew;
            tblDbPortalUser.setTblPcrManifestCollection(tblPcrManifestCollectionNew);
            Collection<TblPcrManifest> attachedTblPcrManifestCollection1New = new ArrayList<TblPcrManifest>();
            for (TblPcrManifest tblPcrManifestCollection1NewTblPcrManifestToAttach : tblPcrManifestCollection1New) {
                tblPcrManifestCollection1NewTblPcrManifestToAttach = em.getReference(tblPcrManifestCollection1NewTblPcrManifestToAttach.getClass(), tblPcrManifestCollection1NewTblPcrManifestToAttach.getId());
                attachedTblPcrManifestCollection1New.add(tblPcrManifestCollection1NewTblPcrManifestToAttach);
            }
            tblPcrManifestCollection1New = attachedTblPcrManifestCollection1New;
            tblDbPortalUser.setTblPcrManifestCollection1(tblPcrManifestCollection1New);
            Collection<TblModuleManifest> attachedTblModuleManifestCollectionNew = new ArrayList<TblModuleManifest>();
            for (TblModuleManifest tblModuleManifestCollectionNewTblModuleManifestToAttach : tblModuleManifestCollectionNew) {
                tblModuleManifestCollectionNewTblModuleManifestToAttach = em.getReference(tblModuleManifestCollectionNewTblModuleManifestToAttach.getClass(), tblModuleManifestCollectionNewTblModuleManifestToAttach.getId());
                attachedTblModuleManifestCollectionNew.add(tblModuleManifestCollectionNewTblModuleManifestToAttach);
            }
            tblModuleManifestCollectionNew = attachedTblModuleManifestCollectionNew;
            tblDbPortalUser.setTblModuleManifestCollection(tblModuleManifestCollectionNew);
            Collection<TblModuleManifest> attachedTblModuleManifestCollection1New = new ArrayList<TblModuleManifest>();
            for (TblModuleManifest tblModuleManifestCollection1NewTblModuleManifestToAttach : tblModuleManifestCollection1New) {
                tblModuleManifestCollection1NewTblModuleManifestToAttach = em.getReference(tblModuleManifestCollection1NewTblModuleManifestToAttach.getClass(), tblModuleManifestCollection1NewTblModuleManifestToAttach.getId());
                attachedTblModuleManifestCollection1New.add(tblModuleManifestCollection1NewTblModuleManifestToAttach);
            }
            tblModuleManifestCollection1New = attachedTblModuleManifestCollection1New;
            tblDbPortalUser.setTblModuleManifestCollection1(tblModuleManifestCollection1New);
            tblDbPortalUser = em.merge(tblDbPortalUser);
            for (TblPcrManifest tblPcrManifestCollectionNewTblPcrManifest : tblPcrManifestCollectionNew) {
                if (!tblPcrManifestCollectionOld.contains(tblPcrManifestCollectionNewTblPcrManifest)) {
                    TblDbPortalUser oldUpdatedByOfTblPcrManifestCollectionNewTblPcrManifest = tblPcrManifestCollectionNewTblPcrManifest.getUpdatedBy();
                    tblPcrManifestCollectionNewTblPcrManifest.setUpdatedBy(tblDbPortalUser);
                    tblPcrManifestCollectionNewTblPcrManifest = em.merge(tblPcrManifestCollectionNewTblPcrManifest);
                    if (oldUpdatedByOfTblPcrManifestCollectionNewTblPcrManifest != null && !oldUpdatedByOfTblPcrManifestCollectionNewTblPcrManifest.equals(tblDbPortalUser)) {
                        oldUpdatedByOfTblPcrManifestCollectionNewTblPcrManifest.getTblPcrManifestCollection().remove(tblPcrManifestCollectionNewTblPcrManifest);
                        em.merge(oldUpdatedByOfTblPcrManifestCollectionNewTblPcrManifest);
                    }
                }
            }
            for (TblPcrManifest tblPcrManifestCollection1NewTblPcrManifest : tblPcrManifestCollection1New) {
                if (!tblPcrManifestCollection1Old.contains(tblPcrManifestCollection1NewTblPcrManifest)) {
                    TblDbPortalUser oldCreatedByOfTblPcrManifestCollection1NewTblPcrManifest = tblPcrManifestCollection1NewTblPcrManifest.getCreatedBy();
                    tblPcrManifestCollection1NewTblPcrManifest.setCreatedBy(tblDbPortalUser);
                    tblPcrManifestCollection1NewTblPcrManifest = em.merge(tblPcrManifestCollection1NewTblPcrManifest);
                    if (oldCreatedByOfTblPcrManifestCollection1NewTblPcrManifest != null && !oldCreatedByOfTblPcrManifestCollection1NewTblPcrManifest.equals(tblDbPortalUser)) {
                        oldCreatedByOfTblPcrManifestCollection1NewTblPcrManifest.getTblPcrManifestCollection1().remove(tblPcrManifestCollection1NewTblPcrManifest);
                        em.merge(oldCreatedByOfTblPcrManifestCollection1NewTblPcrManifest);
                    }
                }
            }
            for (TblModuleManifest tblModuleManifestCollectionNewTblModuleManifest : tblModuleManifestCollectionNew) {
                if (!tblModuleManifestCollectionOld.contains(tblModuleManifestCollectionNewTblModuleManifest)) {
                    TblDbPortalUser oldUpdatedByOfTblModuleManifestCollectionNewTblModuleManifest = tblModuleManifestCollectionNewTblModuleManifest.getUpdatedBy();
                    tblModuleManifestCollectionNewTblModuleManifest.setUpdatedBy(tblDbPortalUser);
                    tblModuleManifestCollectionNewTblModuleManifest = em.merge(tblModuleManifestCollectionNewTblModuleManifest);
                    if (oldUpdatedByOfTblModuleManifestCollectionNewTblModuleManifest != null && !oldUpdatedByOfTblModuleManifestCollectionNewTblModuleManifest.equals(tblDbPortalUser)) {
                        oldUpdatedByOfTblModuleManifestCollectionNewTblModuleManifest.getTblModuleManifestCollection().remove(tblModuleManifestCollectionNewTblModuleManifest);
                        em.merge(oldUpdatedByOfTblModuleManifestCollectionNewTblModuleManifest);
                    }
                }
            }
            for (TblModuleManifest tblModuleManifestCollection1NewTblModuleManifest : tblModuleManifestCollection1New) {
                if (!tblModuleManifestCollection1Old.contains(tblModuleManifestCollection1NewTblModuleManifest)) {
                    TblDbPortalUser oldCreatedByOfTblModuleManifestCollection1NewTblModuleManifest = tblModuleManifestCollection1NewTblModuleManifest.getCreatedBy();
                    tblModuleManifestCollection1NewTblModuleManifest.setCreatedBy(tblDbPortalUser);
                    tblModuleManifestCollection1NewTblModuleManifest = em.merge(tblModuleManifestCollection1NewTblModuleManifest);
                    if (oldCreatedByOfTblModuleManifestCollection1NewTblModuleManifest != null && !oldCreatedByOfTblModuleManifestCollection1NewTblModuleManifest.equals(tblDbPortalUser)) {
                        oldCreatedByOfTblModuleManifestCollection1NewTblModuleManifest.getTblModuleManifestCollection1().remove(tblModuleManifestCollection1NewTblModuleManifest);
                        em.merge(oldCreatedByOfTblModuleManifestCollection1NewTblModuleManifest);
                    }
                }
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = getEntityManager();
        try {
            
            em.getTransaction().begin();
            TblDbPortalUser tblDbPortalUser;
            try {
                tblDbPortalUser = em.getReference(TblDbPortalUser.class, id);
                tblDbPortalUser.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The tblDbPortalUser with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Collection<TblPcrManifest> tblPcrManifestCollectionOrphanCheck = tblDbPortalUser.getTblPcrManifestCollection();
            for (TblPcrManifest tblPcrManifestCollectionOrphanCheckTblPcrManifest : tblPcrManifestCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This TblDbPortalUser (" + tblDbPortalUser + ") cannot be destroyed since the TblPcrManifest " + tblPcrManifestCollectionOrphanCheckTblPcrManifest + " in its tblPcrManifestCollection field has a non-nullable updatedBy field.");
            }
            Collection<TblPcrManifest> tblPcrManifestCollection1OrphanCheck = tblDbPortalUser.getTblPcrManifestCollection1();
            for (TblPcrManifest tblPcrManifestCollection1OrphanCheckTblPcrManifest : tblPcrManifestCollection1OrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This TblDbPortalUser (" + tblDbPortalUser + ") cannot be destroyed since the TblPcrManifest " + tblPcrManifestCollection1OrphanCheckTblPcrManifest + " in its tblPcrManifestCollection1 field has a non-nullable createdBy field.");
            }
            Collection<TblModuleManifest> tblModuleManifestCollectionOrphanCheck = tblDbPortalUser.getTblModuleManifestCollection();
            for (TblModuleManifest tblModuleManifestCollectionOrphanCheckTblModuleManifest : tblModuleManifestCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This TblDbPortalUser (" + tblDbPortalUser + ") cannot be destroyed since the TblModuleManifest " + tblModuleManifestCollectionOrphanCheckTblModuleManifest + " in its tblModuleManifestCollection field has a non-nullable updatedBy field.");
            }
            Collection<TblModuleManifest> tblModuleManifestCollection1OrphanCheck = tblDbPortalUser.getTblModuleManifestCollection1();
            for (TblModuleManifest tblModuleManifestCollection1OrphanCheckTblModuleManifest : tblModuleManifestCollection1OrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This TblDbPortalUser (" + tblDbPortalUser + ") cannot be destroyed since the TblModuleManifest " + tblModuleManifestCollection1OrphanCheckTblModuleManifest + " in its tblModuleManifestCollection1 field has a non-nullable createdBy field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(tblDbPortalUser);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public List<TblDbPortalUser> findTblDbPortalUserEntities() {
        return findTblDbPortalUserEntities(true, -1, -1);
    }

    public List<TblDbPortalUser> findTblDbPortalUserEntities(int maxResults, int firstResult) {
        return findTblDbPortalUserEntities(false, maxResults, firstResult);
    }

    private List<TblDbPortalUser> findTblDbPortalUserEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(TblDbPortalUser.class));
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

    public TblDbPortalUser findTblDbPortalUser(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(TblDbPortalUser.class, id);
        } finally {
            em.close();
        }
    }

    public TblDbPortalUser findTblDbPortalUserByLogin(String username) {
        EntityManager em = getEntityManager();
        try {
            Query query = em.createNamedQuery("TblDbPortalUser.findByLogin");
            query.setParameter("login", username);
            List<TblDbPortalUser> list = query.getResultList();
            if( list != null && !list.isEmpty() ) {
                return list.get(0);
            }
        } finally {
            em.close();
        }
        return null;
    }
    
    public int getTblDbPortalUserCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<TblDbPortalUser> rt = cq.from(TblDbPortalUser.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
