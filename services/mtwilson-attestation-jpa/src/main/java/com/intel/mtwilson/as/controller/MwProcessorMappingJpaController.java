/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.controller;

import com.intel.mtwilson.as.controller.exceptions.ASDataException;
import com.intel.mtwilson.as.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.as.data.MwProcessorMapping;
import com.intel.mtwilson.as.data.TblEventType;
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
public class MwProcessorMappingJpaController implements Serializable {
    private Logger log = LoggerFactory.getLogger(getClass());
    
    public MwProcessorMappingJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(MwProcessorMapping mwProcessorMapping) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(mwProcessorMapping);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public void edit(MwProcessorMapping mwProcessorMapping) throws NonexistentEntityException, ASDataException {
        EntityManager em = getEntityManager();;
        try {
            em.getTransaction().begin();
            em.merge(mwProcessorMapping);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = mwProcessorMapping.getId();
                if (findMwProcessorMapping(id) == null) {
                    throw new NonexistentEntityException("The mwProcessorMapping with id " + id + " no longer exists.");
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
            MwProcessorMapping mwProcessorMapping;
            try {
                mwProcessorMapping = em.getReference(MwProcessorMapping.class, id);
                mwProcessorMapping.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The mwProcessorMapping with id " + id + " no longer exists.", enfe);
            }
            em.remove(mwProcessorMapping);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public List<MwProcessorMapping> findMwProcessorMappingEntities() {
        return findMwProcessorMappingEntities(true, -1, -1);
    }

    public List<MwProcessorMapping> findMwProcessorMappingEntities(int maxResults, int firstResult) {
        return findMwProcessorMappingEntities(false, maxResults, firstResult);
    }

    private List<MwProcessorMapping> findMwProcessorMappingEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(MwProcessorMapping.class));
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

    public MwProcessorMapping findMwProcessorMapping(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(MwProcessorMapping.class, id);
        } finally {
            em.close();
        }
    }

    public int getMwProcessorMappingCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<MwProcessorMapping> rt = cq.from(MwProcessorMapping.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
    public MwProcessorMapping findByProcessorType(String processorType) {
        EntityManager em = getEntityManager();
        MwProcessorMapping result = null;
        try {

            Query query = em.createNamedQuery("MwProcessorMapping.findByProcessorType");
            query.setParameter("processorType", processorType);

            // Nov 14, 2013: Commenting out the below setting for better performance and updating the cacheusage to check cache and then DB            
            //query.setHint(QueryHints.REFRESH, HintValues.TRUE);
            query.setHint(QueryHints.CACHE_USAGE, CacheUsage.CheckCacheThenDatabase);

            List<MwProcessorMapping> results =query.getResultList();
            if (results != null && results.size() > 0)
                result = results.get(0);     

        } finally {
            em.close();
        }      
        return result;
    }
    
    public MwProcessorMapping findByCPUID(String cpuID) {
        EntityManager em = getEntityManager();
        MwProcessorMapping result = null;
        try {

            Query query = em.createNamedQuery("MwProcessorMapping.findByProcessorCpuid");
            query.setParameter("processorCpuid", cpuID);

            query.setHint(QueryHints.REFRESH, HintValues.TRUE);
            query.setHint(QueryHints.CACHE_USAGE, CacheUsage.DoNotCheckCache);

            // MwProcessorMapping result = (MwProcessorMapping) query.getSingleResult();
            // return result;
            List<MwProcessorMapping> results =query.getResultList();
            if (results != null && results.size() > 0)
                result = results.get(0);            
            
        } catch(NoResultException nre) {
            log.error("No platform matched the CPUID {}.", cpuID);
            return null;
        } finally {
            em.close();
        }
        return result;
    }
}
