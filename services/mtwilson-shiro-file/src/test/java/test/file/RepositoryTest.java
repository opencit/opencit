/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package test.file;

import com.intel.dcsg.cpg.crypto.RandomUtil;
import com.intel.dcsg.cpg.crypto.Sha256Digest;
import com.intel.dcsg.cpg.io.ByteArray;
import com.intel.mtwilson.shiro.file.LoginDAO;
import com.intel.mtwilson.shiro.file.model.UserPassword;
import com.intel.mtwilson.shiro.file.model.UserPermission;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import org.apache.shiro.authz.permission.WildcardPermission;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * If you are setting up a test environment you should run the unit tests
 * in this order:
 * 
 * testCreateRole
 * testCreateUser
 * 
 * 
 * References:
 * Validation queries: http://stackoverflow.com/questions/3668506/efficient-sql-test-query-or-validation-query-that-will-work-across-all-or-most
 * 
 * @author jbuhacoff
 */
public class RepositoryTest {
    private static Logger log = LoggerFactory.getLogger(RepositoryTest.class);
/*
    @Test
    public void testCreateRole() throws Exception {
        LoginDAO dao = MyJdbi.authz();
        
        //create a new role
        Role role = new Role();
        role.setId(new UUID());
        role.setRoleName("root");
        role.setDescription("for testing only");
        dao.insertRole(role.getId(), role.getRoleName(), role.getDescription());

        log.debug("Created role {} with id {}", role.getRoleName(), role.getId()); // for example: Created role root with id 6a382588-ab03-4973-b751-0ae927e1a639

        // add permissions to this role
        RolePermission rolePermission = new RolePermission();
        rolePermission.setRoleId(role.getId());
        rolePermission.setPermitDomain("*");
        rolePermission.setPermitAction("*");
        rolePermission.setPermitSelection("*");
        dao.insertRolePermission(rolePermission.getRoleId(), rolePermission.getPermitDomain(), rolePermission.getPermitAction(), rolePermission.getPermitSelection());
        
        dao.close();
    }
    */
    
    @Test
    public void testCreateUser() throws Exception {
        File userFile = new File("target/users.txt");
        File permissionFile = new File("target/permissions.txt");
        LoginDAO dao = new LoginDAO(userFile, permissionFile);
        
        String password = RandomUtil.randomHexString(8); // simulated random password for user
        
        UserPassword userLoginPassword = new UserPassword();
        userLoginPassword.setUsername(RandomUtil.randomHexString(4));
        userLoginPassword.setAlgorithm("SHA-256");
        userLoginPassword.setIterations(1);
        userLoginPassword.setSalt(RandomUtil.randomByteArray(8));
        userLoginPassword.setPasswordHash(passwordHash(userLoginPassword, password));
        dao.createUser(userLoginPassword);
        
        UserPassword exists = dao.findUserByName(userLoginPassword.getUsername());
        assert exists != null && exists.getUsername().equals(userLoginPassword.getUsername());
        

        dao.deleteUserByName(exists.getUsername());

        UserPassword deleted = dao.findUserByName(userLoginPassword.getUsername());
        assert deleted == null;
        
    }
    

    
    private byte[] passwordHash(UserPassword userLoginPassword, String password) {
        if( "SHA256".equalsIgnoreCase(userLoginPassword.getAlgorithm())) {
            // first iteration is mandatory
            Sha256Digest digest = Sha256Digest.digestOf(ByteArray.concat(userLoginPassword.getSalt(), password.getBytes(Charset.forName("UTF-8"))));
            int max = userLoginPassword.getIterations() - 1; // -1 because we just completed the first iteration
            for(int i=0; i<max; i++) {
                digest = Sha256Digest.digestOf(digest.toByteArray());
            }
            return digest.toByteArray();
        }
        return null;
    }
    
    @Test
    public void testReadPermissions() throws IOException {
        String text = "user1=tpm:quote, host:info\n";
        Properties properties = new Properties();
        properties.load(new StringReader(text));
        log.debug("user1 permissions: {}", properties.getProperty("user1"));
        String userPermissions = properties.getProperty("user1");
        List<String> permissions = Arrays.asList(userPermissions.split("\\s*,\\s*"));
        for(String permission : permissions) {
            WildcardPermission shiroPermission = new WildcardPermission(permission);
            log.debug("is allowed to tpm quote? {}", shiroPermission.implies(new WildcardPermission("tpm:quote")));
            log.debug("is allowed to tpm seal? {}", shiroPermission.implies(new WildcardPermission("tpm:seal")));
            log.debug("is allowed to host info? {}", shiroPermission.implies(new WildcardPermission("host:info")));
            log.debug("is allowed to host print? {}", shiroPermission.implies(new WildcardPermission("host:print")));
        }
    }
    
    @Test
    public void testCreatePermissions() throws IOException {
        File userFile = new File("target/users.txt");
        File permissionFile = new File("target/permissions.txt");
        LoginDAO dao = new LoginDAO(userFile, permissionFile);
        dao.addPermission("user1", "tpm:quote");
        String text = FileUtils.readFileToString(permissionFile);
        log.debug("saved permissions:\n{}", text);
    }
    
    @Test
    public void testReadPermissionsFile() throws IOException {
        File userFile = new File("target/users.txt");
        File permissionFile = new File("target/permissions.txt");
        String text = "user1=tpm:quote, host:info\nuser2 = tpm:quote:* , *:info , test\n";
        FileUtils.writeStringToFile(permissionFile, text);
        LoginDAO dao = new LoginDAO(userFile, permissionFile);
        List<UserPermission> p1 = dao.getPermissions("user1");
        for(UserPermission p : p1) {
            log.debug("user1 permission: {}", p.toString());
        }
        List<UserPermission> p2 = dao.getPermissions("user2");
        for(UserPermission p : p2) {
            log.debug("user1 permission: {}", p.toString());
        }
    }
}
