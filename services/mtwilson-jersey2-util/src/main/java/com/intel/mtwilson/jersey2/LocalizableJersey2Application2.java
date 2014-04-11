/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.intel.mtwilson.jersey2;

/**
 *
 * @author rksavino
 */
public class LocalizableJersey2Application2 extends Jersey2Application2 {
    
    public LocalizableJersey2Application2() {
        super();
        register(com.intel.mtwilson.util.ASLocalizationFilter.class);
    }
    
}
