/*
 * Copyright (C) 2011-2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.jpa;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceProviderResolver;
import javax.persistence.spi.PersistenceProviderResolverHolder;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import org.apache.commons.dbcp.managed.BasicManagedDataSource;
import org.apache.commons.lang3.StringUtils;
import org.objectweb.jotm.Current;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This class closes the EntityManagerFactory when when the application stops.
 *
 * It embodies on the fact that we need only one static EntityManagerFactory per
 * PersistenceUnit 
 * for the lifecycle of the application. If each Business Object maintained its
 * own factory we would have to obtain each one and close it to avoid a
 * resource leak.
 *
 * Closing of the EntityManagerFactory is done from this class because its
 * tied to the shutdown of the servlet container and we don't want to tie the
 * BaseBO to the servlet container.
 *
 * This needs to go in web.xml:
 *   <listener>
 *       <listener-class>com.intel.mtwilson.util.jpa.PersistenceManager</listener-class>
 *   </listener>
 *   <context-param>
 *       <param-name>mtwilson-jpa-units</param-name>
 *       <param-value>ASDataPU,MSDataPU</param-value>
 *   </context-param>
 *
 * @author jbuhacoff
 */
public abstract class PersistenceManager implements ServletContextListener {
    private static final Logger log = LoggerFactory.getLogger(PersistenceManager.class);
    
    /**
     * This map contains one static EntityManagerFactory for each Persistence Unit
     * used in the application
     */
    private static final ConcurrentHashMap<String,EntityManagerFactory> factories = new ConcurrentHashMap<String,EntityManagerFactory>();

    public EntityManagerFactory getEntityManagerFactory(String persistenceUnitName) {
        log.debug("PersistenceManager is configured with {} factories in getEntityManagerFactory", factories.keySet().size());
        if( factories.keySet().isEmpty() ) {
            log.debug("PersistenceManager factories is empty, calling configure()");
            configure();
            for(String factoryName : factories.keySet()) {
                EntityManagerFactory factory = factories.get(factoryName);
                if( factory != null && factory.isOpen() ) {
                    log.debug("PersistenceManager is configured with factory {} in getEntityManagerFactory", factoryName);
                }
            }
        }
        
        if( factories.containsKey(persistenceUnitName) ) {
            return factories.get(persistenceUnitName);
        }
        throw new IllegalArgumentException("Cannot return EntityManagerFactory for unknown persistence unit: "+persistenceUnitName);
    }
    
    /**
     * Subclasses must implement this function and call addPersistenceUnit for
     * each persistence unit the application needs to use.
     * Here is an example implementation:
     * public void configure() {
     *   addPersistenceUnit("ASDataPU", ASConfig.getJpaProperties());
     *   addPersistenceUnit("MSDataPU", MSConfig.getJpaProperties());
     * }
     * Subclasses must call configure() from within their constructor.
     */
    public abstract void configure();
    
    public void addPersistenceUnit(String persistenceUnitName, Properties jpaProperties) {
        log.debug("PersistenceManager adding PersistenceUnit {}", persistenceUnitName);
        if( factories.containsKey(persistenceUnitName) ) {
            EntityManagerFactory factory = factories.get(persistenceUnitName);
            if( factory != null && factory.isOpen() ) {
                //factory.close(); // XXX TODO maybe instead of closing... since it's already there and open, just keep it
                return;
            }
        }
        EntityManagerFactory factory = createFactory(persistenceUnitName, jpaProperties);
        log.debug("Created EntityManagerFactory for persistence unit {}", persistenceUnitName);
        factories.put(persistenceUnitName, factory);
    }
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        log.debug("PersistenceManager initialized");
        
        // XXX can we get persistence unit names from web.xml to simplify configuration?
        Enumeration<String> attrs = sce.getServletContext().getAttributeNames();
        log.debug("Servlet Context path {}",sce.getServletContext().getContextPath()); // like /WLMService
        while(attrs.hasMoreElements()) {
            String attr = attrs.nextElement();
//            log.debug("Servlet Context attribute: {} = {}", new String[] { attr, sce.getServletContext().getAttribute(attr).toString() }); // attributes are not necessarily strings... some may be boolean or something else
            log.debug("Servlet Context attribute: {}", attr);             
        }
        Enumeration<String> initparams = sce.getServletContext().getInitParameterNames();
        while(initparams.hasMoreElements()) {
            String param = initparams.nextElement();
//            log.debug("Servlet Context init param: {} = {}", new String[] { param, sce.getServletContext().getInitParameter(param).toString() });
            log.debug("Servlet Context init param: {}",  param);
        }
        
