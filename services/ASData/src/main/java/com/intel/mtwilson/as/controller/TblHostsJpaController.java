/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.as.controller;

import com.intel.mtwilson.as.controller.exceptions.ASDataException;
import com.intel.mtwilson.as.controller.exceptions.IllegalOrphanException;
import com.intel.mtwilson.as.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.as.data.TblHosts;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import com.intel.mtwilson.as.data.TblMle;
import com.intel.mtwilson.as.data.TblSamlAssertion;
import com.intel.mtwilson.crypto.Aes128;
import com.intel.mtwilson.crypto.CryptographyException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import org.eclipse.persistence.internal.jpa.EntityManagerImpl;
import org.eclipse.persistence.queries.DatabaseQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dsmagadX
 */
public class TblHostsJpaController implements Serializable {
    private Logger log = LoggerFactory.getLogger(getClass());

    private EntityManagerFactory emf = null;
    private Aes128 cipher = null;

    public TblHostsJpaController(EntityManagerFactory emf, byte[] dataEncryptionKey) throws CryptographyException {
        this.emf = emf;
        if( dataEncryptionKey == null ) {
            throw new IllegalArgumentException("Missing data encryption key: mtwilson.as.dek");
        }
        cipher = new Aes128(dataEncryptionKey);
    }
    
    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(TblHosts tblHosts) throws CryptographyException {
        log.debug("create tblHosts with policy {} and keystore length {}", tblHosts.getTlsPolicyName(), tblHosts.getTlsKeystore() == null ? "null" : tblHosts.getTlsKeystore().length);
        if (tblHosts.getTblSamlAssertionCollection() == null) {
            tblHosts.setTblSamlAssertionCollection(new ArrayList<TblSamlAssertion>());
        }
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            TblMle vmmMleId = tblHosts.getVmmMleId();
            if (vmmMleId != null) {
                vmmMleId = em.getReference(vmmMleId.getClass(), vmmMleId.getId());
                tblHosts.setVmmMleId(vmmMleId);
            }
            TblMle biosMleId = tblHosts.getBiosMleId();
            if (biosMleId != null) {
                biosMleId = em.getReference(biosMleId.getClass(), biosMleId.getId());
                tblHosts.setBiosMleId(biosMleId);
            }
            Collection<TblSamlAssertion> attachedTblSamlAssertionCollection = new ArrayList<TblSamlAssertion>();
            for (TblSamlAssertion tblSamlAssertionCollectionTblSamlAssertionToAttach : tblHosts.getTblSamlAssertionCollection()) {
                tblSamlAssertionCollectionTblSamlAssertionToAttach = em.getReference(tblSamlAssertionCollectionTblSamlAssertionToAttach.getClass(), tblSamlAssertionCollectionTblSamlAssertionToAttach.getId());
                attachedTblSamlAssertionCollection.add(tblSamlAssertionCollectionTblSamlAssertionToAttach);
            }
            tblHosts.setTblSamlAssertionCollection(attachedTblSamlAssertionCollection);
            
            // encrypt addon connection string, persist, then restore the plaintext
            String addOnConnectionString = tblHosts.getAddOnConnectionInfo();
            if( addOnConnectionString != null ) {
                log.debug("saving with encrypted connection string");
                tblHosts.setAddOnConnectionInfo(cipher.encryptString(addOnConnectionString));
                em.persist(tblHosts);
                tblHosts.setAddOnConnectionInfo(addOnConnectionString);
            }
            else {
                log.debug("saving without encrypting connection string");
                em.persist(tblHosts);
            }
            
            if (vmmMleId != null) {
                vmmMleId.getTblHostsCollection().add(tblHosts);
                em.merge(vmmMleId);
            }
            if (biosMleId != null) {
                biosMleId.getTblHostsCollection().add(tblHosts);
                em.merge(biosMleId);
            }
            for (TblSamlAssertion tblSamlAssertionCollectionTblSamlAssertion : tblHosts.getTblSamlAssertionCollection()) {
                TblHosts oldHostIdOfTblSamlAssertionCollectionTblSamlAssertion = tblSamlAssertionCollectionTblSamlAssertion.getHostId();
                tblSamlAssertionCollectionTblSamlAssertion.setHostId(tblHosts);
                tblSamlAssertionCollectionTblSamlAssertion = em.merge(tblSamlAssertionCollectionTblSamlAssertion);
                if (oldHostIdOfTblSamlAssertionCollectionTblSamlAssertion != null) {
                    oldHostIdOfTblSamlAssertionCollectionTblSamlAssertion.getTblSamlAssertionCollection().remove(tblSamlAssertionCollectionTblSamlAssertion);
                    em.merge(oldHostIdOfTblSamlAssertionCollectionTblSamlAssertion);
                }
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public void edit(TblHosts tblHosts) throws IllegalOrphanException, NonexistentEntityException, ASDataException {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            TblHosts persistentTblHosts = em.find(TblHosts.class, tblHosts.getId());
            TblMle vmmMleIdOld = persistentTblHosts.getVmmMleId();
            TblMle vmmMleIdNew = tblHosts.getVmmMleId();
            TblMle biosMleIdOld = persistentTblHosts.getBiosMleId();
            TblMle biosMleIdNew = tblHosts.getBiosMleId();
            Collection<TblSamlAssertion> tblSamlAssertionCollectionOld = persistentTblHosts.getTblSamlAssertionCollection();
            Collection<TblSamlAssertion> tblSamlAssertionCollectionNew = tblHosts.getTblSamlAssertionCollection();
            List<String> illegalOrphanMessages = null;
            for (TblSamlAssertion tblSamlAssertionCollectionOldTblSamlAssertion : tblSamlAssertionCollectionOld) {
                if (!tblSamlAssertionCollectionNew.contains(tblSamlAssertionCollectionOldTblSamlAssertion)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain TblSamlAssertion " + tblSamlAssertionCollectionOldTblSamlAssertion + " since its hostId field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (vmmMleIdNew != null) {
                vmmMleIdNew = em.getReference(vmmMleIdNew.getClass(), vmmMleIdNew.getId());
                tblHosts.setVmmMleId(vmmMleIdNew);
            }
            if (biosMleIdNew != null) {
                biosMleIdNew = em.getReference(biosMleIdNew.getClass(), biosMleIdNew.getId());
                tblHosts.setBiosMleId(biosMleIdNew);
            }
            Collection<TblSamlAssertion> attachedTblSamlAssertionCollectionNew = new ArrayList<TblSamlAssertion>();
            for (TblSamlAssertion tblSamlAssertionCollectionNewTblSamlAssertionToAttach : tblSamlAssertionCollectionNew) {
                tblSamlAssertionCollectionNewTblSamlAssertionToAttach = em.getReference(tblSamlAssertionCollectionNewTblSamlAssertionToAttach.getClass(), tblSamlAssertionCollectionNewTblSamlAssertionToAttach.getId());
                attachedTblSamlAssertionCollectionNew.add(tblSamlAssertionCollectionNewTblSamlAssertionToAttach);
            }
            tblSamlAssertionCollectionNew = attachedTblSamlAssertionCollectionNew;
            tblHosts.setTblSamlAssertionCollection(tblSamlAssertionCollectionNew);

            // encrypt addon connection string, persist, then restore the plaintext
            String addOnConnectionString = tblHosts.getAddOnConnectionInfo();
            if( addOnConnectionString != null ) {
                tblHosts.setAddOnConnectionInfo(cipher.encryptString(addOnConnectionString));
                tblHosts = em.merge(tblHosts);
                tblHosts.setAddOnConnectionInfo(addOnConnectionString);
            }
            else {
                tblHosts = em.merge(tblHosts);
            }
            
            if (vmmMleIdOld != null && !vmmMleIdOld.equals(vmmMleIdNew)) {
                vmmMleIdOld.getTblHostsCollection().remove(tblHosts);
                vmmMleIdOld = em.merge(vmmMleIdOld);
            }
            if (vmmMleIdNew != null && !vmmMleIdNew.equals(vmmMleIdOld)) {
                vmmMleIdNew.getTblHostsCollection().add(tblHosts);
                em.merge(vmmMleIdNew);
            }
            if (biosMleIdOld != null && !biosMleIdOld.equals(biosMleIdNew)) {
                biosMleIdOld.getTblHostsCollection().remove(tblHosts);
                biosMleIdOld = em.merge(biosMleIdOld);
            }
            if (biosMleIdNew != null && !biosMleIdNew.equals(biosMleIdOld)) {
                biosMleIdNew.getTblHostsCollection().add(tblHosts);
                em.merge(biosMleIdNew);
            }
            for (TblSamlAssertion tblSamlAssertionCollectionNewTblSamlAssertion : tblSamlAssertionCollectionNew) {
                if (!tblSamlAssertionCollectionOld.contains(tblSamlAssertionCollectionNewTblSamlAssertion)) {
                    TblHosts oldHostIdOfTblSamlAssertionCollectionNewTblSamlAssertion = tblSamlAssertionCollectionNewTblSamlAssertion.getHostId();
                    tblSamlAssertionCollectionNewTblSamlAssertion.setHostId(tblHosts);
                    tblSamlAssertionCollectionNewTblSamlAssertion = em.merge(tblSamlAssertionCollectionNewTblSamlAssertion);
                    if (oldHostIdOfTblSamlAssertionCollectionNewTblSamlAssertion != null && !oldHostIdOfTblSamlAssertionCollectionNewTblSamlAssertion.equals(tblHosts)) {
                        oldHostIdOfTblSamlAssertionCollectionNewTblSamlAssertion.getTblSamlAssertionCollection().remove(tblSamlAssertionCollectionNewTblSamlAssertion);
                        em.merge(oldHostIdOfTblSamlAssertionCollectionNewTblSamlAssertion);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = tblHosts.getId();
                if (findTblHosts(id) == null) {
                    throw new NonexistentEntityException("The tblHosts with id " + id + " no longer exists.");
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
            TblHosts tblHosts;
            try {
                tblHosts = em.getReference(TblHosts.class, id);
                tblHosts.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The tblHosts with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Collection<TblSamlAssertion> tblSamlAssertionCollectionOrphanCheck = tblHosts.getTblSamlAssertionCollection();
            for (TblSamlAssertion tblSamlAssertionCollectionOrphanCheckTblSamlAssertion : tblSamlAssertionCollectionOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This TblHosts (" + tblHosts + ") cannot be destroyed since the TblSamlAssertion " + tblSamlAssertionCollectionOrphanCheckTblSamlAssertion + " in its tblSamlAssertionCollection field has a non-nullable hostId field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            TblMle vmmMleId = tblHosts.getVmmMleId();
            if (vmmMleId != null) {
                vmmMleId.getTblHostsCollection().remove(tblHosts);
                em.merge(vmmMleId);
            }
            TblMle biosMleId = tblHosts.getBiosMleId();
            if (biosMleId != null) {
                biosMleId.getTblHostsCollection().remove(tblHosts);
                em.merge(biosMleId);
            }
            em.remove(tblHosts);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
    
    private void decryptStrings(List<TblHosts> list) {
        if( list != null ) {
            for(TblHosts record : list) {
                decryptStrings(record);
            }
        }
    }
    
    private void decryptStrings(TblHosts record) {
        if( record != null && record.getAddOnConnectionInfo() != null && !record.getAddOnConnectionInfo().isEmpty() && !record.getAddOnConnectionInfo().startsWith("http") ) {
            try {
                String decryptedAddOnConnectionInfo = cipher.decryptString(record.getAddOnConnectionInfo());
                record.setAddOnConnectionInfo(decryptedAddOnConnectionInfo);
            } catch (CryptographyException ex) {
               log.error("Cannot decrypt connection string for {}", record.getName()); // ignore the exception... we're either looking at a plaintext record that doesn't need ot be decrypted (saved from previous release) or we can't do anything about the error here anyway.
            }
        }
    }

    public List<TblHosts> findTblHostsEntities() {
        return findTblHostsEntities(true, -1, -1);
    }

    public List<TblHosts> findTblHostsEntities(int maxResults, int firstResult) {
        return findTblHostsEntities(false, maxResults, firstResult);
    }

    private List<TblHosts> findTblHostsEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(TblHosts.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            List<TblHosts> results = q.getResultList();
            decryptStrings(results);
            return results;
        } finally {
            em.close();
        }
    }

    public TblHosts findTblHosts(Integer id) {
        EntityManager em = getEntityManager();
        try {
            TblHosts result = em.find(TblHosts.class, id);
            decryptStrings(result);
            return result;
        } finally {
            em.close();
        }
    }

    public int getTblHostsCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<TblHosts> rt = cq.from(TblHosts.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }

    public TblHosts findByName(String name) {

        TblHosts host = null;
        EntityManager em = getEntityManager();
        try {

            Query query = em.createNamedQuery("TblHosts.findByName");

            query.setParameter("name", name);
          

            List<TblHosts> list = query.getResultList();

            if (list != null && list.size() > 0) {
                host = list.get(0);
                decryptStrings(host);
            }
        } finally {
                em.close();
        }

        return host;

    }
    
    public TblHosts findByAikSha1(String fingerprint) {

        TblHosts host = null;
        EntityManager em = getEntityManager();
        try {

            Query query = em.createNamedQuery("TblHosts.findByAikSha1");

            query.setParameter("aikSha1", fingerprint);
          

            List<TblHosts> list = query.getResultList();

            if (list != null && list.size() > 0) {
                host = list.get(0);
                decryptStrings(host);
            }
        } finally {
                em.close();
        }

        return host;

    }    

      public TblHosts findByIPAddress(String ipAddress) {

        TblHosts host = null;
        EntityManager em = getEntityManager();
        try {
            Query query = em.createNamedQuery("TblHosts.findByIPAddress");

            query.setParameter("iPAddress", ipAddress);
          

            List<TblHosts> list = query.getResultList();

            if (list != null && list.size() > 0) {
                host = list.get(0);
                decryptStrings(host);
            }
        } finally {
                em.close();
        }

        return host;

    }
    
    public List<TblHosts> findHostsByNameSearchCriteria(String searchCriteria) {
        List<TblHosts> hostList = null;
        EntityManager em = getEntityManager();
        
        try {           
            Query query = em.createNamedQuery("TblHosts.findByNameSearchCriteria");
            query.setParameter("search", "%"+searchCriteria+"%");
            
            if (query.getResultList() != null && !query.getResultList().isEmpty()) {
                 
                hostList = query.getResultList();
                decryptStrings(hostList);
            }
            
        } finally {
            em.close();
        }
        
        return hostList;      
    }

}
