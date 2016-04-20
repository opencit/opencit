<?php
/**
*
* @copyright Copyright (c) 2012, Intel Corporation. All rights reserved.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
* AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
* IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
* ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
* LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
* INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
* CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
* ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
* POSSIBILITY OF SUCH DAMAGE.
*
* @package MtWilson
*/
/**
 * Usage example:
 * 
 * require_once 'MtWilson.php';
 * $api = new MtWilson('https://10.1.71.202:8181');
 * $result = $api->getHostTrustStatus('mwtstubosn01h');
 * var_dump($result);
 *
 *  @package MtWilson
 * @author Cheyenne Software, Inc.
 * @link http://www.http2smtp.com
 * @version 0.1
 */
class MtWilson
{
	private $username; // MtWilson API Username (currently not used; may be implemented later with authentication)
	private $httpProxy;
	private $serviceUrl = "https://mtwilson.example.com:8181";
	private $attestationPath = "/AttestationService/resources";
	private $requireTrustedCertificate = true;
        private $verifyHostname = true;
        private $trustedCertificatePath = null;
        
	/**
	* Constructor
	*
	* @param string $serviceUrl the base URL for the Mt Wilson server
	* @return void
	*/
	public function MtWilson($serviceUrl = null) {
		if ($serviceUrl !== null ) {
			$this->setURL($serviceUrl);			
		}
	}

	/**
	* Set Mt Wilson URL
	*
	* @param string $serviceUrl the base URL for Mt Wilson
	* @return void
	*/
	public function setURL($serviceUrl) {
		$this->serviceUrl = $serviceUrl;
	}
	
	/**
	* Set an http proxy for networks behind a strict firewall
	*
	* @param string $proxy the proxy address including port number
	* @return void
	*/
	public function setProxy($httpProxy) {
		$this->httpProxy = $httpProxy;
	}
        
        /**
         * Mt Wilson normally requires SSL connections. You can use this
         * setting to turn that off (not recommended)
         */
        public function setRequireTrustedCertificate($isEnabled) {
            $this->requireTrustedCertificate = $isEnabled;
        }

        /**
         * SSL connections normally require the server's hostname or IP address
         * to match what is in its SSL certificate. YOu can use this option to
         * turn that off (not recommended). 
         */
        public function setVerifyHostname($isEnabled) {
            $this->verifyHostname = $isEnabled;
        }
	
        /**
         * SSL connections normally require the server to present a trusted
         * certificates. Use this setting to name a directory that contains
         * trusted CA certificates (including self-signed server certificates).
         */
        public function setTrustedCertificatePath($pathToFolder) {
            $this->trustedCertificatePath = $pathToFolder;
        }
	
        
	// equivalent to rawurlencode in PHP 5.3.0 or later
	// encodes all non-alphanumeric characters except -_.~ as a percent sign and two hex digits
	// in older PHP versions, the rawurlencode function follows RFC 1738 which had a different reserved characters list, specifically it would encode the ~ as %7E
	// so using this function you can be sure that regardless of PHP version you'll get URL encoding according to RFC 3986 
	private function urlencode_rfc3986($text) {
		$encoded = "";
		$length = strlen($text);
		for($i=0; $i<$length; $i++) {
			$c = substr($text,$i,1);
			if( preg_match("/^[A-Za-z0-9\._~-]$/", $c) ) {
				$encoded .= $c;
			}
			else {
				$encoded .= '%' . sprintf("%02X",ord($c));
			}
		}
		return $encoded;
	}
	
	private function queryString($queryobj) {		
		// first store all key-value pairs in an array we can sort
		$kvpairs = array();
		foreach ($queryobj as $k => $v) {
			//$kvpairs[] = rawurlencode($k) . "=" . rawurlencode($v);
			$kvpairs[] = $this->urlencode_rfc3986($k) . "=" . $this->urlencode_rfc3986($v);
		}
		// sort the array - this is important so the server can verify our signature
		sort($kvpairs, SORT_STRING);
		return implode("&", $kvpairs);
	}
	
        private function url($path, $data) {
            return implode("?", array($this->serviceUrl . '/AttestationService/resources' . $path, $this->queryString($data)));
        }

