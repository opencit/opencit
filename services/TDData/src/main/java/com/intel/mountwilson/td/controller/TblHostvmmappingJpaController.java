/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mountwilson.td.controller;

import com.intel.mountwilson.td.controller.exceptions.NonexistentEntityException;
import com.intel.mountwilson.td.data.TblHostvmmapping;
import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.UserTransaction;

/**
 *
 * @author ssbangal
 */
public class TblHostvmmappingJpaController implements Serializable {

    public TblHostvmmappingJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(TblHostvmmapping tblHostvmmapping) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(tblHostvmmapping);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(TblHostvmmapping tblHostvmmapping) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            tblHostvmmapping = em.merge(tblHostvmmapping);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = tblHostvmmapping.getId();
                if (findTblHostvmmapping(id) == null) {
                    throw new NonexistentEntityException("The tblHostvmmapping with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            TblHostvmmapping tblHostvmmapping;
            try {
                tblHostvmmapping = em.getReference(TblHostvmmapping.class, id);
                tblHostvmmapping.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The tblHostvmmapping with id " + id + " no longer exists.", enfe);
            }
            em.remove(tblHostvmmapping);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<TblHostvmmapping> findTblHostvmmappingEntities() {
        return findTblHostvmmappingEntities(true, -1, -1);
    }

    public List<TblHostvmmapping> findTblHostvmmappingEntities(int maxResults, int firstResult) {
        return findTblHostvmmappingEntities(false, maxResults, firstResult);
    }

    private List<TblHostvmmapping> findTblHostvmmappingEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(TblHostvmmapping.class));
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

    public TblHostvmmapping findTblHostvmmapping(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(TblHostvmmapping.class, id);
        } finally {
            em.close();
        }
    }

    public int getTblHostvmmappingCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<TblHostvmmapping> rt = cq.from(TblHostvmmapping.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
