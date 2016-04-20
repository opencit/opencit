/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.security.core;

/**
 *
 * @author ssbangal
 */
public interface HttpBasicUserFinder {
    String getPasswordForUser(String userName);
    
}
