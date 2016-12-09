/*
 * 2012, U.S. Government, National Security Agency, National Information Assurance Research Laboratory
 * 
 * This is a work of the UNITED STATES GOVERNMENT and is not subject to copyright protection in the United States. Foreign copyrights may apply.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * �Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer. 
 * �Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution. 
 * �Neither the name of the NATIONAL SECURITY AGENCY/NATIONAL INFORMATION ASSURANCE RESEARCH LABORATORY nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package gov.niarl.his.privacyca;

import java.io.*;
import java.math.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.security.*;
import java.security.SecureRandom;
import java.security.cert.*;
import java.security.cert.Certificate;
import java.security.spec.*;
import java.security.interfaces.*;
import javax.security.auth.x500.X500Principal;
import javax.security.cert.CertificateException;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.x509.*;
import org.bouncycastle.x509.extension.AuthorityKeyIdentifierStructure;
import org.bouncycastle.x509.extension.SubjectKeyIdentifierStructure;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.jce.provider.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.util.BitSet;

/**
 * <p>The utils class contains functions that fall into two categories: those that provide 
 * utility to other classes in this package; and those that perform some cross-class 
 * functionality that is specific to this package.</p>
 * <p>This package was created for performing as a Privacy Certification Authority (Privacy CA), 
 * as specified by the Trusted Computing Group. The function <b>processIdentityRequest</b> encompasses 
 * the role of a Privacy CA by taking an identity request, processing it with a CA signing key, 
 * and producing the specified data blobs containing a certificate.</p>
 * <p>The function <b>makeEkCert</b> works similarly to create a certificate for a TPM's Endorsement 
 * Key (EK certificate). This is not a defined role of a Privacy CA, however the process of 
 * creating an EK certificate is not covered in any TCG documentation, and the process of 
 * creating such a certificate is very similar to that used for processing an identity request.</p>
 * <p>The creation of an identity request is generally performed by the TSS function 
 * <b>Tspi_TPM_CollateIdentityRequest</b>. There are times, however, when it may be necessary to 
 * fabricate an identity request from its basic components. The function <b>createIdentityRequest</b> 
 * can be used to do just that. It is flexible enough to create a request in the format of any 
 * of the popular flavors of TSS.</p>
 * 
 * <p>Some of the functions provided in this class require the BouncyCastle security provider library, version 141 (for Java 1.5, this is the library named bcprov-jdk15-141.jar).</p>
 * 
 * @author schawki
 *
 */
