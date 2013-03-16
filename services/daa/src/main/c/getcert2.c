/* Get EK certificate */

#include <stdio.h>
#include <string.h>
#include <trousers/tss.h>
#include "remote.h"
#include "tpm_quote.h"

#define BSIZE	128
#define CKERR(x)	if (result != TSS_SUCCESS) { printf("Error: "); printf((x)); printf("\n"); goto error; }

/* Definitions from section 7 of
 * TCG PC Client Specific Implementation Specification
 * For Conventional BIOS
 */
#define TCG_TAG_PCCLIENT_STORED_CERT		0x1001
#define TCG_TAG_PCCLIENT_FULL_CERT		0x1002
#define TCG_TAG_PCCLIENT_PART_SMALL_CERT	0x1003
#define TCG_FULL_CERT				0
#define TCG_PARTIAL_SMALL_CERT			1

// adapted from tpm_tspi.c in tpm-tools
const char *mapUnknown = "Unknown";

const char *usageSigning = "Signing";
const char *usageStorage = "Storage";
const char *usageIdentity = "Identity";
const char *usageAuthChange = "AuthChange";
const char *usageBind = "Bind";
const char *usageLegacy = "Legacy";

const int flagMax = 7;
const char *flagMap[] = {
        "!VOLATILE, !MIGRATABLE, !REDIRECTION",
        "!VOLATILE, !MIGRATABLE,  REDIRECTION",
        "!VOLATILE,  MIGRATABLE, !REDIRECTION",
        "!VOLATILE,  MIGRATABLE,  REDIRECTION",
        " VOLATILE, !MIGRATABLE, !REDIRECTION",
        " VOLATILE, !MIGRATABLE,  REDIRECTION",
        " VOLATILE,  MIGRATABLE, !REDIRECTION",
        " VOLATILE,  MIGRATABLE,  REDIRECTION",
};

const char *authUsageNever = "Never";
const char *authUsageAlways = "Always";

const char *algRsa = "RSA";
const char *algDes = "DES";
const char *alg3Des = "3DES";
const char *algSha = "SHA";
const char *algHmac = "HMAC";
const char *algAes = "AES";

const char *encNone = "None";
const char *encRsaPkcs15 = "RSAESPKCSv15";
const char *encRsaOaepSha1Mgf1 = "RSAESOAEP_SHA1_MGF1";

const char *sigNone = "None";
const char *sigRsaPkcs15Sha1 = "RSASSAPKCS1v15_SHA1";
const char *sigRsaPkcs15Der = "RSASSAPKCS1v15_DER";

const char *displayKeyUsageMap(UINT32 a_uiData)
{

        switch (a_uiData) {
        case TPM_KEY_SIGNING:
                return usageSigning;

        case TPM_KEY_STORAGE:
                return usageStorage;

        case TPM_KEY_IDENTITY:
                return usageIdentity;

        case TPM_KEY_AUTHCHANGE:
                return usageAuthChange;

        case TPM_KEY_BIND:
                return usageBind;

        case TPM_KEY_LEGACY:
                return usageLegacy;
        }

        return mapUnknown;
}

const char *displayKeyFlagsMap(UINT32 a_uiFlags)
{

        int iPos = a_uiFlags & flagMax;

        return flagMap[iPos];
}

const char *displayAuthUsageMap(UINT32 a_uiData)
{

        switch (a_uiData) {
        case TPM_AUTH_NEVER:
                return authUsageNever;

        case TPM_AUTH_ALWAYS:
                return authUsageAlways;
        }

        return mapUnknown;
}

const char *displayAlgorithmMap(UINT32 a_uiData)
{

        switch (a_uiData) {
        case TCPA_ALG_RSA:
                return algRsa;

        case TCPA_ALG_DES:
                return algDes;

        case TCPA_ALG_3DES:
                return alg3Des;

        case TCPA_ALG_SHA:
                return algSha;

        case TCPA_ALG_HMAC:
                return algHmac;

        case TCPA_ALG_AES:
                return algAes;
        }

        return mapUnknown;
}

const char *displayEncSchemeMap(UINT32 a_uiData)
{

        switch (a_uiData) {
        case TPM_ES_NONE:
                return encNone;

        case TPM_ES_RSAESPKCSv15:
                return encRsaPkcs15;

        case TPM_ES_RSAESOAEP_SHA1_MGF1:
                return encRsaOaepSha1Mgf1;
        }

        return mapUnknown;
}

