/*
 * tpm_sealkey - binds an AES key using an existing TPM key
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

static char filenameInput[PATH_MAX] = "";
static char filenameOutput[PATH_MAX] = "";
static char filenamePublickey[PATH_MAX] = "";


static int parse(const int aOpt, const char *aArg)
{

	switch (aOpt) {
	case 'i':
		strncpy(filenameInput, aArg, PATH_MAX);
		break;
	case 'k':
		strncpy(filenamePublickey, aArg, PATH_MAX);
		break;
	case 'o':
		strncpy(filenameOutput, aArg, PATH_MAX);
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
		     _("Input file containing AES key to bind"));
	logCmdOption("-k, --keyfile",
		     _("Public key file used to bind the AES key, in DER format"));
	logCmdOption("-o, --outfile",
		     _("Output file to store the wrapped AES key"));
}

int main(int argc, char **argv) {
	TSS_HCONTEXT    hContext;
	TSS_HKEY        hKey; 
	TSS_HENCDATA    hEncdata;
	TSS_RESULT      result;
	//BYTE            nonsecret[TCPA_SHA1_160_HASH_LEN] = TSS_WELL_KNOWN_SECRET;
	UINT32          lengthPublickeyFile;
	BYTE            *contentPublickeyFile;
	FILE            *filePublickey;
	UINT32          lengthInputFile;
	BYTE            *contentInputFile;
	FILE            *fileInput;
	UINT32          lengthEncData;
	BYTE            *encData;
	FILE            *fileOutput;
	int             i;
	int             exitCode = -1;
	
	struct option hOpts[] = {
		{"infile"      , required_argument, NULL, 'i'},
		{"keyfile"       , required_argument, NULL, 'k'},
		{"outfile"     , required_argument, NULL, 'o'}
	};
	
	if (genericOptHandler
		    (argc, argv, "i:k:o:", hOpts,
		     sizeof(hOpts) / sizeof(struct option), parse, help) != 0) {
		exitCode = -1;
		goto out;
	}
	
	/* initialize tpm context */
	CATCH_TSS_ERROR( Tspi_Context_Create(&hContext) );
	CATCH_TSS_ERROR( Tspi_Context_Connect(hContext, NULL) );
	
	/* read the public key blob */
	CATCH_NULL( filePublickey = fopen(filenamePublickey, "rb") );
	fseek (filePublickey, 0, SEEK_END);
	lengthPublickeyFile = ftell (filePublickey);
	fseek (filePublickey, 0, SEEK_SET);
	contentPublickeyFile = malloc (lengthPublickeyFile);
	CATCH_ERROR( fread(contentPublickeyFile, 1, lengthPublickeyFile, filePublickey) != lengthPublickeyFile );
	fclose(filePublickey);
	filePublickey = NULL;

	/* create an object to store the public key */
	CATCH_TSS_ERROR( Tspi_Context_CreateObject(hContext, TSS_OBJECT_TYPE_RSAKEY, TSS_KEY_TYPE_BIND | TSS_KEY_SIZE_2048 /* | TSS_KEY_NO_AUTHORIZATION*/ | TSS_KEY_NOT_MIGRATABLE | TSS_KEY_VOLATILE, &hKey) );

	/* load the public key blob into the public key object */
	if( lengthPublickeyFile == 256 ) {
		// it's just the modulus  (256 bytes for 2048-bit key)
		CATCH_TSS_ERROR( Tspi_SetAttribData(hKey, TSS_TSPATTRIB_RSAKEY_INFO, TSS_TSPATTRIB_KEYINFO_RSA_MODULUS, lengthPublickeyFile, contentPublickeyFile) );
	}
	else {
		// it's the complete TCG public keys stucture (284 bytes for 2048-bit key)
		CATCH_TSS_ERROR( Tspi_SetAttribData(hKey, TSS_TSPATTRIB_KEY_BLOB, TSS_TSPATTRIB_KEYBLOB_PUBLIC_KEY, lengthPublickeyFile, contentPublickeyFile) );
	}

	/* read the secret key to bind */
	CATCH_NULL( fileInput = fopen(filenameInput, "rb") );
	fseek (fileInput, 0, SEEK_END);
	lengthInputFile = ftell (fileInput);
	fseek (fileInput, 0, SEEK_SET);
	contentInputFile = malloc (lengthInputFile);
	CATCH_ERROR( fread(contentInputFile, 1, lengthInputFile, fileInput) != lengthInputFile );
	fclose(fileInput);
	fileInput = NULL;
	
	/* create an object to store the secret key */
	CATCH_TSS_ERROR( Tspi_Context_CreateObject(hContext, TSS_OBJECT_TYPE_ENCDATA, TSS_ENCDATA_BIND, &hEncdata) );
	
	/* bind the secret key with the public key */
	CATCH_TSS_ERROR( Tspi_Data_Bind(hEncdata, hKey, lengthInputFile, contentInputFile) );
	
	/* get a reference to the encrypted data .. TPM_BOUND_DATA */
	CATCH_TSS_ERROR( Tspi_GetAttribData(hEncdata, TSS_TSPATTRIB_ENCDATA_BLOB, TSS_TSPATTRIB_ENCDATABLOB_BLOB, &lengthEncData, &encData) );

	/* write the encrypted data to the output file */
	CATCH_NULL( fileOutput = fopen(filenameOutput, "wb") );
	CATCH_ERROR( fwrite(encData, 1, lengthEncData, fileOutput) != lengthEncData );
	fclose(fileOutput);
	fileOutput = NULL;
	
	
	exitCode = 0;
	
	out_close:
	if( hKey ) { Tspi_Context_CloseObject(hContext, hKey); }
	Tspi_Context_Close(hContext);

	out:
	return exitCode;
}
