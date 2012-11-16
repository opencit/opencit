/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mountwilson.td.controller;

import com.intel.mountwilson.td.controller.exceptions.NonexistentEntityException;
import com.intel.mountwilson.td.data.TblHostDetail;
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
public class TblHostDetailJpaController implements Serializable {

    public TblHostDetailJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(TblHostDetail tblHostDetail) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(tblHostDetail);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(TblHostDetail tblHostDetail) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            tblHostDetail = em.merge(tblHostDetail);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = tblHostDetail.getHostDetailID();
                if (findTblHostDetail(id) == null) {
                    throw new NonexistentEntityException("The tblHostDetail with id " + id + " no longer exists.");
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
            TblHostDetail tblHostDetail;
            try {
                tblHostDetail = em.getReference(TblHostDetail.class, id);
                tblHostDetail.getHostDetailID();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The tblHostDetail with id " + id + " no longer exists.", enfe);
            }
            em.remove(tblHostDetail);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<TblHostDetail> findTblHostDetailEntities() {
        return findTblHostDetailEntities(true, -1, -1);
    }

    public List<TblHostDetail> findTblHostDetailEntities(int maxResults, int firstResult) {
        return findTblHostDetailEntities(false, maxResults, firstResult);
    }

    private List<TblHostDetail> findTblHostDetailEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(TblHostDetail.class));
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

    public TblHostDetail findTblHostDetail(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(TblHostDetail.class, id);
        } finally {
            em.close();
        }
    }

    public int getTblHostDetailCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<TblHostDetail> rt = cq.from(TblHostDetail.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
