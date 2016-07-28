/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.ms.controller;

import com.intel.dcsg.cpg.jpa.GenericJpaController;
import com.intel.mtwilson.ms.controller.exceptions.MSDataException;
import com.intel.mtwilson.ms.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.ms.data.MwPortalUser;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.eclipse.persistence.config.CacheUsage;
import org.eclipse.persistence.config.HintValues;
import org.eclipse.persistence.config.QueryHints;

/**
 * @author jbuhacoff
 */
public class MwPortalUserJpaController extends GenericJpaController<MwPortalUser> implements Serializable {

    public MwPortalUserJpaController(EntityManagerFactory emf) {
        super(MwPortalUser.class);
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    @Override
    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(MwPortalUser mwPortalUser) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(mwPortalUser);
            em.getTransaction().commit();
        } finally {
                em.close();
        }
    }

    public void edit(MwPortalUser mwPortalUser) throws NonexistentEntityException, MSDataException {
        EntityManager em = getEntityManager();
        
        try {
            em.getTransaction().begin();
            em.merge(mwPortalUser);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            Integer id = mwPortalUser.getId();
            if (msg == null || msg.length() == 0) {
                if (id != null && findMwPortalUser(id) == null) {
                    throw new NonexistentEntityException("The mwPortalUser with id " + id + " no longer exists.");
                }
            }
            throw new MSDataException(ex);
        } finally {
                em.close();
        }
    }

    public void destroy(Integer id) throws NonexistentEntityException {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            MwPortalUser mwPortalUser;
            try {
                mwPortalUser = em.getReference(MwPortalUser.class, id);
                mwPortalUser.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The mwPortalUser with id " + id + " no longer exists.", enfe);
            }
            em.remove(mwPortalUser);
            em.getTransaction().commit();
        } finally {
                em.close();
        }
    }

    public List<MwPortalUser> findMwPortalUserEntities() {
        return findMwPortalUserEntities(true, -1, -1);
    }

    public List<MwPortalUser> findMwPortalUserEntities(int maxResults, int firstResult) {
        return findMwPortalUserEntities(false, maxResults, firstResult);
    }

    private List<MwPortalUser> findMwPortalUserEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(MwPortalUser.class));
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

    public MwPortalUser findMwPortalUser(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(MwPortalUser.class, id);
        } finally {
            em.close();
        }
    }

    public int getMwPortalUserCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<MwPortalUser> rt = cq.from(MwPortalUser.class);
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
    public List<MwPortalUser> findMwPortalUserByUsernameEnabled(String username) {
        HashMap<String,Object> parameters = new HashMap<String,Object>();
        parameters.put("username", username);
        parameters.put("enabled", Boolean.TRUE);
        return searchByNamedQuery("findByUsernameEnabled", parameters);
    }
    
    public MwPortalUser findMwPortalUserByUserName(String name) {
        MwPortalUser mwKeystoreObj;
        EntityManager em = getEntityManager();

        try {
            Query query = em.createNamedQuery("MwPortalUser.findByUsername");
            query.setParameter("username", name);

            //query.setHint(QueryHints.REFRESH, HintValues.TRUE);
            query.setHint(QueryHints.CACHE_USAGE, CacheUsage.CheckCacheThenDatabase);
//            query.setHint(QueryHints.CACHE_USAGE, CacheUsage.DoNotCheckCache);

            mwKeystoreObj = (MwPortalUser) query.getSingleResult();
        } catch (javax.persistence.NoResultException e) {
            mwKeystoreObj = null;
        } finally {
            em.close();
        }
        return mwKeystoreObj;
    }

    public MwPortalUser findMwPortalUserByUUID(String uuid_hex) {
        MwPortalUser portalUser;
        EntityManager em = getEntityManager();

        try {
            Query query = em.createNamedQuery("MwPortalUser.findByUUID_Hex");
            query.setParameter("uuid_hex", uuid_hex);
            query.setHint(QueryHints.CACHE_USAGE, CacheUsage.CheckCacheThenDatabase); //.DoNotCheckCache);
            portalUser = (MwPortalUser) query.getSingleResult();
        } catch (javax.persistence.NoResultException e) {
            portalUser = null;
        } finally {
            em.close();
        }
        return portalUser;
    }

    public List<MwPortalUser> findMwPortalUsersMatchingName(String name) {

        HashMap<String,Object> parameters = new HashMap<String,Object>();
        parameters.put("username", "%"+name+"%");
        return searchByNamedQuery("findByUsernameLike", parameters);
        
    }

    public List<MwPortalUser> findMwPortalUsersWithEnabledStatus(Boolean enabled) {
        List<MwPortalUser> portalUsers;
        EntityManager em = getEntityManager();

        try {
            Query query = em.createNamedQuery("MwPortalUser.findByEnabled");
            query.setParameter("enabled", enabled);

            query.setHint(QueryHints.CACHE_USAGE, CacheUsage.CheckCacheThenDatabase); //.DoNotCheckCache);

            portalUsers = query.getResultList();
            if( portalUsers == null ) {
                portalUsers = new ArrayList<>();
            }

        } finally {
            em.close();
        }
        return portalUsers;
    }
    
}
