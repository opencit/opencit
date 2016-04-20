/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.as.controller;

import com.intel.mtwilson.as.controller.exceptions.ASDataException;
import com.intel.mtwilson.as.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.as.data.MwKeystore;
import com.intel.dcsg.cpg.jpa.GenericJpaController;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.eclipse.persistence.config.CacheUsage;
import org.eclipse.persistence.config.HintValues;
import org.eclipse.persistence.config.QueryHints;

/**
 *
 * @author jbuhacoff
 */
public class MwKeystoreJpaController extends GenericJpaController<MwKeystore> implements Serializable {

    public MwKeystoreJpaController(EntityManagerFactory emf) {
        super(MwKeystore.class);
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    @Override
    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(MwKeystore mwKeystore) {
        EntityManager em = getEntityManager();
        try {
            
            em.getTransaction().begin();
            em.persist(mwKeystore);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public void edit(MwKeystore mwKeystore) throws NonexistentEntityException, ASDataException {
        EntityManager em = getEntityManager();
        try {
            
            em.getTransaction().begin();
            em.merge(mwKeystore);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = mwKeystore.getId();
                if (findMwKeystore(id) == null) {
                    throw new NonexistentEntityException("The mwKeystore with id " + id + " no longer exists.");
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
            MwKeystore mwKeystore;
            try {
                mwKeystore = em.getReference(MwKeystore.class, id);
                mwKeystore.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The mwKeystore with id " + id + " no longer exists.", enfe);
            }
            em.remove(mwKeystore);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public List<MwKeystore> findMwKeystoreEntities() {
        return findMwKeystoreEntities(true, -1, -1);
    }

    public List<MwKeystore> findMwKeystoreEntities(int maxResults, int firstResult) {
        return findMwKeystoreEntities(false, maxResults, firstResult);
    }

    private List<MwKeystore> findMwKeystoreEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(MwKeystore.class));
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

    public MwKeystore findMwKeystore(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(MwKeystore.class, id);
        } finally {
            em.close();
        }
    }

    public int getMwKeystoreCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<MwKeystore> rt = cq.from(MwKeystore.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }

    /**
     *
     * @param name
     * @return the named MwKeystore, or null if it was not found
     */
    public MwKeystore findMwKeystoreByName(String name) {
        //HashMap<String,Object> parameters = new HashMap<String,Object>();
        //parameters.put("name", name);
        //List<MwKeystore> list = searchByNamedQuery("MwKeystore.findByName", parameters);
        //if( list.isEmpty() ) {
        //    return null;
        //}
        //return list.get(0);
        MwKeystore mwKeystoreObj;
        EntityManager em = getEntityManager();
        Query query = em.createNamedQuery("MwKeystore.findByName");
        query.setParameter("name", name);

        query.setHint(QueryHints.REFRESH, HintValues.TRUE);
        query.setHint(QueryHints.CACHE_USAGE, CacheUsage.DoNotCheckCache);
        try {
          mwKeystoreObj = (MwKeystore) query.getSingleResult();
        }
        catch(javax.persistence.NoResultException e) {
          mwKeystoreObj = null;           
        }       
        return mwKeystoreObj;
    }
}
