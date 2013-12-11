/*
 * Copyright (C) 2013 Intel Corporation
 * All rights reserved.
 */
package com.intel.dcsg.cpg.authz.token;

import com.intel.dcsg.cpg.crypto.Aes;
import com.intel.dcsg.cpg.crypto.CryptographyException;
import com.intel.dcsg.cpg.util.ByteArray;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Date;
import javax.crypto.SecretKey;

/**
 * When encoded, the Token is an opaque token sent to the client that the
 * client must return to the server with each request. The Token is an
 * encrypted and signed data structure that allows the server to authenticate
 * the client's current session without having to keep any server-side state for
 * the client.
 *
 * The server must be configured with a token duration, for example 5 minutes.
 * The server only generates a new Token for the client when the client's
 * current Token has expired or when the client submits a request without
 * a Token.
 * 
 * This Token class can be used as an component in Oauth 2.0 bearer
 * tokens implementations. 
 *
 * <h1>Security Value</h1>
 * When the server implements Referer checking as well, the attack must execute
 * the attack using client software that allows forging headers as well, or rely
 * on a separate cross-site scripting attack to be able to run arbitrary
 * javascript in the client's browser. Therefore this scheme provides effective
 * protection against CSRF but not against CSS.
 * 
 * The use of this Token (via the TokenFactory and TokenValidator objects)
 * allows a server to send some state to the client in a secure way to be 
 * returned later to the server (or to another server that also knows the
 * secret key), and in this way avoid keeping state for
 * each client. 
 * 
 * One benefit of this is that clients can be allowed to set their own
 * session timeout periods and put them in control of their session 
 * security - it won't cost the server anything for clients to stay 
 * "logged in" for longer periods of time because it's not maintaining
 * that state at all. The server can advertise a limit on the timeout period
 * based on how often it rotates or saves archived keys to be able to
 * authenticate client tokens, but the server does not need to enforce this
 * limit because clients that set a timeout period longer than the 
 * practical maximum will simply not be able to use those tokens after
 * the server discards the old keys.
 *
 * <h1>Security Considerations</h1>
 *
 * Server must never send the token in a cookie header because this will
 * enable cross-site request forgeries attacks against the client. The
 * token should be sent in a different header and the client should 
 * return it explicitly as a part of its request content or request headers.
 * 
 * If the attacker exploits a separate CSS vulnerability to execute javascript
 * on the client, the attacker can forge messages and submit them while the user
 * is on the page.
 *
 * It is the client's responsibility to never submit the Token to any
 * other domain besides the one from which it was obtained. This responsibility
 * can be compromised by cross-site scripting.
 * 
 * It is the client's responsibility to ensure that it is connecting to the
 * server via TLS (latest version recommended) and that it is validating the
 * server certificate -  sending the Token to a server without this validation
 * creates a MITM vulnerability that enables the attacker to easily steal the
 * token.
 *
 * See also RFC 6750  http://tools.ietf.org/html/rfc6750 "The OAuth 2.0 Authorization Framework: Bearer Token Usage"
 * especially section 5 on security considerations: http://tools.ietf.org/html/rfc6750#section-5
 * 
 * And also http://hueniverse.com/2010/09/oauth-bearer-tokens-are-a-terrible-idea/
 *
 * <h1>Recipe ideas using Token:</h1>
 *
 * <h2>Token in HTTP header</h2>
 * 
 * 
 * <h2>Token + SessionKey</h2>
 * The server also sends the client a randomly generated SessionKey which the
 * client must use to calculate an HMAC that is sent with every request to the
 * server. The Token includes the SessionKey so that the server can
 * authenticate a request using only information from the request itself (and
 * the server's master secret that is used to encrypt the Token itself).
 *
 * The client signs each request using the SessionKey provided by the server.
 *
 * This scheme creates a desirable situation wherein the client must do some
 * work in order to submit each request, and the client must have knowledge of
 * the SessionKey. Even when the SessionKey is sent to the client in plaintext
 * over an unsecured connection, this scheme prevents cross-site request forgery
 * because it's not possible for an attacker to predict the SessionKey when the
 * attack vector is from another site or from an email message. In order to
 * defeat this scheme, the attacker must steal the Token and corresponding
 * SessionKey from the client, forge the message to be submitted to the server
 * and the client MAC using the SessionKey, and then trick the client into
 * submitting the forged message.
 *
 * SessionKeys cannot be used to authenticate requests after they expire.
 *
 * It is the client's responsibility to never send the SessionKey itself in any
 * request - only the Token and the client MAC calculated using the
 * SessionKey may be sent and only to the server from which the Token was
 * obtained. This responsibility can be compromised by cross-site scripting.
 *
 * If possible, the server should send the SessionKey to the client encrypted
 * using some key that the client already has access to. This might be a
 * password such as the user's login password, or some other persistent secret
 * such as an API Key that the server maintains for each client and that the
 * client may be able to store in local storage. The server and client should
 * communicate over a TLS connection to prevent attackers from picking up the
 * Token and SessionKey from plaintext network traffic.
 *
 * <h1>Implementation Notes</h1>
 *
 * When encoded, the Token contains the following encrypted and signed
 * information:
 *
 * Token version 1 byte (0-255) Server token encryption key ID 16 bytes IV
 * variable length depending on enc. alg (determined by key id) Randomly
 * generated nonce variable length 0-255 bytes preceded by 1 byte length field
 * Timestamp when the Token was generated 8 bytes (java long, in java 8
 * unsigned long) User ID - variable length UTF-8 string preceded by 2 byte
 * length field; the length indicates the encoded length of the string Other
 * state information such as randomly generated SessionKey Token version 1
 * assumes AES-128 and SHA-256, later versions may allow selection of encryption
 * algorithm by the key finder, and variable hash algorithm by embedding the
 * algorithm name in the signed data portion
 *
 *
 * The Token also contains the following plaintext header: Token version
 * Server token key ID
 *
 * When the server receives a request including a Token and Client MAC,
 * the server performs the following steps:
 *
 * 1. Is the token version supported? 2. Is the server token encryption key ID a
 * known key ID? 3. Verify integrity of the encoded Token using the server
 * token key 4. Decrypt the Token using the server token key 5. Is the
 * plaintext token version same as the signed token version? 6. Is the plaintext
 * server token key ID same as the signed server token key ID? 7. Calculate
 * elapsed time since the Token was generated 8. Is the Token
 * expired? (elapsed time > configured max duration) 9. (when using session
 * keys) - Use signed SessionKey to verify the Client MAC
 *
 * @author jbuhacoff
 */
