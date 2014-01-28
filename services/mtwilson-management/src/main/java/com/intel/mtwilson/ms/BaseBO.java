package com.intel.mtwilson.ms;


//import java.util.logging.Logger;
import com.intel.dcsg.cpg.jpa.PersistenceManager;
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
    
    private MSPersistenceManager persistenceManager = new MSPersistenceManager();
    
    public BaseBO(){
    }

    public EntityManagerFactory getMSEntityManagerFactory() {
        return persistenceManager.getEntityManagerFactory("MSDataPU"); // see MSPersistenceManager
    }
    
    public EntityManagerFactory getASEntityManagerFactory() {
        return persistenceManager.getEntityManagerFactory("ASDataPU"); // see MSPersistenceManager
    }
    
    
}
