/*
 * tpm_sealkey - binds an AES key using an existing TPM key
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

static char filenameEncryptedInput[PATH_MAX] = "";
static char filenamePlaintextOutput[PATH_MAX] = "";
static char filenamePrivatekey[PATH_MAX] = "";
static char keypassword[PATH_MAX] = "";
static const char *keypasswordEnv;
static TSS_FLAG keypasswordMode = TSS_SECRET_MODE_PLAIN;
static BOOL decodeHexPassword = FALSE;
static BOOL useEnvironment = FALSE;

static int parse(const int aOpt, const char *aArg)
{

	switch (aOpt) {
	case 'k':
		strncpy(filenamePrivatekey, aArg, PATH_MAX);
		break;
	case 'i':
		strncpy(filenameEncryptedInput, aArg, PATH_MAX);
		break;
	case 'o':
		strncpy(filenamePlaintextOutput, aArg, PATH_MAX);
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
	default:
		return -1;
	}
	return 0;
}

static void help(const char* aCmd)
{
	logCmdHelp(aCmd);
	logCmdOption("-i, --infile",
		     _("Input file containing encrypted AES key"));
	logCmdOption("-k, --keyfile",
		     _("Private key blob file to decrypt the AES key"));
	logCmdOption("-o, --outfile",
		     _("Output file to store the decrypted AES key"));
	logCmdOption("-q, --keypassword",
		     _("Password for using the TPM signing key"));
	logCmdOption("-Q, --keypasswordsha1",
		     _("Password for using the TPM key is provided as SHA-1 output, use as-is"));
	logCmdOption("-t, --env",
		     _("Password arg is name of environment variable containing the password for using the TPM binding key"));
	logCmdOption("-x, --hex",
		     _("The password is provided in hex-encoded format"));
}

int main(int argc, char **argv) {
	TSS_HCONTEXT    hContext;
	TSS_HTPM        hTPM; 
	TSS_HPOLICY     hTPMPolicy;
	TSS_HKEY        hSRK; 
	TSS_HPOLICY     hSRKPolicy;
	TSS_HKEY        hKey; 
	TSS_HPOLICY     hKeyPolicy;
	TSS_HENCDATA    hEncdata;
	TSS_HPOLICY     hEncdataPolicy;
	TSS_RESULT      result;
	BYTE            WELL_KNOWN_SECRET[TCPA_SHA1_160_HASH_LEN] = TSS_WELL_KNOWN_SECRET;
	UINT32          lengthPrivatekeyFile;
	BYTE            *contentPrivatekeyFile;
	FILE            *filePrivatekey;
	UINT32          lengthEncryptedInputFile;
	BYTE            *contentEncryptedInputFile;
	FILE            *fileEncryptedInput;
	UINT32          lengthPlaintextOutput;
	BYTE            *plaintextOutput;
	FILE            *filePlaintextOutput;
	BYTE			*keypasswordBytes = NULL;
	UINT32			lengthKeypasswordBytes;

	int             exitCode = -1;
	
	struct option hOpts[] = {
		{"infile"      , required_argument, NULL, 'i'},
		{"keyfile"       , required_argument, NULL, 'k'},
		{"outfile"     , required_argument, NULL, 'o'},
		{"keypassword"     , required_argument, NULL, 'q'},
		{"keypasswordsha1"     , no_argument, NULL, 'Q'},
		{"env"     , no_argument, NULL, 't'},
		{"hex"     , no_argument, NULL, 'x'}
	};

	if (genericOptHandler
		    (argc, argv, "i:k:o:q:txQ", hOpts,
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
	
	/* load the tpm private key blob, because the private key can decrypt the input data */
	CATCH_NULL( filePrivatekey = fopen(filenamePrivatekey, "rb") );
	fseek (filePrivatekey, 0, SEEK_END);
	lengthPrivatekeyFile = ftell (filePrivatekey);
	fseek (filePrivatekey, 0, SEEK_SET);
	contentPrivatekeyFile = malloc (lengthPrivatekeyFile);
	CATCH_ERROR( fread(contentPrivatekeyFile, 1, lengthPrivatekeyFile, filePrivatekey) != lengthPrivatekeyFile );
	fclose(filePrivatekey);
	filePrivatekey = NULL;
	
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
	
	/* read the encrypted secret key */
	CATCH_NULL( fileEncryptedInput = fopen(filenameEncryptedInput, "rb") );
	fseek (fileEncryptedInput, 0, SEEK_END);
	lengthEncryptedInputFile = ftell (fileEncryptedInput);
	fseek (fileEncryptedInput, 0, SEEK_SET);
	contentEncryptedInputFile = malloc (lengthEncryptedInputFile);
	CATCH_ERROR( fread(contentEncryptedInputFile, 1, lengthEncryptedInputFile, fileEncryptedInput) != lengthEncryptedInputFile );
	fclose(fileEncryptedInput);
	fileEncryptedInput = NULL;
	
	/* prepare an object to store the encrypted secret key */
	CATCH_TSS_ERROR( Tspi_Context_CreateObject(hContext, TSS_OBJECT_TYPE_ENCDATA, TSS_ENCDATA_BIND, &hEncdata) );
	CATCH_TSS_ERROR( Tspi_SetAttribData(hEncdata, TSS_TSPATTRIB_ENCDATA_BLOB, TSS_TSPATTRIB_ENCDATABLOB_BLOB, lengthEncryptedInputFile, contentEncryptedInputFile) );

	/* decrypt the secret key */
	CATCH_TSS_ERROR( Tspi_Data_Unbind(hEncdata, hKey, &lengthPlaintextOutput, &plaintextOutput) );

	/* write the decrypted key to the output file or stdout*/
	if( strlen(filenamePlaintextOutput) > 0 ) {
		CATCH_NULL( filePlaintextOutput = fopen(filenamePlaintextOutput, "wb") );
		CATCH_ERROR( fwrite(plaintextOutput, 1, lengthPlaintextOutput, filePlaintextOutput) != lengthPlaintextOutput );
		fclose(filePlaintextOutput);
		filePlaintextOutput = NULL;
	}
	else {
		CATCH_ERROR( fwrite(plaintextOutput, 1, lengthPlaintextOutput, stdout) != lengthPlaintextOutput );
	}
	
	exitCode = 0;
	
	out_close:
	if( hKey ) { Tspi_Context_CloseObject(hContext, hKey); }
	if( hSRK ) { Tspi_Context_CloseObject(hContext, hSRK); }
	Tspi_Context_Close(hContext);

	out:
	return exitCode;
}
