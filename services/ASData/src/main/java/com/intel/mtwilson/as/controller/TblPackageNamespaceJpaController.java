/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.controller;

import com.intel.mtwilson.as.controller.exceptions.ASDataException;
import com.intel.mtwilson.as.controller.exceptions.IllegalOrphanException;
import com.intel.mtwilson.as.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.as.data.TblPackageNamespace;
import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import com.intel.mtwilson.as.data.TblModuleManifest;
import java.util.ArrayList;
import java.util.Collection;
import org.eclipse.persistence.config.CacheUsage;
import org.eclipse.persistence.config.HintValues;
import org.eclipse.persistence.config.QueryHints;

/**
 *
 * @author dsmagadx
 */
public class TblPackageNamespaceJpaController implements Serializable {

    public TblPackageNamespaceJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(TblPackageNamespace tblPackageNamespace) {
        if (tblPackageNamespace.getTblModuleManifestCollection() == null) {
            tblPackageNamespace.setTblModuleManifestCollection(new ArrayList<TblModuleManifest>());
        }
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            Collection<TblModuleManifest> attachedTblModuleManifestCollection = new ArrayList<TblModuleManifest>();
            for (TblModuleManifest tblModuleManifestCollectionTblModuleManifestToAttach : tblPackageNamespace.getTblModuleManifestCollection()) {
                tblModuleManifestCollectionTblModuleManifestToAttach = em.getReference(tblModuleManifestCollectionTblModuleManifestToAttach.getClass(), tblModuleManifestCollectionTblModuleManifestToAttach.getId());
                attachedTblModuleManifestCollection.add(tblModuleManifestCollectionTblModuleManifestToAttach);
            }
            tblPackageNamespace.setTblModuleManifestCollection(attachedTblModuleManifestCollection);
            em.persist(tblPackageNamespace);
            for (TblModuleManifest tblModuleManifestCollectionTblModuleManifest : tblPackageNamespace.getTblModuleManifestCollection()) {
                TblPackageNamespace oldNameSpaceIDOfTblModuleManifestCollectionTblModuleManifest = tblModuleManifestCollectionTblModuleManifest.getNameSpaceID();
                tblModuleManifestCollectionTblModuleManifest.setNameSpaceID(tblPackageNamespace);
                tblModuleManifestCollectionTblModuleManifest = em.merge(tblModuleManifestCollectionTblModuleManifest);
                if (oldNameSpaceIDOfTblModuleManifestCollectionTblModuleManifest != null) {
                    oldNameSpaceIDOfTblModuleManifestCollectionTblModuleManifest.getTblModuleManifestCollection().remove(tblModuleManifestCollectionTblModuleManifest);
                    em.merge(oldNameSpaceIDOfTblModuleManifestCollectionTblModuleManifest);
                }
            }
            em.getTransaction().commit();
        } finally {
                em.close();
        }
    }

    public void edit(TblPackageNamespace tblPackageNamespace) throws IllegalOrphanException, NonexistentEntityException, ASDataException {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            TblPackageNamespace persistentTblPackageNamespace = em.find(TblPackageNamespace.class, tblPackageNamespace.getId());
            Collection<TblModuleManifest> tblModuleManifestCollectionOld = persistentTblPackageNamespace.getTblModuleManifestCollection();
            Collection<TblModuleManifest> tblModuleManifestCollectionNew = tblPackageNamespace.getTblModuleManifestCollection();
            List<String> illegalOrphanMessages = null;
            for (TblModuleManifest tblModuleManifestCollectionOldTblModuleManifest : tblModuleManifestCollectionOld) {
                if (!tblModuleManifestCollectionNew.contains(tblModuleManifestCollectionOldTblModuleManifest)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain TblModuleManifest " + tblModuleManifestCollectionOldTblModuleManifest + " since its nameSpaceID field is not nullable.");
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
            tblPackageNamespace.setTblModuleManifestCollection(tblModuleManifestCollectionNew);
            tblPackageNamespace = em.merge(tblPackageNamespace);
            for (TblModuleManifest tblModuleManifestCollectionNewTblModuleManifest : tblModuleManifestCollectionNew) {
                if (!tblModuleManifestCollectionOld.contains(tblModuleManifestCollectionNewTblModuleManifest)) {
                    TblPackageNamespace oldNameSpaceIDOfTblModuleManifestCollectionNewTblModuleManifest = tblModuleManifestCollectionNewTblModuleManifest.getNameSpaceID();
                    tblModuleManifestCollectionNewTblModuleManifest.setNameSpaceID(tblPackageNamespace);
                    tblModuleManifestCollectionNewTblModuleManifest = em.merge(tblModuleManifestCollectionNewTblModuleManifest);
                    if (oldNameSpaceIDOfTblModuleManifestCollectionNewTblModuleManifest != null && !oldNameSpaceIDOfTblModuleManifestCollectionNewTblModuleManifest.equals(tblPackageNamespace)) {
                        oldNameSpaceIDOfTblModuleManifestCollectionNewTblModuleManifest.getTblModuleManifestCollection().remove(tblModuleManifestCollectionNewTblModuleManifest);
                        em.merge(oldNameSpaceIDOfTblModuleManifestCollectionNewTblModuleManifest);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = tblPackageNamespace.getId();
                if (findTblPackageNamespace(id) == null) {
                    throw new NonexistentEntityException("The tblPackageNamespace with id " + id + " no longer exists.");
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
            TblPackageNamespace tblPackageNamespace;
            try {
                tblPackageNamespace = em.getReference(TblPackageNamespace.class, id);
                tblPackageNamespace.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The tblPackageNamespace with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Collection<TblModuleManifest> tblModuleManifestCollectionOrphanCheck = tblPackageNamespace.getTblModuleManifestCollection();
            for (TblModuleManifest tblModuleManifestCollectionOrphanCheckTblModuleManifest : tblModuleManifestCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This TblPackageNamespace (" + tblPackageNamespace + ") cannot be destroyed since the TblModuleManifest " + tblModuleManifestCollectionOrphanCheckTblModuleManifest + " in its tblModuleManifestCollection field has a non-nullable nameSpaceID field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(tblPackageNamespace);
            em.getTransaction().commit();
        } finally {
                em.close();
        }
    }

    public List<TblPackageNamespace> findTblPackageNamespaceEntities() {
        return findTblPackageNamespaceEntities(true, -1, -1);
    }

    public List<TblPackageNamespace> findTblPackageNamespaceEntities(int maxResults, int firstResult) {
        return findTblPackageNamespaceEntities(false, maxResults, firstResult);
    }

    private List<TblPackageNamespace> findTblPackageNamespaceEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(TblPackageNamespace.class));
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

    public TblPackageNamespace findTblPackageNamespace(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(TblPackageNamespace.class, id);
        } finally {
            em.close();
        }
    }

    public int getTblPackageNamespaceCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<TblPackageNamespace> rt = cq.from(TblPackageNamespace.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
    public TblPackageNamespace findByName(String name) {
        EntityManager em = getEntityManager();
        try {           
            Query query = em.createNamedQuery("TblPackageNamespace.findByName");
            query.setParameter("name", name);
            
            // Nov 14, 2013: Commenting out the below setting for better performance and updating the cacheusage to check cache and then DB                        
            //query.setHint(QueryHints.REFRESH, HintValues.TRUE);
            query.setHint(QueryHints.CACHE_USAGE, CacheUsage.CheckCacheThenDatabase);
            
            TblPackageNamespace tblPNS = (TblPackageNamespace) query.getSingleResult();
            return tblPNS;

        } finally {
            em.close();
        }
        
    }
    
    
     /**
     *
     * @param namespace
     * @return
     */
    public boolean namespaceExists(String namespace) {
            try {
                findByName(namespace);
                return true;
            }
            catch(Exception e) { 
                return false; 
            }
    }
}


