/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package securesample;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author dsmagadX
 */
public class parserTest {
    
    
    
    public static void main(String[] args){
        
        String input = "<quote_request><nonce>+nao5lHKxcMoqIGY3LuAYQ==</nonce><pcr_list>3,19</pcr_list></quote_request>";
        // Create a pattern to match cat
        Pattern p = Pattern.compile("<pcr_list>(.*?)</");
        // Create a matcher with an input string
        Matcher m = p.matcher(input);
        m.find();
        //String s = m.group(1);
        //StringBuffer sb = new StringBuffer();

    }
    
    
}
