package com.intel.mtwilson.ms.business;


//import java.util.logging.Logger;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Copy of BaseBO with "ManagementDataPU" as the persistence unit name, for
 * use with the new management data objects.
 * 
 * @since 0.5.2
 * @author dsmagadx
 */
public class ManagementBaseBO {
    private static Logger log = LoggerFactory.getLogger(ManagementBaseBO.class);

    private static final EntityManagerFactory entityManagerFactory; // one static factory for all the Business Object classes during the application lifecycle

    static {
        Properties prop = new Properties();
        prop.put("javax.persistence.jdbc.driver", "com.mysql.jdbc.Driver");
        prop.put("javax.persistence.jdbc.url" ,String.format("jdbc:mysql://%s:3306/%s",
                    "127.0.0.1", //10.1.71.90",
                    "mw_as"));
        prop.put("javax.persistence.jdbc.user" ,"root");
        prop.put("javax.persistence.jdbc.password", "password");
        System.out.println(String.format("BaseBO+ASConfig: using driver(%s) url(%s) user(%s)", 
                prop.getProperty("javax.persistence.jdbc.driver"),
                prop.getProperty("javax.persistence.jdbc.url"),
                prop.getProperty("javax.persistence.jdbc.user")
                ));

        try {
            System.out.println("Loading database driver "+prop.getProperty("javax.persistence.jdbc.driver"));
            Class.forName(prop.getProperty("javax.persistence.jdbc.driver"));
        } catch (ClassNotFoundException ex) {
            log.error("Cannot load database driver", ex);
        }
        entityManagerFactory = Persistence.createEntityManagerFactory("MSDataPU",prop);
    }
    
    public ManagementBaseBO(){
        //log.log(Level.INFO, "Database Properties :{0}", prop.toString());
//        entityManagerFactory = Persistence.createEntityManagerFactory("ASDataPU",prop);
    }

    public EntityManagerFactory getEntityManagerFactory() {
//        return PersistenceManager.getEntityManagerFactory(); // already wired for "ASDataPU"
        return entityManagerFactory;
    }
    
}
