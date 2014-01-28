/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.controller;

import com.intel.mtwilson.as.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.as.controller.exceptions.ASDataException;

import com.intel.mtwilson.as.data.TblLocationPcr;
import com.intel.mtwilson.as.data.TblMle;

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

import org.eclipse.persistence.config.CacheUsage;
import org.eclipse.persistence.config.HintValues;
import org.eclipse.persistence.config.QueryHints;

/**
 *
 * @author dsmagadx
 */
public class TblLocationPcrJpaController implements Serializable {
    private Logger log = LoggerFactory.getLogger(getClass());

    public TblLocationPcrJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(TblLocationPcr tblLocationPcr) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(tblLocationPcr);
            em.getTransaction().commit();
        } finally {
                em.close();
        }
    }

    public void edit(TblLocationPcr tblLocationPcr) throws NonexistentEntityException, ASDataException {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(tblLocationPcr);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = tblLocationPcr.getId();
                if (findTblLocationPcr(id) == null) {
                    throw new NonexistentEntityException("The tblLocationPcr with id " + id + " no longer exists.");
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
            TblLocationPcr tblLocationPcr;
            try {
                tblLocationPcr = em.getReference(TblLocationPcr.class, id);
                tblLocationPcr.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The tblLocationPcr with id " + id + " no longer exists.", enfe);
            }
            em.remove(tblLocationPcr);
            em.getTransaction().commit();
        } finally {
                em.close();
        }
    }

    public List<TblLocationPcr> findTblLocationPcrEntities() {
        return findTblLocationPcrEntities(true, -1, -1);
    }

    public List<TblLocationPcr> findTblLocationPcrEntities(int maxResults, int firstResult) {
        return findTblLocationPcrEntities(false, maxResults, firstResult);
    }

    private List<TblLocationPcr> findTblLocationPcrEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(TblLocationPcr.class));
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

    public TblLocationPcr findTblLocationPcr(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(TblLocationPcr.class, id);
        } finally {
            em.close();
        }
    }

    public int getTblLocationPcrCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<TblLocationPcr> rt = cq.from(TblLocationPcr.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
    public String findTblLocationPcrByPcrValue(String pcrValue) {
        EntityManager em = getEntityManager();
        try {          
            Query query = em.createNamedQuery("TblLocationPcr.findByPcrValue");

            query.setParameter("pcrValue", pcrValue);

            query.setHint(QueryHints.REFRESH, HintValues.TRUE);
            query.setHint(QueryHints.CACHE_USAGE, CacheUsage.DoNotCheckCache);

            
            try {
            	
            	TblLocationPcr locationPcr = (TblLocationPcr) query.getSingleResult();
                String location = locationPcr.getLocation();
                
                log.debug("PCR Value  " + pcrValue + " location " + location );
                
                return location;
                
            } catch (NoResultException e) {
                log.error( "NoResultException: Location does not exist for pcr value {} ", 
                        pcrValue);
                return null;
            }
            
            
        } finally {
            em.close();
        }
    }

    public TblLocationPcr findTblLocationPcrByPcrValueEx(String pcrValue) {
        EntityManager em = getEntityManager();
        try {          
            Query query = em.createNamedQuery("TblLocationPcr.findByPcrValue");
            query.setParameter("pcrValue", pcrValue);

            query.setHint(QueryHints.REFRESH, HintValues.TRUE);
            query.setHint(QueryHints.CACHE_USAGE, CacheUsage.DoNotCheckCache);
           
            try {
           	
            	TblLocationPcr locationPcr = (TblLocationPcr) query.getSingleResult();
                return locationPcr;
                
            } catch (NoResultException e) {
                log.error( "NoResultException: Location does not exist for pcr value {} ", 
                        pcrValue);
                return null;
            }            
        } finally {
            em.close();
        }
    }
    
}
