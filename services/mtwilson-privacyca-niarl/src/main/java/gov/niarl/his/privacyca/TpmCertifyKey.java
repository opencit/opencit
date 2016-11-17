package gov.niarl.his.privacyca;

import gov.niarl.his.privacyca.TpmUtils.TpmBytestreamResouceException;
import gov.niarl.his.privacyca.TpmUtils.TpmUnsignedConversionException;

import java.io.ByteArrayInputStream;

public class TpmCertifyKey {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TpmCertifyKey.class);
    private static int TPM_SHA1_160_HASH_LEN = 20;
    private static int TPM_SHA1BASED_NONCE_LEN = TPM_SHA1_160_HASH_LEN;
    private byte[] structVer = {(byte) 0x01, (byte) 0x01, (byte) 0x00, (byte) 0x00};
    private short tpmKeyUsage = 0;//UINT16
    private int tpmKeyFlags = 0;//UINT32
    private byte tpmAuthDataUsage = (byte) 0x00;//BYTE (just 1)
    private TpmKeyParams keyParms = null;
    private byte[] publicKeyDigest = null;
    private byte[] nonce = null;
    private byte parentPCRStatus = (byte) 0x00; // 0x00-false, 0x01-true
    private int pcrInfoSize = 0; //UINT32

    public TpmCertifyKey() {
    }

    public TpmCertifyKey(byte[] blob) throws TpmBytestreamResouceException, TpmUnsignedConversionException {
        //ByteArrayInputStream bs = new ByteArrayInputStream(blob);
        try (ByteArrayInputStream bs = new ByteArrayInputStream(blob)) {
            structVer = TpmUtils.getBytes(bs, 4);
            tpmKeyUsage = TpmUtils.getUINT16(bs);
            tpmKeyFlags = TpmUtils.getUINT32(bs);
            tpmAuthDataUsage = TpmUtils.getBytes(bs, 1)[0]; //byte
            keyParms = new TpmKeyParams(bs); //TpmKeyParams
            publicKeyDigest = TpmUtils.getBytes(bs, TPM_SHA1_160_HASH_LEN);
            nonce = TpmUtils.getBytes(bs, TPM_SHA1BASED_NONCE_LEN);
            parentPCRStatus = TpmUtils.getBytes(bs, 1)[0];
            pcrInfoSize = TpmUtils.getUINT32(bs);
        } catch (Exception e) {
            log.error("Error in TpmCertifyKey", e);
            throw new TpmUtils.TpmBytestreamResouceException("Error in TpmCertifyKey");
        }
    }

    public byte[] getPublicKeyDigest() {
        return publicKeyDigest;
    }

    public String getPublicKeyDigestAsString() {
        return TpmUtils.byteArrayToHexString(publicKeyDigest);
    }

    public String getNonceAsString() {
        return TpmUtils.byteArrayToHexString(nonce);
    }

    public boolean getParentPCRStatus() {
        return (parentPCRStatus != 0);
    }

    public int getPcrInfoSize() {
        return pcrInfoSize;
    }

    /**
     * Set the TPM_STRUCT_VER, should always be 0x01010000.
     *
     * @param newStructVer
     */
    public void setStructVer(byte[] newStructVer) {
        structVer = newStructVer;
    }

    /**
     * Get the TPM_STRUCT_VER.
     *
     * @return
     */
    public byte[] getStructVer() {
        return structVer;
    }

    /**
     * Set the key usage. See the TPM Main Specification Part 2: Structures,
     * section 5.8 for detailed information about TPM_KEY_USAGE.
     *
     * @param newTpmKeyUsage
     */
    public void setTpmKeyUsage(short newTpmKeyUsage) {
        tpmKeyUsage = newTpmKeyUsage;
    }

    /**
     * Get the key usage value.
     *
     * @return
     */
    public short getTpmKeyUsage() {
        return tpmKeyUsage;
    }

    /**
     * Set the key flags. See the TPM Main Specification Part 2: Structures,
     * section 5.9 for detailed information about TPM_KEY_FLAGS.
     *
     * @param newTpmKeyFlags
     */
    public void setTpmKeyFlags(int newTpmKeyFlags) {
        tpmKeyFlags = newTpmKeyFlags;
    }

    /**
     * Get the key flags.
     *
     * @return
     */
    public int getTpmKeyFlags() {
        return tpmKeyFlags;
    }

    /**
     * Set the key auth data usage. See the TPM Main Specification Part 2:
     * Structures, section 5.9 for detailed information about
     * TPM_AUTH_DATA_USAGE.
     *
     * @param newTpmAuthDataUsage
     */
    public void setTpmAuthDataUsage(byte newTpmAuthDataUsage) {
        tpmAuthDataUsage = newTpmAuthDataUsage;
    }

    /**
     * Get the key auth data usage.
     *
     * @return
     */
    public byte getTpmAuthDataUsage() {
        return tpmAuthDataUsage;
    }

    /**
     * Set the TPM_KEY_PARMS using a TpmKeyParams object.
     *
     * @param newKeyParms
     */
    public void setKeyParms(TpmKeyParams newKeyParms) {
        keyParms = newKeyParms;
    }

    /**
     * Get the TPM_KEY_PARMS for the key.
     *
     * @return
     */
    public TpmKeyParams getKeyParms() {
        return keyParms;
    }
}
