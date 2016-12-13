/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.controller;

import com.intel.mtwilson.as.controller.exceptions.ASDataException;
import com.intel.mtwilson.as.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.as.data.TblPcrManifest;
import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
//import com.intel.mtwilson.as.data.TblDbPortalUser;
import com.intel.mtwilson.as.data.TblMle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import org.eclipse.persistence.config.CacheUsage;
import org.eclipse.persistence.config.HintValues;
import org.eclipse.persistence.config.QueryHints;

/**
 *
 * @author dsmagadx
 */
public class TblPcrManifestJpaController implements Serializable {
    private Logger log = LoggerFactory.getLogger(getClass());

    public TblPcrManifestJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        EntityManager em = emf.createEntityManager();
        return em;
    }

    public void create(TblPcrManifest tblPcrManifest) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            // @since 1.1 we are relying on the audit log for "created on", "created by", etc. type information
            /*
            TblDbPortalUser updatedBy = tblPcrManifest.getUpdatedBy();
            if (updatedBy != null) {
                updatedBy = em.getReference(updatedBy.getClass(), updatedBy.getId());
                tblPcrManifest.setUpdatedBy(updatedBy);
            }
            TblDbPortalUser createdBy = tblPcrManifest.getCreatedBy();
            if (createdBy != null) {
                createdBy = em.getReference(createdBy.getClass(), createdBy.getId());
                tblPcrManifest.setCreatedBy(createdBy);
            }*/
            TblMle mleId = tblPcrManifest.getMleId();
            if (mleId != null) {
                mleId = em.getReference(mleId.getClass(), mleId.getId());
                tblPcrManifest.setMleId(mleId);
            }
            em.persist(tblPcrManifest);
            // @since 1.1 we are relying on the audit log for "created on", "created by", etc. type information
            /*
            if (updatedBy != null) {
                updatedBy.getTblPcrManifestCollection().add(tblPcrManifest);
                em.merge(updatedBy);
            }
            if (createdBy != null) {
                createdBy.getTblPcrManifestCollection().add(tblPcrManifest);
                em.merge(createdBy);
            }*/
            if (mleId != null) {
                mleId.getTblPcrManifestCollection().add(tblPcrManifest);
                em.merge(mleId);
            }
            em.getTransaction().commit();
        } finally {
                em.close();
        }
    }

    public void create_v2(TblPcrManifest tblPcrManifest, EntityManager em) {
        try {
            TblMle mleId = tblPcrManifest.getMleId();
            if (mleId != null) {
                mleId = em.getReference(mleId.getClass(), mleId.getId());
                tblPcrManifest.setMleId(mleId);
            }
            em.persist(tblPcrManifest);

            if (mleId != null) {
                mleId.getTblPcrManifestCollection().add(tblPcrManifest);
                em.merge(mleId);
            }
        } finally {
        }
    }

    public void edit(TblPcrManifest tblPcrManifest) throws NonexistentEntityException, ASDataException {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            TblPcrManifest persistentTblPcrManifest = em.find(TblPcrManifest.class, tblPcrManifest.getId());
            // @since 1.1 we are relying on the audit log for "created on", "created by", etc. type information
            /*
            TblDbPortalUser updatedByOld = persistentTblPcrManifest.getUpdatedBy();
            TblDbPortalUser updatedByNew = tblPcrManifest.getUpdatedBy();
            TblDbPortalUser createdByOld = persistentTblPcrManifest.getCreatedBy();
            TblDbPortalUser createdByNew = tblPcrManifest.getCreatedBy();
            */
            TblMle mleIdOld = persistentTblPcrManifest.getMleId();
            TblMle mleIdNew = tblPcrManifest.getMleId();
            // @since 1.1 we are relying on the audit log for "created on", "created by", etc. type information
            /*
            if (updatedByNew != null) {
                updatedByNew = em.getReference(updatedByNew.getClass(), updatedByNew.getId());
                tblPcrManifest.setUpdatedBy(updatedByNew);
            }
            if (createdByNew != null) {
                createdByNew = em.getReference(createdByNew.getClass(), createdByNew.getId());
                tblPcrManifest.setCreatedBy(createdByNew);
            }*/
            if (mleIdNew != null) {
                mleIdNew = em.getReference(mleIdNew.getClass(), mleIdNew.getId());
                tblPcrManifest.setMleId(mleIdNew);
            }
            tblPcrManifest = em.merge(tblPcrManifest);
            // @since 1.1 we are relying on the audit log for "created on", "created by", etc. type information
            /*
            if (updatedByOld != null && !updatedByOld.equals(updatedByNew)) {
                updatedByOld.getTblPcrManifestCollection().remove(tblPcrManifest);
                updatedByOld = em.merge(updatedByOld);
            }
            if (updatedByNew != null && !updatedByNew.equals(updatedByOld)) {
                updatedByNew.getTblPcrManifestCollection().add(tblPcrManifest);
                em.merge(updatedByNew);
            }
            if (createdByOld != null && !createdByOld.equals(createdByNew)) {
                createdByOld.getTblPcrManifestCollection().remove(tblPcrManifest);
                createdByOld = em.merge(createdByOld);
            }
            if (createdByNew != null && !createdByNew.equals(createdByOld)) {
                createdByNew.getTblPcrManifestCollection().add(tblPcrManifest);
                em.merge(createdByNew);
            }
            */
            if (mleIdOld != null && !mleIdOld.equals(mleIdNew)) {
                mleIdOld.getTblPcrManifestCollection().remove(tblPcrManifest);
                mleIdOld = em.merge(mleIdOld);
            }
            if (mleIdNew != null && !mleIdNew.equals(mleIdOld)) {
                mleIdNew.getTblPcrManifestCollection().add(tblPcrManifest);
                em.merge(mleIdNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = tblPcrManifest.getId();
                if (findTblPcrManifest(id) == null) {
                    throw new NonexistentEntityException("The tblPcrManifest with id " + id + " no longer exists.");
                }
            }
            throw new ASDataException(ex);
        } finally {
                em.close();
        }
    }

    public void edit_v2(TblPcrManifest tblPcrManifest, EntityManager em) throws NonexistentEntityException, ASDataException {
        try {
            TblPcrManifest persistentTblPcrManifest = em.find(TblPcrManifest.class, tblPcrManifest.getId());

            TblMle mleIdOld = persistentTblPcrManifest.getMleId();
            TblMle mleIdNew = tblPcrManifest.getMleId();

            if (mleIdNew != null) {
                mleIdNew = em.getReference(mleIdNew.getClass(), mleIdNew.getId());
                tblPcrManifest.setMleId(mleIdNew);
            }
            tblPcrManifest = em.merge(tblPcrManifest);

            if (mleIdOld != null && !mleIdOld.equals(mleIdNew)) {
                mleIdOld.getTblPcrManifestCollection().remove(tblPcrManifest);
                mleIdOld = em.merge(mleIdOld);
            }
            if (mleIdNew != null && !mleIdNew.equals(mleIdOld)) {
                mleIdNew.getTblPcrManifestCollection().add(tblPcrManifest);
                em.merge(mleIdNew);
            }
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = tblPcrManifest.getId();
                if (findTblPcrManifest(id) == null) {
                    throw new NonexistentEntityException("The tblPcrManifest with id " + id + " no longer exists.");
                }
            }
            throw new ASDataException(ex);
        } finally {
        }
    }

    public void destroy(Integer id) throws NonexistentEntityException {
    	EntityManager em = getEntityManager();
    	try {
            em.getTransaction().begin();
            TblPcrManifest tblPcrManifest;
            try {
                tblPcrManifest = em.getReference(TblPcrManifest.class, id);
                tblPcrManifest.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The tblPcrManifest with id " + id + " no longer exists.", enfe);
            }
            // @since 1.1 we are relying on the audit log for "created on", "created by", etc. type information
            /*
            TblDbPortalUser updatedBy = tblPcrManifest.getUpdatedBy();
            if (updatedBy != null) {
                updatedBy.getTblPcrManifestCollection().remove(tblPcrManifest);
                em.merge(updatedBy);
            }
            TblDbPortalUser createdBy = tblPcrManifest.getCreatedBy();
            if (createdBy != null) {
                createdBy.getTblPcrManifestCollection().remove(tblPcrManifest);
                em.merge(createdBy);
            }
            */
            TblMle mleId = tblPcrManifest.getMleId();
            if (mleId != null) {
                mleId.getTblPcrManifestCollection().remove(tblPcrManifest);
                em.merge(mleId);
            }
            em.remove(tblPcrManifest);
            em.getTransaction().commit();
    	}
    	finally {
            em.close();        		
    	}
    }

    public List<TblPcrManifest> findTblPcrManifestEntities() {
        return findTblPcrManifestEntities(true, -1, -1);
    }

    public List<TblPcrManifest> findTblPcrManifestEntities(int maxResults, int firstResult) {
        return findTblPcrManifestEntities(false, maxResults, firstResult);
    }

    private List<TblPcrManifest> findTblPcrManifestEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(TblPcrManifest.class));
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

    public TblPcrManifest findTblPcrManifest(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(TblPcrManifest.class, id);
        } finally {
            em.close();
        }
    }

    public int getTblPcrManifestCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<TblPcrManifest> rt = cq.from(TblPcrManifest.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }

    public TblPcrManifest findPcrManifestById(Integer id) {
        EntityManager em = getEntityManager();
        try {

            Query query = em.createNamedQuery("TblPcrManifest.findById");
            query.setParameter("id", id);

            query.setHint(QueryHints.REFRESH, HintValues.TRUE);
            query.setHint(QueryHints.CACHE_USAGE, CacheUsage.DoNotCheckCache);

            TblPcrManifest manifest = (TblPcrManifest) query.getSingleResult();
            return manifest;

        } finally {
            em.close();
        }
    }
    

    /**
     * Added By: Sudhir on June 20, 2012
     * 
     * This method checks if the specified pcr entry already exists for the MLE.
     * @param id: Identity of the MLE
     * @param pcrName: Name of the PCR
     * @return : Single row result if there is a match or else null.
     */
    public List<TblPcrManifest> findByMleIdName(Integer id, String pcrName) {
        EntityManager em = getEntityManager();
        try {

            Query query = em.createNamedQuery("TblPcrManifest.findByMleIdName");
            query.setParameter("mleId", id);
            query.setParameter("name", pcrName);

            query.setHint(QueryHints.REFRESH, HintValues.TRUE);
            query.setHint(QueryHints.CACHE_USAGE, CacheUsage.DoNotCheckCache);

                        
            return (List<TblPcrManifest>) query.getResultList();            

        } catch(NoResultException e){
        	log.error(String.format("PCR Manifest for MLE %d PCR#  not found in Database ", id, pcrName));
        	return null;
        } finally {
            em.close();
        }               
    }        
    
    public TblPcrManifest findByMleIdNamePcrBank(Integer mle_id, String pcrName, String pcrBank) {
        EntityManager em = getEntityManager();
        try {
           Query query = em.createNamedQuery("TblPcrManifest.findByMleIdNamePcrBank");
           query.setParameter("mleId", mle_id);
           query.setParameter("name", pcrName);
           query.setParameter("pcrBank", pcrBank);
           
            query.setHint(QueryHints.REFRESH, HintValues.TRUE);
            query.setHint(QueryHints.CACHE_USAGE, CacheUsage.DoNotCheckCache);
            
            return (TblPcrManifest)query.getSingleResult();
        } catch(NoResultException e) {
            log.error(String.format("PCR Manifest for MLE %d %s:PCR# %s not found in Database ", mle_id, pcrBank, pcrName));
            return null;
        } finally {
            em.close();
        }
    }

    public List<TblPcrManifest> findTblPcrManifestByMleUuid(String mleUuid) {
        
        EntityManager em = getEntityManager();
        try {

            Query query = em.createNamedQuery("TblPcrManifest.findByMleUuidHex");
            query.setParameter("mle_uuid_hex", mleUuid);

            List<TblPcrManifest> pcrList = query.getResultList();
            return pcrList;

        } catch(NoResultException e){
        	log.error(String.format("MLE information with UUID {} not found in the DB.", mleUuid));
        	return null;
        } finally {
            em.close();
        }               
    }    


    public TblPcrManifest findTblPcrManifestByUuid(String uuid) {
        
        EntityManager em = getEntityManager();
        try {

            Query query = em.createNamedQuery("TblPcrManifest.findByUuidHex");
            query.setParameter("uuid_hex", uuid);

            TblPcrManifest pcrObj = (TblPcrManifest) query.getSingleResult();
            return pcrObj;

        } catch(NoResultException e){
        	log.error(String.format("PCR information with UUID {} not found in the DB.", uuid));
        	return null;
        } finally {
            em.close();
        }               
    }    

    public List<TblPcrManifest> findTblPcrManifestByPcrName(String pcrName) {
        
        EntityManager em = getEntityManager();
        try {

            Query query = em.createNamedQuery("TblPcrManifest.findByName");
            query.setParameter("name", pcrName);

            List<TblPcrManifest> pcrList = query.getResultList();
            return pcrList;

        } catch(NoResultException e){
        	log.error(String.format("PCR information with name {} not found in the DB.", pcrName));
        	return null;
        } finally {
            em.close();
        }               
    }    

    public List<TblPcrManifest> findByPcrValue(String pcrValue) {
        
        EntityManager em = getEntityManager();
        try {

            Query query = em.createNamedQuery("TblPcrManifest.findByValue");
            query.setParameter("value", pcrValue);

            List<TblPcrManifest> pcrList = query.getResultList();
            return pcrList;

        } catch(NoResultException e){
        	log.error(String.format("PCR information with value {} not found in the DB.", pcrValue));
        	return null;
        } finally {
            em.close();
        }               
    }    
}
