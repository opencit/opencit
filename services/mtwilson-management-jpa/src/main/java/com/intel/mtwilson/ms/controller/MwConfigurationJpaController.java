/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.ms.controller;

import com.intel.dcsg.cpg.jpa.GenericJpaController;
import com.intel.mtwilson.ms.controller.exceptions.MSDataException;
import com.intel.mtwilson.ms.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.ms.data.MwConfiguration;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jbuhacoff
 */
public class MwConfigurationJpaController extends GenericJpaController<MwConfiguration> implements Serializable {
    private Logger log = LoggerFactory.getLogger(getClass());

    public MwConfigurationJpaController(EntityManagerFactory emf) {
        super(MwConfiguration.class);
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    @Override
    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(MwConfiguration mwConfiguration) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(mwConfiguration);
            em.getTransaction().commit();
        } finally {
                em.close();
        }
    }

    public void edit(MwConfiguration mwConfiguration) throws NonexistentEntityException, MSDataException {
        EntityManager em = getEntityManager();
        
        try {
            em.getTransaction().begin();
            em.merge(mwConfiguration);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            String key = mwConfiguration.getKey();
            if (msg == null || msg.length() == 0) {
                if (key != null && findMwConfiguration(key) == null) {
                    throw new NonexistentEntityException("The mwConfiguration with key " + key + " no longer exists.");
                }
            }
            throw new MSDataException(ex);
        } finally {
                em.close();
        }
    }

    public void destroy(String key) throws NonexistentEntityException {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            MwConfiguration mwConfiguration;
            try {
                mwConfiguration = em.getReference(MwConfiguration.class, key);
                mwConfiguration.getKey();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The mwConfiguration with key " + key + " no longer exists.", enfe);
            }
            em.remove(mwConfiguration);
            em.getTransaction().commit();
        } finally {
                em.close();
        }
    }

    public List<MwConfiguration> findMwConfigurationEntities() {
        return findMwConfigurationEntities(true, -1, -1);
    }

    public List<MwConfiguration> findMwConfigurationEntities(int maxResults, int firstResult) {
        return findMwConfigurationEntities(false, maxResults, firstResult);
    }

    private List<MwConfiguration> findMwConfigurationEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(MwConfiguration.class));
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

    public MwConfiguration findMwConfiguration(String key) {
        EntityManager em = getEntityManager();
        try {
            return em.find(MwConfiguration.class, key);
        } finally {
            em.close();
        }
    }

    public int getMwConfigurationCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<MwConfiguration> rt = cq.from(MwConfiguration.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }

    /**
     * Primary method to find if there is an active user with the given username for login purposes
     * @param all
     * @param maxResults
     * @param firstResult
     * @return 
     */
    public MwConfiguration findMwConfigurationByKey(String key) {
        HashMap<String,Object> parameters = new HashMap<String,Object>();
        parameters.put("key", key);
        List<MwConfiguration> list = searchByNamedQuery("findByKey", parameters);
        if( list.isEmpty() ) { return null; }
        return list.get(0);
    }
    
    /**
     * If the key already exists, it overwrites it. If it does not already exist,
     * it is created.
     * @param key
     * @param value
     * @throws NonexistentEntityException
     * @throws Exception 
     */
    public void setMwConfiguration(String key, String value) throws NonexistentEntityException, MSDataException {
        MwConfiguration setting = findMwConfigurationByKey(key);
        if( setting == null ) {
            setting = new MwConfiguration(key, value, null);
            create(setting);
        }
        else {
            setting.setValue(value);
            edit(setting);
        }
    }
    
}
