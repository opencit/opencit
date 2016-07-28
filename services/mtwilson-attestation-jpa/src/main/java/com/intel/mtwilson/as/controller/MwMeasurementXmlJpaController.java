/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.as.controller;

import com.intel.mtwilson.as.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.as.controller.exceptions.PreexistingEntityException;
import com.intel.mtwilson.as.data.MwMeasurementXml;
import com.intel.mtwilson.as.data.MwMleSource;
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
 * @author ssbangal
 */
public class MwMeasurementXmlJpaController implements Serializable {
    private Logger log = LoggerFactory.getLogger(getClass());
    
    public MwMeasurementXmlJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(MwMeasurementXml mwMeasurementXml) throws PreexistingEntityException, Exception {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(mwMeasurementXml);
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findMwMeasurementXml(mwMeasurementXml.getId()) != null) {
                throw new PreexistingEntityException("MwMeasurementXml " + mwMeasurementXml + " already exists.", ex);
            }
            throw ex;
        } finally {
            em.close();
        }
    }

    public void edit(MwMeasurementXml mwMeasurementXml) throws NonexistentEntityException, Exception {
        EntityManager em = getEntityManager();
        try {            
            em.getTransaction().begin();
            em.merge(mwMeasurementXml);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                String id = mwMeasurementXml.getId();
                if (findMwMeasurementXml(id) == null) {
                    throw new NonexistentEntityException("The mwMeasurementXml with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            em.close();
        }
    }

    public void destroy(String id) throws NonexistentEntityException {
        EntityManager em = getEntityManager();
        try {            
            em.getTransaction().begin();
            MwMeasurementXml mwMeasurementXml;
            try {
                mwMeasurementXml = em.getReference(MwMeasurementXml.class, id);
                mwMeasurementXml.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The mwMeasurementXml with id " + id + " no longer exists.", enfe);
            }
            em.remove(mwMeasurementXml);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public List<MwMeasurementXml> findMwMeasurementXmlEntities() {
        return findMwMeasurementXmlEntities(true, -1, -1);
    }

    public List<MwMeasurementXml> findMwMeasurementXmlEntities(int maxResults, int firstResult) {
        return findMwMeasurementXmlEntities(false, maxResults, firstResult);
    }

    private List<MwMeasurementXml> findMwMeasurementXmlEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(MwMeasurementXml.class));
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

    public MwMeasurementXml findMwMeasurementXml(String id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(MwMeasurementXml.class, id);
        } finally {
            em.close();
        }
    }

    public int getMwMeasurementXmlCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<MwMeasurementXml> rt = cq.from(MwMeasurementXml.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }

    /**
     * Retrieves the Measurement XML for the specified MLE if it exists.
     * @param id
     * @return 
     */
    public MwMeasurementXml findByMleId(Integer id) {
        
        EntityManager em = getEntityManager();
        try {

            Query query = em.createNamedQuery("MwMeasurementXml.findByMleID");
            query.setParameter("mleId", id);

            query.setHint(QueryHints.REFRESH, HintValues.TRUE);
            query.setHint(QueryHints.CACHE_USAGE, CacheUsage.DoNotCheckCache);

            MwMeasurementXml measurementXml = (MwMeasurementXml) query.getSingleResult();
            return measurementXml;

        } catch(NoResultException e){
        	log.error(String.format("MLE information with identity %d not found in the DB.", id));
        	return null;
        } finally {
            em.close();
        }               
    }    
    
}
