/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.as.controller;

import com.intel.mtwilson.as.controller.exceptions.ASDataException;
import com.intel.mtwilson.as.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.as.data.MwRequestLog;
import com.intel.dcsg.cpg.jpa.GenericJpaController;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NamedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 *
 * @author jbuhacoff
 */
public class MwRequestLogJpaController extends GenericJpaController<MwRequestLog> implements Serializable {

    public MwRequestLogJpaController(EntityManagerFactory emf) {
        super(MwRequestLog.class);
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    @Override
    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(MwRequestLog mwRequestLog) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(mwRequestLog);
            em.getTransaction().commit();
        } finally {
                em.close();
        }
    }

    public void edit(MwRequestLog mwRequestLog) throws NonexistentEntityException, ASDataException {
        EntityManager em = getEntityManager();
        try {
            
            em.getTransaction().begin();
            em.merge(mwRequestLog);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = mwRequestLog.getId();
                if (findMwRequestLog(id) == null) {
                    throw new NonexistentEntityException("The mwRequestLog with id " + id + " no longer exists.");
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
            MwRequestLog mwRequestLog;
            try {
                mwRequestLog = em.getReference(MwRequestLog.class, id);
                mwRequestLog.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The mwRequestLog with id " + id + " no longer exists.", enfe);
            }
            em.remove(mwRequestLog);
            em.getTransaction().commit();
        } finally {
                em.close();
        }
    }

    public List<MwRequestLog> findMwRequestLogEntities() {
        return findMwRequestLogEntities(true, -1, -1);
    }

    public List<MwRequestLog> findMwRequestLogEntities(int maxResults, int firstResult) {
        return findMwRequestLogEntities(false, maxResults, firstResult);
    }

    private List<MwRequestLog> findMwRequestLogEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(MwRequestLog.class));
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

    public MwRequestLog findMwRequestLog(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(MwRequestLog.class, id);
        } finally {
            em.close();
        }
    }

    public int getMwRequestLogCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<MwRequestLog> rt = cq.from(MwRequestLog.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }

    public List<MwRequestLog> findAllRequests() {
        return searchByNamedQuery("findAll");
    }
    public List<MwRequestLog> findRequestByInstance(String instance) {
        return searchByNamedQuery("findByInstance", "instance", instance);
    }
    public List<MwRequestLog> findRequestByMd5Hash(String md5Hash) {
        return searchByNamedQuery("findByMd5Hash", "md5_hash", md5Hash);
    }
    public List<MwRequestLog> findRequestBySource(String source) {
        return searchByNamedQuery("findBySource", "source", source);
    }
    public List<MwRequestLog> findCertificateByContentLike(String content) {
        return searchByNamedQuery("findByContentLike", "content", "%"+content+"%");
    }
    public List<MwRequestLog> findCertificateByReceivedAfter(Date receivedAfter) {
        return searchByNamedQuery("findByReceivedAfter", "received", receivedAfter);
    }
    public List<MwRequestLog> findCertificateByReceivedBefore(Date receivedBefore) {
        return searchByNamedQuery("findByReceivedBefore", "received", receivedBefore);
    }
    public List<MwRequestLog> findCertificateByReceived(Boolean received) {
        return searchByNamedQuery("findByReceived", "received", received);
    }
    public List<MwRequestLog> findBySourceMd5HashReceivedAfter(String source, byte[] md5Hash, Date receivedAfter) {
        HashMap<String,Object> parameters = new HashMap<String,Object>();
        parameters.put("source", source);
        parameters.put("md5_hash", md5Hash);
        parameters.put("received", receivedAfter);
        return searchByNamedQuery("findBySourceMd5HashReceivedAfter", parameters);
    }

}
