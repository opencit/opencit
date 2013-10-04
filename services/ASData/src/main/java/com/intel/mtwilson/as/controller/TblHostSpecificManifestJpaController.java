/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.controller;

import com.intel.mtwilson.as.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.as.controller.exceptions.ASDataException;

import com.intel.mtwilson.as.data.TblHostSpecificManifest;
import java.io.Serializable;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import com.intel.mtwilson.as.data.TblModuleManifest;

/**
 *
 * @author dsmagadx
 */
public class TblHostSpecificManifestJpaController implements Serializable {
    private Logger log = LoggerFactory.getLogger(getClass());

    public TblHostSpecificManifestJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(TblHostSpecificManifest tblHostSpecificManifest) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            TblModuleManifest moduleManifestID = tblHostSpecificManifest.getModuleManifestID();
            if (moduleManifestID != null) {
                moduleManifestID = em.getReference(moduleManifestID.getClass(), moduleManifestID.getId());
                tblHostSpecificManifest.setModuleManifestID(moduleManifestID);
            }
            em.persist(tblHostSpecificManifest);
            if (moduleManifestID != null) {
                moduleManifestID.getTblHostSpecificManifestCollection().add(tblHostSpecificManifest);
                em.merge(moduleManifestID);
            }
            em.getTransaction().commit();
        } finally {
                em.close();
        }
    }

    public void edit(TblHostSpecificManifest tblHostSpecificManifest) throws NonexistentEntityException, ASDataException {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            TblHostSpecificManifest persistentTblHostSpecificManifest = em.find(TblHostSpecificManifest.class, tblHostSpecificManifest.getId());
            TblModuleManifest moduleManifestIDOld = persistentTblHostSpecificManifest.getModuleManifestID();
            TblModuleManifest moduleManifestIDNew = tblHostSpecificManifest.getModuleManifestID();
            if (moduleManifestIDNew != null) {
                moduleManifestIDNew = em.getReference(moduleManifestIDNew.getClass(), moduleManifestIDNew.getId());
                tblHostSpecificManifest.setModuleManifestID(moduleManifestIDNew);
            }
            tblHostSpecificManifest = em.merge(tblHostSpecificManifest);
            if (moduleManifestIDOld != null && !moduleManifestIDOld.equals(moduleManifestIDNew)) {
                moduleManifestIDOld.getTblHostSpecificManifestCollection().remove(tblHostSpecificManifest);
                moduleManifestIDOld = em.merge(moduleManifestIDOld);
            }
            if (moduleManifestIDNew != null && !moduleManifestIDNew.equals(moduleManifestIDOld)) {
                moduleManifestIDNew.getTblHostSpecificManifestCollection().add(tblHostSpecificManifest);
                em.merge(moduleManifestIDNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = tblHostSpecificManifest.getId();
                if (findTblHostSpecificManifest(id) == null) {
                    throw new NonexistentEntityException("The tblHostSpecificManifest with id " + id + " no longer exists.");
                }
            }
            throw new ASDataException(ex);
        } finally {
                em.close();
        }
    }

    public void destroy(Integer id) throws NonexistentEntityException {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            TblHostSpecificManifest tblHostSpecificManifest;
            try {
                tblHostSpecificManifest = em.getReference(TblHostSpecificManifest.class, id);
                tblHostSpecificManifest.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The tblHostSpecificManifest with id " + id + " no longer exists.", enfe);
            }
            TblModuleManifest moduleManifestID = tblHostSpecificManifest.getModuleManifestID();
            if (moduleManifestID != null) {
                moduleManifestID.getTblHostSpecificManifestCollection().remove(tblHostSpecificManifest);
                em.merge(moduleManifestID);
            }
            em.remove(tblHostSpecificManifest);
            em.getTransaction().commit();
        } finally {
                em.close();
        }
    }

    public List<TblHostSpecificManifest> findTblHostSpecificManifestEntities() {
        return findTblHostSpecificManifestEntities(true, -1, -1);
    }

    public List<TblHostSpecificManifest> findTblHostSpecificManifestEntities(int maxResults, int firstResult) {
        return findTblHostSpecificManifestEntities(false, maxResults, firstResult);
    }

    private List<TblHostSpecificManifest> findTblHostSpecificManifestEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(TblHostSpecificManifest.class));
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

    public TblHostSpecificManifest findTblHostSpecificManifest(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(TblHostSpecificManifest.class, id);
        } finally {
            em.close();
        }
    }

    public int getTblHostSpecificManifestCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<TblHostSpecificManifest> rt = cq.from(TblHostSpecificManifest.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
    public TblHostSpecificManifest findByHostID(int hostId) {
    	EntityManager em = getEntityManager();
        try {
            Query query = em.createNamedQuery("TblHostSpecificManifest.findByHostID");
            query.setParameter("hostID", hostId);
            
            TblHostSpecificManifest tblHostSpecificManifest = (TblHostSpecificManifest) query.getSingleResult();
            return tblHostSpecificManifest;

        }catch (NoResultException e) {
            log.error("NoResultException: No Host specific manifest for Host [{}]", 
                    String.valueOf(hostId));
            return null;
        } finally {
            em.close();
        }
    	    	
    }

    public TblHostSpecificManifest findByModuleAndHostID(int hostId, int moduleID) {
    	EntityManager em = getEntityManager();
        try {
            Query query = em.createNamedQuery("TblHostSpecificManifest.findByModuleAndHostID");
            query.setParameter("hostID", hostId);
            query.setParameter("Module_Manifest_ID", moduleID);
            
            TblHostSpecificManifest tblHostSpecificManifest = (TblHostSpecificManifest) query.getSingleResult();
            return tblHostSpecificManifest;

        }catch (NoResultException e) {
            log.error("NoResultException: No Host specific manifest for Host [{}]", 
                    String.valueOf(hostId));
            return null;
        } finally {
            em.close();
        }
    	    	
    }
    
}
