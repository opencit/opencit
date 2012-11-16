/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.as.controller;

import com.intel.mtwilson.as.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.as.data.MwCertificateX509;
import com.intel.mtwilson.jpa.GenericJpaController;
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
 * @author jbuhacoff
 */
public class MwCertificateX509JpaController extends GenericJpaController<MwCertificateX509> implements Serializable {

    public MwCertificateX509JpaController(EntityManagerFactory emf) {
        super(MwCertificateX509.class);
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    @Override
    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(MwCertificateX509 mwCertificateX509) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(mwCertificateX509);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(MwCertificateX509 mwCertificateX509) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            mwCertificateX509 = em.merge(mwCertificateX509);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = mwCertificateX509.getId();
                if (findMwCertificateX509(id) == null) {
                    throw new NonexistentEntityException("The mwCertificateX509 with id " + id + " no longer exists.");
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
            MwCertificateX509 mwCertificateX509;
            try {
                mwCertificateX509 = em.getReference(MwCertificateX509.class, id);
                mwCertificateX509.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The mwCertificateX509 with id " + id + " no longer exists.", enfe);
            }
            em.remove(mwCertificateX509);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<MwCertificateX509> findMwCertificateX509Entities() {
        return findMwCertificateX509Entities(true, -1, -1);
    }

    public List<MwCertificateX509> findMwCertificateX509Entities(int maxResults, int firstResult) {
        return findMwCertificateX509Entities(false, maxResults, firstResult);
    }

    private List<MwCertificateX509> findMwCertificateX509Entities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(MwCertificateX509.class));
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

    public MwCertificateX509 findMwCertificateX509(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(MwCertificateX509.class, id);
        } finally {
            em.close();
        }
    }

    public int getMwCertificateX509Count() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<MwCertificateX509> rt = cq.from(MwCertificateX509.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
