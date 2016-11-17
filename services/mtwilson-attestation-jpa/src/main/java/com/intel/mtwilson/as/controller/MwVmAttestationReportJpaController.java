/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.as.controller;

import com.intel.mtwilson.as.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.as.controller.exceptions.PreexistingEntityException;
import com.intel.mtwilson.as.data.MwVmAttestationReport;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 *
 * @author ssbangal
 */
public class MwVmAttestationReportJpaController implements Serializable {

    public MwVmAttestationReportJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(MwVmAttestationReport mwVmAttestationReport) throws PreexistingEntityException, Exception {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(mwVmAttestationReport);
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findMwVmAttestationReport(mwVmAttestationReport.getId()) != null) {
                throw new PreexistingEntityException("MwVmAttestationReport " + mwVmAttestationReport + " already exists.", ex);
            }
            throw ex;
        } finally {
            em.close();
        }
    }

    public void edit(MwVmAttestationReport mwVmAttestationReport) throws NonexistentEntityException, Exception {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(mwVmAttestationReport);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                String id = mwVmAttestationReport.getId();
                if (findMwVmAttestationReport(id) == null) {
                    throw new NonexistentEntityException("The mwVmAttestationReport with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            em.close();
        }
    }

    public void destroy(String id) throws NonexistentEntityException {
        EntityManager em = getEntityManager();
        try {            
            em.getTransaction().begin();
            MwVmAttestationReport mwVmAttestationReport;
            try {
                mwVmAttestationReport = em.getReference(MwVmAttestationReport.class, id);
                mwVmAttestationReport.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The mwVmAttestationReport with id " + id + " no longer exists.", enfe);
            }
            em.remove(mwVmAttestationReport);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public List<MwVmAttestationReport> findMwVmAttestationReportEntities() {
        return findMwVmAttestationReportEntities(true, -1, -1);
    }

    public List<MwVmAttestationReport> findMwVmAttestationReportEntities(int maxResults, int firstResult) {
        return findMwVmAttestationReportEntities(false, maxResults, firstResult);
    }

    private List<MwVmAttestationReport> findMwVmAttestationReportEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(MwVmAttestationReport.class));
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

    public MwVmAttestationReport findMwVmAttestationReport(String id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(MwVmAttestationReport.class, id);
        } finally {
            em.close();
        }
    }

    public int getMwVmAttestationReportCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<MwVmAttestationReport> rt = cq.from(MwVmAttestationReport.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
    /**
     * Retrieves the list of all the attestation reports for the particular VM.
     * @param vmInstanceId
     * @return 
     */
    public List<MwVmAttestationReport> findByVMInstanceId(String vmInstanceId) {
        List<MwVmAttestationReport> mwVmAttestationReportList;
        EntityManager em = getEntityManager();
        try {

            Query query = em.createNamedQuery("MwVmAttestationReport.findByVmInstanceId");
            query.setParameter("vmInstanceId", vmInstanceId);

            mwVmAttestationReportList = query.getResultList();
        } finally {
            em.close();
        }
        return mwVmAttestationReportList;
    }
    
    /**
     * Retrieves the latest VM attestation report which is not expired.
     * @param vmInstanceId
     * @return 
     */
    public MwVmAttestationReport findByVMAndExpiry(String vmInstanceId) {
        MwVmAttestationReport mwVmAttestationReport = null;
        EntityManager em = getEntityManager();
        try {

            Query query = em.createNamedQuery("MwVmAttestationReport.findByVMAndExpiry");
            query.setParameter("now", new Date(System.currentTimeMillis()));
            query.setParameter("vmInstanceId", vmInstanceId);

            List<MwVmAttestationReport> list = query.getResultList();
            if (list != null && list.size() > 0) {
                mwVmAttestationReport = list.get(0);
            }
        } finally {
            em.close();
        }
        return mwVmAttestationReport;
    }

    /**
     * Retrieves the list of all the VM attestation reports for the specified VM, which have not expired.
     * @param vmInstanceId
     * @return 
     */
    public List<MwVmAttestationReport> findListByVMAndExpiry(String vmInstanceId) {
        List<MwVmAttestationReport> mwVmAttestationReportList;
        EntityManager em = getEntityManager();
        try {

            Query query = em.createNamedQuery("MwVmAttestationReport.findByVMAndExpiry");
            query.setParameter("now", new Date(System.currentTimeMillis()));
            query.setParameter("vmInstanceId", vmInstanceId);

            mwVmAttestationReportList = query.getResultList();
        } finally {
            em.close();
        }
        return mwVmAttestationReportList;
    }
    
    /**
     * Retrieves the list of all the VM attestation reports for the specified VM between the date range specified.
     * @param vmInstanceId
     * @param fromDate
     * @param toDate
     * @return 
     */
    public List<MwVmAttestationReport> getListByVMAndDateRange(String vmInstanceId, Date fromDate, Date toDate) {
        List<MwVmAttestationReport> mwVmAttestationReportList;
        EntityManager em = getEntityManager();
        try {
            Query query = em.createNamedQuery("MwVmAttestationReport.findByVMAndRangeOfCreatedTs");
            query.setParameter("vmInstanceId", vmInstanceId);
            query.setParameter("fromCreatedTs", fromDate);
            query.setParameter("toCreatedTs", toDate);

            mwVmAttestationReportList = query.getResultList();
        } finally {
            em.close();
        }
        return mwVmAttestationReportList;        
    }

    /**
     * Retrieves the list of attestation reports for all the VMs that are/were running on the specified host.
     * @param hostName
     * @return 
     */
    public List<MwVmAttestationReport> findByHostName(String hostName) {
        List<MwVmAttestationReport> mwVmAttestationReportList;
        EntityManager em = getEntityManager();
        try {

            Query query = em.createNamedQuery("MwVmAttestationReport.findByHostName");
            query.setParameter("hostName", hostName);

            mwVmAttestationReportList = query.getResultList();
        } finally {
            em.close();
        }
        return mwVmAttestationReportList;
    }
  
    /**
     * Retrieves the list of attestation reports for all the VMs that are/were running on the specified host, which are still not expired.
     * @param hostName
     * @return 
     */
    public List<MwVmAttestationReport> findListByHostAndExpiry(String hostName) {
        List<MwVmAttestationReport> mwVmAttestationReportList;
        EntityManager em = getEntityManager();
        try {

            Query query = em.createNamedQuery("MwVmAttestationReport.findByHostAndExpiry");
            query.setParameter("now", new Date(System.currentTimeMillis()));
            query.setParameter("hostName", hostName);

            mwVmAttestationReportList = query.getResultList();
        } finally {
            em.close();
        }
        return mwVmAttestationReportList;
    }
   
    
    /**
     * Retrieves the list of VM attestation reports for all the VMs that are/were running on the specified Host over the specified date range.
     * @param hostName
     * @param fromDate
     * @param toDate
     * @return 
     */
    public List<MwVmAttestationReport> getListByHostAndDateRange(String hostName, Date fromDate, Date toDate) {
        List<MwVmAttestationReport> mwVmAttestationReportList;
        EntityManager em = getEntityManager();
        try {
            Query query = em.createNamedQuery("MwVmAttestationReport.findByHostAndRangeOfCreatedTs");
            query.setParameter("hostName", hostName);
            query.setParameter("fromCreatedTs", fromDate);
            query.setParameter("toCreatedTs", toDate);

            mwVmAttestationReportList = query.getResultList();
        } finally {
            em.close();
        }
        return mwVmAttestationReportList;        
    }
    
    
    /**
     * Retrieves the VM attestation report with the specified ID.
     * @param id
     * @return 
     */
    public MwVmAttestationReport findById(String id) {
        MwVmAttestationReport mwVmAttestationReport = null;
        EntityManager em = getEntityManager();
        try {

            Query query = em.createNamedQuery("MwVmAttestationReport.findById");
            query.setParameter("id", id);

            List<MwVmAttestationReport> list = query.getResultList();
            if (list != null && list.size() > 0) {
                mwVmAttestationReport = list.get(0);
            }
        } finally {
            em.close();
        }
        return mwVmAttestationReport;
    }
    
}
