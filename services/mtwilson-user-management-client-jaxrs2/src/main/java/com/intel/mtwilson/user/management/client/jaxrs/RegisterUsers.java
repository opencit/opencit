/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.user.management.client.jaxrs;

import com.intel.mtwilson.jaxrs2.client.MtWilsonClient;
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
      * This is a helper function that allows the administrator to create/register new users with
      * certificate based mechanism for logging into the system. This function is basically a 
      * combination of createUser and createUserLoginCertificate functions.
     * @param RegisterUserWithCertificate object with the details of the user and the certificate that
     * has to be associated to the user.
     * @return boolean indicating whether the request was successful or not.
     * @since Mt.Wilson 2.0
     * @mtwRequiresPermissions users:create,user_login_certificates:create
     * @mtwContentTypeReturned JSON/XML/YAML
     * @mtwMethodType POST
     * @mtwSampleRestCall
     * <pre>
     * https://server.com:8181/mtwilson/v2/rpc/register-user-with-certificate
     * Input: {"user":{"username":"superadmin99","locale":"en_US","comment":"Need to manage user accounts."},
     * "user_login_certificate":{"certificate":"MIICrjCCAZagAwIBAgIIEGqMm0g6T4YwDQYJKoZIhvcNAQELBQAwFzEVMBMGA1UEAxMMc3VwZXJhZG1pbjk5MB4XDTE0MDUyODA1NTA1OFoXDTE1M
     * DUyODA1NTA1OFowFzEVMBMGA1UEAxMMc3VwZXJhZG1pbjk5MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAu7WANgGPK651vxXjuNvqjwxiFtzJyTlBzTnCw4Xg7/s8LdS8Ak/ZRO/SfimdQEQn
     * 608IOyLnzJtuWgSw83qN0xOFPnUb0Am7XXtKNOtK9IMTB4Dh9IU7D0BAQU/IDThha42hBhGsCX5ilhKJpjhZ7z4eGrILo7HqhclfZjS32lvO4lyhRk1ZZcBcjRJtXIeC7NGj5rT6XuY0amUm7FlgubFg0n1
     * Fo7DIJquuZWBkKzNqHlmMZi+uKrGpECNWhPuWzJ/0g8rfQvl3V1WX9o2a68608GzDCQtHqwHbG2jEX4iWrfzda8lvVEK4ESbvZ+l+Xy5GO0K2AwPst+Rev+JnPwIDAQABMA0GCSqGSIb3DQEBCwUAA4IBAQA
     * 4SXqbZfRmqL6sC7ljbrg1zNK7edhlQUE8qQZuTVRcHOKzB/wEgSPoPypUsezXqsdmJEP3AoyZO3/LlN7l//RlrmVGN2CXoW4/W1z9sDpUzUO1BO5vjdo6KZfLk8s1zX5vqKDGdlcp/0R1TIQcm0bwAtIOJ11
     * LCriChCaeukAOnxT4yctbA0kdxCPSb/wJMJqYaQZ7+0psuzNkRvfSALIx1o2JTe2mGlA0wq0Ur3FgzSSf2mvFhaBbCZ5e4e7UO4B8xONSBy8FOdbix6F3AKdNmn/mHfCzMpBJhjBmHEzkI+5wfli/5zpSd5Z
     * gnYfl2Y1AA758E1yqXLtyU8JHQkKT"}}
     * Output: {"user":{"username":"superadmin99","locale":"en_US","comment":"Need to manage user accounts."},
     * "user_login_certificate":{"certificate":"MIICrjCCAZa.....yqXLtyU8JHQkKT","enabled":false},"result":true}
     * </pre>
     * @mtwSampleApiCall
     * <pre>
     *  String userName = "superadmin99";
     *  User user = new User();
     *  user.setUsername(userName);
     *  user.setLocale(Locale.US);
     *  user.setComment("Need to manage user accounts."); 
     *  KeyPair keyPair = RsaUtil.generateRsaKeyPair(RsaUtil.MINIMUM_RSA_KEY_SIZE);
     *  X509Certificate certificate = X509Builder.factory().selfSigned(String.format("CN=%s", userName), keyPair).expires(365, TimeUnit.DAYS).build();
     *  UserLoginCertificate userLoginCertificate = new UserLoginCertificate();
     *  userLoginCertificate.setCertificate(certificate.getEncoded());
     *  RegisterUserWithCertificate rpcUserWithCert = new RegisterUserWithCertificate(); 
     *  rpcUserWithCert.setUser(user);
     *  rpcUserWithCert.setUserLoginCertificate(userLoginCertificate);
     *  boolean registerUserWithCertificate = client.registerUserWithCertificate(rpcUserWithCert);
     * </pre>
     */
    public boolean registerUserWithCertificate(RegisterUserWithCertificate obj) {
        boolean isUserRegistered = false;
        log.debug("target: {}", getTarget().getUri().toString());
        Object result = getTarget().path("rpc/register-user-with-certificate").request().accept(MediaType.APPLICATION_JSON).post(Entity.json(obj), Object.class);
        if (result.getClass().equals(LinkedHashMap.class)) {
            LinkedHashMap resultMap = (LinkedHashMap)result;
            if (resultMap.containsKey("result")) {
                isUserRegistered = Boolean.parseBoolean(resultMap.get("result").toString().trim());
                log.debug("Result of user registration with certificate is {}.", isUserRegistered);
            }
        }
        return isUserRegistered;
    }
    
}
