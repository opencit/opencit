using System;
using System.Collections.Generic;
using System.Linq;
using System.Text; // UTF8Encoding
using System.Web; // HttpUtility.UrlPathEncode
using System.IO; // MemoryStream
using System.Configuration;
using System.Security.Cryptography; // HMACSHA256
using System.Net; // for HttpWebRequest

/*
 * Requires the following assemblies:
 * System.Web for HttpUtility
 * System.Security (mscorlib) for System.Security.Cryptography.HMACSHA256
 */
namespace MtWilsonHttpAuthorization
{
    public class SignatureBlock
    {
        public string httpMethod;
        public string absoluteUrl;
        public string fromToken;
        public string nonce;
        public string requestBody;
        public string signatureMethod;
        public string timestamp;

        public SignatureBlock()
        {
        }

        public string GetText() 
        {
            string[] label = new String[] { "HttpMethod", "AbsoluteUrl", "FromToken", "Nonce", "SignatureMethod","Timestamp" };
            string[] input = new String[] {  httpMethod,   absoluteUrl,   fromToken,   nonce,   signatureMethod,  timestamp };
            List<string> errors = new List<string>();
            for(int i = 0; i<input.Length; i++) {
                if( String.IsNullOrEmpty(input[i]) ) {
                    errors.Add(label[i] + " is required");
                }
            }
            if( requestBody == null ) { requestBody = String.Empty; }
            if (errors.Count > 0)
            {
                throw new ArgumentException(String.Format("Cannot create signature block: {0}", String.Join(", ", errors)));
            }
            return String.Format("Request: {0} {1}\nFrom: {2}\nTimestamp: {3}\nNonce: {4}\nSignature-Method: {5}\n\n{6}",
                new string[] { httpMethod, absoluteUrl, fromToken, timestamp, nonce, signatureMethod, requestBody });
        }
    }

    public class HttpRequestURL
    {
        private string path;
        private string query;
        public HttpRequestURL(string path, string query)
        {
            this.path = path;
            this.query = query;
        }
        public string GetURL()
        {
            return path + (String.IsNullOrEmpty(query) ? "" : "?" + query);
        }

        /*
         * The alphanumeric characters a-z, A-Z, and 0-9 remain the same
         * The special characters dot, hyphen, star, and underscore remain the same
         * The space character is converted to %20  (not a plus sign)
         * All other characters are unsafe and are converted to a sequence of
         * bytes using UTF-8 and then each byte is encoded using the %xy hexadecimal
         * representation.
         * 
         * If the value is null, an empty string will be returned
         */
        private string encode(string value)
        {
            if (String.IsNullOrEmpty(value)) { return String.Empty; }
            return HttpUtility.UrlPathEncode(value);
        }
    }
    /// <summary>
    /// Generates the Authorization headers for Mt Wilson web service API requests
    /// </summary>
    public class MtWilsonClient
    {
        private string clientId;
        private string secretKey;
        private Encoding utf8; //UTF8Encoding utf8;
        private HMACSHA256 hmac;
        private Random rnd = new Random();

        public MtWilsonClient(string clientId, string secretKey)
        {
            this.clientId = clientId;
            this.secretKey = secretKey;
            this.utf8 = System.Text.Encoding.UTF8; //new UTF8Encoding();
            this.hmac = new HMACSHA256(utf8.GetBytes(secretKey));
        }

