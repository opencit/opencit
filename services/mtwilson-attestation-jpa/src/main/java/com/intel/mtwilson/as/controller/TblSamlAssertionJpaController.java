/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.controller;

import com.intel.mtwilson.as.controller.exceptions.ASDataException;
import com.intel.mtwilson.as.controller.exceptions.NonexistentEntityException;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import com.intel.mtwilson.as.data.TblHosts;
import com.intel.mtwilson.as.data.TblSamlAssertion;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dsmagadX
 */
public class TblSamlAssertionJpaController implements Serializable {
    private Logger log = LoggerFactory.getLogger(getClass());

    public TblSamlAssertionJpaController( EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(TblSamlAssertion tblSamlAssertion) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            TblHosts hostId = tblSamlAssertion.getHostId();
            if (hostId != null) {
                hostId = em.getReference(hostId.getClass(), hostId.getId());
                tblSamlAssertion.setHostId(hostId);
            }
            em.persist(tblSamlAssertion);
//            if (hostId != null) {
//                hostId.getTblSamlAssertionCollection().add(tblSamlAssertion);
//                em.merge(hostId);
//            }
            em.getTransaction().commit();
        } finally {
                em.close();
        }
    }

    public void edit(TblSamlAssertion tblSamlAssertion) throws NonexistentEntityException, ASDataException {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            //#6358: Variable 'persistentTblSamlAssertion' was never read after being assigned.
            //TblSamlAssertion persistentTblSamlAssertion = em.find(TblSamlAssertion.class, tblSamlAssertion.getId());
            //#5821: Variable 'hostIdOld' was never read after being assigned
            //TblHosts hostIdOld = persistentTblSamlAssertion.getHostId();
            TblHosts hostIdNew = tblSamlAssertion.getHostId();
            if (hostIdNew != null) {
                hostIdNew = em.getReference(hostIdNew.getClass(), hostIdNew.getId());
                tblSamlAssertion.setHostId(hostIdNew);
            }
            //tblSamlAssertion = em.merge(tblSamlAssertion);
//            if (hostIdOld != null && !hostIdOld.equals(hostIdNew)) {
//                hostIdOld.getTblSamlAssertionCollection().remove(tblSamlAssertion);
//                hostIdOld = em.merge(hostIdOld);
//            }
//            if (hostIdNew != null && !hostIdNew.equals(hostIdOld)) {
//                hostIdNew.getTblSamlAssertionCollection().add(tblSamlAssertion);
//                em.merge(hostIdNew);
//            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = tblSamlAssertion.getId();
                if (findTblSamlAssertion(id) == null) {
                    throw new NonexistentEntityException("The tblSamlAssertion with id " + id + " no longer exists.");
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
            TblSamlAssertion tblSamlAssertion;
            try {
                tblSamlAssertion = em.getReference(TblSamlAssertion.class, id);
                tblSamlAssertion.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The tblSamlAssertion with id " + id + " no longer exists.", enfe);
            }
//            TblHosts hostId = tblSamlAssertion.getHostId();
//            if (hostId != null) {
//                hostId.getTblSamlAssertionCollection().remove(tblSamlAssertion);
//                em.merge(hostId);
//            }
            em.remove(tblSamlAssertion);
            em.getTransaction().commit();
        } finally {
                em.close();
        }
    }

    public List<TblSamlAssertion> findTblSamlAssertionEntities() {
        return findTblSamlAssertionEntities(true, -1, -1);
    }

    public List<TblSamlAssertion> findTblSamlAssertionEntities(int maxResults, int firstResult) {
        return findTblSamlAssertionEntities(false, maxResults, firstResult);
    }

    private List<TblSamlAssertion> findTblSamlAssertionEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(TblSamlAssertion.class));
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

    public TblSamlAssertion findTblSamlAssertion(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(TblSamlAssertion.class, id);
        } finally {
            em.close();
        }
    }

    public int getTblSamlAssertionCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<TblSamlAssertion> rt = cq.from(TblSamlAssertion.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
    public TblSamlAssertion findByHostAndExpiry(String host) {
        TblSamlAssertion tblSamlAssertion = null;
        EntityManager em = getEntityManager();
        try {

            Query query = em.createNamedQuery("TblSamlAssertion.findByHostAndExpiry");

            query.setParameter("now", new Date(System.currentTimeMillis()));
            query.setParameter("hostName", host);

            List<TblSamlAssertion> list = query.getResultList();

            if (list != null && list.size() > 0) {
                tblSamlAssertion = list.get(0);
            }
        } finally {
                em.close();
        }

        return tblSamlAssertion;

    }
    
    public List<TblSamlAssertion> findListByHostAndExpiry(String host) {
        List<TblSamlAssertion> tblSamlAssertionList;
        EntityManager em = getEntityManager();
        try {
            Query query = em.createNamedQuery("TblSamlAssertion.findByHostAndExpiry");
            query.setParameter("now", new Date(System.currentTimeMillis()));
            query.setParameter("hostName", host);

            tblSamlAssertionList = query.getResultList();
        } finally {
                em.close();
        }

        return tblSamlAssertionList;
    }
    
    public List<TblSamlAssertion> findByHostID(TblHosts  hostID) {
        EntityManager em = getEntityManager();
        try {

            Query query = em.createNamedQuery("TblSamlAssertion.findByHostID");
            query.setParameter("hostId", hostID);
            
            return query.getResultList();

        } finally {
                em.close();
        }

    }
    
    public TblSamlAssertion findByAssertionUuid(String assertionUuid) {
        TblSamlAssertion tblSamlAssertion = null;
        EntityManager em = getEntityManager();
        try {
            Query query = em.createNamedQuery("TblSamlAssertion.findByAssertionUuid");
            query.setParameter("assertionUuid", assertionUuid);

            List<TblSamlAssertion> list = query.getResultList();

            if (list != null && list.size() > 0) {
                tblSamlAssertion = list.get(0);
            }
        } finally {
                em.close();
        }

        return tblSamlAssertion;
    }
    
    /**
     * Retrieves the list of SAML assertions for the specified host between the durations specified.
     * @param host
     * @param fromDate
     * @param toDate
     * @return 
     */
    public List<TblSamlAssertion> getListByDate(String host, Date fromDate, Date toDate) {
        List<TblSamlAssertion> tblSamlAssertionList;
        EntityManager em = getEntityManager();
        try {
            Query query = em.createNamedQuery("TblSamlAssertion.findByRangeOfCreatedTs");
            query.setParameter("hostName", host);
            query.setParameter("fromCreatedTs", fromDate);
            query.setParameter("toCreatedTs", toDate);

            tblSamlAssertionList = query.getResultList();
        } finally {
                em.close();
        }

        return tblSamlAssertionList;
        
    }
    
    public List<String> findHostnamesWithExpiredCache(Integer expiryInSeconds) {
        EntityManager em = getEntityManager();
        try {
            log.info("AutoRefreshTrust: findHostnamesWithExpiredCache");
            // To find the list of hosts which would have their SAML getting expired, we calculate what is the earliest create date for which the SAML would expire
            // and also add a buffer time of about 5 min so that we might get to processing the host before it actually expires.
            Query query = em.createNativeQuery("SELECT h.Name FROM mw_hosts as h WHERE NOT EXISTS ( SELECT ID FROM mw_saml_assertion as t WHERE h.ID = t.host_id AND t.created_ts > ? )");
            Calendar maxCache = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");	
            String currentTime = sdf.format(maxCache.getTime());
            maxCache.add(Calendar.SECOND, -expiryInSeconds);
            log.info("AutoRefreshTrust: Query hosts whose SAML was created after {}. Current time is {}.", sdf.format(maxCache.getTime()), currentTime);
            query.setParameter(1, maxCache);
            List<String> results = query.getResultList();
            return results;
        } finally {
            em.close();
        }
    }
}
