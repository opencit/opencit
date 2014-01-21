/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.controller;

import com.intel.mtwilson.as.controller.exceptions.ASDataException;
import com.intel.mtwilson.as.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.as.data.TblOem;
import java.io.Serializable;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 *
 * @author dsmagadx
 */
public class TblOemJpaController implements Serializable {
    private Logger log = LoggerFactory.getLogger(getClass());

    public TblOemJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(TblOem tblOem) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(tblOem);
            em.getTransaction().commit();
        } finally {
                em.close();
        }
    }

    public void edit(TblOem tblOem) throws NonexistentEntityException, ASDataException {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(tblOem);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = tblOem.getId();
                if (findTblOem(id) == null) {
                    throw new NonexistentEntityException("The tblOem with id " + id + " no longer exists.");
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
            TblOem tblOem;
            try {
                tblOem = em.getReference(TblOem.class, id);
                tblOem.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The tblOem with id " + id + " no longer exists.", enfe);
            }
            em.remove(tblOem);
            em.getTransaction().commit();
        } finally {
                em.close();
        }
    }

    public List<TblOem> findTblOemEntities() {
        return findTblOemEntities(true, -1, -1);
    }

    public List<TblOem> findTblOemEntities(int maxResults, int firstResult) {
        return findTblOemEntities(false, maxResults, firstResult);
    }

    private List<TblOem> findTblOemEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(TblOem.class));
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

    public TblOem findTblOem(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(TblOem.class, id);
        } finally {
            em.close();
        }
    }

    public int getTblOemCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<TblOem> rt = cq.from(TblOem.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }

    public TblOem findTblOemByName(String name) {
        EntityManager em = getEntityManager();
        try {
            Query query = em.createNamedQuery("TblOem.findByName");
            query.setParameter("name", name);
            TblOem tblOem = (TblOem) query.getSingleResult();
            return tblOem;

        } catch(NoResultException e){
            log.error( "NoResultException : OEM [{}] not found", name);
            return null;
        }finally {
            em.close();
        }
    }
    
    public List<TblOem> findTblOemByNameLike(String name) {
        EntityManager em = getEntityManager();
        try {
            Query query = em.createNamedQuery("TblOem.findByNameLike");
            query.setParameter("name", "%" + name + "%");
            List<TblOem> resultList = query.getResultList();
            return resultList;

        } catch(NoResultException e){
            log.error( "NoResultException : OEMs matching {} not found", name);
            return null;
        }finally {
            em.close();
        }
    }

    public TblOem findTblOemByUUID(String uuid_hex) {
        
        EntityManager em = getEntityManager();
        try {
            Query query = em.createNamedQuery("TblOem.findByUUID_Hex");
            query.setParameter("uuid_hex", uuid_hex);
            TblOem tblOem = (TblOem) query.getSingleResult();
            return tblOem;

        } catch(NoResultException e){
            log.error( "NoResultException : OEM with UUID {} not found", uuid_hex);
            return null;
        }finally {
            em.close();
        }        
    }
    
}
