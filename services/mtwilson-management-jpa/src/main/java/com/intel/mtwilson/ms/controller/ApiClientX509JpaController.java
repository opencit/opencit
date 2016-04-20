/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.ms.controller;

import com.intel.mtwilson.ms.controller.exceptions.IllegalOrphanException;
import com.intel.mtwilson.ms.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.ms.data.ApiClientX509;
import com.intel.mtwilson.ms.data.ApiRoleX509;
import com.intel.dcsg.cpg.jpa.GenericJpaController;
import com.intel.mtwilson.ms.controller.exceptions.MSDataException;
import java.io.Serializable;
import java.util.*;
import javax.persistence.*;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dsmagadx
 */
public class ApiClientX509JpaController extends GenericJpaController<ApiClientX509> implements Serializable {
    private Logger log = LoggerFactory.getLogger(getClass());

    public ApiClientX509JpaController(EntityManagerFactory emf) {
        super(ApiClientX509.class);
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    @Override
    public EntityManager getEntityManager() {
        EntityManager em = emf.createEntityManager();
        if( em == null ) { throw new IllegalStateException("Cannot obtain entity manager"); }
        return em;
    }

    public void create(ApiClientX509 apiClientX509) {
        if (apiClientX509.getApiRoleX509Collection() == null) {
            apiClientX509.setApiRoleX509Collection(new ArrayList<ApiRoleX509>());
        }
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            Collection<ApiRoleX509> attachedApiRoleX509Collection = new ArrayList<>();
            for (ApiRoleX509 apiRoleX509CollectionApiRoleX509ToAttach : apiClientX509.getApiRoleX509Collection()) {
                apiRoleX509CollectionApiRoleX509ToAttach = em.getReference(apiRoleX509CollectionApiRoleX509ToAttach.getClass(), apiRoleX509CollectionApiRoleX509ToAttach.getApiRoleX509PK());
                attachedApiRoleX509Collection.add(apiRoleX509CollectionApiRoleX509ToAttach);
            }
            apiClientX509.setApiRoleX509Collection(attachedApiRoleX509Collection);
            em.persist(apiClientX509);
            for (ApiRoleX509 apiRoleX509CollectionApiRoleX509 : apiClientX509.getApiRoleX509Collection()) {
                ApiClientX509 oldApiClientX509OfApiRoleX509CollectionApiRoleX509 = apiRoleX509CollectionApiRoleX509.getApiClientX509();
                apiRoleX509CollectionApiRoleX509.setApiClientX509(apiClientX509);
                apiRoleX509CollectionApiRoleX509 = em.merge(apiRoleX509CollectionApiRoleX509);
                if (oldApiClientX509OfApiRoleX509CollectionApiRoleX509 != null) {
                    oldApiClientX509OfApiRoleX509CollectionApiRoleX509.getApiRoleX509Collection().remove(apiRoleX509CollectionApiRoleX509);
                    em.merge(oldApiClientX509OfApiRoleX509CollectionApiRoleX509);
                }
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public void edit(ApiClientX509 apiClientX509) throws IllegalOrphanException, NonexistentEntityException, MSDataException {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            ApiClientX509 persistentApiClientX509 = em.find(ApiClientX509.class, apiClientX509.getId());
            Collection<ApiRoleX509> apiRoleX509CollectionOld = persistentApiClientX509.getApiRoleX509Collection();
            Collection<ApiRoleX509> apiRoleX509CollectionNew = apiClientX509.getApiRoleX509Collection();
            List<String> illegalOrphanMessages = null;
            for (ApiRoleX509 apiRoleX509CollectionOldApiRoleX509 : apiRoleX509CollectionOld) {
                if (!apiRoleX509CollectionNew.contains(apiRoleX509CollectionOldApiRoleX509)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain ApiRoleX509 " + apiRoleX509CollectionOldApiRoleX509 + " since its apiClientX509 field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Collection<ApiRoleX509> attachedApiRoleX509CollectionNew = new ArrayList<ApiRoleX509>();
            for (ApiRoleX509 apiRoleX509CollectionNewApiRoleX509ToAttach : apiRoleX509CollectionNew) {
                apiRoleX509CollectionNewApiRoleX509ToAttach = em.getReference(apiRoleX509CollectionNewApiRoleX509ToAttach.getClass(), apiRoleX509CollectionNewApiRoleX509ToAttach.getApiRoleX509PK());
                attachedApiRoleX509CollectionNew.add(apiRoleX509CollectionNewApiRoleX509ToAttach);
            }
            apiRoleX509CollectionNew = attachedApiRoleX509CollectionNew;
            apiClientX509.setApiRoleX509Collection(apiRoleX509CollectionNew);
            apiClientX509 = em.merge(apiClientX509);
            for (ApiRoleX509 apiRoleX509CollectionNewApiRoleX509 : apiRoleX509CollectionNew) {
                if (!apiRoleX509CollectionOld.contains(apiRoleX509CollectionNewApiRoleX509)) {
                    ApiClientX509 oldApiClientX509OfApiRoleX509CollectionNewApiRoleX509 = apiRoleX509CollectionNewApiRoleX509.getApiClientX509();
                    apiRoleX509CollectionNewApiRoleX509.setApiClientX509(apiClientX509);
                    apiRoleX509CollectionNewApiRoleX509 = em.merge(apiRoleX509CollectionNewApiRoleX509);
                    if (oldApiClientX509OfApiRoleX509CollectionNewApiRoleX509 != null && !oldApiClientX509OfApiRoleX509CollectionNewApiRoleX509.equals(apiClientX509)) {
                        oldApiClientX509OfApiRoleX509CollectionNewApiRoleX509.getApiRoleX509Collection().remove(apiRoleX509CollectionNewApiRoleX509);
                        em.merge(oldApiClientX509OfApiRoleX509CollectionNewApiRoleX509);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = apiClientX509.getId();
                if (findApiClientX509(id) == null) {
                    throw new NonexistentEntityException("The apiClientX509 with id " + id + " no longer exists.");
                }
            }
            throw new MSDataException(ex);
        } finally {
                em.close();
        }
    }

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            ApiClientX509 apiClientX509;
            try {
                apiClientX509 = em.getReference(ApiClientX509.class, id);
                apiClientX509.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The apiClientX509 with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Collection<ApiRoleX509> apiRoleX509CollectionOrphanCheck = apiClientX509.getApiRoleX509Collection();
            for (ApiRoleX509 apiRoleX509CollectionOrphanCheckApiRoleX509 : apiRoleX509CollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This ApiClientX509 (" + apiClientX509 + ") cannot be destroyed since the ApiRoleX509 " + apiRoleX509CollectionOrphanCheckApiRoleX509 + " in its apiRoleX509Collection field has a non-nullable apiClientX509 field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(apiClientX509);
            em.getTransaction().commit();
        } finally {
                em.close();
        }
    }

    public List<ApiClientX509> findApiClientX509Entities() {
        return findApiClientX509Entities(true, -1, -1);
    }

    public List<ApiClientX509> findApiClientX509Entities(int maxResults, int firstResult) {
        return findApiClientX509Entities(false, maxResults, firstResult);
    }

    private List<ApiClientX509> findApiClientX509Entities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(ApiClientX509.class));
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

    public ApiClientX509 findApiClientX509(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(ApiClientX509.class, id);
        } finally {
            em.close();
        }
    }

    public int getApiClientX509Count() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<ApiClientX509> rt = cq.from(ApiClientX509.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }

    /**
     * Added to facilitate the authentication filter. -jbuhacoff 20120621
     * @since 0.5.2
     * @param fingerprint
     * @return 
     */
    public ApiClientX509 findApiClientX509ByFingerprint(byte[] fingerprint) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<ApiClientX509> query = em.createNamedQuery("ApiClientX509.findByFingerprint", ApiClientX509.class);
            query.setParameter("fingerprint", fingerprint);
            List<ApiClientX509> list = query.getResultList();
            if( list != null && !list.isEmpty() ) {
                return list.get(0);
            }
        } finally {
            em.close();
        }
        return null;
    }
    
    //
    public ApiClientX509 findEnabledApiClientX509ByFingerprint(byte[] fingerprint) {
//        List<ApiClientX509> list = searchByNamedQuery("findByFingerprintEnabled", "fingerprint", fingerprint);
        HashMap<String,Object> parameters = new HashMap<String,Object>();
        parameters.put("fingerprint", fingerprint);
        parameters.put("enabled", true);
        List<ApiClientX509> list = searchByNamedQuery("findByFingerprintEnabled", parameters);
        
        if( list != null && !list.isEmpty() ) {
            return list.get(0);
        }
        return null;
    }
    
    public List<ApiClientX509> findApiClientX509ByEnabled(Boolean enabled) {
        return searchByNamedQuery("findByEnabled", "enabled", enabled);
    }
    public List<ApiClientX509> findApiClientX509ByExpiresAfter(Date expiresAfter) {
        return searchByNamedQuery("findByExpiresAfter", "expires", expiresAfter);
    }
    public List<ApiClientX509> findApiClientX509ByExpiresBefore(Date expiresBefore) {
        return searchByNamedQuery("findByExpiresBefore", "expires", expiresBefore);
    }
    public List<ApiClientX509> findApiClientX509ByIssuer(String issuer) {
        return searchByNamedQuery("findByIssuer", "issuer", issuer);
    }
    public List<ApiClientX509> findApiClientX509ByNameLike(String name) {
        return searchByNamedQuery("findByNameLike", "name", "%"+name+"%");
    }
    public List<ApiClientX509> findApiClientX509ByName(String name) {
        return searchByNamedQuery("findByName", "name", name);
    }
    public List<ApiClientX509> findApiClientX509BySerialNumber(Integer serialNumber) {
        return searchByNamedQuery("findBySerialNumber", "serialNumber", serialNumber);
    }
    public List<ApiClientX509> findApiClientX509ByStatus(String status) {
        return searchByNamedQuery("findByStatus", "status", status);
    }
    public List<ApiClientX509> findApiClientX509ByCommentLike(String comment) {
        return searchByNamedQuery("findByCommentLike", "comment", "%"+comment+"%");
    }
    public List<ApiClientX509> findApiClientX509ByEnabledStatus(Boolean enabled, String status) {
        HashMap<String,Object> parameters = new HashMap<String,Object>();
        parameters.put("enabled", enabled);
        parameters.put("status", status);
        return searchByNamedQuery("findByEnabledStatus", parameters);
    }
    
    public ApiClientX509 findApiClientX509ByUUID(String uuid_hex) {
        
        List<ApiClientX509> list = searchByNamedQuery("findByUuid", "uuid_hex", uuid_hex);
        if( list != null && !list.isEmpty() ) {
            return list.get(0);
        }
        return null;
        
    }

    public List<ApiClientX509> findApiClientX509ByUserUUID(String user_uuid_hex) {
        
        List<ApiClientX509> list = searchByNamedQuery("findByUserUuid", "user_uuid_hex", user_uuid_hex);
        return list;
        
    }
    
}
