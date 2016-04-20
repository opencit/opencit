/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.controller;

import com.intel.mtwilson.as.controller.exceptions.IllegalOrphanException;
import com.intel.mtwilson.as.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.as.controller.exceptions.ASDataException;
import com.intel.mtwilson.as.data.TblMle;
import java.io.Serializable;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import com.intel.mtwilson.as.data.TblHosts;
import java.util.ArrayList;
import java.util.Collection;
import com.intel.mtwilson.as.data.TblPcrManifest;
import com.intel.mtwilson.as.data.TblModuleManifest;

import javax.persistence.NoResultException;
import org.eclipse.persistence.config.CacheUsage;
import org.eclipse.persistence.config.HintValues;
import org.eclipse.persistence.config.QueryHints;

/**
 *
 * @author dsmagadx
 */
public class TblMleJpaController implements Serializable {
    private Logger log = LoggerFactory.getLogger(getClass());

    public TblMleJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        EntityManager em = emf.createEntityManager();
        em.clear();
        return em;
    }

    public void create(TblMle tblMle) {
        if (tblMle.getTblHostsCollection() == null) {
            tblMle.setTblHostsCollection(new ArrayList<TblHosts>());
        }
        if (tblMle.getTblHostsCollection1() == null) {
            tblMle.setTblHostsCollection1(new ArrayList<TblHosts>());
        }
        if (tblMle.getTblPcrManifestCollection() == null) {
            tblMle.setTblPcrManifestCollection(new ArrayList<TblPcrManifest>());
        }
        if (tblMle.getTblModuleManifestCollection() == null) {
            tblMle.setTblModuleManifestCollection(new ArrayList<TblModuleManifest>());
        }
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            Collection<TblHosts> attachedTblHostsCollection = new ArrayList<TblHosts>();
            for (TblHosts tblHostsCollectionTblHostsToAttach : tblMle.getTblHostsCollection()) {
                tblHostsCollectionTblHostsToAttach = em.getReference(tblHostsCollectionTblHostsToAttach.getClass(), tblHostsCollectionTblHostsToAttach.getId());
                attachedTblHostsCollection.add(tblHostsCollectionTblHostsToAttach);
            }
            tblMle.setTblHostsCollection(attachedTblHostsCollection);
            Collection<TblHosts> attachedTblHostsCollection1 = new ArrayList<TblHosts>();
            for (TblHosts tblHostsCollection1TblHostsToAttach : tblMle.getTblHostsCollection1()) {
                tblHostsCollection1TblHostsToAttach = em.getReference(tblHostsCollection1TblHostsToAttach.getClass(), tblHostsCollection1TblHostsToAttach.getId());
                attachedTblHostsCollection1.add(tblHostsCollection1TblHostsToAttach);
            }
            tblMle.setTblHostsCollection1(attachedTblHostsCollection1);
            Collection<TblPcrManifest> attachedTblPcrManifestCollection = new ArrayList<TblPcrManifest>();
            for (TblPcrManifest tblPcrManifestCollectionTblPcrManifestToAttach : tblMle.getTblPcrManifestCollection()) {
                tblPcrManifestCollectionTblPcrManifestToAttach = em.getReference(tblPcrManifestCollectionTblPcrManifestToAttach.getClass(), tblPcrManifestCollectionTblPcrManifestToAttach.getId());
                attachedTblPcrManifestCollection.add(tblPcrManifestCollectionTblPcrManifestToAttach);
            }
            tblMle.setTblPcrManifestCollection(attachedTblPcrManifestCollection);
            Collection<TblModuleManifest> attachedTblModuleManifestCollection = new ArrayList<TblModuleManifest>();
            for (TblModuleManifest tblModuleManifestCollectionTblModuleManifestToAttach : tblMle.getTblModuleManifestCollection()) {
                tblModuleManifestCollectionTblModuleManifestToAttach = em.getReference(tblModuleManifestCollectionTblModuleManifestToAttach.getClass(), tblModuleManifestCollectionTblModuleManifestToAttach.getId());
                attachedTblModuleManifestCollection.add(tblModuleManifestCollectionTblModuleManifestToAttach);
            }
            tblMle.setTblModuleManifestCollection(attachedTblModuleManifestCollection);
            em.persist(tblMle);
            for (TblHosts tblHostsCollectionTblHosts : tblMle.getTblHostsCollection()) {
                TblMle oldVmmMleIdOfTblHostsCollectionTblHosts = tblHostsCollectionTblHosts.getVmmMleId();
                tblHostsCollectionTblHosts.setVmmMleId(tblMle);
                tblHostsCollectionTblHosts = em.merge(tblHostsCollectionTblHosts);
                if (oldVmmMleIdOfTblHostsCollectionTblHosts != null) {
                    oldVmmMleIdOfTblHostsCollectionTblHosts.getTblHostsCollection().remove(tblHostsCollectionTblHosts);
                    em.merge(oldVmmMleIdOfTblHostsCollectionTblHosts);
                }
            }
            for (TblHosts tblHostsCollection1TblHosts : tblMle.getTblHostsCollection1()) {
                TblMle oldBiosMleIdOfTblHostsCollection1TblHosts = tblHostsCollection1TblHosts.getBiosMleId();
                tblHostsCollection1TblHosts.setBiosMleId(tblMle);
                tblHostsCollection1TblHosts = em.merge(tblHostsCollection1TblHosts);
                if (oldBiosMleIdOfTblHostsCollection1TblHosts != null) {
                    oldBiosMleIdOfTblHostsCollection1TblHosts.getTblHostsCollection1().remove(tblHostsCollection1TblHosts);
                    em.merge(oldBiosMleIdOfTblHostsCollection1TblHosts);
                }
            }
            for (TblPcrManifest tblPcrManifestCollectionTblPcrManifest : tblMle.getTblPcrManifestCollection()) {
                TblMle oldMleIdOfTblPcrManifestCollectionTblPcrManifest = tblPcrManifestCollectionTblPcrManifest.getMleId();
                tblPcrManifestCollectionTblPcrManifest.setMleId(tblMle);
                tblPcrManifestCollectionTblPcrManifest = em.merge(tblPcrManifestCollectionTblPcrManifest);
                if (oldMleIdOfTblPcrManifestCollectionTblPcrManifest != null) {
                    oldMleIdOfTblPcrManifestCollectionTblPcrManifest.getTblPcrManifestCollection().remove(tblPcrManifestCollectionTblPcrManifest);
                    em.merge(oldMleIdOfTblPcrManifestCollectionTblPcrManifest);
                }
            }
            for (TblModuleManifest tblModuleManifestCollectionTblModuleManifest : tblMle.getTblModuleManifestCollection()) {
                TblMle oldMleIdOfTblModuleManifestCollectionTblModuleManifest = tblModuleManifestCollectionTblModuleManifest.getMleId();
                tblModuleManifestCollectionTblModuleManifest.setMleId(tblMle);
                tblModuleManifestCollectionTblModuleManifest = em.merge(tblModuleManifestCollectionTblModuleManifest);
                if (oldMleIdOfTblModuleManifestCollectionTblModuleManifest != null) {
                    oldMleIdOfTblModuleManifestCollectionTblModuleManifest.getTblModuleManifestCollection().remove(tblModuleManifestCollectionTblModuleManifest);
                    em.merge(oldMleIdOfTblModuleManifestCollectionTblModuleManifest);
                }
            }
            em.getTransaction().commit();
        } finally {
                em.close();
        }
    }

    public void edit(TblMle tblMle) throws IllegalOrphanException, NonexistentEntityException, ASDataException {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            TblMle persistentTblMle = em.find(TblMle.class, tblMle.getId());
            Collection<TblHosts> tblHostsCollectionOld = persistentTblMle.getTblHostsCollection();
            Collection<TblHosts> tblHostsCollectionNew = tblMle.getTblHostsCollection();
            Collection<TblHosts> tblHostsCollection1Old = persistentTblMle.getTblHostsCollection1();
            Collection<TblHosts> tblHostsCollection1New = tblMle.getTblHostsCollection1();
            Collection<TblPcrManifest> tblPcrManifestCollectionOld = persistentTblMle.getTblPcrManifestCollection();
            Collection<TblPcrManifest> tblPcrManifestCollectionNew = tblMle.getTblPcrManifestCollection();
            Collection<TblModuleManifest> tblModuleManifestCollectionOld = persistentTblMle.getTblModuleManifestCollection();
            Collection<TblModuleManifest> tblModuleManifestCollectionNew = tblMle.getTblModuleManifestCollection();
            List<String> illegalOrphanMessages = null;
            for (TblHosts tblHostsCollectionOldTblHosts : tblHostsCollectionOld) {
                if (!tblHostsCollectionNew.contains(tblHostsCollectionOldTblHosts)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain TblHosts " + tblHostsCollectionOldTblHosts + " since its vmmMleId field is not nullable.");
                }
            }
            for (TblHosts tblHostsCollection1OldTblHosts : tblHostsCollection1Old) {
                if (!tblHostsCollection1New.contains(tblHostsCollection1OldTblHosts)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain TblHosts " + tblHostsCollection1OldTblHosts + " since its biosMleId field is not nullable.");
                }
            }
            for (TblPcrManifest tblPcrManifestCollectionOldTblPcrManifest : tblPcrManifestCollectionOld) {
                if (!tblPcrManifestCollectionNew.contains(tblPcrManifestCollectionOldTblPcrManifest)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain TblPcrManifest " + tblPcrManifestCollectionOldTblPcrManifest + " since its mleId field is not nullable.");
                }
            }
            for (TblModuleManifest tblModuleManifestCollectionOldTblModuleManifest : tblModuleManifestCollectionOld) {
                if (!tblModuleManifestCollectionNew.contains(tblModuleManifestCollectionOldTblModuleManifest)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain TblModuleManifest " + tblModuleManifestCollectionOldTblModuleManifest + " since its mleId field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Collection<TblHosts> attachedTblHostsCollectionNew = new ArrayList<TblHosts>();
            for (TblHosts tblHostsCollectionNewTblHostsToAttach : tblHostsCollectionNew) {
                tblHostsCollectionNewTblHostsToAttach = em.getReference(tblHostsCollectionNewTblHostsToAttach.getClass(), tblHostsCollectionNewTblHostsToAttach.getId());
                attachedTblHostsCollectionNew.add(tblHostsCollectionNewTblHostsToAttach);
            }
            tblHostsCollectionNew = attachedTblHostsCollectionNew;
            tblMle.setTblHostsCollection(tblHostsCollectionNew);
            Collection<TblHosts> attachedTblHostsCollection1New = new ArrayList<TblHosts>();
            for (TblHosts tblHostsCollection1NewTblHostsToAttach : tblHostsCollection1New) {
                tblHostsCollection1NewTblHostsToAttach = em.getReference(tblHostsCollection1NewTblHostsToAttach.getClass(), tblHostsCollection1NewTblHostsToAttach.getId());
                attachedTblHostsCollection1New.add(tblHostsCollection1NewTblHostsToAttach);
            }
            tblHostsCollection1New = attachedTblHostsCollection1New;
            tblMle.setTblHostsCollection1(tblHostsCollection1New);
            Collection<TblPcrManifest> attachedTblPcrManifestCollectionNew = new ArrayList<TblPcrManifest>();
            for (TblPcrManifest tblPcrManifestCollectionNewTblPcrManifestToAttach : tblPcrManifestCollectionNew) {
                tblPcrManifestCollectionNewTblPcrManifestToAttach = em.getReference(tblPcrManifestCollectionNewTblPcrManifestToAttach.getClass(), tblPcrManifestCollectionNewTblPcrManifestToAttach.getId());
                attachedTblPcrManifestCollectionNew.add(tblPcrManifestCollectionNewTblPcrManifestToAttach);
            }
            tblPcrManifestCollectionNew = attachedTblPcrManifestCollectionNew;
            tblMle.setTblPcrManifestCollection(tblPcrManifestCollectionNew);
            Collection<TblModuleManifest> attachedTblModuleManifestCollectionNew = new ArrayList<TblModuleManifest>();
            for (TblModuleManifest tblModuleManifestCollectionNewTblModuleManifestToAttach : tblModuleManifestCollectionNew) {
                tblModuleManifestCollectionNewTblModuleManifestToAttach = em.getReference(tblModuleManifestCollectionNewTblModuleManifestToAttach.getClass(), tblModuleManifestCollectionNewTblModuleManifestToAttach.getId());
                attachedTblModuleManifestCollectionNew.add(tblModuleManifestCollectionNewTblModuleManifestToAttach);
            }
            tblModuleManifestCollectionNew = attachedTblModuleManifestCollectionNew;
            tblMle.setTblModuleManifestCollection(tblModuleManifestCollectionNew);
            tblMle = em.merge(tblMle);
            for (TblHosts tblHostsCollectionNewTblHosts : tblHostsCollectionNew) {
                if (!tblHostsCollectionOld.contains(tblHostsCollectionNewTblHosts)) {
                    TblMle oldVmmMleIdOfTblHostsCollectionNewTblHosts = tblHostsCollectionNewTblHosts.getVmmMleId();
                    tblHostsCollectionNewTblHosts.setVmmMleId(tblMle);
                    tblHostsCollectionNewTblHosts = em.merge(tblHostsCollectionNewTblHosts);
                    if (oldVmmMleIdOfTblHostsCollectionNewTblHosts != null && !oldVmmMleIdOfTblHostsCollectionNewTblHosts.equals(tblMle)) {
                        oldVmmMleIdOfTblHostsCollectionNewTblHosts.getTblHostsCollection().remove(tblHostsCollectionNewTblHosts);
                        em.merge(oldVmmMleIdOfTblHostsCollectionNewTblHosts);
                    }
                }
            }
            for (TblHosts tblHostsCollection1NewTblHosts : tblHostsCollection1New) {
                if (!tblHostsCollection1Old.contains(tblHostsCollection1NewTblHosts)) {
                    TblMle oldBiosMleIdOfTblHostsCollection1NewTblHosts = tblHostsCollection1NewTblHosts.getBiosMleId();
                    tblHostsCollection1NewTblHosts.setBiosMleId(tblMle);
                    tblHostsCollection1NewTblHosts = em.merge(tblHostsCollection1NewTblHosts);
                    if (oldBiosMleIdOfTblHostsCollection1NewTblHosts != null && !oldBiosMleIdOfTblHostsCollection1NewTblHosts.equals(tblMle)) {
                        oldBiosMleIdOfTblHostsCollection1NewTblHosts.getTblHostsCollection1().remove(tblHostsCollection1NewTblHosts);
                        em.merge(oldBiosMleIdOfTblHostsCollection1NewTblHosts);
                    }
                }
            }
            for (TblPcrManifest tblPcrManifestCollectionNewTblPcrManifest : tblPcrManifestCollectionNew) {
                if (!tblPcrManifestCollectionOld.contains(tblPcrManifestCollectionNewTblPcrManifest)) {
                    TblMle oldMleIdOfTblPcrManifestCollectionNewTblPcrManifest = tblPcrManifestCollectionNewTblPcrManifest.getMleId();
                    tblPcrManifestCollectionNewTblPcrManifest.setMleId(tblMle);
                    tblPcrManifestCollectionNewTblPcrManifest = em.merge(tblPcrManifestCollectionNewTblPcrManifest);
                    if (oldMleIdOfTblPcrManifestCollectionNewTblPcrManifest != null && !oldMleIdOfTblPcrManifestCollectionNewTblPcrManifest.equals(tblMle)) {
                        oldMleIdOfTblPcrManifestCollectionNewTblPcrManifest.getTblPcrManifestCollection().remove(tblPcrManifestCollectionNewTblPcrManifest);
                        em.merge(oldMleIdOfTblPcrManifestCollectionNewTblPcrManifest);
                    }
                }
            }
            for (TblModuleManifest tblModuleManifestCollectionNewTblModuleManifest : tblModuleManifestCollectionNew) {
                if (!tblModuleManifestCollectionOld.contains(tblModuleManifestCollectionNewTblModuleManifest)) {
                    TblMle oldMleIdOfTblModuleManifestCollectionNewTblModuleManifest = tblModuleManifestCollectionNewTblModuleManifest.getMleId();
                    tblModuleManifestCollectionNewTblModuleManifest.setMleId(tblMle);
                    tblModuleManifestCollectionNewTblModuleManifest = em.merge(tblModuleManifestCollectionNewTblModuleManifest);
                    if (oldMleIdOfTblModuleManifestCollectionNewTblModuleManifest != null && !oldMleIdOfTblModuleManifestCollectionNewTblModuleManifest.equals(tblMle)) {
                        oldMleIdOfTblModuleManifestCollectionNewTblModuleManifest.getTblModuleManifestCollection().remove(tblModuleManifestCollectionNewTblModuleManifest);
                        em.merge(oldMleIdOfTblModuleManifestCollectionNewTblModuleManifest);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = tblMle.getId();
                if (findTblMle(id) == null) {
                    throw new NonexistentEntityException("The tblMle with id " + id + " no longer exists.");
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
            TblMle tblMle;
            try {
                tblMle = em.getReference(TblMle.class, id);
                tblMle.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The tblMle with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Collection<TblHosts> tblHostsCollectionOrphanCheck = tblMle.getTblHostsCollection();
            for (TblHosts tblHostsCollectionOrphanCheckTblHosts : tblHostsCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This TblMle (" + tblMle + ") cannot be destroyed since the TblHosts " + tblHostsCollectionOrphanCheckTblHosts + " in its tblHostsCollection field has a non-nullable vmmMleId field.");
            }
            Collection<TblHosts> tblHostsCollection1OrphanCheck = tblMle.getTblHostsCollection1();
            for (TblHosts tblHostsCollection1OrphanCheckTblHosts : tblHostsCollection1OrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This TblMle (" + tblMle + ") cannot be destroyed since the TblHosts " + tblHostsCollection1OrphanCheckTblHosts + " in its tblHostsCollection1 field has a non-nullable biosMleId field.");
            }
            Collection<TblPcrManifest> tblPcrManifestCollectionOrphanCheck = tblMle.getTblPcrManifestCollection();
            for (TblPcrManifest tblPcrManifestCollectionOrphanCheckTblPcrManifest : tblPcrManifestCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This TblMle (" + tblMle + ") cannot be destroyed since the TblPcrManifest " + tblPcrManifestCollectionOrphanCheckTblPcrManifest + " in its tblPcrManifestCollection field has a non-nullable mleId field.");
            }
            Collection<TblModuleManifest> tblModuleManifestCollectionOrphanCheck = tblMle.getTblModuleManifestCollection();
            for (TblModuleManifest tblModuleManifestCollectionOrphanCheckTblModuleManifest : tblModuleManifestCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This TblMle (" + tblMle + ") cannot be destroyed since the TblModuleManifest " + tblModuleManifestCollectionOrphanCheckTblModuleManifest + " in its tblModuleManifestCollection field has a non-nullable mleId field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(tblMle);
            em.getTransaction().commit();
        } finally {
                em.close();
        }
    }

    public List<TblMle> findTblMleEntities() {
        return findTblMleEntities(true, -1, -1);
    }

    public List<TblMle> findTblMleEntities(int maxResults, int firstResult) {
        return findTblMleEntities(false, maxResults, firstResult);
    }

    private List<TblMle> findTblMleEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(TblMle.class));
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

    public TblMle findTblMle(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(TblMle.class, id);
        } finally {
            em.close();
        }
    }

    public int getTblMleCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<TblMle> rt = cq.from(TblMle.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }

    public TblMle findMleByNameAndVersion(String name, String version, String mleType) {

        TblMle mle = null;
        EntityManager em = getEntityManager();
        try {         
            Query query = em.createNamedQuery("TblMle.findByNameAndVersion");

            query.setParameter("name", name);
            query.setParameter("version", version);
            query.setParameter("mletype", mleType);

            List<TblMle> list = query.getResultList();
            
            if(list.size() > 0 )
                mle = list.get(0);
        } finally {
            em.close();
        }
        
        return mle;
    }
    
    public TblMle findMleByNameAndVersion(String name, String version) {

        TblMle mle = null;
        EntityManager em = getEntityManager();
        try {           
            Query query = em.createNamedQuery("TblMle.findByNameAndVersionNoType");

            query.setParameter("name", name);
            query.setParameter("version", version);
            

            List<TblMle> list = query.getResultList();
            
            if(list.size() > 0 )
                mle = list.get(0);
        } finally {
            em.close();
        }
        
        return mle;

    }
        
    public List<TblMle> findMleByNameSearchCriteria(String searchCriteria) {
        List<TblMle> mleList = new ArrayList<TblMle>();
        EntityManager em = getEntityManager();
        try {           
            Query query = em.createNamedQuery("TblMle.findBiosMleByNameSearchCriteria");
            query.setParameter("search", "%"+searchCriteria+"%");
            
            List<TblMle> biosList = query.getResultList();
            if(biosList != null && biosList.size() > 0 )
                mleList.addAll(biosList);
            
            query = em.createNamedQuery("TblMle.findVmmMleByNameSearchCriteria");
            query.setParameter("search", "%"+searchCriteria+"%");

            List<TblMle> vmmList = query.getResultList();
            if(biosList != null && vmmList.size() > 0)
                mleList.addAll(vmmList);
            
            // This would be adding duplicate entries
            //mleList.addAll(vmmList);


        } finally {
            em.close();
        }
        
        return mleList;
        
    }

    public TblMle findMleById(Integer id) {
        EntityManager em = getEntityManager();
        try {
            
            Query query = em.createNamedQuery("TblMle.findById");
            query.setParameter("id", id);
            
            query.setHint(QueryHints.REFRESH, HintValues.TRUE);
            query.setHint(QueryHints.CACHE_USAGE, CacheUsage.DoNotCheckCache);
            
            TblMle mle= (TblMle) query.getSingleResult();
            return mle;

        } finally {
            em.close();
        }
                
    }

      public TblMle findBiosMle(String mleName, String mleVersion, String oemName  ) {
        EntityManager em = getEntityManager();
        try {
            
            Query query = em.createNamedQuery("TblMle.findBiosMle");

            query.setParameter("name", mleName);
            query.setParameter("version", mleVersion);
            query.setParameter("oemName", oemName);

            // Nov 14, 2013: Commenting out the below setting for better performance and updating the cacheusage to check cache and then DB                        
            //query.setHint(QueryHints.REFRESH, HintValues.TRUE);
            query.setHint(QueryHints.CACHE_USAGE, CacheUsage.CheckCacheThenDatabase);
            
            
            try {
            	
            	TblMle mle = (TblMle) query.getSingleResult();
            	return mle;
            } catch (NoResultException e) {
                log.error( "NoResultException: BIOS MLE does not exist Name {} Version {} Oem Name {} ", 
                        mleName, mleVersion, oemName);
                return null;
            }
               
        } finally {
            em.close();
        }
    }
      public TblMle findVmmMle(String mleName, String mleVersion, String osName, String osVersion  ) {
        EntityManager em = getEntityManager();
        try {
            
            Query query = em.createNamedQuery("TblMle.findVmmMle");

            query.setParameter("name", mleName);
            query.setParameter("version", mleVersion);
            query.setParameter("osName", osName);
            query.setParameter("osVersion", osVersion);
            
            // Nov 14, 2013: Commenting out the below setting for better performance and updating the cacheusage to check cache and then DB            
            //query.setHint(QueryHints.REFRESH, HintValues.TRUE);
            query.setHint(QueryHints.CACHE_USAGE, CacheUsage.CheckCacheThenDatabase);

            
            try {
            	TblMle mle = (TblMle) query.getSingleResult();
                return mle;

            } catch (NoResultException e) {
                log.error( "NoResultException: VMM MLE does not exist Name {} Version {} Os Name {} Os Version {}", 
                        mleName, mleVersion, osName, osVersion);
                return null;
            }
            
            
        } finally {
            em.close();
        }
        

    }    
      
    public List<TblMle> findBIOSMLEByNameSearchCriteria(String searchCriteria) {
        List<TblMle> mleList = new ArrayList<TblMle>();
        EntityManager em = getEntityManager();
        try {           
            Query query = em.createNamedQuery("TblMle.findBiosMleByNameSearchCriteria");
            query.setParameter("search", searchCriteria+"%");
            
            List<TblMle> biosList = query.getResultList();
            if(biosList != null && biosList.size() > 0 )
                mleList.addAll(biosList);         

        } finally {
            em.close();
        }
        
        return mleList;
        
    }

    public List<TblMle> findVMMMLEByNameSearchCriteria(String searchCriteria) {
        List<TblMle> mleList = new ArrayList<TblMle>();
        EntityManager em = getEntityManager();
        try {                       
            Query query = em.createNamedQuery("TblMle.findVmmMleByNameSearchCriteria");
            query.setParameter("search", searchCriteria+"%");

            List<TblMle> vmmList = query.getResultList();
            if(vmmList != null && vmmList.size() > 0)
                mleList.addAll(vmmList);
            
            // The below statement used to add the values again causing duplicates.
            //mleList.addAll(vmmList);
        } finally {
            em.close();
        }
        
        return mleList;
        
    }
    
    public TblMle findTblMleByUUID(String uuid_hex) {
        
        EntityManager em = getEntityManager();
        try {
            Query query = em.createNamedQuery("TblMle.findByUUID_Hex");
            query.setParameter("uuid_hex", uuid_hex);
            TblMle tblOem = (TblMle) query.getSingleResult();
            return tblOem;

        } catch(NoResultException e){
            log.error( "NoResultException : MLE with UUID {} not found", uuid_hex);
            return null;
        }finally {
            em.close();
        }        
    }

    public List<TblMle> findByNameLike(String name) {
        List<TblMle> mleList = new ArrayList<TblMle>();
        EntityManager em = getEntityManager();
        try {                       
            Query query = em.createNamedQuery("TblMle.findByNameLike");
            query.setParameter("name", "%"+name+"%");

            List<TblMle> vmmList = query.getResultList();
            if(vmmList != null && vmmList.size() > 0)
                mleList.addAll(vmmList);
            
        } finally {
            em.close();
        }
        
        return mleList;        
    }

    public List<TblMle> findByName(String name) {
        List<TblMle> mleList = new ArrayList<TblMle>();
        EntityManager em = getEntityManager();
        try {                       
            Query query = em.createNamedQuery("TblMle.findByName");
            query.setParameter("name", name);

            List<TblMle> vmmList = query.getResultList();
            if(vmmList != null && vmmList.size() > 0)
                mleList.addAll(vmmList);
            
        } finally {
            em.close();
        }
        
        return mleList;        
    }

    public List<TblMle> findByOemUuid(String oemUuid) {
        List<TblMle> mleList = new ArrayList<TblMle>();
        EntityManager em = getEntityManager();
        try {                       
            Query query = em.createNamedQuery("TblMle.findByOemUuid");
            query.setParameter("oem_uuid_hex", oemUuid);

            List<TblMle> vmmList = query.getResultList();
            if(vmmList != null && vmmList.size() > 0)
                mleList.addAll(vmmList);
            
        } finally {
            em.close();
        }
        
        return mleList;        
    }

    public List<TblMle> findByOsUuid(String osUuid) {
        List<TblMle> mleList = new ArrayList<TblMle>();
        EntityManager em = getEntityManager();
        try {                       
            Query query = em.createNamedQuery("TblMle.findByOsUuid");
            query.setParameter("os_uuid_hex", osUuid);

            List<TblMle> vmmList = query.getResultList();
            if(vmmList != null && vmmList.size() > 0)
                mleList.addAll(vmmList);
            
        } finally {
            em.close();
        }
        
        return mleList;        
    }

    public List<TblMle> findBiosMleByVersion(String mleVersion, String oemName) {
        List<TblMle> mleList = new ArrayList<>();
        EntityManager em = getEntityManager();
        try {                       
            Query query = em.createNamedQuery("TblMle.findBiosMleByVersion");            
            query.setParameter("version", mleVersion);
            query.setParameter("oemName", oemName);

            List<TblMle> vmmList = query.getResultList();
            if(vmmList != null && vmmList.size() > 0)
                mleList.addAll(vmmList);
            
        } finally {
            em.close();
        }        
        return mleList;        
    }

    public List<TblMle> findVmmMleByVersion(String mleVersion, String osName, String osVersion) {
        List<TblMle> mleList = new ArrayList<>();
        EntityManager em = getEntityManager();
        try {                       
            Query query = em.createNamedQuery("TblMle.findVmmMleByVersion");            
            query.setParameter("version", mleVersion);
            query.setParameter("osName", osName);
            query.setParameter("osVersion", osVersion);

            List<TblMle> vmmList = query.getResultList();
            if(vmmList != null && vmmList.size() > 0)
                mleList.addAll(vmmList);
            
        } finally {
            em.close();
        }        
        return mleList;        
    }

    public List<TblMle> findBiosMleByTarget(String mleVersion, String oemName, String targetType, String targetValue) {
        List<TblMle> mleList = new ArrayList<>();
        EntityManager em = getEntityManager();
        try {                       
            Query query = em.createNamedQuery("TblMle.findBiosMleByTarget");            
            query.setParameter("version", mleVersion);
            query.setParameter("oemName", oemName);
            query.setParameter("target_type", targetType);
            query.setParameter("target_value", targetValue);

            List<TblMle> biosList = query.getResultList();
            if(biosList != null && biosList.size() > 0)
                mleList.addAll(biosList);
            
        } finally {
            em.close();
        }        
        return mleList;        
    }

    public List<TblMle> findVmmMleByTarget(String mleVersion, String osName, String osVersion, String targetType, String targetValue) {
        List<TblMle> mleList = new ArrayList<>();
        EntityManager em = getEntityManager();
        try {                       
            Query query = em.createNamedQuery("TblMle.findVmmMleByTarget");            
            query.setParameter("version", mleVersion);
            query.setParameter("osName", osName);
            query.setParameter("osVersion", osVersion);
            query.setParameter("target_type", targetType);
            query.setParameter("target_value", targetValue);

            List<TblMle> vmmList = query.getResultList();
            if(vmmList != null && vmmList.size() > 0)
                mleList.addAll(vmmList);
            
        } finally {
            em.close();
        }        
        return mleList;        
    }

    public List<TblMle> findByMleType(String mleType) {
        List<TblMle> mleList = new ArrayList<>();
        EntityManager em = getEntityManager();
        try {                       
            Query query = em.createNamedQuery("TblMle.findByMLEType");            
            query.setParameter("mLEType", mleType);

            List<TblMle> vmmList = query.getResultList();
            if(vmmList != null && vmmList.size() > 0)
                mleList.addAll(vmmList);
            
        } finally {
            em.close();
        }        
        return mleList;        
    }
    
}
