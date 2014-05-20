/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.user.management.client.jaxrs;

import com.intel.mtwilson.client.jaxrs.common.MtWilsonClient;
import com.intel.mtwilson.user.management.rest.v2.model.RegisterUserWithCertificate;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Properties;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegisterUsers extends MtWilsonClient {
    
    Logger log = LoggerFactory.getLogger(getClass().getName());

    public RegisterUsers(URL url) throws Exception{
        super(url);
    }

    public RegisterUsers(Properties properties) throws Exception {
        super(properties);
    }
    
     /**
     * @param 
     * @return 
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions 
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8443/mtwilson/v2/rpc/register-hosts
     * Input: 
     * Output: 
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     * </pre>
     */
    public boolean registerUserWithCertificate(RegisterUserWithCertificate obj) {
        boolean isUserRegistered = false;
        log.debug("target: {}", getTarget().getUri().toString());
        Object result = getTarget().path("rpc/register-user-with-certificate").request().accept(MediaType.APPLICATION_JSON).post(Entity.json(obj), Object.class);
        if (result.getClass().equals(LinkedHashMap.class)) {
            LinkedHashMap resultMap = (LinkedHashMap)result;
            if (resultMap.containsKey("result")) {
                isUserRegistered = (boolean) resultMap.get("result");
                log.debug("Result of user registration with certificate is {}.", isUserRegistered);
            }
        }
        return isUserRegistered;
    }
    
}
