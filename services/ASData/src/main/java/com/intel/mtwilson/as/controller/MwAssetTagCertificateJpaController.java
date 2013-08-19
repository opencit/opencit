/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.controller;

import com.intel.mtwilson.as.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.as.data.MwAssetTagCertificate;
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
 * @author ssbangal
 */
public class MwAssetTagCertificateJpaController implements Serializable {

    public MwAssetTagCertificateJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(MwAssetTagCertificate mwAssetTagCertificate) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(mwAssetTagCertificate);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(MwAssetTagCertificate mwAssetTagCertificate) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            mwAssetTagCertificate = em.merge(mwAssetTagCertificate);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = mwAssetTagCertificate.getId();
                if (findMwAssetTagCertificate(id) == null) {
                    throw new NonexistentEntityException("The mwAssetTagCertificate with id " + id + " no longer exists.");
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
            MwAssetTagCertificate mwAssetTagCertificate;
            try {
                mwAssetTagCertificate = em.getReference(MwAssetTagCertificate.class, id);
                mwAssetTagCertificate.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The mwAssetTagCertificate with id " + id + " no longer exists.", enfe);
            }
            em.remove(mwAssetTagCertificate);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<MwAssetTagCertificate> findMwAssetTagCertificateEntities() {
        return findMwAssetTagCertificateEntities(true, -1, -1);
    }

    public List<MwAssetTagCertificate> findMwAssetTagCertificateEntities(int maxResults, int firstResult) {
        return findMwAssetTagCertificateEntities(false, maxResults, firstResult);
    }

    private List<MwAssetTagCertificate> findMwAssetTagCertificateEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(MwAssetTagCertificate.class));
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

    public MwAssetTagCertificate findMwAssetTagCertificate(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(MwAssetTagCertificate.class, id);
        } finally {
            em.close();
        }
    }

    public int getMwAssetTagCertificateCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<MwAssetTagCertificate> rt = cq.from(MwAssetTagCertificate.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
