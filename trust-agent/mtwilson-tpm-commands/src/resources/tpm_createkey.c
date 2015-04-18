/*
 * tpm_createsigningkey - creates an RSA key in the TPM for signing or binding
 * 
 * Copyright (C) 2015 Intel Corporation. All rights reserved.
 *
 * Author: Jonathan Buhacoff <jonathan.buhacoff@intel.com>
 */

#include <sys/types.h>
#include <sys/stat.h>
#include <getopt.h>
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

#include "tpm_tspi.h"
#include "tpm_utils.h"
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

static char filenamePrivatekeyblobOutput[PATH_MAX] = "";
static char filenamePublickeyOutput[PATH_MAX] = "";
static TSS_FLAG keyType = 0;
static TSS_FLAG keyAuth = TSS_KEY_NO_AUTHORIZATION;
static char keypassword[PATH_MAX] = "";
static const char *keypasswordEnv;
static TSS_FLAG keypasswordMode = TSS_SECRET_MODE_PLAIN;
static BOOL decodeHexPassword = FALSE;
static BOOL useEnvironment = FALSE;

static int parse(const int aOpt, const char *aArg)
{

	switch (aOpt) {
	case 'b':
		keyType = TSS_KEY_TYPE_BIND;
		break;
	case 's':
		keyType = TSS_KEY_TYPE_SIGNING;
		break;
	case 'k':
		strncpy(filenamePrivatekeyblobOutput, aArg, PATH_MAX);
		break;
	case 'p':
		strncpy(filenamePublickeyOutput, aArg, PATH_MAX);
		break;
	case 'q':
		strncpy(keypassword, aArg, PATH_MAX);
		keyAuth = TSS_KEY_AUTHORIZATION;
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
	logCmdOption("-b, --binding",
		     _("Create a binding key"));
	logCmdOption("-s, --signing",
		     _("Create a signing key"));
	logCmdOption("-k, --keyout",
		     _("Output file to store the private key blob"));
	logCmdOption("-p, --pubout",
		     _("Output file to store the public key"));
	logCmdOption("-q, --keypassword",
		     _("Password for using the TPM key"));
	logCmdOption("-Q, --keypasswordsha1",
		     _("Password for using the TPM key is provided as SHA-1 output, use as-is"));
	logCmdOption("-t, --env",
		     _("Password arg is name of environment variable containing the password for using the TPM key"));
	logCmdOption("-x, --hex",
		     _("The password is provided in hex-encoded format"));
}

int main(int argc, char **argv) {
	TSS_HCONTEXT    hContext;
	TSS_HTPM        hTPM;
	TSS_HKEY        hSRK; 
	TSS_HPOLICY     hSRKPolicy; 
	TSS_HKEY        hKey; 
	TSS_HPOLICY     hKeyPolicy; 
	TSS_FLAG        keyflags;
	TSS_RESULT      result;
	BYTE            WELL_KNOWN_SECRET[TCPA_SHA1_160_HASH_LEN] = TSS_WELL_KNOWN_SECRET;
	UINT32          lengthPublickey;
	BYTE            *contentPublickey;
	FILE            *filePublickey;
	UINT32          lengthPrivatekeyblob;
	BYTE            *contentPrivatekeyblob;
	FILE            *filePrivatekeyblob;
	BYTE			*keypasswordBytes = NULL;
	UINT32			lengthKeypasswordBytes;
	int             i;
	int             exitCode = -1;
	
	struct option hOpts[] = {
		{"keyout"      , required_argument, NULL, 'k'},
		{"pubout"       , required_argument, NULL, 'p'},
		{"signing"       , no_argument, NULL, 's'},
		{"binding"       , no_argument, NULL, 'b'},
		{"keypassword"     , required_argument, NULL, 'q'},
		{"keypasswordsha1"     , no_argument, NULL, 'Q'},
		{"env"     , no_argument, NULL, 't'},
		{"hex"     , no_argument, NULL, 'x'}
	};
	
	if (genericOptHandler
		    (argc, argv, "k:p:q:bstxQ", hOpts,
		     sizeof(hOpts) / sizeof(struct option), parse, help) != 0) {
		exitCode = -1;
		goto out;
	}
	if( keyType == 0 ) {
	    fprintf(stderr, "Must specify key type either binding or signing\n");
		exitCode = -1;
		goto out;
	}

	keyflags = keyType | TSS_KEY_SIZE_2048 | keyAuth | TSS_KEY_NOT_MIGRATABLE | TSS_KEY_VOLATILE;
	
	/* initialize tpm context */
	CATCH_TSS_ERROR( Tspi_Context_Create(&hContext) );
	CATCH_TSS_ERROR( Tspi_Context_Connect(hContext, NULL) );
	CATCH_TSS_ERROR( Tspi_Context_GetTpmObject(hContext, &hTPM) );

	/* load SRK */
	CATCH_TSS_ERROR( Tspi_Context_LoadKeyByUUID(hContext, TSS_PS_TYPE_SYSTEM, SRK_UUID, &hSRK) );
	CATCH_TSS_ERROR( Tspi_GetPolicyObject(hSRK, TSS_POLICY_USAGE, &hSRKPolicy) );
	CATCH_TSS_ERROR( Tspi_Policy_SetSecret(hSRKPolicy, TSS_SECRET_MODE_PLAIN, sizeof(WELL_KNOWN_SECRET), WELL_KNOWN_SECRET) );
	CATCH_TSS_ERROR( Tspi_Policy_AssignToObject(hSRKPolicy, hSRK) );

	/* create RSA key */
	CATCH_TSS_ERROR( Tspi_Context_CreateObject(hContext, TSS_OBJECT_TYPE_RSAKEY, keyflags, &hKey) );
	
	
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
	
	CATCH_TSS_ERROR( Tspi_Key_CreateKey(hKey, hSRK, NULL_HPCRS) );
	CATCH_TSS_ERROR( Tspi_Key_LoadKey(hKey, hSRK) );

	/* write the private key blob to file */
	CATCH_TSS_ERROR( Tspi_GetAttribData(hKey, TSS_TSPATTRIB_KEY_BLOB, TSS_TSPATTRIB_KEYBLOB_BLOB, &lengthPrivatekeyblob, &contentPrivatekeyblob) );
	CATCH_NULL( filePrivatekeyblob = fopen(filenamePrivatekeyblobOutput, "wb") );
	CATCH_ERROR( fwrite(contentPrivatekeyblob, 1, lengthPrivatekeyblob, filePrivatekeyblob) != lengthPrivatekeyblob );
	fclose(filePrivatekeyblob);
	filePrivatekeyblob = NULL;
	
	/* write the public key to file */
	CATCH_TSS_ERROR( Tspi_Key_GetPubKey(hKey, &lengthPublickey, &contentPublickey) );
	CATCH_NULL( filePublickey = fopen(filenamePublickeyOutput, "wb") );
	CATCH_ERROR( fwrite(contentPublickey, 1, lengthPublickey, filePublickey) != lengthPublickey );
	fclose(filePublickey);
	filePublickey = NULL;
	
	exitCode = 0;
	
	out_close:
	if( hKey ) { Tspi_Context_CloseObject(hContext, hKey); }
	Tspi_Context_Close(hContext);

	out:
	return exitCode;
}
