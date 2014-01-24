/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.controller;

import com.intel.mtwilson.as.controller.exceptions.ASDataException;
import com.intel.mtwilson.as.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.as.data.MwApiClientHttpBasic;
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

/**
 *
 * @author ssbangal
 */
public class MwApiClientHttpBasicJpaController implements Serializable {

    public MwApiClientHttpBasicJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(MwApiClientHttpBasic mwApiClientHttpBasic) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(mwApiClientHttpBasic);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public void edit(MwApiClientHttpBasic mwApiClientHttpBasic) throws NonexistentEntityException, ASDataException {
        EntityManager em =  getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(mwApiClientHttpBasic);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = mwApiClientHttpBasic.getId();
                if (findMwApiClientHttpBasic(id) == null) {
                    throw new NonexistentEntityException("The mwApiClientHttpBasic with id " + id + " no longer exists.");
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
            MwApiClientHttpBasic mwApiClientHttpBasic;
            try {
                mwApiClientHttpBasic = em.getReference(MwApiClientHttpBasic.class, id);
                mwApiClientHttpBasic.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The mwApiClientHttpBasic with id " + id + " no longer exists.", enfe);
            }
            em.remove(mwApiClientHttpBasic);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public List<MwApiClientHttpBasic> findMwApiClientHttpBasicEntities() {
        return findMwApiClientHttpBasicEntities(true, -1, -1);
    }

    public List<MwApiClientHttpBasic> findMwApiClientHttpBasicEntities(int maxResults, int firstResult) {
        return findMwApiClientHttpBasicEntities(false, maxResults, firstResult);
    }

    private List<MwApiClientHttpBasic> findMwApiClientHttpBasicEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(MwApiClientHttpBasic.class));
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

    public MwApiClientHttpBasic findMwApiClientHttpBasic(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(MwApiClientHttpBasic.class, id);
        } finally {
            em.close();
        }
    }

    public int getMwApiClientHttpBasicCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<MwApiClientHttpBasic> rt = cq.from(MwApiClientHttpBasic.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
    /**
     * This function returns the 
     * @param userName
     * @return 
     */
    public MwApiClientHttpBasic findByUserName(String userName) {
        EntityManager em = getEntityManager();
        
        try {
            Query query = em.createNamedQuery("MwApiClientHttpBasic.findByUserName");
            query.setParameter("userName", userName);

            query.setHint(QueryHints.REFRESH, HintValues.TRUE);
            query.setHint(QueryHints.CACHE_USAGE, CacheUsage.DoNotCheckCache);

            MwApiClientHttpBasic apiClientObj = (MwApiClientHttpBasic) query.getSingleResult();
            return apiClientObj;

        } catch(NoResultException e){
        	return null;
        } finally {
            em.close();
        }               
    } 
    
}
