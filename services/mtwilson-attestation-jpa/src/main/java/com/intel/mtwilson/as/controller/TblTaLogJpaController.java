/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.controller;

import com.intel.mtwilson.as.controller.exceptions.ASDataException;
import com.intel.mtwilson.as.controller.exceptions.IllegalOrphanException;
import com.intel.mtwilson.as.controller.exceptions.NonexistentEntityException;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import com.intel.mtwilson.as.data.TblModuleManifestLog;
import com.intel.mtwilson.as.data.TblTaLog;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;


/**
 *
 * @author dsmagadx
 */
public class TblTaLogJpaController implements Serializable {
    public TblTaLogJpaController( EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(TblTaLog tblTaLog) {
        if (tblTaLog.getTblModuleManifestLogCollection() == null) {
            tblTaLog.setTblModuleManifestLogCollection(new ArrayList<TblModuleManifestLog>());
        }
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            Collection<TblModuleManifestLog> attachedTblModuleManifestLogCollection = new ArrayList<TblModuleManifestLog>();
            for (TblModuleManifestLog tblModuleManifestLogCollectionTblModuleManifestLogToAttach : tblTaLog.getTblModuleManifestLogCollection()) {
                tblModuleManifestLogCollectionTblModuleManifestLogToAttach = em.getReference(tblModuleManifestLogCollectionTblModuleManifestLogToAttach.getClass(), tblModuleManifestLogCollectionTblModuleManifestLogToAttach.getId());
                attachedTblModuleManifestLogCollection.add(tblModuleManifestLogCollectionTblModuleManifestLogToAttach);
            }
            tblTaLog.setTblModuleManifestLogCollection(attachedTblModuleManifestLogCollection);
            em.persist(tblTaLog);
            for (TblModuleManifestLog tblModuleManifestLogCollectionTblModuleManifestLog : tblTaLog.getTblModuleManifestLogCollection()) {
                TblTaLog oldTaLogIdOfTblModuleManifestLogCollectionTblModuleManifestLog = tblModuleManifestLogCollectionTblModuleManifestLog.getTaLogId();
                tblModuleManifestLogCollectionTblModuleManifestLog.setTaLogId(tblTaLog);
                tblModuleManifestLogCollectionTblModuleManifestLog = em.merge(tblModuleManifestLogCollectionTblModuleManifestLog);
                if (oldTaLogIdOfTblModuleManifestLogCollectionTblModuleManifestLog != null) {
                    oldTaLogIdOfTblModuleManifestLogCollectionTblModuleManifestLog.getTblModuleManifestLogCollection().remove(tblModuleManifestLogCollectionTblModuleManifestLog);
                    em.merge(oldTaLogIdOfTblModuleManifestLogCollectionTblModuleManifestLog);
                }
            }
            em.getTransaction().commit();
        } finally {
                em.close();
        }
    }