public class Token {

    private final byte version = 1;
//    private byte[] keyId; // 16 bytes
    private byte[] nonce; // variable length 16-240 bytes
    private long timestamp; // seconds since unix epoch  Jan 1, 1970
    private byte[] content; // variable length application-specific data

    protected Token() {
    }

    public int getVersion() {
        return version;
    }

//    public byte[] getKeyId() {
//        return keyId;
//    }

    public byte[] getNonce() {
        return nonce;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public byte[] getContent() {
        return content;
    }

//    protected void setKeyId(byte[] keyId) {
//        this.keyId = keyId;
//    }

    protected void setNonce(byte[] nonce) {
        this.nonce = nonce;
    }

    protected void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    protected void setContent(byte[] content) {
        this.content = content;
    }


    /**
     * The nonce length and nonce are written before the version field to
     * prevent known plaintext attack (since the version is on the unprotected
     * header, if it was first the attacker would know the value of the first
     * byte which might be useful when trying to break the encryption. It is
     * expected that all versions will use a nonce so the nonce is first
     * preceded by its length and after reading the nonce length and nonce the
     * next byte should be the version; this can be used as a quick decryption
     * checksum because the decrypted version byte should equal the version byte
     * in the public header.
     *
     * @return
     * @throws IOException
     */
    protected byte[] encodeContent() {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(buffer);
        try {
            out.writeShort(nonce.length);
            out.write(nonce);
            out.writeByte(version);
            out.writeLong(timestamp);
            out.writeShort(content.length);
            out.write(content);
            out.close();
            return buffer.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException(e); // we should never get an IOException from ByteArrayOutputStream
        }
    }
}
