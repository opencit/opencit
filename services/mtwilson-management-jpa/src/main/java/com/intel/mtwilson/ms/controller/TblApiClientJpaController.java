/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.ms.controller;

import com.intel.mtwilson.ms.controller.exceptions.IllegalOrphanException;
import com.intel.mtwilson.ms.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.ms.data.TblApiClient;
import java.io.Serializable;
import java.util.List;
import javax.persistence.*;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
//import com.intel.mountwilson.as.data.TblModuleManifest;

/**
 *
 * @author dsmagadx
 */
public class TblApiClientJpaController implements Serializable {

    public TblApiClientJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(TblApiClient tblApiClient) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(tblApiClient);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public void edit(TblApiClient tblApiClient) throws NonexistentEntityException {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            TblApiClient persistentTblApiClient = em.find(TblApiClient.class, tblApiClient.getClientId());
            if( persistentTblApiClient == null ) {
                throw new NonexistentEntityException("The tblApiClient with client id " + tblApiClient.getClientId() + " no longer exists.");
            }
            em.merge(tblApiClient);
            em.getTransaction().commit();
        } finally {
                em.close();
        }
    }

    public void destroy(String clientId) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = getEntityManager();
        try {
            
            em.getTransaction().begin();
            TblApiClient tblApiClient;
            try {
                tblApiClient = em.getReference(TblApiClient.class, clientId);
                tblApiClient.getClientId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The TblApiClient with id " + clientId + " no longer exists.", enfe);
            }

            em.remove(tblApiClient);
            em.getTransaction().commit();
        } finally {
                em.close();
        }
    }

    public List<TblApiClient> listAllTblApiClients() {
        return listTblApiClients(true, -1, -1);
    }

    public List<TblApiClient> listTblApiClients(int maxResults, int firstResult) {
        return listTblApiClients(false, maxResults, firstResult);
    }

    private List<TblApiClient> listTblApiClients(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(TblApiClient.class));
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

    public TblApiClient findTblApiClient(String clientId) {
        EntityManager em = getEntityManager();
        try {
            return em.find(TblApiClient.class, clientId);
        } finally {
            em.close();
        }
    }

    public TblApiClient findTblApiClientByClientId(String clientId) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<TblApiClient> query = em.createNamedQuery("TblApiClient.findByClientId", TblApiClient.class);
            query.setParameter("clientId", clientId);
            List<TblApiClient> list = query.getResultList();
            if( list != null && !list.isEmpty() ) {
                return list.get(0);
            }
        } finally {
            em.close();
        }
        return null;
    }

    public TblApiClient findTblApiClientByFingerprint(byte[] fingerprint) {
        EntityManager em = getEntityManager();
        try {
            Query query = em.createNamedQuery("TblApiClient.findByFingerprint");
            query.setParameter("fingerprint", fingerprint);
            List<TblApiClient> list = query.getResultList();
            if( list != null && !list.isEmpty() ) {
                return list.get(0);
            }
        } finally {
            em.close();
        }
        return null;
    }
    
    public int getTblApiClientCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<TblApiClient> rt = cq.from(TblApiClient.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
