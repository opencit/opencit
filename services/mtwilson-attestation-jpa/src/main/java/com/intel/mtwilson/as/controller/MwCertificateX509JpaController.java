/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.as.controller;

import com.intel.mtwilson.as.controller.exceptions.ASDataException;
import com.intel.mtwilson.as.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.as.data.MwCertificateX509;
import com.intel.dcsg.cpg.jpa.GenericJpaController;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
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
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(mwCertificateX509);
            em.getTransaction().commit();
        } finally {
                em.close();
        }
    }

    public void edit(MwCertificateX509 mwCertificateX509) throws NonexistentEntityException, ASDataException {
        EntityManager em = getEntityManager();
        try {
            
            em.getTransaction().begin();
            em.merge(mwCertificateX509);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = mwCertificateX509.getId();
                if (findMwCertificateX509(id) == null) {
                    throw new NonexistentEntityException("The mwCertificateX509 with id " + id + " no longer exists.");
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
                em.close();
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

    public List<MwCertificateX509> findAllCertificates() {
        return searchByNamedQuery("findAll");
    }
    public List<MwCertificateX509> findCertificateByName(String name) {
        return searchByNamedQuery("findByName", "name", name);
    }
    public List<MwCertificateX509> findCertificateByNameLike(String name) {
        return searchByNamedQuery("findByNameLike", "name", "%"+name+"%");
    }
    public List<MwCertificateX509> findCertificateByMD5(String md5_hash) {
        return searchByNamedQuery("findByMD5", "md5_hash", md5_hash);
    }
    public List<MwCertificateX509> findCertificateByMD5Enabled(String md5_hash, Boolean enabled) {
        HashMap<String,Object> parameters = new HashMap<String,Object>();
        parameters.put("md5_hash", md5_hash);
        parameters.put("enabled", enabled);
        return searchByNamedQuery("findByMD5Enabled", parameters);
    }
    public List<MwCertificateX509> findCertificateBySHA1(String sha1_hash) {
        return searchByNamedQuery("findBySHA1", "sha1_hash", sha1_hash);
    }
    public List<MwCertificateX509> findCertificateBySHA1Enabled(String sha1_hash, Boolean enabled) {
        HashMap<String,Object> parameters = new HashMap<String,Object>();
        parameters.put("sha1_hash", sha1_hash);
        parameters.put("enabled", enabled);
        return searchByNamedQuery("findBySHA1Enabled", parameters);
    }
    public List<MwCertificateX509> findCertificateByIssuer(String issuer) {
        return searchByNamedQuery("findByIssuer", "issuer", issuer);
    }
    public List<MwCertificateX509> findCertificateByCommentLike(String comment) {
        return searchByNamedQuery("findByCommentLike", "comment", "%"+comment+"%");
    }
    public List<MwCertificateX509> findCertificateByExpiresAfter(Date expiresAfter) {
        return searchByNamedQuery("findByExpiresAfter", "expires", expiresAfter);
    }
    public List<MwCertificateX509> findCertificateByExpiresBefore(Date expiresBefore) {
        return searchByNamedQuery("findByExpiresBefore", "expires", expiresBefore);
    }
    public List<MwCertificateX509> findCertificateByEnabled(Boolean enabled) {
        return searchByNamedQuery("findByEnabled", "enabled", enabled);
    }
    public List<MwCertificateX509> findCertificateByStatus(String status) {
        return searchByNamedQuery("findByStatus", "status", status);
    }
    public List<MwCertificateX509> findCertificateByEnabledStatus(Boolean enabled, String status) {
        HashMap<String,Object> parameters = new HashMap<String,Object>();
        parameters.put("enabled", enabled);
        parameters.put("status", status);
        return searchByNamedQuery("findByEnabledStatus", parameters);
    }
    
}
