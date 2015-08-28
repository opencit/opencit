/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.as.controller;

import com.intel.mtwilson.as.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.as.controller.exceptions.PreexistingEntityException;
import com.intel.mtwilson.as.data.MwVmSamlAssertion;
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
public class MwVmSamlAssertionJpaController implements Serializable {

    public MwVmSamlAssertionJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(MwVmSamlAssertion mwVmSamlAssertion) throws PreexistingEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(mwVmSamlAssertion);
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findMwVmSamlAssertion(mwVmSamlAssertion.getId()) != null) {
                throw new PreexistingEntityException("MwVmSamlAssertion " + mwVmSamlAssertion + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(MwVmSamlAssertion mwVmSamlAssertion) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            mwVmSamlAssertion = em.merge(mwVmSamlAssertion);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                String id = mwVmSamlAssertion.getId();
                if (findMwVmSamlAssertion(id) == null) {
                    throw new NonexistentEntityException("The mwVmSamlAssertion with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(String id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            MwVmSamlAssertion mwVmSamlAssertion;
            try {
                mwVmSamlAssertion = em.getReference(MwVmSamlAssertion.class, id);
                mwVmSamlAssertion.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The mwVmSamlAssertion with id " + id + " no longer exists.", enfe);
            }
            em.remove(mwVmSamlAssertion);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<MwVmSamlAssertion> findMwVmSamlAssertionEntities() {
        return findMwVmSamlAssertionEntities(true, -1, -1);
    }

    public List<MwVmSamlAssertion> findMwVmSamlAssertionEntities(int maxResults, int firstResult) {
        return findMwVmSamlAssertionEntities(false, maxResults, firstResult);
    }

    private List<MwVmSamlAssertion> findMwVmSamlAssertionEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(MwVmSamlAssertion.class));
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

    public MwVmSamlAssertion findMwVmSamlAssertion(String id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(MwVmSamlAssertion.class, id);
        } finally {
            em.close();
        }
    }

    public int getMwVmSamlAssertionCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<MwVmSamlAssertion> rt = cq.from(MwVmSamlAssertion.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
