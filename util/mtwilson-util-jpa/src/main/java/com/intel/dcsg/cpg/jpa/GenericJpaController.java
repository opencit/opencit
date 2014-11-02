/*
 * Copyright (C) 2012 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.jpa;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic JPA Controller with convenience methods that simplify implementation
 * of named queries when customizing generated controller.
 * 
 * This class abstracts common functionality that was previously duplicated
 * across various JPA Controllers.
 * 
 * @author jbuhacoff
 * @since 0.5.4
 */
public abstract class GenericJpaController<T> {
    private Logger log = LoggerFactory.getLogger(getClass());
    private Class entityClass;
    
    protected GenericJpaController(Class<T> entityClass) {
        this.entityClass = entityClass;
        log.debug("JpaController with entityClass {}", entityClass.getName());
    }
    
    public abstract EntityManager getEntityManager();
    
   /**
     * Added to facilitate the management application.
     * Each parameter can be Integer, Boolean, Date, Calendar, or String.
     * Date parameters are set as TemporalType.DATE
     * Calendar parameters are set as TemporalType.TIMESTAMP
     * You can pass java.sql.Date, java.sql.Time, or java.sql.Timestamp to 
     * ensure the correct interpretation of your Date parameter.
     * @param queryName from the entity bean NamedQuery annotations 
     * @param parameters to provide to the query, must be the correct number
     * @return list of entities, or empty list if none were found
     * @since 0.5.4
     */
    protected List<T> searchByNamedQuery(String queryName, Map<String,Object> parameters) {
        log.debug("Named query {} with {} parameters",  queryName, String.valueOf(parameters.keySet().size()));
        EntityManager em = getEntityManager();
        try {
            TypedQuery<T> query = em.createNamedQuery(entityClass.getSimpleName()+"."+queryName, entityClass);
            for(String variableName : parameters.keySet()) {
                Object variableValue = parameters.get(variableName);
                log.debug("Named query: {} Variable: {} Value: {}", queryName, variableName, variableValue.toString());
                setQueryParameter(query, variableName, variableValue);
            }
            List<T> list = query.getResultList();
            if( list != null && !list.isEmpty() ) {
                return list;
            }
        } finally {
            em.close();
        }
        return Collections.EMPTY_LIST;        
    }

    /**
     * TODO: support time instants from JodaTime or javax.time and set them as a Date or Calendar with TemporalType.Time 
     * TODO: support a way to specify that a date should be interpreted as TemporalType.DateTime ... maybe by looking for javax.sql.DateTime types??    
     * @param query
     * @param parameterName
     * @param parameterValue 
     */
    private void setQueryParameter(TypedQuery<T> query, String parameterName, Object parameterValue) {
        if( parameterValue instanceof java.util.Date ) {
            if( parameterValue instanceof java.sql.Time  ) {
                query.setParameter(parameterName, (java.util.Date)parameterValue, TemporalType.TIME);                    
            }
            else if( parameterValue instanceof java.sql.Timestamp  ) {
                query.setParameter(parameterName, (java.util.Date)parameterValue, TemporalType.TIMESTAMP);                    
            }
//            //unnecessary: klocwork 76
//            else if( parameterValue instanceof java.sql.Date ) {
//                query.setParameter(parameterName, (java.util.Date)parameterValue, TemporalType.DATE);                    
//            }
            else {
                query.setParameter(parameterName, (java.util.Date)parameterValue, TemporalType.DATE);                   
            }
        }
        else if( parameterValue instanceof Calendar ) {
            query.setParameter(parameterName, (Calendar)parameterValue, TemporalType.TIMESTAMP);                    
        }
        else {
            query.setParameter(parameterName, parameterValue);
        }        
    }
    
    /**
     * Convenience method for a 1-parameter named query.
     * Added to facilitate implementation of named queries in generated
     * JPA controllers. 
     * @param queryName
     * @param variableName
     * @param variableValue
     * @return 
     * @since 0.5.4
     */
    protected List<T> searchByNamedQuery(String queryName, String variableName, Object variableValue) {
        HashMap<String,Object> parameters = new HashMap<String,Object>();
        parameters.put(variableName, variableValue);
        return searchByNamedQuery(queryName, parameters);
    }

    /**
     * Convenience method for a 0-parameter named query.
     * Added to facilitate implementation of named queries in generated
     * JPA controllers.
     * @param queryName
     * @return 
     * @since 0.5.4
     */
    protected List<T> searchByNamedQuery(String queryName) {
        HashMap<String,Object> parameters = new HashMap<String,Object>();
        return searchByNamedQuery(queryName, parameters);
    }
    
}
