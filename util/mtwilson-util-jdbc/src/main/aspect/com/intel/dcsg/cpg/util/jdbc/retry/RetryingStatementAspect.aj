package com.intel.dcsg.cpg.util.jdbc.retry;

import java.lang.reflect.Method;
import org.aspectj.lang.reflect.MethodSignature;
import org.aspectj.lang.Signature;
import java.lang.annotation.Annotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.sql.SQLException;

public aspect RetryingStatementAspect {
    private Logger log = LoggerFactory.getLogger(RetryingStatementAspect.class);

    /*
     * Identify all public methods in RetryingStatement that throw SQLException.
     */
    pointcut tryStatementMethod() :
        execution(public * *.*(..) throws SQLException) && this(com.intel.dcsg.cpg.util.jdbc.retry.RetryingStatement);

    /*
     * This advice applies to public methods in RetryingStatement that throw SQLException.
     * It wraps their contents with a try/catch block, and if the exception caught
     * looks like a temporary connection failure then it automatically closes the
     * delegate connection, opens a new connection from the datasource, and tries
     * the same method again.
     * This is a bit of a simplistic approach because there are methods that are 
     * dependent on state but if we retry and it fails due to bad state the next
     * exception will not be the same as the connection failure and then we will
     * rethrow it. 
     */
    Object around() throws SQLException: tryStatementMethod()  {
        final Object[] args = thisJoinPoint.getArgs();
        log.debug("hey we are in the statement round advice!!  args: {}", args);
        try {
          return proceed();
        }
        catch(SQLException e) {
          log.debug("statement advice caught exception!", e);
          throw e;
        }
    }

}
