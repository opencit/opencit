/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.controller;

import com.intel.mtwilson.as.controller.exceptions.ASDataException;
import com.intel.mtwilson.as.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.as.data.TblModuleManifestLog;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import com.intel.mtwilson.as.data.TblTaLog;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;

/**
 *
 * @author dsmagadx
 */
public class TblModuleManifestLogJpaController implements Serializable {
    public TblModuleManifestLogJpaController(EntityManagerFactory emf) {
        
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(TblModuleManifestLog tblModuleManifestLog) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            TblTaLog taLogId = tblModuleManifestLog.getTaLogId();
            if (taLogId != null) {
                taLogId = em.getReference(taLogId.getClass(), taLogId.getId());
                tblModuleManifestLog.setTaLogId(taLogId);
            }
            em.persist(tblModuleManifestLog);
            if (taLogId != null) {
                taLogId.getTblModuleManifestLogCollection().add(tblModuleManifestLog);
                em.merge(taLogId);
            }
            em.getTransaction().commit();
        } finally {
                em.close();
        }
    }

    public void edit(TblModuleManifestLog tblModuleManifestLog) throws NonexistentEntityException, ASDataException {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            TblModuleManifestLog persistentTblModuleManifestLog = em.find(TblModuleManifestLog.class, tblModuleManifestLog.getId());
            TblTaLog taLogIdOld = persistentTblModuleManifestLog.getTaLogId();
            TblTaLog taLogIdNew = tblModuleManifestLog.getTaLogId();
            if (taLogIdNew != null) {
                taLogIdNew = em.getReference(taLogIdNew.getClass(), taLogIdNew.getId());
                tblModuleManifestLog.setTaLogId(taLogIdNew);
            }
            tblModuleManifestLog = em.merge(tblModuleManifestLog);
            if (taLogIdOld != null && !taLogIdOld.equals(taLogIdNew)) {
                taLogIdOld.getTblModuleManifestLogCollection().remove(tblModuleManifestLog);
                taLogIdOld = em.merge(taLogIdOld);
            }
            if (taLogIdNew != null && !taLogIdNew.equals(taLogIdOld)) {
                taLogIdNew.getTblModuleManifestLogCollection().add(tblModuleManifestLog);
                em.merge(taLogIdNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = tblModuleManifestLog.getId();
                if (findTblModuleManifestLog(id) == null) {
                    throw new NonexistentEntityException("The tblModuleManifestLog with id " + id + " no longer exists.");
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
            TblModuleManifestLog tblModuleManifestLog;
            try {
                tblModuleManifestLog = em.getReference(TblModuleManifestLog.class, id);
                tblModuleManifestLog.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The tblModuleManifestLog with id " + id + " no longer exists.", enfe);
            }
            TblTaLog taLogId = tblModuleManifestLog.getTaLogId();
            if (taLogId != null) {
                taLogId.getTblModuleManifestLogCollection().remove(tblModuleManifestLog);
                em.merge(taLogId);
            }
            em.remove(tblModuleManifestLog);
            em.getTransaction().commit();
        } finally {
                em.close();
        }
    }

    public List<TblModuleManifestLog> findTblModuleManifestLogEntities() {
        return findTblModuleManifestLogEntities(true, -1, -1);
    }

    public List<TblModuleManifestLog> findTblModuleManifestLogEntities(int maxResults, int firstResult) {
        return findTblModuleManifestLogEntities(false, maxResults, firstResult);
    }

    private List<TblModuleManifestLog> findTblModuleManifestLogEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(TblModuleManifestLog.class));
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

    public TblModuleManifestLog findTblModuleManifestLog(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(TblModuleManifestLog.class, id);
        } finally {
            em.close();
        }
    }

    public int getTblModuleManifestLogCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<TblModuleManifestLog> rt = cq.from(TblModuleManifestLog.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
    public TblModuleManifestLog findByTaLogIdAndName(TblTaLog tblTaLog, String componentName){
    
        EntityManager em = getEntityManager();
        try {
            Query query = em.createNamedQuery("TblModuleManifestLog.findByTaLogIdAndName");
            query.setParameter("taLogId", tblTaLog);
            query.setParameter("name", componentName);   
            
            TblModuleManifestLog singleResult = (TblModuleManifestLog) query.getSingleResult();
            return singleResult;
            
        } catch(NoResultException e){
        	return null;
        } finally {
            em.close();
        }
    }
    
}
