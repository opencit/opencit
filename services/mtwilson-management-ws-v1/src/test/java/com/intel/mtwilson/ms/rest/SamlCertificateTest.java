/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.ms.rest;

import com.intel.mtwilson.ms.rest.SamlCertificate;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 *
 * @author dsmagadX
 */
public class SamlCertificateTest {
    
    public SamlCertificateTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Test of getSamlCertificate method, of class SamlCertificate.
     */
    @Test
    public void testGetSamlCertificate() {
        System.out.println("getSamlCertificate");
        SamlCertificate instance = new SamlCertificate();
        byte[] expResult = null;
        byte[] result = instance.getSamlCertificate();
        System.err.println("Saml Cert == " + result.toString());

    }

  
}
