package com.intel.mtwilson.as.helper;

import org.junit.Test;
//import sun.misc.BASE64Encoder;
//import sun.misc.BASE64Decoder;
/**
 * These tests verify that the data model serializes and de-serializes properly.
 * 
 * Commented out the Base64 tests because:
 * 1) they passed, commons-encoder output is identical to the Sun encoder
 * 2) the Sun encoder is a private API, and on some JVM's it's not available to programs
 * 
 * When you use the commons encoder, be aware that:
 * Base64.encodeBase64String returns output with newlines to fit into 76 character wide space
 * So to get a "pure" string you need to do new String(Base64.encodeBase64(some bytes));
 *
 * @author jbuhacoff
 */
public class VMwareClientTest {

	/*
    @Test
    public void commonsEncoderCompatibleWithSun() {
        String sunResult = new BASE64Encoder().encode("hello world!".getBytes());
        String commonsResult = Base64.encodeBase64String("hello world!".getBytes());
        System.out.println("hello world! encoded: "+sunResult);
        assertEquals(sunResult, commonsResult);
    }

    @Test
    public void commonsDecoderCompatibleWithSun() throws IOException {
        String sunResult = new String(new BASE64Decoder().decodeBuffer("aGVsbG8gd29ybGQh"));
        String commonsResult = new String(Base64.decodeBase64("aGVsbG8gd29ybGQh"));
        System.out.println("aGVsbG8gd29ybGQh decoded: "+sunResult);
        assertEquals(sunResult, commonsResult);
    }
    */
    
    @Test
    public void testHelper() {
        try {
            System.out.println(System.getenv());

            //HashMap result = VMwareHelper.getQuoteInformationForHost("10.1.71.108", "0,20", "https://10.1.71.115:444/sdk;RAUser;Intel123!");
//            System.out.println("Result - " + result.toString());
//            HashMap result = new VMwareClient().getQuoteInformationForHost("10.1.71.141", "0,20", "https://10.1.71.142:444/sdk;Administrator;P@ssw0rd");
            
            
            //HashMap result = VMwareHelper.getQuoteInformationForHost("10.1.71.154", "0,20", "https://10.1.71.142:444/sdk;Administrator;P@ssw0rd");	
//            System.out.println("Result - " + result.toString());
              
//            ManagedObjectReference result = VMwareHelper.findChild("10.1.71.108", "0,20", "https://10.1.71.115:444/sdk;RAUser;Intel123!");
            
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    
}