const char *displaySigSchemeMap(UINT32 a_uiData)
{

        switch (a_uiData) {
        case TPM_SS_NONE:
                return sigNone;

        case TPM_SS_RSASSAPKCS1v15_SHA1:
                return sigRsaPkcs15Sha1;

        case TPM_SS_RSASSAPKCS1v15_DER:
                return sigRsaPkcs15Der;
        }

        return mapUnknown;
}



// adapted from tpm_log.c in tpm-tools
void printHex(int a_iLen, void *a_pData)
{

        int i, iByte;
        char *pData = a_pData;

        for (i = 0; i < a_iLen; i++) {
                if ((i % 32) == 0) {
                        if (a_iLen > 32) {
                                printf("\n\t");
                        }
                } else if ((i % 4) == 0) {
                        printf(" ");
                }

                iByte = pData[i];
                iByte &= 0x000000ff;
                printf("%02x", iByte);
        }

        printf("\n");

        //return a_iLen;
}

int
main (int argc, char **argv)
{
	TSS_HCONTEXT	hContext;
	TSS_HNVSTORE	hNV;
	TSS_HTPM	hTpm;
	TSS_HPOLICY	hTpmPolicy;
	TSS_HPOLICY	hSrkPolicy;
	TSS_HKEY	hSRK;
	TSS_HKEY	hEK;
	TSS_VALIDATION  valid;
	TSS_UUID	SRK_UUID = TSS_UUID_SRK;
	FILE		*f_out;
	TSS_RESULT	result;
	BYTE		srkSecret[] = TSS_WELL_KNOWN_SECRET;

	if (argc != 2) {
		printf ("Usage: %s outfilename\n", argv[0]);
		exit (1);
	}

	if ((f_out = fopen (argv[1], "wb")) == NULL) {
		printf ("Unable to open '%s' for output\n", argv[1]);
		exit (1);
	}

	//UNICODE *hostAddress = ascii_to_unicode("10.1.71.103"); // added 20120618
	UNICODE *hostAddress = NULL;
	result = Tspi_Context_Create(&hContext); CKERR("Tspi_Context_Create");
	result = Tspi_Context_Connect(hContext, hostAddress); CKERR("Tspi_Context_Connect"); // changed from NULL to hostAddress 20120618


	//if( isOwnerRequiredToReadEK == TRUE ) // TSS_PS_TYPE_SYSTEM == 2

	result = Tspi_Context_LoadKeyByUUID(hContext, TSS_PS_TYPE_SYSTEM, SRK_UUID, &hSRK); CKERR("Tspi_Context_LoadKeyByUUID(srk)");
        result = Tspi_GetPolicyObject (hSRK, TSS_POLICY_USAGE, &hSrkPolicy); CKERR("Tspi_GetPolicyObject(srk)");
        result = Tspi_Policy_SetSecret(hSrkPolicy, TSS_SECRET_MODE_SHA1, sizeof(srkSecret), srkSecret); CKERR("Tspi_Policy_SetSecret(srk)");

	result = Tspi_Context_GetTpmObject(hContext, &hTpm); CKERR("Tspi_Context_GetTpmObject");
	//result = Tspi_GetPolicyObject(hContext, TSS_POLICY_USAGE, &hTpmPolicy); CKERR("Tspi_GetPolicyObject(tpm)");
	result = Tspi_Context_CreateObject(hContext, TSS_OBJECT_TYPE_POLICY, TSS_POLICY_USAGE, &hTpmPolicy); CKERR("Tspi_Context_CreateObject(tpm)");
	result = Tspi_Policy_AssignToObject(hTpmPolicy, hTpm); CKERR("Tspi_Policy_AssignToObject(tpm)");
	result = Tspi_Policy_SetSecret(hTpmPolicy, TSS_SECRET_MODE_PLAIN, strlen("intel"), "intel"); CKERR("Tspi_Policy_SetSecret(tpm)");
	// end if isOwnerRequiredToReadEK == TRUE

	// create validation data structure
	//valid = NULL;
	BYTE            nonce[20];
	//BYTE nonce[20] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}; // null challenge hash for now; for full implementation see aikquote where we load a challenge file and then hash it. so chalmd is the sha1 hash of the challenge.
	valid.ulExternalDataLength = sizeof(nonce);
	valid.rgbExternalData = nonce;

	result = Tspi_TPM_GetPubEndorsementKey(hTpm, TRUE, NULL, &hEK); CKERR("Tspi_TPM_GetPubEndorsementKey"); // the NULL is optional validation data (we would provide) for the TPM to sign using endorsement key. THIS CALL REQUIRES SETTING THE OWNER PASSWORD FIRST (second parameter TRUE).
	//result = Tspi_TPM_GetPubEndorsementKey(hTpm, FALSE, &valid, &hEK); CKERR("Tspi_TPM_GetPubEndorsementKey"); // the NULL is optional validation data (we would provide) for the TPM to sign using endorsement key.  THIS CALL WOULD ONLY WORK IF THE TPM IS NOT OWNED (second parameter FALSE).

	// now display the key; inspired by tpm_utils.c from tpm-tools
	UINT32 uiAttr, uiAttrVersionSize, uiAttrModulusSize;
        BYTE *pAttrVersion, *pAttrModulus;
        UINT32 uiAlg;
	result = Tspi_GetAttribData(hEK, TSS_TSPATTRIB_KEY_INFO,
                          TSS_TSPATTRIB_KEYINFO_VERSION, &uiAttrVersionSize,
                          &pAttrVersion); CKERR("Tspi_GetAttribData(version)");
	printf("Version (hex): \n"); printHex(uiAttrVersionSize, pAttrVersion);
	result = Tspi_GetAttribUint32(hEK, TSS_TSPATTRIB_KEY_INFO, 
                            TSS_TSPATTRIB_KEYINFO_USAGE, &uiAttr); CKERR("Tspi_GetAttribUint32(usage)");
	printf("Usage: 0x%04x (%s)\n", uiAttr, displayKeyUsageMap(uiAttr));
	result = Tspi_GetAttribUint32(hEK, TSS_TSPATTRIB_KEY_INFO,
                            TSS_TSPATTRIB_KEYINFO_KEYFLAGS, &uiAttr); CKERR("Tspi_GetAttribUint32(flags)");
	printf("Flags: 0x%08x (%s)\n", uiAttr, displayKeyFlagsMap(uiAttr));
	result = Tspi_GetAttribUint32(hEK, TSS_TSPATTRIB_KEY_INFO,
                            TSS_TSPATTRIB_KEYINFO_AUTHUSAGE, &uiAttr); CKERR("Tspi_GetAttribUint32(authusage)");
	printf("AuthUsage: 0x%02x (%s)\n", uiAttr, displayAuthUsageMap(uiAttr));
	result = Tspi_GetAttribUint32(hEK, TSS_TSPATTRIB_KEY_INFO,
                            TSS_TSPATTRIB_KEYINFO_ALGORITHM, &uiAlg); CKERR("Tspi_GetAttribUint32(alg)");
	printf("Algorithm: 0x%08x (%s)\n", uiAlg, displayAlgorithmMap(uiAlg));
	if( uiAlg == TCPA_ALG_RSA ) {
		result = Tspi_GetAttribUint32(hEK, TSS_TSPATTRIB_RSAKEY_INFO,
                                    TSS_TSPATTRIB_KEYINFO_RSA_KEYSIZE,
                                    &uiAttr); CKERR("Tspi_GetAttribUint32(keysize)");
		printf("Key Size: %d bits\n", uiAttr);
		result = Tspi_GetAttribData(hEK, TSS_TSPATTRIB_RSAKEY_INFO,
                          TSS_TSPATTRIB_KEYINFO_RSA_MODULUS, &uiAttrModulusSize,
                          &pAttrModulus); CKERR("Tspi_GetAttribData(modulus)");
		printf("Modulus:\n"); printHex(uiAttrModulusSize, pAttrModulus);
	}
	result = Tspi_GetAttribUint32(hEK, TSS_TSPATTRIB_KEY_INFO,
                            TSS_TSPATTRIB_KEYINFO_ENCSCHEME, &uiAttr); CKERR("Tspi_GetAttribUint32(encscheme)");
	printf("Encryption Scheme: 0x%08x (%s)\n", uiAttr, displayEncSchemeMap(uiAttr));
	result = Tspi_GetAttribUint32(hEK, TSS_TSPATTRIB_KEY_INFO,
                            TSS_TSPATTRIB_KEYINFO_SIGSCHEME, &uiAttr); CKERR("Tspi_GetAttribUint32(sigscheme)");
	printf("Signature Scheme: 0x%08x (%s)\n", uiAttr, displaySigSchemeMap(uiAttr));

	//free(hostAddress); // added 20120618
	printf ("Success!\n");
	return 0;

error:
	printf ("Failure, error code: 0x%x\n", result);
	printf ("Message: %s\n", tss_result(result)); // added 20120618
	return 1;
}
