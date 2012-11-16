/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mountwilson.td.controller;

import com.intel.mountwilson.td.controller.exceptions.NonexistentEntityException;
import com.intel.mountwilson.td.data.TblMle;
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
public class TblMleJpaController implements Serializable {

    public TblMleJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(TblMle tblMle) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(tblMle);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(TblMle tblMle) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            tblMle = em.merge(tblMle);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = tblMle.getId();
                if (findTblMle(id) == null) {
                    throw new NonexistentEntityException("The tblMle with id " + id + " no longer exists.");
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
            TblMle tblMle;
            try {
                tblMle = em.getReference(TblMle.class, id);
                tblMle.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The tblMle with id " + id + " no longer exists.", enfe);
            }
            em.remove(tblMle);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
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
    
}
