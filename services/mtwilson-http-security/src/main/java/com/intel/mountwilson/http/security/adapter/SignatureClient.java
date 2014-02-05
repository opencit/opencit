/*
 * This code is heavily based on open source code and should not be copyrighted.
 */
package com.intel.mountwilson.http.security.adapter;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.IOUtils;
import com.intel.mtwilson.security.http.HmacAuthorization;
import com.intel.dcsg.cpg.crypto.HmacCredential;

/**
 * This class requires the following libraries:
 * org.apache.commons.io.IOUtils from commons-io
 * 
 * Uses the RequestAuthorization class to sign requests before sending them
 */
public class SignatureClient {
        private final Pattern headerPattern = Pattern.compile("([\\w\\d-]+): (.+)");
    
	private static final String version = "0.0";
        private final HmacAuthorization auth;
	
	public SignatureClient(String userId, String secretKey) {
            auth = new HmacAuthorization(new HmacCredential(userId, secretKey));
	}

        /**
         * Opens a new HttpURLConnection and adds the Authorization header
         * using the userId and secretKey provided to the constructor.
         * 
         * You can add your own headers to the HttpURLConnection before sending the request.
         * 
         * @param method the Http method such as "GET" or "POST"
         * @param urlstring the complete URL such as http://example.com/some/path
         * @param body the body of the request, or null
         * @param headers an array of http headers in "Header-Name: Value" format to be added to the request; or null
         * @return an open HttpURLConnection object; you must read the response and close it
         * @throws NoSuchAlgorithmException if your environment is missing the HmacSHA256 algorithm
         * @throws InvalidKeyException if the secretKey value you provided to the constructor is not suitable for use with HmacSHA256
         * @throws IOException if there was a problem generating the nonce
         */
        public HttpURLConnection openConnection(String method, String urlstring, String body, String[] headers) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
            URL url = new URL(urlstring);
            HttpURLConnection c = (HttpURLConnection)url.openConnection();
            c.setDoOutput(true);
            c.setRequestMethod(method);
            c.setRequestProperty("Authorization", auth.getAuthorization(method, urlstring, body));
            if( headers != null ) {
                for(String h : headers) {
                    Matcher m = headerPattern.matcher(h);
                    if( m.matches() ) {
                        c.setRequestProperty(m.group(1), m.group(2));
                    }
                }
            }
            OutputStreamWriter out = new OutputStreamWriter(c.getOutputStream());
            out.write(body);
            out.flush();
            out.close();		
            return c;
        }
        

        /**
         * Convenience method to make a signed HTTP request and return the response.
         * @param method HTTP method such as "GET", "POST", "PUT", "DELETE", "HEAD", or "OPTIONS"
         * @param urlstring URL such as http://www.example.com/path/to/resource
         * @param body optional; if not needed use null
         * @throws IOException 
         * @return the resonse body only; headers are not available through this method
         * @throws NoSuchAlgorithmException if your environment is missing the HmacSHA256 algorithm
         * @throws InvalidKeyException if the secretKey value you provided to the constructor is not suitable for use with HmacSHA256
         * @throws IOException if there was a problem generating the nonce
         */
	public String request(String method, String urlstring, String body, String[] headers) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
            HttpURLConnection c = openConnection(method, urlstring, body, headers);
            InputStream cin = c.getInputStream();
            try {
            	String content = IOUtils.toString(cin);
                return content;
            }
            finally {
            	cin.close();
                c.disconnect();
            }
	}
	

}
