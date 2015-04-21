/*
 * tpm_signdata - signs input data using using an existing TPM signing private key
 * 
 * Copyright (C) 2015 Intel Corporation. All rights reserved.
 *
 * Author: Jonathan Buhacoff <jonathan.buhacoff@intel.com>
 */

 
#include <sys/types.h>
#include <sys/stat.h>
#include <limits.h>
#include <ctype.h>
#include <fcntl.h>
#include <unistd.h>
#include <stdio.h>
#include <string.h>
#include <trousers/tss.h>
#include <tss/tcs.h>
#include <tss/tspi.h>
#include <tss/platform.h>
#include <tss/tss_typedef.h>
#include <tss/tss_structs.h>
#include <tss/tpm.h>

#include "tpm_utils.h"
#include "tpm_tspi.h"


//#include <getopt.h>

//#include <openssl/bio.h>
//#include <openssl/ssl.h>
//#include <openssl/err.h>

// this macro assumes the following declarations are in scope:
// 1. TSS_HCONTEXT hContext 
// 2. TSS_RESULT result
// 3. label out_close: for goto, which calls at least Tspi_Context_Close(hContext) 
// 4. int exitCode
// how to use it:
// CATCH_TSS_ERROR( Tspi_Context_Create(&hContext) );
#define CATCH_TSS_ERROR(a) \
	result = a; \
	if( result != TSS_SUCCESS ) { \
		fprintf(stderr, "%s:%d error 0x%x (%s) from %s\n", __FILE__, __LINE__, \
			result, Trspi_Error_String(result), #a); \
		exitCode = result; \
		goto out_close; }

#define CATCH_NULL(e) \
	exitCode--; \
	if( (e) == NULL ) { \
		fprintf(stderr, "%s:%d error: %s\n", __FILE__, __LINE__, #e); \
		goto out_close; \
	}

#define CATCH_ERROR(e) \
	exitCode--; \
	if( e ) { \
		fprintf(stderr, "%s:%d error: %s\n", __FILE__, __LINE__, #e); \
		goto out_close; \
	}

extern const char *__progname;

static char filenameInput[PATH_MAX] = "";
static char filenameOutput[PATH_MAX] = "";
static char filenamePrivatekey[PATH_MAX] = "";
static char keypassword[PATH_MAX] = "";
static const char *keypasswordEnv;
static TSS_FLAG keypasswordMode = TSS_SECRET_MODE_PLAIN;
static BOOL decodeHexPassword = FALSE;
static BOOL useEnvironment = FALSE;
//static BOOL useNIARL = FALSE;


static int parse(const int aOpt, const char *aArg)
{

	switch (aOpt) {
	case 'i':
		strncpy(filenameInput, aArg, PATH_MAX);
		break;
	case 'k':
		strncpy(filenamePrivatekey, aArg, PATH_MAX);
		break;
	case 'o':
		strncpy(filenameOutput, aArg, PATH_MAX);
		break;
	case 'q':
		strncpy(keypassword, aArg, PATH_MAX);
		break;
	case 'Q':
		keypasswordMode = TSS_SECRET_MODE_SHA1;
		break;
	case 't':
		useEnvironment = TRUE;
		break;
	case 'x':
		decodeHexPassword = TRUE;
		break;
//	case 'N':
//		useNIARL = TRUE;
//		break;
	default:
		return -1;
	}
	return 0;
}

static void help(const char* aCmd)
{
	logCmdHelp(aCmd);
	logCmdOption("-i, --infile",
		     _("Input file containing hash to sign"));
	logCmdOption("-k, --keyfile",
		     _("Private key blob file containing the TPM signing key"));
	logCmdOption("-o, --outfile",
		     _("Output file to store the RSA signature"));
	logCmdOption("-q, --keypassword",
		     _("Password for using the TPM signing key"));
	logCmdOption("-Q, --keypasswordsha1",
		     _("Password for using the TPM key is provided as SHA-1 output, use as-is"));
	logCmdOption("-t, --env",
		     _("Password arg is name of environment variable containing the password for using the TPM signing key"));
	logCmdOption("-x, --hex",
		     _("The password is provided in hex-encoded format"));
	logCmdOption("-N, --NIARL",
		     _("Load keys using NIARL style"));
}

int main(int argc, char **argv) {
	TSS_HCONTEXT    hContext;
	TSS_HTPM        hTPM; 
	TSS_HPOLICY     hTPMPolicy;
	TSS_HKEY        hSRK; 
	TSS_HPOLICY     hSRKPolicy;
	TSS_HKEY        hKey; 
	TSS_HPOLICY     hKeyPolicy;
	TSS_HHASH       hHash;
	TSS_RESULT      result;
	BYTE            WELL_KNOWN_SECRET[TCPA_SHA1_160_HASH_LEN] = TSS_WELL_KNOWN_SECRET;
	UINT32          lengthPrivatekeyFile;
	BYTE            *contentPrivatekeyFile;
	FILE            *filePrivatekey;
	UINT32          lengthInputFile;
	BYTE            *contentInputFile;
	FILE            *fileInput;
	UINT32          lengthSignatureData;
	BYTE            *signatureData;
	FILE            *fileOutput;
	BYTE			*passwordBytes = NULL;
	UINT32			lengthPasswordBytes;
	BYTE			*keypasswordBytes = NULL;
	UINT32			lengthKeypasswordBytes;
	int             i;
	int             exitCode = -1;
	
	struct option hOpts[] = {
		{"infile"      , required_argument, NULL, 'i'},
		{"keyfile"       , required_argument, NULL, 'k'},
		{"outfile"     , required_argument, NULL, 'o'},
		{"keypassword"     , required_argument, NULL, 'q'},
		{"keypasswordsha1"     , no_argument, NULL, 'Q'},
		{"env"     , no_argument, NULL, 't'},
		{"hex"     , no_argument, NULL, 'x'}
//		{"NIARL"     , no_argument, NULL, 'N'}
	};
	
	if (genericOptHandler
		    (argc, argv, "i:k:o:p:q:txQN", hOpts,
		     sizeof(hOpts) / sizeof(struct option), parse, help) != 0) {
		exitCode = -1;
		goto out;
	}

	/* initialize tpm context */
	CATCH_TSS_ERROR( Tspi_Context_Create(&hContext) );
	CATCH_TSS_ERROR( Tspi_Context_Connect(hContext, NULL) );
	CATCH_TSS_ERROR( Tspi_Context_GetTpmObject(hContext, &hTPM) );

	/* load SRK */
	CATCH_TSS_ERROR( Tspi_Context_LoadKeyByUUID(hContext, TSS_PS_TYPE_SYSTEM, SRK_UUID, &hSRK) );
	//CATCH_TSS_ERROR( Tspi_GetPolicyObject(hSRK, TSS_POLICY_USAGE, &hSRKPolicy) );
   	CATCH_TSS_ERROR( Tspi_Context_CreateObject(hContext, TSS_OBJECT_TYPE_POLICY, TSS_POLICY_USAGE, &hSRKPolicy) );
	CATCH_TSS_ERROR( Tspi_Policy_SetSecret(hSRKPolicy, TSS_SECRET_MODE_PLAIN, sizeof(WELL_KNOWN_SECRET), WELL_KNOWN_SECRET) );
	CATCH_TSS_ERROR( Tspi_Policy_AssignToObject(hSRKPolicy, hSRK) );
	
	/* read the private key blob */
	CATCH_NULL( filePrivatekey = fopen(filenamePrivatekey, "rb") );
	fseek (filePrivatekey, 0, SEEK_END);
	lengthPrivatekeyFile = ftell (filePrivatekey);
	fseek (filePrivatekey, 0, SEEK_SET);
	contentPrivatekeyFile = malloc (lengthPrivatekeyFile);
	CATCH_ERROR( fread(contentPrivatekeyFile, 1, lengthPrivatekeyFile, filePrivatekey) != lengthPrivatekeyFile );
	fclose(filePrivatekey);
	filePrivatekey = NULL;

/*	
		if( useNIARL ) {
			
			TSS_FLAG niarlKeyFlags = TSS_KEY_TYPE_SIGNING | TSS_KEY_SIZE_2048  | TSS_KEY_VOLATILE | TSS_KEY_AUTHORIZATION | TSS_KEY_NOT_MIGRATABLE;
			TSS_UUID			uuid_sign = TSS_UUID_USK2;
	uuid_sign.rgbNode[5] = 0x04; // (BYTE)i_keyindex;   // trustagent default signing key index = 4   (default binding key index = 3) 
	uuid_sign.rgbNode[0] = 0x06;
			
			
	CATCH_TSS_ERROR( Tspi_Context_CreateObject(hContext, TSS_OBJECT_TYPE_RSAKEY, niarlKeyFlags, &hKey) );
	CATCH_TSS_ERROR( Tspi_Context_GetKeyByUUID(hContext, TSS_PS_TYPE_SYSTEM, uuid_sign, &hKey) ); 
	//  NOTE: if you get error 0x2020 (Key not found in persistent storage) you have to load the key first, you can use the NIARL "set key" command for that.
		}
		else {
	CATCH_TSS_ERROR( Tspi_Context_LoadKeyByBlob(hContext, hSRK, lengthPrivatekeyFile, contentPrivatekeyFile, &hKey) );
		}
*/
	CATCH_TSS_ERROR( Tspi_Context_LoadKeyByBlob(hContext, hSRK, lengthPrivatekeyFile, contentPrivatekeyFile, &hKey) );

	
	/* set the authentication for the private key */
    if (keypassword != NULL && strlen(keypassword) > 0 ) {
    	CATCH_TSS_ERROR( Tspi_Context_CreateObject(hContext, TSS_OBJECT_TYPE_POLICY, TSS_POLICY_USAGE, &hKeyPolicy) );
      if( useEnvironment ) {
                keypasswordEnv = getenv(keypassword);
                if (!keypasswordEnv) {
                        fprintf(stderr, "%s is not defined\n", keypassword);
                        goto out_close;
                }
				strncpy(keypassword, keypasswordEnv, PATH_MAX);
	  }
	  if( decodeHexPassword ) {
			if( hex2bytea(keypassword, &keypasswordBytes, &lengthKeypasswordBytes) != 0 ) {
                                fprintf(stderr, "Invalid hex secret\n");
                                goto out_close;
                        }
			CATCH_TSS_ERROR( Tspi_Policy_SetSecret(hKeyPolicy, keypasswordMode, lengthKeypasswordBytes, keypasswordBytes) );
	  }
	  else {
			CATCH_TSS_ERROR( Tspi_Policy_SetSecret(hKeyPolicy, keypasswordMode, strlen(keypassword), (BYTE *)keypassword) );
	  }
	  
	  CATCH_TSS_ERROR( Tspi_Policy_AssignToObject(hKeyPolicy, hKey) );
	}
  	  CATCH_TSS_ERROR( Tspi_Key_LoadKey(hKey, hSRK) );
	
	/* read the hash to sign */
	CATCH_NULL( fileInput = fopen(filenameInput, "rb") );
	fseek (fileInput, 0, SEEK_END);
	lengthInputFile = ftell (fileInput);
	fseek (fileInput, 0, SEEK_SET);
	contentInputFile = malloc (lengthInputFile);
	CATCH_ERROR( fread(contentInputFile, 1, lengthInputFile, fileInput) != lengthInputFile );
	fclose(fileInput);
	fileInput = NULL;
	
	/* create an object to store the hash to sign */
	CATCH_TSS_ERROR( Tspi_Context_CreateObject(hContext, TSS_OBJECT_TYPE_HASH, TSS_HASH_SHA1, &hHash) );
	
	/* set the input hash in the hash object */
	CATCH_TSS_ERROR( Tspi_Hash_SetHashValue(hHash, lengthInputFile, contentInputFile) );
	
	/* perform the signature */
	CATCH_TSS_ERROR( Tspi_Hash_Sign(hHash, hKey, &lengthSignatureData, &signatureData) );

	/* write the signature data to the output file or stdout */
	if( strlen(filenameOutput) > 0 ) {
		CATCH_NULL( fileOutput = fopen(filenameOutput, "wb") );
		CATCH_ERROR( fwrite(signatureData, 1, lengthSignatureData, fileOutput) != lengthSignatureData );
		fclose(fileOutput);
		fileOutput = NULL;
	}
	else {
		CATCH_ERROR( fwrite(signatureData, 1, lengthSignatureData, stdout) != lengthSignatureData );
	}
	
	CATCH_TSS_ERROR( Tspi_Context_FreeMemory( hContext, signatureData ) );
	
	exitCode = 0;
	
	out_close:
	if( passwordBytes) { free(passwordBytes); }
	if( keypasswordBytes) { free(keypasswordBytes); }
	if( hKey ) { Tspi_Context_CloseObject(hContext, hKey); }
	if( hSRK ) { Tspi_Context_CloseObject(hContext, hSRK); }
	Tspi_Context_Close(hContext);

	out:
	return exitCode;
}