        /*
         * Generates the content of an Authorization header like this:
         * 
MtWilson http_method="GET",
         uri="http://localhost:8080/AttestationService/resources/hosts/trust?hostName=10.1.71.103",
         username="cm9vdA==",
         nonce="MjAxMi0wMy0wMVQxNToyMjoyOS42NDUyMTUyLTA4OjAwGHEbQHWC4C+P3d+Nz56EMA==",
         signature_method="HMAC-SHA256",
         timestamp="2012-03-01T15:22:29-08:00",
         signature="HSDHAkTXgAHzrMMiOyBH0viESVHNiZ/KYjrnwln6rww="
         * 
         * The request url must already have the query parameters in the query string.
         * 
         */
        public string getAuthorization(string httpMethod, string requestUrl, string requestBody)
        {
            string nonceStr = System.Convert.ToBase64String(nonce());
            SignatureBlock signatureBlock = new SignatureBlock();
            signatureBlock.httpMethod = httpMethod;
            signatureBlock.absoluteUrl = new HttpRequestURL(requestUrl, null).GetURL(); // the NULL should be replaced with QUERY PARAMETERS ??? the server doesn't care (as of Feb 2012), it does not map the actual uri to the signature uri
            signatureBlock.fromToken = System.Convert.ToBase64String(utf8.GetBytes(clientId)); // base-64 encoded version of client id.
            signatureBlock.nonce = nonceStr; // base-64 encoded nonce data, but this is opaque to the server: the server doesn't care what we put in the nonce or how we encoded it.
            signatureBlock.requestBody = requestBody;
            signatureBlock.signatureMethod = "HMAC-SHA256";
            signatureBlock.timestamp = DateTimeOffset.Now.ToString("yyyy-MM-ddTHH:mm:sszzz"); // example: 2012-03-01T15:22:29-08:00
            string content = signatureBlock.GetText();
            string signature = sign(content);
            string realm = null;
            string header = String.Format("MtWilson {0}", headerParams(signatureBlock, realm, signature));
            Console.WriteLine("Generating Authorization: " + header);
            return header;

        }

        /*
         * Convenience method to add the Authorization header to an existing request.
         * The HttpMethod and RequestUri of the request must already be set. 
         * The request must not include any content in the body. If you have content to send,
         * use the two-argument version of authorize(). 
         */
        public void authorize(HttpWebRequest httpRequest)
        {
            string header = getAuthorization(httpRequest.Method, httpRequest.RequestUri.ToString(), null);
            httpRequest.Headers.Add("Authorization: " + header);
        }

        /*
         * Convenience method to add the Authorization header to an existing request.
         * The HttpMethod and RequestUri of the request must already be set. 
         * The content is what will be sent in the body of the request (with GetRequestStream).
         * It's not possible to read the input stream of a request after it's been set with the .NET API,
         * which is why the content has to be provided separately to this method. 
         * You can also pass null content, empty string, or use the one-argument version of authorize() if you don't
         * have any content to send.
         */
        public void authorize(HttpWebRequest httpRequest, String content)
        {
            string header = getAuthorization(httpRequest.Method, httpRequest.RequestUri.ToString(), content);
            httpRequest.Headers.Add("Authorization: " + header);
        }


        private byte[] nonce()
        {
            MemoryStream stream = new MemoryStream();
            string timeStr = DateTimeOffset.Now.ToString("o"); // "o" looks like 2008-06-15T21:15:07.0000000.  See also http://msdn.microsoft.com/en-us/library/zdtaw1bw.aspx
            Byte[] time = utf8.GetBytes(timeStr);
            Byte[] random = new Byte[16];
            rnd.NextBytes(random);
            stream.Write(time, 0, time.Length);
            stream.Write(random, 0, random.Length);
            Byte[] nonce = stream.ToArray();
            stream.Close();
            return nonce; // System.Convert.ToBase64String(nonce);
        }

        private string sign(string text)
        {
            return System.Convert.ToBase64String(hmac.ComputeHash(utf8.GetBytes(text)));
        }

        private string headerParams(SignatureBlock sb, string realm, string signature)
        {
            string[] label = new String[] { "http_method", "uri",         "username",    "nonce", "signature_method", "timestamp", "realm", "signature" };
            string[] input = new String[] { sb.httpMethod, sb.absoluteUrl, sb.fromToken, sb.nonce, sb.signatureMethod, sb.timestamp, realm, signature };
            List<string> errors = new List<string>();
            List<string> param = new List<string>();
            for (int i = 0; i < input.Length; i++)
            {
                if (input[i] != null && input[i].Contains("\"") )
                {
                    errors.Add(label[i] + " contains quotes");
                }
                if (!String.IsNullOrEmpty(input[i]))
                {
                    param.Add(String.Format("{0}=\"{1}\"", label[i], input[i]));
                }
            }
            if (errors.Count > 0)
            {
                throw new ArgumentException(String.Format("Cannot create authorization header: {0}", String.Join(", ", errors)));
            }
            return String.Join(", ", param);
        }
    }
}