        /*
        // close any factories that may already be open...
        for(String factoryName : factories.keySet()) {
            EntityManagerFactory factory = factories.get(factoryName);
            if( factory != null && factory.isOpen() ) {
                log.debug("PersistenceManager closing factory {} in contextInitialized", factoryName);
                factory.close();
            }
            factories.remove(factoryName);
        }
        // create factories according to the subclass implementation
        configure();
        */
//        System.out.println(String.format("PersistenceManager: Context initialized, EntityManagerFactory is %s", entityManagerFactory.isOpen() ? "open" : "closed"));
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        for(String factoryName : factories.keySet()) {
            EntityManagerFactory factory = factories.get(factoryName);
            if( factory != null && factory.isOpen() ) {
                log.debug("PersistenceManager closing factory {} in contextDestroyed", factoryName);
                factory.close();
            }
            factories.remove(factoryName);
        }
    }

    
    /**
     * 
     * @param persistenceUnitName as defined in the persistence.xml file, for example "ASDataPU" or "MSDataPU"
     * @param properties for initializing the persistence unit: javax.persistence.jdbc.driver, etc
     * 
     * The original form of this method called Persistence.createEntityManagerFactory(String,Map) to create
     * the EntityManagerFactory. However, the EclipseLink JPA implementation was not properly maintaining the 
     * database connection pool (removing stale connections, automatically creating new replacement connections)
     * and that was causing database connection errors to occur when the application is used, left idle for
     * some number of hours (dependent on database idle timeout setting) and then used again. 
     * 
     * The new form calls Persistence.createContainerEntityManagedFactory(String,Map) which allows us to
     * specify our own database connection pool (Apache DBCP using Commons Pool) but at the cost of having
     * to parse persistence.xml ourselves to get the JPA configuration.
     * @return 
     */
    public static EntityManagerFactory createFactory(String persistenceUnitName, Properties properties) {
//            EntityManagerFactory factory = Persistence.createEntityManagerFactory(persistenceUnitName,properties); // use provided JPA implementation (EclipseLink) which does not maintain the connection pool propertly
            EntityManagerFactory factory = createEntityManagerFactory(persistenceUnitName, properties); // create our own with Apache DBCP and Commons Pool
            return factory;
    }
    
    /**
     * The new form of this method.  The old form simply wraps this one now.
     * Loads persistence.xml from the classpath and creates an entity manager
     * with those settings, which can now include Apache Commons Pool and JDBC.
     * 
     * @param persistenceUnitName
     * @param properties
     * @return 
     */
    public static EntityManagerFactory createEntityManagerFactory(String persistenceUnitName, Properties jpaProperties) {
        log.debug("Loading database driver {} for persistence unit {}", new String[] { jpaProperties.getProperty("javax.persistence.jdbc.driver"), persistenceUnitName });
        try {
            Class.forName(jpaProperties.getProperty("javax.persistence.jdbc.driver"));
        } catch (ClassNotFoundException ex) {
            log.error("Cannot load JDBC Driver for persistence unit", ex);
        }
        
        PersistenceUnitInfo persistenceUnitInfo = null;
        try {
            persistenceUnitInfo = getPersistenceUnitInfo(persistenceUnitName, jpaProperties);
        }
        catch(IOException e) {
            throw new PersistenceException("Cannot load PersistenceUnit named "+persistenceUnitName, e);            
        }
        if( persistenceUnitInfo == null ) {
            throw new PersistenceException("Cannot find PersistenceUnit named "+persistenceUnitName);
        }

        EntityManagerFactory emf = null;
        PersistenceProviderResolver resolver = PersistenceProviderResolverHolder.getPersistenceProviderResolver();

        List<PersistenceProvider> providers = resolver.getPersistenceProviders();
        
        // check if we have the requested provider
        if( persistenceUnitInfo.getPersistenceProviderClassName() != null ) {
            log.debug("Looking for specific JPA provider: {}", persistenceUnitInfo.getPersistenceProviderClassName());
            for(PersistenceProvider provider : providers) {
                log.debug("Looking at provider: {}", provider.getClass().getName());
                if( provider.getClass().getName().equals(persistenceUnitInfo.getPersistenceProviderClassName()) ) {
                    emf = provider.createContainerEntityManagerFactory(persistenceUnitInfo, persistenceUnitInfo.getProperties()); // important: must use the properties as returned by the persistenceUnitInfo because it may have altered them... specifically:  remove user and password entries after creating datasource to force eclipselink to call getConnection() instead of getConnection(user,password)
                    if( emf != null ) {
                        log.debug("Found requested persistence provider");
                        return emf;
                    }
                }
            }
        }
        // check if any other provider can accomodate the persistence unit
        log.debug("Looking for any compatible JPA provider");
        for (PersistenceProvider provider : providers) {
            log.debug("Looking at provider: {}", provider.getClass().getName());
            emf = provider.createContainerEntityManagerFactory(persistenceUnitInfo, persistenceUnitInfo.getProperties()); // important: must use the properties as returned by the persistenceUnitInfo because it may have altered them... specifically:  remove user and password entries after creating datasource to force eclipselink to call getConnection() instead of getConnection(user,password)
            if (emf != null) {
                log.debug("Found compatible persistence provider");
                return emf;
            }
        }
        throw new PersistenceException("No Persistence provider for EntityManager named " + persistenceUnitName);
    }
    
    private static final HashMap<String,CustomPersistenceUnitInfoImpl> persistenceInfoMap = new HashMap<String,CustomPersistenceUnitInfoImpl>();
    
    /**
     * Read persistence.xml and create the PersistenceUnitInfo object for JPA.
     * This is necessary because the Persistence API has two modes: 
     * 1) the Provider (EclipseLink in this case) reads persistence.xml and 
     * configures based on that; but it's not implementing pools properly and
     * lacks any configuration settings to change its behavior
     * 2) the Container (GlassFish or Tomcat in this case) reads persistence.xml
     * or allows the operator to configure the persistence unit using some
     * other means
     * 
     * So for complete control we need to implement the PersistenceUnitInfo the
     * way the Container does, yet without using the Container itself so we 
     * can maintain portability. 
     * 
     * To maintain simplicity
     * we are keeping as much configuration in  persistence.xml as possible.
     * 
     * @param persistenceUnitName
     * @return 
     */
    public static PersistenceUnitInfo getPersistenceUnitInfo(String persistenceUnitName, Properties jpaProperties) throws IOException {
        if( !persistenceInfoMap.containsKey(persistenceUnitName) ) {
            loadPersistenceUnit(persistenceUnitName);
        }
        if( persistenceInfoMap.containsKey(persistenceUnitName) ) {
            CustomPersistenceUnitInfoImpl unitInfo = persistenceInfoMap.get(persistenceUnitName);
            if( unitInfo != null ) {
                if( unitInfo.ds == null ) {
                    log.debug("Found PersistenceUnit {}, creating DataSource", persistenceUnitName);
                    Properties copy = new Properties();
                    copy.putAll(jpaProperties);
                    unitInfo.jpaProperties = copy;
                    unitInfo.ds = createDataSource(copy);
                    // the Apache BasicDataSource does not support getConnection(username,password) so in order to force EclipseLink to use getConnection() we remove the username and password from the JPA properties
                    unitInfo.jpaProperties.remove("javax.persistence.jdbc.user");
                    unitInfo.jpaProperties.remove("javax.persistence.jdbc.password");
                }
                return unitInfo;
            }
        }
        throw new FileNotFoundException("Cannot find persistence.xml for "+persistenceUnitName);
    }
    /*
    private static void loadAllPersistenceUnits() throws IOException {
        ClassLoader cl = PersistenceManager.class.getClassLoader();
        log.debug("Loading all persistence.xml files in classpath using classloader: {}", cl.getClass().getName());
        ArrayList<URL> list = new ArrayList<URL>();
        list.addAll(getResources(cl, "META-INF/persistence.xml"));
        for(URL url : list) {
            CustomPersistenceUnitInfoImpl p = readPersistenceXml(url);
            persistenceInfoMap.put(p.getPersistenceUnitName(), p);
        }
    }*/
    private static void loadPersistenceUnit(String persistenceUnitName) throws IOException {
        ClassLoader cl = PersistenceManager.class.getClassLoader();
        log.debug("Loading persistence.xml for {} using classloader: {}", new String[] { persistenceUnitName,  cl.getClass().getName() });
        ArrayList<URL> list = new ArrayList<URL>();
        list.addAll(listResources(cl, String.format("META-INF/persistence-%s.xml", persistenceUnitName)));
        list.addAll(listResources(cl, String.format("META-INF/%s/persistence.xml", persistenceUnitName)));
        list.addAll(listResources(cl, String.format("META-INF/persistence.xml", persistenceUnitName)));
        for(URL url : list) {
            CustomPersistenceUnitInfoImpl p = readPersistenceXml(url);
            // only save the descriptor if we do not already have one loaded for that persistence unit
            if( !persistenceInfoMap.containsKey(p.getPersistenceUnitName()) ) {
                persistenceInfoMap.put(p.getPersistenceUnitName(), p);
            }
        }
    }
    
    private static List<URL> listResources(ClassLoader cl, String name) throws IOException {
        Enumeration<URL> urls = cl.getResources(name);
        List<URL> list = Collections.list(urls);
        return list;
    }
    
    private static List<String> listNodeValues(NodeList nodeset) {
        ArrayList<String> list = new ArrayList<String>();
        for(int i=0; i<nodeset.getLength(); i++) {
            list.add(nodeset.item(i).getNodeValue());
        }
        return list;
    }

    private static CustomPersistenceUnitInfoImpl readPersistenceXml(URL url) throws IOException {
        log.debug("Loading {}", url.toExternalForm());
        InputStream in = null;
        try {
            in = url.openStream();
            CustomPersistenceUnitInfoImpl p = readPersistenceXml(in);
            p.url = url;
            return p;
        }
        finally {
            if( in != null ) { in.close(); }
        }
    }
    
    private static CustomPersistenceUnitInfoImpl readPersistenceXml(InputStream in) throws IOException {
        try {
            
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(in);
            log.debug("document element tag name: "+document.getDocumentElement().getTagName());

            // create a persistence unit info object and fill it in with information we find in persistence.xml
            CustomPersistenceUnitInfoImpl p = new CustomPersistenceUnitInfoImpl();
            // XXX TODO prefer to use XPath or maybe commons-digest parser but right now it's not working
/*            
            XPathFactory  factory=XPathFactory.newInstance();
            XPath xPath=factory.newXPath();

//            XPathExpression  persistenceUnitXPath = xPath.compile("/persistence/persistence-unit[1]"); // XXX currently supporting only one persistence-unit per file
//            Node persistenceUnit = (Node)persistenceUnitXPath.evaluate(document.getDocumentElement(), XPathConstants.NODE);
            
            XPathExpression  unitNameXPath = xPath.compile("/persistence-unit/@name"); // don't need prefix /persistence/persistence-unit  because we're already lookint at that node when we evaluate
            XPathExpression  transactionTypeXPath = xPath.compile("/persistence-unit/@transaction-type");
            XPathExpression  providerXPath = xPath.compile("/persistence-unit/provider"); //journal/article[@date='January-2004']/title");
            XPathExpression  classListXPath = xPath.compile("/persistence-unit/class"); //journal/article[@date='January-2004']/title");
            
            String unitName = unitNameXPath.evaluate(document.getDocumentElement());
            String transactionType = transactionTypeXPath.evaluate(document.getDocumentElement());
            String provider = providerXPath.evaluate(document.getDocumentElement());
            NodeList classListNodeset = (NodeList) classListXPath.evaluate(document.getDocumentElement(), XPathConstants.NODESET);
            List<String> classList = listNodeValues(classListNodeset);
            */
            
            NodeList persistenceUnitNodes = document.getElementsByTagName("persistence-unit"); // XXX TODO there could be more than one in a file;  we may need to return a List<CustomPersistenceUnitInfoImpl> from this function instead of just one
            String unitName = persistenceUnitNodes.item(0).getAttributes().getNamedItem("name").getTextContent();
            String transactionType = persistenceUnitNodes.item(0).getAttributes().getNamedItem("transaction-type").getTextContent();
            
            String provider = document.getElementsByTagName("provider").item(0).getTextContent();
            
            NodeList classNodes = document.getElementsByTagName("class");
            ArrayList<String> classList = new ArrayList<String>();
            for(int i=0; i<classNodes.getLength(); i++) {
                classList.add(classNodes.item(i).getTextContent());
            }

            log.debug("Persistence Unit Name: "+unitName);
            log.debug("Transaction Type: "+transactionType);
            log.debug("Provider: "+provider);
            log.debug("Class List: "+StringUtils.join(classList, ", "));
            
            
            p.persistenceUnitName = unitName;
            p.persistenceUnitProvider = provider;
            p.transactionType = transactionType;
            p.classList = classList;
            
            return p;
        }
        catch(ParserConfigurationException e) {
            throw new IOException("Cannot initialize XML parser", e);
        }
        catch(SAXException e) {
            throw new IOException("Cannot parse XML document", e);
        }/*
        catch(XPathExpressionException e) {
            throw new IOException("Cannot initialize XPATH engine", e);
        }*/
        
    }
    /**
            p.jdbcDriver = jpaProperties.getProperty("javax.persistence.jdbc.driver");
            p.jdbcUrl = jpaProperties.getProperty("javax.persistence.jdbc.url");
            p.jdbcUsername = jpaProperties.getProperty("javax.persistence.jdbc.user");
            p.jdbcPassword = jpaProperties.getProperty("javax.persistence.jdbc.password");
    * 
    * @param jpaProperties
    * @return 
    */    
    private static DataSource createDataSource(Properties jpaProperties) {
        BasicManagedDataSource ds = new BasicManagedDataSource();
        Current tm = new Current();
        ds.setAccessToUnderlyingConnectionAllowed(true);
        ds.setConnectionInitSqls(Collections.EMPTY_LIST);
        ds.setDefaultAutoCommit(true);
//        ds.setDefaultCatalog("mw_as"); // not needed when using the url...
        ds.setDefaultReadOnly(false);
//        ds.setDefaultTransactionIsolation(0);
        ds.setDriverClassLoader(ClassLoader.getSystemClassLoader());
        ds.setDriverClassName(jpaProperties.getProperty("javax.persistence.jdbc.driver"));
        ds.setInitialSize(10);
        ds.setLogAbandoned(true);
//        ds.setLogWriter(null); // null disables logging; TODO: see if we can get a PrintWriter from slf4j... and for some reason calls createDataSource() whic hdoesn't make sense
//        ds.setLoginTimeout(30); // in seconds ;   not supported by basicdatasource... and for some reason calls createDataSource() whic hdoesn't make sense
        ds.setMaxActive(50); // max 50 active connections to database
        ds.setMaxIdle(10); // max 10 idle connections in the pool
        ds.setMaxOpenPreparedStatements(50); // no limit
        ds.setMaxWait(-1); // wait indefinitely for a new connection from the pool
        ds.setMinEvictableIdleTimeMillis(1000*60*30); // (milliseconds) connection may be idle up to 30 minutes before being evicted
        ds.setMinIdle(5); // min 5 idle connections in the pool
        ds.setNumTestsPerEvictionRun(10); // how many connections to test each time
        ds.setPassword(jpaProperties.getProperty("javax.persistence.jdbc.password"));
        ds.setPoolPreparedStatements(true);
        ds.setRemoveAbandoned(true);
        ds.setRemoveAbandonedTimeout(60*10); // (seconds) connection may be abandoned for up to 10 minutes before being removed
        ds.setTestOnBorrow(true);
        ds.setTestOnReturn(false);
        ds.setTestWhileIdle(true);
        ds.setTimeBetweenEvictionRunsMillis(1000*60); // (milliseconds) check which idle connections should be evicted once every minute
        ds.setUrl(jpaProperties.getProperty("javax.persistence.jdbc.url"));
        ds.setUsername(jpaProperties.getProperty("javax.persistence.jdbc.user"));
        ds.setValidationQuery("SELECT 1");
        ds.setValidationQueryTimeout(2); // (seconds) how long to wait on a result for the validation query before giving up
//        DataSourceConnectionFactory connectionFactory = new DataSourceConnectionFactory(dataSource, dbUsername, dbPassowrd);
//        PoolableConnectionFactory dbcpFactory = new PoolableConnectionFactory(connectionFactory, pool, validationQuery, validationQueryTimeoutSeconds, false, false);
//        poolingDataSource = new PoolingDataSource(pool);
        ds.setTransactionManager(tm);
        return ds;
    }
}
