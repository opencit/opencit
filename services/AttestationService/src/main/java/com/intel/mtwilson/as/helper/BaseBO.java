package com.intel.mtwilson.as.helper;

import com.intel.mtwilson.jpa.PersistenceManager;
import javax.persistence.EntityManagerFactory;

/**
 * Modified to move the EntityManagerFactory creation to the PersistenceManager
 * class, which also listens for application shutdown and then properly closes
 * the EntityManagerFactory. This prevents JPA errors when re-deploying the application
 * to a running web server multiple times.
 * 
 * @author dsmagadx
 */
public class BaseBO {
    
    private PersistenceManager persistenceManager = new ASPersistenceManager();
    
    public BaseBO(){
        this(new ASPersistenceManager());
    }
    public BaseBO(PersistenceManager pm) {
        this.persistenceManager = pm;
    }

    public EntityManagerFactory getEntityManagerFactory() {
        return persistenceManager.getEntityManagerFactory("ASDataPU"); // see ASPersistenceManager
    }


    
}
