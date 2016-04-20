/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.ms.controller;

import com.intel.mtwilson.ms.controller.exceptions.MSDataException;
import com.intel.mtwilson.ms.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.ms.controller.exceptions.PreexistingEntityException;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import com.intel.mtwilson.ms.data.ApiClientX509;
import com.intel.mtwilson.ms.data.ApiRoleX509;
import com.intel.mtwilson.ms.data.ApiRoleX509PK;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author dsmagadx
 */
public class ApiRoleX509JpaController implements Serializable {

    public ApiRoleX509JpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(ApiRoleX509 apiRoleX509) throws PreexistingEntityException, MSDataException {
        if (apiRoleX509.getApiRoleX509PK() == null) {
            apiRoleX509.setApiRoleX509PK(new ApiRoleX509PK());
        }
        apiRoleX509.getApiRoleX509PK().setApiclientx509ID(apiRoleX509.getApiClientX509().getId());
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            ApiClientX509 apiClientX509 = apiRoleX509.getApiClientX509();
            if (apiClientX509 != null) {
                apiClientX509 = em.getReference(apiClientX509.getClass(), apiClientX509.getId());
                apiRoleX509.setApiClientX509(apiClientX509);
            }
            em.persist(apiRoleX509);
            if (apiClientX509 != null) {
                apiClientX509.getApiRoleX509Collection().add(apiRoleX509);
                em.merge(apiClientX509);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findApiRoleX509(apiRoleX509.getApiRoleX509PK()) != null) {
                throw new PreexistingEntityException("ApiRoleX509 " + apiRoleX509 + " already exists.", ex);
            }
            throw new MSDataException("Error in ApiRoleX509JpaController.create",ex);
        } finally {
                em.close();
        }
    }

    public void edit(ApiRoleX509 apiRoleX509) throws NonexistentEntityException, MSDataException {
        apiRoleX509.getApiRoleX509PK().setApiclientx509ID(apiRoleX509.getApiClientX509().getId());
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            ApiRoleX509 persistentApiRoleX509 = em.find(ApiRoleX509.class, apiRoleX509.getApiRoleX509PK());
            ApiClientX509 apiClientX509Old = persistentApiRoleX509.getApiClientX509();
            ApiClientX509 apiClientX509New = apiRoleX509.getApiClientX509();
            if (apiClientX509New != null) {
                apiClientX509New = em.getReference(apiClientX509New.getClass(), apiClientX509New.getId());
                apiRoleX509.setApiClientX509(apiClientX509New);
            }
            apiRoleX509 = em.merge(apiRoleX509);
            if (apiClientX509Old != null && !apiClientX509Old.equals(apiClientX509New)) {
                apiClientX509Old.getApiRoleX509Collection().remove(apiRoleX509);
                apiClientX509Old = em.merge(apiClientX509Old);
            }
            if (apiClientX509New != null && !apiClientX509New.equals(apiClientX509Old)) {
                apiClientX509New.getApiRoleX509Collection().add(apiRoleX509);
                em.merge(apiClientX509New);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                ApiRoleX509PK id = apiRoleX509.getApiRoleX509PK();
                if (findApiRoleX509(id) == null) {
                    throw new NonexistentEntityException("The apiRoleX509 with id " + id + " no longer exists.");
                }
            }
            throw new MSDataException("Error in ApiRoleX509JpaController.edit",ex);
        } finally {
                em.close();
        }
    }

    public void destroy(ApiRoleX509PK id) throws NonexistentEntityException {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            ApiRoleX509 apiRoleX509;
            try {
                apiRoleX509 = em.getReference(ApiRoleX509.class, id);
                apiRoleX509.getApiRoleX509PK();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The apiRoleX509 with id " + id + " no longer exists.", enfe);
            }
            ApiClientX509 apiClientX509 = apiRoleX509.getApiClientX509();
            if (apiClientX509 != null) {
                apiClientX509.getApiRoleX509Collection().remove(apiRoleX509);
                em.merge(apiClientX509);
            }
            em.remove(apiRoleX509);
            em.getTransaction().commit();
        } finally {
                em.close();
        }
    }

    public List<ApiRoleX509> findApiRoleX509Entities() {
        return findApiRoleX509Entities(true, -1, -1);
    }

    public List<ApiRoleX509> findApiRoleX509Entities(int maxResults, int firstResult) {
        return findApiRoleX509Entities(false, maxResults, firstResult);
    }

    private List<ApiRoleX509> findApiRoleX509Entities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(ApiRoleX509.class));
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

    public ApiRoleX509 findApiRoleX509(ApiRoleX509PK id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(ApiRoleX509.class, id);
        } finally {
            em.close();
        }
    }

    public int getApiRoleX509Count() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<ApiRoleX509> rt = cq.from(ApiRoleX509.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