        /*
        private function prepare($data) {
		// generate signed post data
		unset($data['_signature'], $data['_timestamp']); // to prevent confusion with our own signature and timestamp
		$data['_account'] = $this->account;
		$data['_timestamp'] = time();
		$querystring = $this->queryString($data);
		$signature = $this->sign($querystring);
        $post_data = $querystring . "&_signature=" . rawurlencode($signature);
        return $post_data;
	}*/
        
        
        public function getHostTrustStatus($hostname) {
            return json_decode($this->GET($this->url("/hosts/trust", array("hostName"=>$hostname))));
        }

	/**
	* Send a message
	*
	* @param string $path the endpoint
        * @param array $data the data to POST
	* @return boolean
	*/
	public function GET($path) {
            return $this->request("GET",$path, null);
	}

	/**
	* Send a message
	*
	* @param string $path the endpoint
        * @param array $data the data to POST
	* @return boolean
	*/
	public function DELETE($path) {
            return $this->request("DELETE", $path, null);
	}
        
	/**
	* Send a message
	*
	* @param string $path the endpoint
        * @param array $data the data to POST
	* @return boolean
	*/
	public function POST($path, $body) {
            return $this->request("POST", $path, $body);
	}

	/**
	* Send a message
	*
	* @param string $path the endpoint
        * @param array $data the data to POST
	* @return boolean
	*/
	public function PUT($path, $body) {
            return $this->request("PUT", $path, $body);
	}
        
        
	// watch out: if you use this method, there's a rate limit of about 1-2 messages per minute. exceeding this will cause your IP to be blocked!  this is an anti-spam measure.
	private function request($httpMethod, $url, $body) {
            error_log("$httpMethod $url\n$body");
        $curl = curl_init();

        curl_setopt($curl, CURLOPT_HEADER, false);  // enable to see HTTP 1.1 response headers in the result
        curl_setopt($curl, CURLOPT_FOLLOWLOCATION, false); // do not follow redirects; we may want to change this later but must also set CURLOPT_MAXREDIRS to something sensible
        
        if( $httpMethod == "GET" ) {
            curl_setopt($curl, CURLOPT_HTTPGET, true);
        }
        else if( $httpMethod == "POST" ) {
            curl_setopt($curl, CURLOPT_POST, true);
            curl_setopt($curl, CURLOPT_POSTFIELDS, $body);            
        }
        else if( $httpMethod == "PUT" ) {
            // we use a custom request of PUT instead of the built-in curl PUT because the built-in looks for a file to send, and we just want to send data from a variable like in POST
            curl_setopt($curl, CURLOPT_PUT, 'PUT');
            $stream = fopen('data://text/plain;base64,' . base64_encode($body),'r');
            curl_setopt($curl, CURLOPT_INFILE, $stream);            
        }
        curl_setopt($curl, CURLOPT_URL, $url);
        //curl_setopt($curl, CURLOPT_HTTPHEADER, array("X-Account: ".$this->account, "X-Template: ".$template));
		
        if( isset($this->httpProxy) ) {
			curl_setopt($curl, CURLOPT_HTTPPROXYTUNNEL, true);
			//curl_setopt($curl,�CURLOPT_PROXYTYPE,�CURLPROXY_HTTP); // optional??? seems to work fine without it, and using it gives errors.
			curl_setopt($curl, CURLOPT_PROXY, $this->httpProxy);        	
        }
        
        // security settings
        curl_setopt($curl, CURLOPT_SSL_VERIFYPEER, $this->requireTrustedCertificate);
        curl_setopt($curl, CURLOPT_SSL_VERIFYHOST, $this->verifyHostname);
        if( $this->trustedCertificatePath != null ) {
            curl_setopt($curl, CURLOPT_CAPATH, $this->trustedCertificatePath);
        }
        
        // trusted certs for verifypeer: curl_setopt($connection, CURLOPT_CAPATH, "path:/");  for directory of certs,  or curl_setopt($connection, CURLOPT_CAINFO, "path:/ca-bundle.crt");   or just one CA: curl_setopt($connection, CURLOPT_CAINFO, "path:/ca.pem");
        
        curl_setopt($curl, CURLOPT_RETURNTRANSFER, true);
        $response = curl_exec($curl);
        error_log($response);
        if( curl_errno($curl) ) {
            error_log("Error: ".curl_error($curl));
        }
        if( !$response ) {
            error_log("Could not request $httpMethod $path" . ($body==null?" ":" with body: $body"));
        }
        
        curl_close($curl);
        
        return $response;
	}
	
}
/* END MtWilson CLASS */

?>