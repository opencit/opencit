/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.controller;

import com.intel.mtwilson.as.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.as.data.MwAssetTagCertificate;
import com.intel.dcsg.cpg.jpa.GenericJpaController;
import java.io.Serializable;
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
public class MwAssetTagCertificateJpaController extends GenericJpaController<MwAssetTagCertificate> implements Serializable {

    private EntityManagerFactory emf = null;

    public MwAssetTagCertificateJpaController(EntityManagerFactory emf) {
        super(MwAssetTagCertificate.class);
        this.emf = emf;
    }
    
    @Override
    public EntityManager getEntityManager() {
        EntityManager em = emf.createEntityManager();
        if( em == null ) { throw new IllegalStateException("Cannot obtain entity manager"); }
        return em;
    }

    public void create(MwAssetTagCertificate mwAssetTagCertificate) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(mwAssetTagCertificate);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public void edit(MwAssetTagCertificate mwAssetTagCertificate) throws NonexistentEntityException {
        EntityManager em = getEntityManager();        
        try {
            em.getTransaction().begin();
            em.merge(mwAssetTagCertificate);            
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            Integer id = mwAssetTagCertificate.getId();
            if (msg == null || msg.length() == 0) {
                if (id != null && findMwAssetTagCertificate(id) == null) {
                    throw new NonexistentEntityException("The mwAssetTagCertificate with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
                em.close();
        }
    }

    public void destroy(Integer id) throws NonexistentEntityException {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            MwAssetTagCertificate mwAssetTagCertificate;
            try {
                mwAssetTagCertificate = em.getReference(MwAssetTagCertificate.class, id);
                mwAssetTagCertificate.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The mwAssetTagCertificate with id " + id + " no longer exists.", enfe);
            }
            em.remove(mwAssetTagCertificate);
            em.getTransaction().commit();
        } finally {
                em.close();
        }
    }

    public List<MwAssetTagCertificate> findMwAssetTagCertificateEntities() {
        return findMwAssetTagCertificateEntities(true, -1, -1);
    }

    public List<MwAssetTagCertificate> findMwAssetTagCertificateEntities(int maxResults, int firstResult) {
        return findMwAssetTagCertificateEntities(false, maxResults, firstResult);
    }

    private List<MwAssetTagCertificate> findMwAssetTagCertificateEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(MwAssetTagCertificate.class));
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

    public MwAssetTagCertificate findMwAssetTagCertificate(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(MwAssetTagCertificate.class, id);
        } finally {
            em.close();
        }
    }

    public int getMwAssetTagCertificateCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<MwAssetTagCertificate> rt = cq.from(MwAssetTagCertificate.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
    public List<MwAssetTagCertificate> findAssetTagCertificateBySha1Hash(byte[] sha1Hash) {
        return searchByNamedQuery("findBySha1Hash", "sHA1Hash", sha1Hash);
    }
    
    public List<MwAssetTagCertificate> findAssetTagCertificateBySha256Hash(byte[] sha256Hash) {
        return searchByNamedQuery("findBySha256Hash", "sHA256Hash", sha256Hash);
    }
    
    public List<MwAssetTagCertificate> findAssetTagCertificatesByHostUUID(String uuid) {
        return searchByNamedQuery("findByUuid", "uuid", uuid);
    }

    public List<MwAssetTagCertificate> findAssetTagCertificatesByHostID(int hostID) {
        return searchByNamedQuery("findByHostID", "hostID", hostID);
    }

    public List<MwAssetTagCertificate> findAssetTagCertificatesByUuid(String uuid) {
        return searchByNamedQuery("findByUuidHex", "uuid_hex", uuid);
    }

}
