/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.controller;

import com.intel.mtwilson.as.controller.exceptions.ASDataException;
import com.intel.mtwilson.as.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.as.data.TblOs;
import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.eclipse.persistence.config.CacheUsage;
import org.eclipse.persistence.config.HintValues;
import org.eclipse.persistence.config.QueryHints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dsmagadx
 */
public class TblOsJpaController implements Serializable {
    private Logger log = LoggerFactory.getLogger(getClass());

    public TblOsJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(TblOs tblOs) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(tblOs);
            em.getTransaction().commit();
        } finally {
                em.close();
        }
    }

    public void edit(TblOs tblOs) throws NonexistentEntityException, ASDataException {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(tblOs);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = tblOs.getId();
                if (findTblOs(id) == null) {
                    throw new NonexistentEntityException("The tblOs with id " + id + " no longer exists.");
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
            TblOs tblOs;
            try {
                tblOs = em.getReference(TblOs.class, id);
                tblOs.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The tblOs with id " + id + " no longer exists.", enfe);
            }
            em.remove(tblOs);
            em.getTransaction().commit();
        } finally {
                em.close();
        }
    }

    public List<TblOs> findTblOsEntities() {
        return findTblOsEntities(true, -1, -1);
    }

    public List<TblOs> findTblOsEntities(int maxResults, int firstResult) {
        return findTblOsEntities(false, maxResults, firstResult);
    }

    private List<TblOs> findTblOsEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(TblOs.class));
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

    public TblOs findTblOs(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(TblOs.class, id);
        } finally {
            em.close();
        }
    }

    public int getTblOsCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<TblOs> rt = cq.from(TblOs.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }

    public TblOs findTblOsByNameVersion(String name, String version) {
        EntityManager em = getEntityManager();
        try {
            Query query = em.createNamedQuery("TblOs.findTblOsByNameVersion");
            query.setParameter("name", name);
            query.setParameter("version", version);
            
            query.setHint(QueryHints.REFRESH, HintValues.TRUE);
            query.setHint(QueryHints.CACHE_USAGE, CacheUsage.DoNotCheckCache);

           
            TblOs tblOs = (TblOs) query.getSingleResult();
            return tblOs;

        } catch(NoResultException e){
            return null;
        }finally {
            em.close();
        }
    }
    
    public List<TblOs> findTblOsByName(String name) {
        EntityManager em = getEntityManager();
        try {
            Query query = em.createNamedQuery("TblOs.findByName");
            query.setParameter("name", name);
            List<TblOs> resultList = query.getResultList();
            return resultList;

        } catch(NoResultException e){
            log.error( "NoResultException : OS matching {} not found", name);
            return null;
        }finally {
            em.close();
        }
    }
    
    public List<TblOs> findTblOsByNameLike(String name) {
        EntityManager em = getEntityManager();
        try {
            Query query = em.createNamedQuery("TblOs.findByNameLike");
            query.setParameter("name", "%" + name + "%");
            List<TblOs> resultList = query.getResultList();
            return resultList;

        } catch(NoResultException e){
            log.error( "NoResultException : OS matching {} not found", name);
            return null;
        }finally {
            em.close();
        }
    }

    public TblOs findTblOsByUUID(String uuid_hex) {
        
        EntityManager em = getEntityManager();
        try {
            Query query = em.createNamedQuery("TblOs.findByUUID_Hex");
            query.setParameter("uuid_hex", uuid_hex);
            TblOs tblOem = (TblOs) query.getSingleResult();
            return tblOem;

        } catch(NoResultException e){
            log.error( "NoResultException : OS with UUID {} not found", uuid_hex);
            return null;
        }finally {
            em.close();
        }        
    }
    
}
