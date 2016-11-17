/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.as.controller;

import com.intel.mtwilson.as.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.as.controller.exceptions.PreexistingEntityException;
import com.intel.mtwilson.as.data.MwHostPreRegistrationDetails;
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
public class MwHostPreRegistrationDetailsJpaController implements Serializable {

    public MwHostPreRegistrationDetailsJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(MwHostPreRegistrationDetails mwHostPreRegistrationDetails) throws PreexistingEntityException, Exception {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(mwHostPreRegistrationDetails);
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findMwHostPreRegistrationDetails(mwHostPreRegistrationDetails.getId()) != null) {
                throw new PreexistingEntityException("MwHostPreRegistrationDetails " + mwHostPreRegistrationDetails + " already exists.", ex);
            }
            throw ex;
        } finally {
            em.close();
        }
    }

    public void edit(MwHostPreRegistrationDetails mwHostPreRegistrationDetails) throws NonexistentEntityException, Exception {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(mwHostPreRegistrationDetails);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                String id = mwHostPreRegistrationDetails.getId();
                if (findMwHostPreRegistrationDetails(id) == null) {
                    throw new NonexistentEntityException("The mwHostPreRegistrationDetails with id " + id + " no longer exists.");
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
            MwHostPreRegistrationDetails mwHostPreRegistrationDetails;
            try {
                mwHostPreRegistrationDetails = em.getReference(MwHostPreRegistrationDetails.class, id);
                mwHostPreRegistrationDetails.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The mwHostPreRegistrationDetails with id " + id + " no longer exists.", enfe);
            }
            em.remove(mwHostPreRegistrationDetails);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public List<MwHostPreRegistrationDetails> findMwHostPreRegistrationDetailsEntities() {
        return findMwHostPreRegistrationDetailsEntities(true, -1, -1);
    }

    public List<MwHostPreRegistrationDetails> findMwHostPreRegistrationDetailsEntities(int maxResults, int firstResult) {
        return findMwHostPreRegistrationDetailsEntities(false, maxResults, firstResult);
    }

    private List<MwHostPreRegistrationDetails> findMwHostPreRegistrationDetailsEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(MwHostPreRegistrationDetails.class));
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

    public MwHostPreRegistrationDetails findMwHostPreRegistrationDetails(String id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(MwHostPreRegistrationDetails.class, id);
        } finally {
            em.close();
        }
    }

    public int getMwHostPreRegistrationDetailsCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<MwHostPreRegistrationDetails> rt = cq.from(MwHostPreRegistrationDetails.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
    /**
     * Retrieves the latest user name and password registered for the specified host.
     * @param name
     * @return 
     */
    public MwHostPreRegistrationDetails findByName(String name) {

        MwHostPreRegistrationDetails host = null;
        EntityManager em = getEntityManager();
        try {

            Query query = em.createNamedQuery("MwHostPreRegistrationDetails.findByName");
            query.setParameter("name", name);

            List<MwHostPreRegistrationDetails> list = query.getResultList();
            if (list != null && list.size() > 0) {
                host = list.get(0);

            }
        } finally {
            em.close();
        }
        return host;
    }
    
}