public class TpmUtils {
        private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TpmUtils.class);
	/**
	 * Converts an integer to a four-byte array.
	 * 
	 * @param integer The integer to convert.
	 * @return A byte array with a length of 4 representing the integer as a UINT32.
	 * @throws TpmUnsignedConversionException This function does not work if the integer to convert is a negative number.
	 */
	public static byte[] intToByteArray (final int integer) 
			throws TpmUnsignedConversionException {
		if (integer < 0) throw new TpmUnsignedConversionException("Cannot convert negative integer to UINT32 array: " + integer);
		byte [] toReturn = new byte[4];
		toReturn[3] = (byte)((integer >> 0)&0x000000ff);
		toReturn[2] = (byte)((integer >> 8)&0x000000ff);
		toReturn[1] = (byte)((integer >>16)&0x000000ff);
		toReturn[0] = (byte)((integer >>24)&0x000000ff);
		return toReturn;
	}
	/**
	 * Converts a short integer to a two-byte array.
	 * 
	 * @param shortInt The short integer to convert.
	 * @return A byte array with a length of 2 representing the integer as a UINT16.
	 * @throws TpmUnsignedConversionException This function does not work if the integer to convert is a negative number.
	 */
	public static byte[] shortToByteArray (final short shortInt) 
			throws TpmUnsignedConversionException {	
		if (shortInt < 0) throw new TpmUnsignedConversionException("Cannot convert negative short to UINT16 array: " + shortInt);
		byte [] toReturn = new byte[2];
		toReturn[1] = (byte)(shortInt&0x000000ff);
		toReturn[0] = (byte)((shortInt >> 8)&0x000000ff);
		return toReturn;
	}
	/**
	 * Extracts four bytes in the form of a UINT32 from a ByteArrayInputStream and converts it to an integer. The byte stream will be reduced
	 * by four bytes. Note: the Java integer is signed, but the UINT32 is not (by definition). The integer was chosen to hold the value of a UINT32 
	 * because both are four bytes in size, even though the Java integer has a positive max value half that of the UINT32. As this conversion 
	 * function converts from a UINT32, there sill never be a negative value extracted. The possibility exists that a UINT32 with a value greater 
	 * than the integer MAX_VALUE will attempt to be extracted using this function. In that case, an exception will be thrown. This situation
	 * is not likely to occur, as UINT32 values are used for two purposes by the TCG: defined attributes/flags, and size values. When a UINT32 is 
	 * used as an attribute or flag, the value is not as important as is the bit order. (Also, there are no attributes of flags in which the most 
	 * significant bit is set.) As a size, the value is usually in numbers of bytes, and occasionally in number of bits. In either case, there is 
	 * not likely to be a size, even in bits, that comes near to approaching the max value of integer. 
	 * 
	 * @param source The ByteArrayInputStream from which the UINT32 is to be extracted.
	 * @return An integer with the value of the UINT32.
	 * @throws TpmUnsignedConversionException The UINT32 being extracted is too large to be stored in an integer.
	 * @throws TpmBytestreamResouceException A UINT32 is four bytes in length, this exception is throws in there are not at least 4 bytes available to extract.
	 */
	public static int getUINT32(ByteArrayInputStream source) 
			throws TpmUnsignedConversionException,
			TpmBytestreamResouceException {

            if (source.available() < 4) {
                throw new TpmBytestreamResouceException("There is not enough room in the bytestream to extract a UINT32.");
            }
            int retval;
            try {
                byte[] temp = IOUtils.toByteArray(source,4);

                //int k = source.read(temp, 0, 4);
                if ((temp[0] & 0x80) == 0x80) {
                    throw new TpmUnsignedConversionException("Cannot convert UINT32 to signed Integer: too large - would be converted to negative.");
                }
                retval = (int) ((temp[0] << 24 & 0xff000000)
                        + (int) (temp[1] << 16 & 0x00ff0000)
                        + (int) (temp[2] << 8 & 0x0000ff00)
                        + (int) (temp[3] << 0 & 0x000000ff));
                return retval;
            } 
            catch (IOException e) {
                log.error("Error converting to UINT32, Exception: {}", e);
                throw new TpmBytestreamResouceException("Error converting to UINT32");
            }
           
	}
	/**
	 * Extracts a UINT16 from a bytestream and stores is as a short. See getUINT32 for issues that apply to this function.
	 * 
	 * @param source The ByteArrayInputStream from which the UINT16 will be extracted.
	 * @return A short with the value of the UINT16 extracted.
	 * @throws TpmUnsignedConversionException Thrown if the UINT16 is too large to be stored as a short.
	 * @throws TpmBytestreamResouceException Thrown if there are not at least two bytes available in the bytestream to extract.
	 */
	public static short getUINT16(ByteArrayInputStream source) 
			throws TpmUnsignedConversionException,
			TpmBytestreamResouceException {
		if (source.available() < 2) {
			throw new TpmBytestreamResouceException("There is not enough room in the bytestream to extract a UINT32.");
		}
		int retval;
                try {
		//byte[] temp = new byte[2];
                byte[] temp = IOUtils.toByteArray(source,2);
		//int k = source.read(temp, 0, 2);
		if ((temp[0]&0x80) == 0x80) throw new TpmUnsignedConversionException("Cannot convert UINT16 to signed Short: too large - would be converted to negative.");
		retval = (int)((temp[0]<<8)&0x0000ff00) + 
			 	 (int)((temp[1]<<0)&0x000000ff);
		return (short)retval;
                }
                catch (IOException e) {
                log.error("Error converting to UINT16, Exception: {}", e);
                throw new TpmBytestreamResouceException("Error converting to UINT16");
            }
	}
	/**
	 * This exception is thrown to indicate an error in converting between a signed and an unsigned 
	 * number of equal bit-lengths. This can be a result of attempting to convert an unsigned number 
	 * of capacity greater than that of an equal-length signed number, or attempting to convert a 
	 * negative signed number to an unsigned number.
	 * 
	 * @author schawki
	 *
	 */
	public static class TpmUnsignedConversionException extends Exception {
		private static final long serialVersionUID = 0;
		public TpmUnsignedConversionException(String msg) {
			super(msg);
		}
	}
	/**
	 * This error is thrown when attempting to read more bytes from a bytestream or array than are 
	 * available to read. For example, attempting to pull a four-byte UINT32 from a stream containing 
	 * only three bytes.
	 * 
	 * @author schawki
	 *
	 */
	public static class TpmBytestreamResouceException extends Exception {
		private static final long serialVersionUID = 0;
		public TpmBytestreamResouceException(String msg) {
			super(msg);
		}
	}
	/**
	 * Extracts a specified number of bytes from a ByteArrayInputStream and places them into a byte array.
	 * 
	 * @param source The ByteArrayInputStream from which to extract the requested number of bytes. 
	 * @param size The number of bytes to extract.
	 * @return A byte array of size <b>size</b>.
	 * @throws TpmBytestreamResouceException Thrown if the number of bytes requested exceeds the number of available bytes in the bytestream.
	 */
	public static byte[] getBytes(ByteArrayInputStream source, int size) 
			throws TpmBytestreamResouceException {
		if (source.available() < size) {
			throw new TpmBytestreamResouceException("There are not enough available bytes in the bytestream to extract the requested number.");
		}
                try{
		//byte[] retval = new byte[size];
                byte[] retval = IOUtils.toByteArray(source, size);
		//int k = source.read(retval, 0, size);
		return retval;
                }
                catch (IOException e) {
                log.error("Error getting bytes, Exception: {}", e);
                throw new TpmBytestreamResouceException("Error getting bytes");
            }
	}
    public static X509Certificate makeCert(TpmPubKey aik, String sanLabel, RSAPrivateKey privKey, X509Certificate caCert, int validityDays, int level) throws InvalidKeySpecException,
            NoSuchAlgorithmException,
            CertificateEncodingException,
            NoSuchProviderException,
            SignatureException,
            InvalidKeyException {
        Security.addProvider(new BouncyCastleProvider());
        X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();
        certGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
        certGen.setIssuerDN(caCert.getSubjectX500Principal());
        certGen.setNotBefore(new java.sql.Time(System.currentTimeMillis()));
        Calendar expiry = Calendar.getInstance();
        expiry.add(Calendar.DAY_OF_YEAR, validityDays);
        certGen.setNotAfter(expiry.getTime());
        certGen.setSubjectDN(new X500Principal(""));
        certGen.setPublicKey(aik.getKey());
        certGen.setSignatureAlgorithm("SHA256withRSA");
        certGen.addExtension(org.bouncycastle.asn1.x509.X509Extension.subjectAlternativeName /*org.bouncycastle.asn1.x509.X509Extensions.SubjectAlternativeName*/, true, new GeneralNames(new GeneralName(GeneralName.rfc822Name, sanLabel)));
        X509Certificate cert = certGen.generate(privKey, "BC");
        return cert;
    }

    /**
     * Creates a new X509 V3 certificate for use as an Attestation Identity Key (AIK) using the BouncyCastle provider. The certificate is designed in the direction of the Trusted Computing Group's specification of certificates for the Trusted Platform Module, although in its current form this function does not meet the standard. To that extent, the Subject Name field is left blank, and the V3 Subject Alternative Name field is marked critical and populated with the ID Label specified in the supplied TPM_Identity_Proof structure.
     *
     * @param idProof The TPM_Identity_Proof structure, used for the identity label field.
     * @param privKey The Privacy CA's private key for signing the certificate.
     * @param caCert The Privacy CA's public key certificate.
     * @param validityDays The number of days until the created certificate expires, from the time this function is run.
     * @param level Currently not used.
     * @return An AIK certificate.
     * @throws InvalidKeySpecException Passed on from the BouncyCastle certificate generator.
     * @throws NoSuchAlgorithmException Passed on from the BouncyCastle certificate generator.
     * @throws CertificateEncodingException Passed on from the BouncyCastle certificate generator.
     * @throws NoSuchProviderException Thrown if the BouncyCastle provider cannot be found.
     * @throws SignatureException Passed on from the BouncyCastle certificate generator.
     * @throws InvalidKeyException Passed on from the BouncyCastle certificate generator.
     */
    public static X509Certificate makeCert(TpmIdentityProof idProof, RSAPrivateKey privKey, X509Certificate caCert, int validityDays, int level)
            throws InvalidKeySpecException,
            NoSuchAlgorithmException,
            CertificateEncodingException,
            NoSuchProviderException,
            SignatureException,
            InvalidKeyException {
        return makeCert(idProof.getAik(), new String(idProof.getIdLableBytes()), privKey, caCert, validityDays, level);
    }
	/**
	 * Pulls the system time in "MMM d, yyyy h:mm:ss a" format as a string, suitable for use in a log file.
	 * @return String as described above.
	 */
	public static String getTime() {
		Calendar time = Calendar.getInstance();
		SimpleDateFormat formatter = new SimpleDateFormat("MMM d, yyyy h:mm:ss a");
		String newString = formatter.format(time.getTime());
		return newString;
	}
	/**
	 * Creates a key pair and associated certificate for a certificate authority. An RSA key pair 
	 * of specified size is stored with the self-signed certificate in an encrypted PKCS 12 key 
	 * store file. The format of the certificate and PKCS 12 file are a replica of what is created 
	 * by OpenSSL.
	 * 
	 * @param keySize The size (in bits) of the RSA key to create
	 * @param caName The subject name for the new Certificate Authority (do not include "CN=")
	 * @param newP12Pass The password for encrypting the PKCS 12 file
	 * @param p12FileName The name for the PKCS 12 key store file (should end with .p12)
	 * @param validityDays The number of days the certificate should be valid before expiring
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws IllegalStateException
	 * @throws SignatureException
	 * @throws KeyStoreException
	 * @throws java.security.cert.CertificateException
	 * @throws IOException
	 */
	public static void createCaP12(int keySize, String caName, String newP12Pass, String p12FileName, int validityDays) 
			throws NoSuchAlgorithmException, 
				InvalidKeyException, 
				IllegalStateException, 
				SignatureException, 
				KeyStoreException, 
				java.security.cert.CertificateException, 
				IOException{
		Security.addProvider(new BouncyCastleProvider());
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		keyGen.initialize(keySize);
		KeyPair keyPair = keyGen.generateKeyPair();
		RSAPrivateKey privKey = (RSAPrivateKey)keyPair.getPrivate();
		RSAPublicKey pubKey = (RSAPublicKey)keyPair.getPublic();
		X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();
		certGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
		certGen.setIssuerDN(new X500Principal("CN=" + caName));
		certGen.setNotBefore(new java.sql.Time(System.currentTimeMillis()));
		Calendar expiry = Calendar.getInstance();
		expiry.add(Calendar.DAY_OF_YEAR, validityDays);
		certGen.setNotAfter(expiry.getTime());
		certGen.setSubjectDN(new X500Principal("CN=" + caName));
		certGen.setPublicKey(pubKey);
		certGen.setSignatureAlgorithm("SHA256withRSA");
		certGen.addExtension(X509Extensions.SubjectKeyIdentifier, false, new SubjectKeyIdentifierStructure(pubKey));
		certGen.addExtension(X509Extensions.BasicConstraints, true, new BasicConstraints(true));
		X509Certificate caCert = certGen.generate(privKey);
		certGen.addExtension(X509Extensions.AuthorityKeyIdentifier, false, new AuthorityKeyIdentifierStructure(caCert));
		caCert = certGen.generate(privKey);
		FileOutputStream newp12 = new FileOutputStream(p12FileName);
		
		try {
			KeyStore keystore = KeyStore.getInstance("PKCS12");
			keystore.load(null, newP12Pass.toCharArray());
			Certificate [] chain = {caCert};
			keystore.setKeyEntry("1", privKey, newP12Pass.toCharArray(), chain);
			keystore.store(newp12, newP12Pass.toCharArray());
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally{
			newp12.close();
		}
		
	}
	/**
	 * This function creates a p12 file for a client, creating a new RSA key pair of specified size. A certificate generated, signed by a the CA using the specified private key and CA certificate file. Both the client and CA certificates are stored as a chain in the p12 file. The client certificate's serial number is a system time in miliseconds.
	 * 
	 * @param keySize Size of the key to generate
	 * @param subjectName Subject name for the client certificate
	 * @param newP12Pass Password to use for encrypting the p12 file
	 * @param p12FileName name for the generated file
	 * @param validityDays number of days the client certificate should be valid
	 * @param caCert The CA's certificate
	 * @param caPrivKey The CA's private key, for signing the client certificate
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws IllegalStateException
	 * @throws SignatureException
	 * @throws KeyStoreException
	 * @throws java.security.cert.CertificateException
	 * @throws IOException
	 */
	public static void createClientP12(int keySize, String subjectName, String newP12Pass, String p12FileName, int validityDays, X509Certificate caCert, RSAPrivateKey caPrivKey) 
			throws NoSuchAlgorithmException, 
				InvalidKeyException, 
				IllegalStateException, 
				SignatureException, 
				KeyStoreException, 
				java.security.cert.CertificateException, 
				IOException{
		Security.addProvider(new BouncyCastleProvider());
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		keyGen.initialize(keySize);
		KeyPair keyPair = keyGen.generateKeyPair();
		RSAPrivateKey privKey = (RSAPrivateKey)keyPair.getPrivate();
		RSAPublicKey pubKey = (RSAPublicKey)keyPair.getPublic();
		X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();
		certGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
		certGen.setIssuerDN(caCert.getSubjectX500Principal());
		certGen.setNotBefore(new java.sql.Time(System.currentTimeMillis()));
		Calendar expiry = Calendar.getInstance();
		expiry.add(Calendar.DAY_OF_YEAR, validityDays);
		certGen.setNotAfter(expiry.getTime());
		certGen.setSubjectDN(new X500Principal("CN=" + subjectName));
		certGen.setPublicKey(pubKey);
		certGen.setSignatureAlgorithm("SHA256withRSA");
		certGen.addExtension(X509Extensions.SubjectKeyIdentifier, false, new SubjectKeyIdentifierStructure(pubKey));
		certGen.addExtension(X509Extensions.AuthorityKeyIdentifier, false, new AuthorityKeyIdentifierStructure(caCert));
		certGen.addExtension(X509Extensions.BasicConstraints, true, new BasicConstraints(false));
		X509Certificate clientCert = certGen.generate(caPrivKey);
		FileOutputStream newp12 = new FileOutputStream(p12FileName);
		
		try {
			KeyStore keystore = KeyStore.getInstance("PKCS12");
			keystore.load(null, newP12Pass.toCharArray());
			System.out.println(clientCert.toString());
			System.out.println(caCert.toString());
			Certificate [] chain = {clientCert, caCert};
			keystore.setKeyEntry("1", privKey, newP12Pass.toCharArray(), chain);
			keystore.store(newp12, newP12Pass.toCharArray());
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally{
			newp12.close();
		}
		
	}
	/**
	 * Creates an Endorsement Key (EK) Certificate. This certificate is not fully meet Trusted Computing Group specifications. Aside from the key and label source, the 
	 * certificate is basically identical to the AIK certificate made by the makeCert function. The label used is "TPM EK Credential". Ideally, an EK certificate should 
	 * only be created once per TPM and stored in the TPM's NVRAM, but this function can be used to provide the certificate while a method is being researched for the 
	 * permanent storage of the EK certificate in NVRAM. 
	 * 
	 * @param pubEkMod The modulus of the public Endorsement Key (EK) in the form of a byte array.
	 * @param privKey The Privacy CA's private signing key.
	 * @param caCert The Privacy CA's public key certificate.
	 * @param validityDays The number of days until expiration, from the time this function is run.
	 * @return An EK certificate for the specified TPM's EK.
	 * @throws NoSuchAlgorithmException Passed on from the BouncyCastle certificate generator.
	 * @throws InvalidKeySpecException Passed on from the BouncyCastle certificate generator.
	 * @throws SignatureException Passed on from the BouncyCastle certificate generator.
	 * @throws NoSuchProviderException Thrown if the BouncyCastle provider cannot be found.
	 * @throws InvalidKeyException Passed on from the BouncyCastle certificate generator.
	 * @throws CertificateEncodingException Passed on from the BouncyCastle certificate generator.
	 */
	public static X509Certificate makeEkCert(byte [] pubEkMod, RSAPrivateKey privKey, X509Certificate caCert, int validityDays) 
			throws NoSuchAlgorithmException, 
			InvalidKeySpecException, 
			SignatureException, 
			NoSuchProviderException, 
			InvalidKeyException, 
			CertificateEncodingException {
		Security.addProvider(new BouncyCastleProvider());
		X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();
		certGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
		certGen.setIssuerDN(caCert.getSubjectX500Principal());
		certGen.setNotBefore(new java.sql.Time(System.currentTimeMillis()));
		Calendar expiry = Calendar.getInstance();
		expiry.add(Calendar.DAY_OF_YEAR, validityDays);
		certGen.setNotAfter(expiry.getTime());
		certGen.setSubjectDN(new X500Principal(""));
		byte [] pubExp = new byte[3];
		pubExp[0] = (byte)(0x01 & 0xff);
//		pubExp[1] = (byte)(0x00 & 0xff);
		pubExp[1] = (byte)(0x00);
		pubExp[2] = (byte)(0x01 & 0xff);
		RSAPublicKey pubEk = TpmUtils.makePubKey(pubEkMod, pubExp);
		certGen.setPublicKey(pubEk);
		certGen.setSignatureAlgorithm("SHA256withRSA");
		certGen.addExtension(org.bouncycastle.asn1.x509.X509Extension.subjectAlternativeName /*org.bouncycastle.asn1.x509.X509Extensions.SubjectAlternativeName*/, true, new GeneralNames(new GeneralName(GeneralName.rfc822Name, "TPM EK Credential")));
		X509Certificate cert = certGen.generate(privKey, "BC");
		return cert;
	}
	/**
	 * Create a Java RSAPublicKey using the specified modulus and public exponent in byte array form.
	 * 
	 * @param modulus The RSA key modulus in the form of a byte array.
	 * @param exponent The RSA public exponent in the form of a byte array.
	 * @return An RSAPublicKey.
	 * @throws NoSuchAlgorithmException Thrown if the Java KeyFactory doesn't know what "RSA" is.
	 * @throws InvalidKeySpecException Thrown if the key material is bad.
	 */
	public static RSAPublicKey makePubKey(byte[] modulus, byte[] exponent) 
			throws NoSuchAlgorithmException, 
			InvalidKeySpecException {
		BigInteger modulusBI = byteArrayToBigInt(modulus);
		BigInteger exponentBI = byteArrayToBigInt(exponent);
		RSAPublicKeySpec newKeySpec = new RSAPublicKeySpec(modulusBI, exponentBI);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		RSAPublicKey newKey = (RSAPublicKey)keyFactory.generatePublic(newKeySpec);
		return newKey;
	}
	/**
	 * Generate an RSAPrivateKey object using a modulus and exponent, where both are provided as byte arrays.
	 * 
	 * @param modulus
	 * @param exponent
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
	public static RSAPrivateKey makePrivKey(byte[] modulus, byte[] exponent) 
			throws NoSuchAlgorithmException, 
			InvalidKeySpecException {
		BigInteger modulusBI = byteArrayToBigInt(modulus);
		BigInteger exponentBI = byteArrayToBigInt(exponent);
		RSAPrivateKeySpec newKeySpec = new RSAPrivateKeySpec(modulusBI, exponentBI);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		RSAPrivateKey newKey = (RSAPrivateKey)keyFactory.generatePrivate(newKeySpec);
		return newKey;
	}
	/**
	 * Create a Java Big Integer from a specified byte array. Intended for converting an RSA modulus or exponent, which is often
	 * specified in the form of a byte array for TPM activities. This function properly accounts for the highest-order bit set to
	 * avoid any complications arising from a conversion to the signed Big Integer. 
	 * 
	 * @param incoming The byte array to convert.
	 * @return The Big Integer with the value of the byte array.
	 */
	private static BigInteger byteArrayToBigInt(byte[] incoming) {
		byte [] tempArray;
		if ((incoming[0]&0x80) == 0x80) {
			tempArray = new byte[incoming.length + 1];
			tempArray[0] = (byte)0x00;
			for (int i = 0; i < incoming.length; i++)
				tempArray[i+1] = incoming[i];
		}
		else {
			tempArray = incoming;
		}
		return new BigInteger(tempArray);
	}
	/**
	 * This function is not yet implemented. The intended purpose is to check the validity of an EK certificate supplied in an identity 
	 * request. A valid certificate is one signed by a trusted entity. Another potential version of validity is the EK being present in 
	 * a database of known TPMs.
	 * 
	 * @param ekCred The EK certificate from the identity proof.
	 * @return <b>True</b>, if EK certificate passes verification.
	 */
	public static boolean verifyTPM(X509Certificate ekCred) {
		return true;
	}
	/**
	 * Creates a string of uppercase hexidecimal duples representing the supplied byte array. They are placed in lines containing a specified number of duples..
	 * 
	 * @param blob The byte array to turn into a string.
	 * @param perLine The number of hexidecimal duples to place on each line.
	 * @return A String, perhaps multi-line.
	 */
	public static String byteArrayToString(byte [] blob, int perLine) {
		String returnVal = "";
		StringBuffer sb = new StringBuffer();
                
                if (blob==null) return returnVal;
                
		for(int i = 0; i < blob.length; i++) {
			String hexDigit = Integer.toHexString((int)blob[i] & 0xff).toUpperCase();
			if (hexDigit.length() == 1)
				hexDigit = "0" + hexDigit;
//				hexDigit = sb.append("0").append(hexDigit).toString();
			returnVal = sb.append(hexDigit).append(" ").toString();
			if (((i+1)%perLine == 0) && (i < (blob.length - 1)))
				returnVal = sb.append("\n").toString();
		}
		return returnVal;
	}
	/**
	 * Retrieve a private key from a PKCS #12 store. It is expected that the P12 file will contain only one private key and one public key certificate.
	 * 
	 * @param filename The name of the P12 file.
	 * @param password The password needed to extract from the specified P12 file.
	 * @return The private key.
	 * @throws KeyStoreException Passed on from called functions.
	 * @throws IOException Passed on from called functions.
	 * @throws NoSuchAlgorithmException Passed on from called functions.
	 * @throws UnrecoverableKeyException Passed on from called functions.
	 * @throws javax.security.cert.CertificateException Passed on from called functions.
	 * @throws java.security.cert.CertificateException Passed on from called functions.
	 */
	public static RSAPrivateKey privKeyFromP12(String filename, String password) 
			throws KeyStoreException, 
			IOException, 
			NoSuchAlgorithmException, 
			UnrecoverableKeyException, 
			javax.security.cert.CertificateException, 
			java.security.cert.CertificateException {
		KeyStore caKs = KeyStore.getInstance("PKCS12");
		FileInputStream fis = new FileInputStream(filename); 
		try {
			caKs.load(fis, password.toCharArray());
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally{
				fis.close();
		}
		
		//caKs.load(ConfigHelper.getResourceAsStream(filename), password.toCharArray());
		Enumeration<String> aliases = caKs.aliases();
		RSAPrivateKey privKey = null;
		while(aliases.hasMoreElements()) {
			String name = aliases.nextElement();
			privKey = (RSAPrivateKey)caKs.getKey(name, password.toCharArray());
		}
		return privKey;
	}
	/**
	 * Retrieve a public key certificate from a PKCS #12 store. It is expected that the P12 file will contain only one private key and one public key certificate.
	 * 
	 * @param filename The name of the P12 file.
	 * @param password The password needed to extract from the specified P12 file.
	 * @return The private key.
	 * @throws IOException Passed on from called functions.
	 * @throws NoSuchAlgorithmException Passed on from called functions.
	 * @throws UnrecoverableKeyException Passed on from called functions.
	 * @throws javax.security.cert.CertificateException Passed on from called functions.
	 * @throws java.security.cert.CertificateException Passed on from called functions.
	 */
	public static X509Certificate certFromP12(String filename, String password) 
			throws KeyStoreException, 
			IOException, 
			NoSuchAlgorithmException, 
			javax.security.cert.CertificateException, 
			java.security.cert.CertificateException {
		KeyStore caKs = KeyStore.getInstance("PKCS12");
		FileInputStream fis =new FileInputStream(filename);
		try {
			caKs.load(fis, password.toCharArray());
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally{
				fis.close();
		}
		
		
		Enumeration<String> aliases = caKs.aliases();
		X509Certificate cert = null;
		while(aliases.hasMoreElements()) {
			String name = aliases.nextElement();
			cert = (X509Certificate)caKs.getCertificate(name);
		}
		return cert;
	}
	/**
	 * Retrieve a certificate as an X509Certificate object from a file (generally .cer or .crt using DER or PEM encoding)
	 * @param filename
	 * @return
	 * @throws KeyStoreException
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws javax.security.cert.CertificateException
	 * @throws java.security.cert.CertificateException
	 */
	public static X509Certificate certFromFile(String filename) 
			throws KeyStoreException, 
			IOException, 
			NoSuchAlgorithmException, 
			javax.security.cert.CertificateException, 
			java.security.cert.CertificateException {
		try(InputStream certStream = new FileInputStream(filename)){
    //		byte [] certBytes = new byte[certStream.available()];
                    byte[] certBytes = IOUtils.toByteArray(certStream);                    
                    javax.security.cert.X509Certificate cert = javax.security.cert.X509Certificate.getInstance(certBytes);
                    return convertX509Cert(cert);
                }
                catch(Exception e){
                    log.error("Error encountered while reading cert from file",e);
                    throw new java.security.cert.CertificateException("Error encountered while reading cert from file");
                }		
	}
	/**
	 * Retrieve a certificate as an X509Certificate object from a byte string, assuming DER encoding.
	 * @param certBytes
	 * @return
	 * @throws CertificateException
	 * @throws CertificateEncodingException
	 * @throws java.security.cert.CertificateException
	 */
	public static X509Certificate certFromBytes(byte [] certBytes) 
			throws CertificateException, 
			CertificateEncodingException, 
			java.security.cert.CertificateException{
//		java.security.cert.CertificateFactory cf = java.security.cert.CertificateFactory.getInstance("X.509");
                java.security.cert.CertificateFactory cf = java.security.cert.CertificateFactory.getInstance("X.509", new BouncyCastleProvider());
		return (java.security.cert.X509Certificate)cf.generateCertificate(new ByteArrayInputStream(certBytes));
	}
	/**
	 * Take an incoming identity request, unpack the contents, create an identity certificate, and return it in the properly formated form.
	 * 
	 * @param idRequestBlob The TPM_Identity_Request, as a byte array, as received from the output of the Tspi_TPM_CollateIdentityRequest TSS function.
	 * @param caPrivKey The Privacy CA's private signing key.
	 * @param caPubCert The Privacy CA's public key certificate.
	 * @param validityDays The number of validity days, after which the certificate will expire.
	 * @return An idResponse, containing both TPM_ASYM_CA_CONTENTS and TPM_SYM_CA_ATTESTATION structures, suitable as input for the Tspi_TPM_ActivateIdentity function.
	 * @throws PrivacyCaException Thrown if an error occurs when processing the request.
	 * @throws TpmUtils.TpmUnsignedConversionException Passed from called functions, this most likely reflects a poorly constructed Identity Request or its base Identity Proof. 
	 * @throws BadPaddingException Passed if an encryption/decryption error occurs.
	 * @throws IllegalBlockSizeException Passed if an encryption/decryption error occurs.
	 * @throws InvalidAlgorithmParameterException Passed if an encryption/decryption error occurs.
	 * @throws NoSuchPaddingException Passed if an encryption/decryption error occurs.
	 * @throws NoSuchAlgorithmException Passed if an encryption/decryption error occurs.
	 * @throws InvalidKeyException Passed if an encryption/decryption error occurs.
	 * @throws CertificateEncodingException Passed if an certificate creation error occurs.
	 * @throws IOException Passed if an certificate creation error occurs.
	 * @throws InvalidKeySpecException Passed if an certificate creation error occurs.
	 * @throws SignatureException Passed if an certificate creation error occurs.
	 * @throws NoSuchProviderException Passed if an certificate creation error occurs.
	 * @throws javax.security.cert.CertificateException Passed if an certificate creation error occurs.
	 * @throws java.security.cert.CertificateException Passed if an certificate creation error occurs.
	 * @throws TpmUtils.TpmBytestreamResouceException Passed from called functions, this most likely reflects a poorly constructed Identity Request or its base Identity Proof. 
	 */
	public static idResponse processIdentityRequest (byte [] idRequestBlob, RSAPrivateKey caPrivKey, X509Certificate caPubCert, int validityDays) 
			throws PrivacyCaException, 
			TpmUtils.TpmUnsignedConversionException, 
			BadPaddingException, 
			IllegalBlockSizeException, 
			InvalidAlgorithmParameterException, 
			NoSuchPaddingException, 
			NoSuchAlgorithmException, 
			InvalidKeyException, 
			CertificateEncodingException, 
			IOException, 
			InvalidKeySpecException, 
			SignatureException, 
			NoSuchProviderException, 
			javax.security.cert.CertificateException, 
			java.security.cert.CertificateException,
			TpmUtils.TpmBytestreamResouceException {
		TpmIdentityRequest request = new TpmIdentityRequest(idRequestBlob);
		TpmIdentityProof idProof = request.decrypt(caPrivKey);
		if (idProof.getEkCredBytes().length == 0) throw new PrivacyCaException("PrivacyCaException: Error parsing TPM_IDENTITY_PROOF: there is no endorsement credential.");
		if (!idProof.checkValidity((RSAPublicKey)caPubCert.getPublicKey())) throw new PrivacyCaException("Request does not pass integrity check: identity binding does not pass verification.");
		TpmSymCaAttestation symPart = new TpmSymCaAttestation();
		symPart.setAikCredential(TpmUtils.makeCert(idProof, caPrivKey, caPubCert, validityDays, 0));
		TpmAsymCaContents asymPart = new TpmAsymCaContents();
		//pass symmetric encryption mode here
		TpmSymmetricKey temp = symPart.encrypt(TpmKeyParams.TPM_ALG_AES, TpmKeyParams.TPM_ES_SYM_CBC_PKCS5PAD, request.getSymKeyParams().getTrouSerSmode(), !request.getSymkeyEncscheme()); //see the !
		asymPart.setSymmetricKey(temp);
		asymPart.setDigest(idProof.getAik());
		//pass asymmetric encryption mode here
		asymPart.encrypt((RSAPublicKey)idProof.getEkCred().getPublicKey(), !request.getOeapMode());//see the !
		idResponse returnval = new idResponse(asymPart, symPart);
		return returnval;
	}
	/**
	 * Used by the Privacy CA (version 1) to process Identity Requests that do not contain an EC. The EC is not validated, and the AIC is returned in plaintext. The client can then get the AIC without using ActivateIdentity. This is here because the Windows (NTRU) TSS client is broken, and cannot include the EC in an Identity Request, and also cannot perform an ActivateIdentity properly. 
	 * 
	 * @param idRequestBlob The incomming Identity Request with no EC
	 * @param caPrivKey The Privacy CA's private key
	 * @param caPubCert The Privacy CA's certificate
	 * @param validityDays The number of days before AIC expiration
	 * @return the AIC in the form of an X509Certificate
	 * @throws PrivacyCaException
	 * @throws TpmUtils.TpmUnsignedConversionException
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws InvalidAlgorithmParameterException
	 * @throws NoSuchPaddingException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws CertificateEncodingException
	 * @throws IOException
	 * @throws InvalidKeySpecException
	 * @throws SignatureException
	 * @throws NoSuchProviderException
	 * @throws javax.security.cert.CertificateException
	 * @throws java.security.cert.CertificateException
	 * @throws TpmUtils.TpmBytestreamResouceException
	 */
	public static X509Certificate partiallyProcessIdentityRequest (byte [] idRequestBlob, RSAPrivateKey caPrivKey, X509Certificate caPubCert, int validityDays) 
			throws PrivacyCaException, 
			TpmUtils.TpmUnsignedConversionException, 
			BadPaddingException, 
			IllegalBlockSizeException, 
			InvalidAlgorithmParameterException, 
			NoSuchPaddingException, 
			NoSuchAlgorithmException, 
			InvalidKeyException, 
			CertificateEncodingException, 
			IOException, 
			InvalidKeySpecException, 
			SignatureException, 
			NoSuchProviderException, 
			javax.security.cert.CertificateException, 
			java.security.cert.CertificateException,
			TpmUtils.TpmBytestreamResouceException {
		TpmIdentityRequest request = new TpmIdentityRequest(idRequestBlob);
		TpmIdentityProof idProof = request.decrypt(caPrivKey);
		if (!idProof.checkValidity((RSAPublicKey)caPubCert.getPublicKey())) throw new PrivacyCaException("Request does not pass integrity check: identity binding does not pass verification.");
		return TpmUtils.makeCert(idProof, caPrivKey, caPubCert, validityDays, 0);
	}
	/**
	 * Generate a new identity request. User supplied data for a new request is the identity label 
	 * and the Privacy CA's public key. This function relies upon a method to obtain the public key 
	 * of a newly-created identity key (to be certified by the Privacy CA), an identity binding 
	 * signed by the TPM, and any certificates available from the TPM's non-volatile storage 
	 * (Endorsement, Platform, and/or Conformance certificates). Note: the endorsement certificate 
	 * must be present to have a complete request! <b>The functionality to get this TPM-supplied 
	 * data is currently not available.</b>
	 * 
	 * @param idLabel A string, to be submitted in ASCII, to be used as the subject alternative name for the identity certificate
	 * @param caPubKey The public key of the Privacy CA
	 * @param IV A boolean flag to specify the placement of the symmetric encryption initialization vector (<b>true</b> indicates that IV should be placed at the beginning of symmetrically encrypted blob instead of the symmetric key parameters)
	 * @param symKey A boolean flag to specify the symmetric key encryption scheme flag (<b>true</b> indicates the encryption scheme should be specified as "TSS_ES_NONE")
	 * @param oaep A boolean flag to indicate if the asymmetric OAEP padding will use the string specified in the TSS 1.1b "Main" document (<b>true</b> indicates  that the string should be left blank)
	 * @return
	 * @throws TpmUtils.TpmUnsignedConversionException
	 * @throws NoSuchPaddingException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidAlgorithmParameterException
	 * @throws InvalidKeyException
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws IOException
	 */
	public static TpmIdentityRequest createIdentityRequest(String idLabel, RSAPublicKey caPubKey, boolean IV, boolean symKey, boolean oaep)
			throws TpmUtils.TpmUnsignedConversionException,
			NoSuchPaddingException,
			NoSuchAlgorithmException,
			InvalidAlgorithmParameterException,
			InvalidKeyException,
			BadPaddingException,
			IllegalBlockSizeException,
			IOException {
		byte [] identityBinding = "".getBytes();
		byte [] identityKey = "".getBytes(); //must be just the modulus!!!
		byte [] endorsementCert = "".getBytes();
		byte [] platformCert = "".getBytes();
		byte [] conformanceCert = "".getBytes();
		// Assemble Identity Proof
		TpmIdentityProof idProof = new TpmIdentityProof(idLabel.getBytes(), identityBinding, new TpmPubKey(identityKey), endorsementCert, platformCert, conformanceCert, IV, symKey, oaep);
		// Encrypt Identity Proof into Identity Request using Privacy CA public key
		TpmIdentityRequest idReq = new TpmIdentityRequest(idProof, caPubKey);
		return idReq;
	}
	/**
	 * Fills a 16 byte array with random data, using nanoTime.
	 * 
	 * @return A byte array of length 16 containing new random data.
	 * @throws IOException
	 */
	public static byte [] createRandomBytes(int numBytes) 
			throws IOException {
		SecureRandom random = new SecureRandom();		
		//byte [] randomBytes = longToByteArray(random.nextLong());
		byte [] randomBytes = new byte[numBytes];
		random.nextBytes(randomBytes);
		return randomBytes;
	}
	/**
	 * Encode an X509 Certificate in the PEM (base64) encoding format.
	 * 
	 * @param cert The certificate to encode.
	 * @return A String with the base64 encoded certificate.
	 * @throws CertificateEncodingException Thrown if there is a problem with the certificate.
	 */
	public static String pemEncodeCert(X509Certificate cert) 
			throws CertificateEncodingException {
		return "-----BEGIN CERTIFICATE-----" + base64encode(cert.getEncoded(), false) + "-----END CERTIFICATE-----";
	}
	/**
	 * Base64 encode a byte array.
	 * 
	 * @param toEncode The byte array to encode.
	 * @param breakLines Set <b>true</b> if it is desired to place line break at every 76 characters, per spec (not done often).
	 * @return The String of the encoded array.
	 */
	public static String base64encode(byte [] toEncode, boolean breakLines) {
		StringBuffer sb =new StringBuffer();
		char[] charArray = new String(Base64.encode(toEncode)).toCharArray();
		String toReturn = "";
		for (int i = 0; i < charArray.length; i++){
			if(breakLines){ if((i%64 == 0)) toReturn = sb.append("\n").toString(); } else { toReturn = sb.append(charArray[i]).toString(); }
		}
		return toReturn;
	}
	public static byte[] base64decode(String encoded){
		return Base64.decode(encoded);
	}
	/**
	 * Convert a <b>javax</b> X509Certificate to a <b>java</b> X509Certificate.
	 * 
	 * @param cert A certificate in <b>javax.security.cert.X509Certificate</b> format
	 * @return A certificate in <b>java.security.cert.X509Certificate</b> format
	 */
	public static java.security.cert.X509Certificate convertX509Cert(javax.security.cert.X509Certificate cert)
			throws java.security.cert.CertificateEncodingException,
			javax.security.cert.CertificateEncodingException,
			java.security.cert.CertificateException,
			javax.security.cert.CertificateException {
		java.security.cert.CertificateFactory cf = java.security.cert.CertificateFactory.getInstance("X.509");
		return (java.security.cert.X509Certificate)cf.generateCertificate(new ByteArrayInputStream(cert.getEncoded()));
	}
	/**
	 * Convert a <b>java</b> X509Certificate to a <b>javax</b> X509Certificate.
	 * 
	 * @param cert A certificate in <b>java.security.cert.X509Certificate</b> format
	 * @return A certificate in <b>javax.security.cert.X509Certificate</b> format
	 */
	public static javax.security.cert.X509Certificate convertX509Cert(java.security.cert.X509Certificate cert)
			throws java.security.cert.CertificateEncodingException,
			javax.security.cert.CertificateEncodingException,
			java.security.cert.CertificateException,
			javax.security.cert.CertificateException {
		return javax.security.cert.X509Certificate.getInstance(cert.getEncoded());
	}
	/**
	 * Given a string of hexadecimal characters, convert to a byte array. No checks are performed to ensure that the string is all valid hexidecimal characters (0-9, a-f, A-F) or that there is an even number of characters. 
	 * @param s The hexadecimal string
	 * @return A byte array
	 */
	public static byte[] hexStringToByteArray(String s) {
		int sizeInt = s.length()/2;
		byte [] returnArray = new byte[sizeInt];
		String byteVal;
		for (int i = 0; i < sizeInt; i++) {
			int index = 2 * i;
			byteVal = s.substring(index, index + 2);
			returnArray[i] = (byte)(Integer.parseInt(byteVal, 16));
//			returnArray[i] = (byte)(Integer.parseInt(byteVal, 16) & 0xff);
		}
		return returnArray;
	}
	/**
	 * Convert a byte array to a hexidecimal character string. The string will have no delimeter between hexidecimal duples, and has no line breaks.
	 * @param b Byte array to convert
	 * @return A string of hexidecimal characters
	 */
	public static String byteArrayToHexString(byte[] b) {
		StringBuffer sb = new StringBuffer();
		String returnStr = "";
		for (int i = 0; i < b.length; i++) {
			String singleByte = Integer.toHexString(b[i] & 0xff);
			if (singleByte.length() != 2) singleByte = "0" + singleByte;
//			returnStr += singleByte;
			returnStr = sb.append(singleByte).toString();
		}
		return returnStr;
	}
	/**
	 * Convert a byte array to a hexidecimal character string, in a format that can be placed as a parameter in C++. The hexidecimal byte duples are each prefixed with "0x" and delimted with a comma and space (", "). Example: "0x0a, 0xbc, "
	 * @param b Byte array to convert
	 * @return String in the format described above
	 */
	public static String byteArrayToCppHexString(byte[] b){
		StringBuffer sb =new StringBuffer();
		String returnStr = "";
		for (int i = 0; i < b.length; i++) {
			String singleByte = Integer.toHexString(b[i] & 0xff);
			if (singleByte.length() != 2) singleByte = "0" + singleByte;
			returnStr = sb.append("0x").append(singleByte).append(", ").toString();
		}
		return returnStr;
	}
	/**
	 * Concatenate two byte arrays into one, in the order they are specified.
	 * @param blob1 Byte array to be placed first in the concatenation
	 * @param blob2 Byte array to be placed last in the concatenation
	 * @return 
	 */
	public static byte[] concat(byte[] blob1, byte[] blob2){
		byte[] toReturn = new byte[blob1.length + blob2.length];
		System.arraycopy(blob1, 0, toReturn, 0, blob1.length);
		System.arraycopy(blob2, 0, toReturn, blob1.length, blob2.length);
		return toReturn;
	}
	/**
	 * Perform a SHA-1 hash of a given byte array
	 * @param blob Byte array to hash
	 * @return SHA-1 hash of the specified byte array. Should always be 20 bytes in length
	 * @throws NoSuchAlgorithmException
	 */
	public static byte[] sha1hash(byte[] blob)
			throws NoSuchAlgorithmException{
		byte[] toReturn;
		MessageDigest md = MessageDigest.getInstance("SHA1");
		md.update(blob);
		toReturn = md.digest();
		return toReturn;
	}
	/**
	 * Perform an asymmetric encryption of a byte array in the way specified by the TCG for all TPM-related operations, using the given OAEP string (in case it is not the TCG-specified "TCPA")
	 * @param payload Byte array to encrypt
	 * @param pubKey RSA public key to use for encryption
	 * @param OAEPstring The OAEP string to use for padding
	 * @return An encrypted blob of the length of the pubKey's modulus
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws InvalidAlgorithmParameterException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public static byte[] tcgAsymEncrypt(byte[] payload, RSAPublicKey pubKey, String OAEPstring)
			throws NoSuchAlgorithmException,
				NoSuchPaddingException,
				InvalidKeyException,
				InvalidAlgorithmParameterException,
				IllegalBlockSizeException,
				BadPaddingException{
		OAEPParameterSpec oaepSpec = new OAEPParameterSpec("Sha1", "MGF1", MGF1ParameterSpec.SHA1, new PSource.PSpecified(OAEPstring.getBytes()));
		Cipher asymCipher = Cipher.getInstance("RSA/ECB/OAEPWithSha1AndMGF1Padding");
		asymCipher.init(Cipher.PUBLIC_KEY, pubKey, oaepSpec);
		asymCipher.update(payload);
		byte [] toReturn = asymCipher.doFinal();
		return toReturn;
	}
	/**
	 * Perform an asymmetric encryption of a byte array in the way specified by the TCG for all TPM-related operations, using the OAEP string "TCPA" as specified.
	 * @param payload Byte array to encrypt
	 * @param pubKey RSA public key to use for encryption
	 * @return The encrypted blob of the length of the pubKey's modulus
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws InvalidAlgorithmParameterException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public static byte[] tcgAsymEncrypt(byte[] payload, RSAPublicKey pubKey)
			throws NoSuchAlgorithmException,
				NoSuchPaddingException,
				InvalidKeyException,
				InvalidAlgorithmParameterException,
				IllegalBlockSizeException,
				BadPaddingException{
		return tcgAsymEncrypt(payload, pubKey, "TCPA");
	}
	/**
	 * Perform a symmetric encryption of a byte array in the way specified by the TCG for all TPM-related symmetric encryption activities. The given key and IV are used.
	 * @param payload Byte array to encrypt
	 * @param key Symmetric (AES) key to use. Exception will be thrown if key is an invalid length.
	 * @param iv Initialization Vector to use. Exception will be thrown if IV is an invalid length.
	 * @return Encrypted byte blob.
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws InvalidAlgorithmParameterException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public static byte[] tcgSymEncrypt(byte[] payload, byte[] key, byte[] iv)
			throws NoSuchAlgorithmException, 
				NoSuchPaddingException, 
				InvalidKeyException, 
				InvalidAlgorithmParameterException, 
				IllegalBlockSizeException, 
				BadPaddingException{
		Cipher symCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		IvParameterSpec ivSpec = new IvParameterSpec(iv);
		SecretKeySpec symKey = new SecretKeySpec(key, "AES");
		symCipher.init(Cipher.ENCRYPT_MODE, symKey, ivSpec);
		byte [] toReturn = symCipher.doFinal(payload);
		return toReturn;
	}
	/**
	 * Create a random 128-bit value that can be used as an AES key or IV.
	 * @return
	 * @throws IOException
	 */
	public static byte[] newRandomAESValue() //key or iv
			throws IOException{
		return TpmUtils.createRandomBytes(16);
	}
	/**
	 * Decrypt an TCG-style asymmetrically encrypted byte blob, given the correct RSA private key and OAEP string. If properly encrypted, the OAEP string should be "TCPA" (less the quotes).
	 * @param ciphertext Asymmetrically encrypted byte array
	 * @param privKey The RSA private key to be used to decrypt the ciphertext
	 * @param OAEPstring The OAEP string that was used for padding (should be "TCPA")
	 * @return The decrypted byte array
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws InvalidAlgorithmParameterException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public static byte[] tcgAsymDecrypt(byte[] ciphertext, RSAPrivateKey privKey, String OAEPstring) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException{
		Cipher asymCipher = Cipher.getInstance("RSA/ECB/OAEPWithSha1AndMGF1Padding");
		OAEPParameterSpec oaepSpec = new OAEPParameterSpec("Sha1", "MGF1", MGF1ParameterSpec.SHA1, new PSource.PSpecified(OAEPstring.getBytes()));
		asymCipher.init(Cipher.PRIVATE_KEY, privKey, oaepSpec);
		asymCipher.update(ciphertext);
		byte[] toReturn = asymCipher.doFinal();
		return toReturn;
	}
	/**
	 * Decrypt an AES/CBC/PKCS5Paddded symmetrically encrypted blob, using the given key and IV.
	 * @param ciphertext The encrypted byte array
	 * @param key The key used to perform the decryption
	 * @param iv The Initialization Vector used
	 * @return The decrypted byte array
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws InvalidAlgorithmParameterException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public static byte[] tcgSymDecrypt(byte[] ciphertext, byte[] key, byte[] iv) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException{
		Cipher symCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		IvParameterSpec ivSpec = new IvParameterSpec(iv);
		symCipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), ivSpec);
		return symCipher.doFinal(ciphertext);
	}

    /**
     *
     * @param key
     * @param symCaAttestation
     * @return
     * @throws java.io.IOException
     * @throws TpmUnsignedConversionException
     * @throws TpmBytestreamResouceException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws InvalidAlgorithmParameterException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    public static byte[] decryptSymCaAttestation(byte[]key, byte[] symCaAttestation) throws IOException, TpmUnsignedConversionException, TpmBytestreamResouceException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {        
        // once get the secret, we need to decrypt the sysmCaAttestation to get the aikcert
        /* the symCaAttestation is in the format of TPM_SYM_CA_ATTESTATION
            * UINT32          credSize   -- size of the credential parameter
            * TPM_KEY_PARMS   algorithm  -- indicator and parameters forthe symmetic algorithm
            * BYTE *          credential -- result of encryption TPM_IDENTITY_CREDENTIAL using the session_key and the algorithm indicated "algorithm"
            *          In this context it is: byte [] encryptedBlob = TpmUtils.concat(iv, TpmUtils.tcgSymEncrypt(challengeRaw, key, iv));
         */
        ByteArrayInputStream bs = new ByteArrayInputStream(symCaAttestation);        
        int credsize = TpmUtils.getUINT32(bs);   
        TpmKeyParams keyParms = new TpmKeyParams(bs);
        log.debug("Consumed Key Params " + keyParms);
        byte[] iv = new byte[16];
        if(bs.read(iv, 0, iv.length) == -1) {
            throw new IOException("Failed to read iv");
        }
        int ciphertextLen = credsize - 16;
        byte[] ciphertext = new byte[ciphertextLen];
        if(bs.read(ciphertext, 0, ciphertextLen) == -1) {
            throw new IOException("Failed to read Cipher Text");
        }

        return TpmUtils.tcgSymDecrypt(ciphertext, key, iv);
    }

	/**
	 * Generate a Hashed Message Authentication Code for TCS function authentication using the given auth blob and concatenation of all *H1 (1H1, 2H1, etc) values for the function.
	 * @param authBlob 20 byte auth code for the object in question
	 * @param xH1concat A concatenation of all of the authenticated *H1 parameters for the function, e.g. 1H1, 2H1, 3H1, etc. 
	 * @return The hmac blob suitable to be used for passing as a TCS parameter
	 * @throws Exception
	 */
	public static byte[] hmac(byte[] authBlob, byte[] xH1concat) throws Exception{
		Mac mac = Mac.getInstance("HmacSha1");
		SecretKey key = new SecretKeySpec(authBlob, "HmacSha1");
		mac.init(key);
		mac.update(xH1concat);
		return mac.doFinal();
	}
	/**
	 * Returns <b>true</b> if both byte arrays sent in parameters are the same length and have the exact same contents for each respective elements.
	 * @param array1
	 * @param array2
	 * @return
	 */
	public static boolean compareByteArrays(byte[] array1, byte[] array2){
		if(array1.length != array2.length)
			return false;
		for(int i = 0; i < array1.length; i++)
			if(array1[i] != array2[i])
				return false;
		return true;
	}
	/**
	 * Get the system's Fully Qualified Domain Name as a string
	 * @return the system's FQDN
	 */
	public static String getHostname(){
		String hostname = "";
		try{
			//hostname = InetAddress.getLocalHost().getHostName();
			hostname = InetAddress.getLocalHost().getCanonicalHostName();
		}
		catch (UnknownHostException u){
			StringTokenizer st = new StringTokenizer(u.getMessage());
			while (st.hasMoreTokens()) hostname = st.nextToken();
		}
		return hostname;
	}
}
