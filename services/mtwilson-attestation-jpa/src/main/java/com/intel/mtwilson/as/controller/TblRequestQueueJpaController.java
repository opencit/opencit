/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.controller;

import com.intel.mtwilson.as.controller.exceptions.ASDataException;
import com.intel.mtwilson.as.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.as.data.TblRequestQueue;
import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 *
 * @author dsmagadx
 */
public class TblRequestQueueJpaController implements Serializable {

    public TblRequestQueueJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(TblRequestQueue tblRequestQueue) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(tblRequestQueue);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public void edit(TblRequestQueue tblRequestQueue) throws NonexistentEntityException, ASDataException {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(tblRequestQueue);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = tblRequestQueue.getId();
                if (findTblRequestQueue(id) == null) {
                    throw new NonexistentEntityException("The tblRequestQueue with id " + id + " no longer exists.");
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
            TblRequestQueue tblRequestQueue;
            try {
                tblRequestQueue = em.getReference(TblRequestQueue.class, id);
                tblRequestQueue.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The tblRequestQueue with id " + id + " no longer exists.", enfe);
            }
            em.remove(tblRequestQueue);
            em.getTransaction().commit();
        } finally {
                em.close();
        }
    }

    public List<TblRequestQueue> findTblRequestQueueEntities() {
        return findTblRequestQueueEntities(true, -1, -1);
    }

    public List<TblRequestQueue> findTblRequestQueueEntities(int maxResults, int firstResult) {
        return findTblRequestQueueEntities(false, maxResults, firstResult);
    }

    private List<TblRequestQueue> findTblRequestQueueEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(TblRequestQueue.class));
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

    public TblRequestQueue findTblRequestQueue(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(TblRequestQueue.class, id);
        } finally {
            em.close();
        }
    }

    public int getTblRequestQueueCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<TblRequestQueue> rt = cq.from(TblRequestQueue.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