    public void edit(TblTaLog tblTaLog) throws IllegalOrphanException, NonexistentEntityException, ASDataException {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            TblTaLog persistentTblTaLog = em.find(TblTaLog.class, tblTaLog.getId());
            Collection<TblModuleManifestLog> tblModuleManifestLogCollectionOld = persistentTblTaLog.getTblModuleManifestLogCollection();
            Collection<TblModuleManifestLog> tblModuleManifestLogCollectionNew = tblTaLog.getTblModuleManifestLogCollection();
            List<String> illegalOrphanMessages = null;
            for (TblModuleManifestLog tblModuleManifestLogCollectionOldTblModuleManifestLog : tblModuleManifestLogCollectionOld) {
                if (!tblModuleManifestLogCollectionNew.contains(tblModuleManifestLogCollectionOldTblModuleManifestLog)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain TblModuleManifestLog " + tblModuleManifestLogCollectionOldTblModuleManifestLog + " since its taLogId field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Collection<TblModuleManifestLog> attachedTblModuleManifestLogCollectionNew = new ArrayList<TblModuleManifestLog>();
            for (TblModuleManifestLog tblModuleManifestLogCollectionNewTblModuleManifestLogToAttach : tblModuleManifestLogCollectionNew) {
                tblModuleManifestLogCollectionNewTblModuleManifestLogToAttach = em.getReference(tblModuleManifestLogCollectionNewTblModuleManifestLogToAttach.getClass(), tblModuleManifestLogCollectionNewTblModuleManifestLogToAttach.getId());
                attachedTblModuleManifestLogCollectionNew.add(tblModuleManifestLogCollectionNewTblModuleManifestLogToAttach);
            }
            tblModuleManifestLogCollectionNew = attachedTblModuleManifestLogCollectionNew;
            tblTaLog.setTblModuleManifestLogCollection(tblModuleManifestLogCollectionNew);
            tblTaLog = em.merge(tblTaLog);
            for (TblModuleManifestLog tblModuleManifestLogCollectionNewTblModuleManifestLog : tblModuleManifestLogCollectionNew) {
                if (!tblModuleManifestLogCollectionOld.contains(tblModuleManifestLogCollectionNewTblModuleManifestLog)) {
                    TblTaLog oldTaLogIdOfTblModuleManifestLogCollectionNewTblModuleManifestLog = tblModuleManifestLogCollectionNewTblModuleManifestLog.getTaLogId();
                    tblModuleManifestLogCollectionNewTblModuleManifestLog.setTaLogId(tblTaLog);
                    tblModuleManifestLogCollectionNewTblModuleManifestLog = em.merge(tblModuleManifestLogCollectionNewTblModuleManifestLog);
                    if (oldTaLogIdOfTblModuleManifestLogCollectionNewTblModuleManifestLog != null && !oldTaLogIdOfTblModuleManifestLogCollectionNewTblModuleManifestLog.equals(tblTaLog)) {
                        oldTaLogIdOfTblModuleManifestLogCollectionNewTblModuleManifestLog.getTblModuleManifestLogCollection().remove(tblModuleManifestLogCollectionNewTblModuleManifestLog);
                        em.merge(oldTaLogIdOfTblModuleManifestLogCollectionNewTblModuleManifestLog);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = tblTaLog.getId();
                if (findTblTaLog(id) == null) {
                    throw new NonexistentEntityException("The tblTaLog with id " + id + " no longer exists.");
                }
            }
            throw new ASDataException(ex);
        } finally {
                em.close();
        }
    }

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            TblTaLog tblTaLog;
            try {
                tblTaLog = em.getReference(TblTaLog.class, id);
                tblTaLog.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The tblTaLog with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Collection<TblModuleManifestLog> tblModuleManifestLogCollectionOrphanCheck = tblTaLog.getTblModuleManifestLogCollection();
            for (TblModuleManifestLog tblModuleManifestLogCollectionOrphanCheckTblModuleManifestLog : tblModuleManifestLogCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This TblTaLog (" + tblTaLog + ") cannot be destroyed since the TblModuleManifestLog " + tblModuleManifestLogCollectionOrphanCheckTblModuleManifestLog + " in its tblModuleManifestLogCollection field has a non-nullable taLogId field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(tblTaLog);
            em.getTransaction().commit();
        } finally {
                em.close();
        }
    }

    public List<TblTaLog> findTblTaLogEntities() {
        return findTblTaLogEntities(true, -1, -1);
    }

    public List<TblTaLog> findTblTaLogEntities(int maxResults, int firstResult) {
        return findTblTaLogEntities(false, maxResults, firstResult);
    }

    private List<TblTaLog> findTblTaLogEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(TblTaLog.class));
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

    public TblTaLog findTblTaLog(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(TblTaLog.class, id);
        } finally {
            em.close();
        }
    }

    public int getTblTaLogCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<TblTaLog> rt = cq.from(TblTaLog.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    // Custom find methods
    
        
    public List<TblTaLog> findTrustStatusByHostId(int hostId, int maxresults  ){        
        EntityManager em = getEntityManager();
        try {

            Query query = em.createNamedQuery("TblTaLog.findTrustStatusByHostId");

            query.setParameter("hostID", hostId);
            query.setMaxResults(maxresults);

            List<TblTaLog> logs = query.getResultList();
            
            return logs;

        } finally {
            em.close();
        }
    }

    
     public List<TblTaLog> findLogsByHostId(int hostId , Date lastUpdatedTs ){
        
        EntityManager em = getEntityManager();
        try {
            Query query = em.createNamedQuery("TblTaLog.findLogsByHostId");
            query.setParameter("hostID", hostId);
            query.setParameter("updatedOn", lastUpdatedTs);
            
            List<TblTaLog> logs = query.getResultList();
            return logs;
            
        } finally {
            em.close();
        }
    }

    public Date findLastStatusTs(Integer hostId) {
        Date lastUpdateTs = null;
        EntityManager em = getEntityManager();
        try {
            Query query = em.createNamedQuery("TblTaLog.findLastStatusTs");
            query.setParameter("hostID", hostId);
            query.setMaxResults(1);
            List<TblTaLog> logs = query.getResultList();
            if(logs != null && logs.size() == 1)
                lastUpdateTs = logs.get(0).getUpdatedOn();
            
        } finally {
            em.close();
        }
        return lastUpdateTs;
    }

    public List<TblTaLog> findLogsByHostId(int hostId ){
        
        EntityManager em = getEntityManager();
        try {
            Query query = em.createNamedQuery("TblTaLog.findLogsByHostId2");
            query.setParameter("hostID", hostId);
            
            List<TblTaLog> logs = query.getResultList();
            return logs;
            
        } finally {
            em.close();
        }
    }
    
    public TblTaLog getHostTALogEntryBefore(int hostId, Date expiryTime) {

        EntityManager em = getEntityManager();
        try {
            Query query = em.createNamedQuery("TblTaLog.getHostTALogEntryBefore");
            query.setParameter("hostId", hostId);
            query.setParameter("expiryTs", expiryTime);
            
            List<TblTaLog> logs = query.getResultList();
            if(logs != null && logs.size() > 0) {
                return logs.get(0);
            }
            
        } finally {
            em.close();
        }
        return null;
    }

    public TblTaLog findLatestTrustStatusByHostUuid(String uuid, Date expiryTime) {

        EntityManager em = getEntityManager();
        try {
            Query query = em.createNamedQuery("TblTaLog.findLatestTrustStatusByHostUuid");
            query.setParameter("host_uuid_hex", uuid);
            query.setParameter("expiryTs", expiryTime);
            
            List<TblTaLog> logs = query.getResultList();
            if(logs != null && logs.size() > 0) {
                return logs.get(0);
            }
            
        } finally {
            em.close();
        }
        return null;
    }

    public TblTaLog findByUuid(String uuid) {

        EntityManager em = getEntityManager();
        try {
            Query query = em.createNamedQuery("TblTaLog.findByUuid");
            query.setParameter("uuid_hex", uuid);
            
            TblTaLog log = (TblTaLog) query.getSingleResult();
            return log;
            
        } finally {
            em.close();
        }
    }

}
