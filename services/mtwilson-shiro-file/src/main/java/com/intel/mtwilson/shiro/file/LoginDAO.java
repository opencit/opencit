/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.mtwilson.shiro.file;

import com.intel.mtwilson.shiro.file.model.UserPassword;
import com.intel.mtwilson.shiro.file.model.UserPermission;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.shiro.authz.permission.WildcardPermission;
import org.apache.shiro.util.StringUtils;

/**
 * 
 * @author jbuhacoff
 */
public class LoginDAO  {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LoginDAO.class);

    private final File userFile; 
    private final File permissionFile; 
    private final Map<String,UserPassword> users;
    private final Map<String,List<UserPermission>> permissions;
    
    public LoginDAO(File userFile, File permissionFile) throws IOException {
        this.userFile = userFile;
        this.permissionFile = permissionFile;
        this.users = new HashMap<>();
        this.permissions = new HashMap<>();
        load();
    }
    
    private String toString(UserPassword userLoginPassword) {
        return String.format("%s:%s:%s:%s", 
                userLoginPassword.getUsername(),
                userLoginPassword.getAlgorithm(),
//                userLoginPassword.getIterations(), // omitting the iterations %d placeholder
                Base64.encodeBase64String(userLoginPassword.getSalt()),
                Base64.encodeBase64String(userLoginPassword.getPasswordHash())
                );
    }
    
    private UserPassword toUserLoginPassword(String text) {
        String[] parts = text.split(":");
        UserPassword userLoginPassword = new UserPassword();
        userLoginPassword.setUsername(parts[0]);
        userLoginPassword.setAlgorithm(parts[1]);
//        userLoginPassword.setIterations(Integer.valueOf(parts[2]));
        userLoginPassword.setSalt(Base64.decodeBase64(parts[2]));
        userLoginPassword.setPasswordHash(Base64.decodeBase64(parts[3]));
        return userLoginPassword;
    }

    public void createUser(UserPassword userLoginPassword) throws IOException {
        if( users.containsKey(userLoginPassword.getUsername())) {
            throw new IllegalArgumentException("User already exists");
        }
        users.put(userLoginPassword.getUsername(), userLoginPassword);
        save();
    }
    
    public void storeUser(UserPassword userLoginPassword) throws IOException {
        if( !users.containsKey(userLoginPassword.getUsername())) {
            throw new IllegalArgumentException("User does not exist");
        }
        users.put(userLoginPassword.getUsername(), userLoginPassword);
        save();
    }
    
    public UserPassword findUserByName(String username) {
        return users.get(username);
    }
    
    public void deleteUserByName(String username) throws IOException {
        if( !users.containsKey(username)) {
            throw new IllegalArgumentException("User does not exist");
        }
        users.remove(username);
        save();
    }
    
    public void addPermission(String username, String permissionText) throws IOException {
        List<UserPermission> list = permissions.get(username);
        if( list == null ) {
            list = new ArrayList<>();
        }
        log.debug("adding permission for user {}", username);
        list.add(UserPermission.parse(permissionText));
        permissions.put(username, list);
        save();
    }
    
    public void removePermission(String username, String permissionText) throws IOException {
        List<UserPermission> list = permissions.get(username);
        if( list == null ) { return; }
        ArrayList<UserPermission> accepted = new ArrayList<>(); // new list of permissions for the user
        WildcardPermission removed = new WildcardPermission(permissionText);
        for(UserPermission permission : list) {
            WildcardPermission item = new WildcardPermission(permission.toString());
            if( !item.implies(removed) ) {
                accepted.add(permission); // adding all permissions that are NOT the one we are removing
            }
        }
        permissions.put(username, accepted);
        save();
    }
    
    public List<UserPermission> getPermissions(String username) {
        List<UserPermission> list = permissions.get(username);
        if( list == null ) {
            list = Collections.EMPTY_LIST;
        }
        return list;
    }

    private void save() throws IOException {
        ArrayList<String> userLines = new ArrayList<>();
        // store users
        for(String username : users.keySet()) {
            UserPassword userLoginPassword = users.get(username);
            String line = toString(userLoginPassword);
            userLines.add(line);
        }
        FileUtils.writeLines(userFile, userLines);
        // store permissions
        ArrayList<String> permissionLines = new ArrayList<>();
        for(String username : permissions.keySet()) {
            log.debug("Saving permissions for {}", username);
            List<UserPermission> permissionList = permissions.get(username);
            ArrayList<String> permissionTextList = new ArrayList<>();
            for(UserPermission permission : permissionList) {
                permissionTextList.add(permission.toString());
            }
            CommaSeparatedValues csv = new CommaSeparatedValues(permissionTextList);
            KeyValuePair line = new KeyValuePair(username, csv.toString());
            permissionLines.add(line.toString());
        }
        FileUtils.writeLines(permissionFile, permissionLines);
    }
    
    private void load() throws IOException {
        if( !userFile.exists() ) {
            log.debug("Password file does not exist");
            return; // not an error because caller can add users and then call save() to create the password file
        }
        // load users
        List<String> userLines = FileUtils.readLines(userFile);
        for(String line : userLines) {
            UserPassword userLoginPassword = toUserLoginPassword(line);
            users.put(userLoginPassword.getUsername(), userLoginPassword);
        }
        // load permissions
        List<String> permissionLines = FileUtils.readLines(permissionFile);
        for(String line : permissionLines) {
            KeyValuePair userPermission = KeyValuePair.parse(line);
            String user = userPermission.getKey();
            CommaSeparatedValues permissionList = CommaSeparatedValues.parse(userPermission.getValue());
            ArrayList<UserPermission> list = new ArrayList<>();
            for(String item : permissionList.getValues()) {
                list.add(UserPermission.parse(item));
            }
            permissions.put(user,list);
        }
    }

    public static class KeyValuePair {
        private String key;
        private String value;
        public KeyValuePair(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return String.format("%s=%s", key, value);
        }
        
        public static KeyValuePair parse(String text) {
            List<String> pair = Arrays.asList(text.split("\\s*=\\s*"));
            if( pair.size() != 2 ) {
                throw new IllegalArgumentException("Invalid format: "+text);
            }
            return new KeyValuePair(pair.get(0), pair.get(1));
        }
    }
    
    public static class CommaSeparatedValues {
        private List<String> values;
        public CommaSeparatedValues(String... array) {
            this.values = Arrays.asList(array);
        }
        public CommaSeparatedValues(List<String> list) {
            this.values = list;
        }
        public List<String> getValues() { 
            return values;
        }

        @Override
        public String toString() {
            return StringUtils.join(values.iterator(), ",");
        }
        public static CommaSeparatedValues parse(String text) {
            List<String> values = Arrays.asList(text.split("\\s*,\\s*"));
            return new CommaSeparatedValues(values);
        }
    }
}
