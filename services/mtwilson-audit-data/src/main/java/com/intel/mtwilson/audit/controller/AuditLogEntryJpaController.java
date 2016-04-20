/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.audit.controller;

import com.intel.mtwilson.audit.controller.exceptions.AuditDataException;
import com.intel.mtwilson.audit.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.audit.data.AuditLogEntry;
import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.UserTransaction;

/**
 *
 * @author dsmagadx
 */
public class AuditLogEntryJpaController implements Serializable {

    public AuditLogEntryJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    
    public AuditLogEntryJpaController( EntityManagerFactory emf) {
        this.emf = emf;
    }

    
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(AuditLogEntry auditLogEntry) {
        EntityManager em = getEntityManager();
        try {
            
            em.getTransaction().begin();
            em.persist(auditLogEntry);
            em.getTransaction().commit();
        } finally {
                em.close();
        }
    }

    public void edit(AuditLogEntry auditLogEntry) throws NonexistentEntityException, AuditDataException {
        EntityManager em = getEntityManager();
        try {
            
            em.getTransaction().begin();
            em.merge(auditLogEntry);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = auditLogEntry.getId();
                if (findAuditLogEntry(id) == null) {
                    throw new NonexistentEntityException("The auditLogEntry with id " + id + " no longer exists.");
                }
            }
            throw new AuditDataException(ex);
        } finally {
                em.close();
        }
    }

    public void destroy(Integer id) throws NonexistentEntityException {
        EntityManager em = getEntityManager();
        try {
            
            em.getTransaction().begin();
            AuditLogEntry auditLogEntry;
            try {
                auditLogEntry = em.getReference(AuditLogEntry.class, id);
                auditLogEntry.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The auditLogEntry with id " + id + " no longer exists.", enfe);
            }
            em.remove(auditLogEntry);
            em.getTransaction().commit();
        } finally {
                em.close();
        }
    }

    public List<AuditLogEntry> findAuditLogEntryEntities() {
        return findAuditLogEntryEntities(true, -1, -1);
    }

    public List<AuditLogEntry> findAuditLogEntryEntities(int maxResults, int firstResult) {
        return findAuditLogEntryEntities(false, maxResults, firstResult);
    }

    private List<AuditLogEntry> findAuditLogEntryEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(AuditLogEntry.class));
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

    public AuditLogEntry findAuditLogEntry(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(AuditLogEntry.class, id);
        } finally {
            em.close();
        }
    }

    public int getAuditLogEntryCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<AuditLogEntry> rt = cq.from(AuditLogEntry.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
