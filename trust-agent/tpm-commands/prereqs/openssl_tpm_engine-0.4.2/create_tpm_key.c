/*
 *
 *   Copyright (C) International Business Machines  Corp., 2005-2006
 *
 *   This program is free software;  you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; version 2.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY;  without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See
 *   the GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program;  if not, write to the Free Software
 *   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 *   ===================================================================
 *   In addition, as a special exception, the copyright holders give
 *   permission to link the code of portions of this program with the
 *   OpenSSL library under certain conditions as described in each
 *   individual source file, and distribute linked combinations
 *   including the two.
 *   You must obey the GNU General Public License in all respects
 *   for all of the code used other than OpenSSL.  If you modify
 *   file(s) with this exception, you may extend this exception to your
 *   version of the file(s), but you are not obligated to do so.  If you
 *   do not wish to do so, delete this exception statement from your
 *   version.  If you delete this exception statement from all source
 *   files in the program, then also delete it here.
 *   ===================================================================
 */


#include <stdio.h>
#include <getopt.h>
#include <string.h>
#include <strings.h>
#include <errno.h>

#include <openssl/rsa.h>
#include <openssl/pem.h>
#include <openssl/evp.h>
#include <openssl/err.h>

#include <trousers/tss.h>
#include <trousers/trousers.h>

#define print_error(a,b) \
	fprintf(stderr, "%s:%d %s result: 0x%x (%s)\n", __FILE__, __LINE__, \
		a, b, Trspi_Error_String(b))

static struct option long_options[] = {
	{"enc-scheme", 1, 0, 'e'},
	{"sig-scheme", 1, 0, 'q'},
	{"key-size", 1, 0, 's'},
	{"auth", 0, 0, 'a'},
	{"popup", 0, 0, 'p'},
	{"wrap", 1, 0, 'w'},
	{"help", 0, 0, 'h'},
	{0, 0, 0, 0}
};

void
usage(char *argv0)
{
	fprintf(stderr, "\t%s: create a TPM key and write it to disk\n"
		"\tusage: %s [options] <filename>\n\n"
		"\tOptions:\n"
		"\t\t-e|--enc-scheme  encryption scheme to use [PKCSV15] or OAEP\n"
		"\t\t-q|--sig-scheme  signature scheme to use [DER] or SHA1\n"
		"\t\t-s|--key-size    key size in bits [2048]\n"
		"\t\t-a|--auth        require a password for the key [NO]\n"
		"\t\t-p|--popup       use TSS GUI popup dialogs to get the password "
		"for the\n\t\t\t\t key [NO] (implies --auth)\n"
		"\t\t-w|--wrap [file] wrap an existing openssl PEM key\n"
		"\t\t-h|--help        print this help message\n"
		"\nReport bugs to %s\n",
		argv0, argv0, PACKAGE_BUGREPORT);
	exit(-1);
}

TSS_UUID SRK_UUID = TSS_UUID_SRK;

void
openssl_print_errors()
{
	ERR_load_ERR_strings();
	ERR_load_crypto_strings();
	ERR_print_errors_fp(stderr);
}

RSA *
openssl_read_key(char *filename)
{
        BIO *b = NULL;
        RSA *rsa = NULL;

        b = BIO_new_file(filename, "r");
        if (b == NULL) {
                fprintf(stderr, "Error opening file for read: %s\n", filename);
                return NULL;
        }

        if ((rsa = PEM_read_bio_RSAPrivateKey(b, NULL, 0, NULL)) == NULL) {
                fprintf(stderr, "Reading key %s from disk failed.\n", filename);
                openssl_print_errors();
        }
	BIO_free(b);

        return rsa;
}

int
openssl_get_modulus_and_prime(RSA *rsa, unsigned int *size_n, unsigned char *n,
			      unsigned int *size_p, unsigned char *p)
{
	/* get the modulus from the RSA object */
	if ((*size_n = BN_bn2bin(rsa->n, n)) <= 0) {
		openssl_print_errors();
		return -1;
	}

	/* get one of the primes from the RSA object */
	if ((*size_p = BN_bn2bin(rsa->p, p)) <= 0) {
		openssl_print_errors();
		return -1;
	}

	return 0;
}


int main(int argc, char **argv)
{
	TSS_HCONTEXT	hContext;
	TSS_FLAG	initFlags = TSS_KEY_TYPE_LEGACY | TSS_KEY_VOLATILE;
	TSS_HKEY	hKey;
	TSS_HKEY	hSRK;
	TSS_RESULT	result;
	TSS_HPOLICY	srkUsagePolicy, keyUsagePolicy, keyMigrationPolicy;
	BYTE		*blob;
	UINT32		blob_size, srk_authusage;
	BIO		*outb;
	ASN1_OCTET_STRING *blob_str;
	unsigned char	*blob_asn1 = NULL;
	int		asn1_len;
	char		*filename, c, *openssl_key = NULL;
	int		option_index, auth = 0, popup = 0, wrap = 0;
	UINT32		enc_scheme = TSS_ES_RSAESPKCSV15;
	UINT32		sig_scheme = TSS_SS_RSASSAPKCS1V15_DER;
	UINT32		key_size = 2048;
	RSA		*rsa;

	while (1) {
		option_index = 0;
		c = getopt_long(argc, argv, "pe:q:s:ahw:",
				long_options, &option_index);
		if (c == -1)
			break;

		switch (c) {
			case 'a':
				initFlags |= TSS_KEY_AUTHORIZATION;
				auth = 1;
				break;
			case 'h':
				usage(argv[0]);
				break;
			case 's':
				key_size = atoi(optarg);
				break;
			case 'e':
				if (!strncasecmp("oaep", optarg, 4)) {
					enc_scheme = TSS_ES_RSAESOAEP_SHA1_MGF1;
				} else if (strncasecmp("pkcs", optarg, 4)) {
					usage(argv[0]);
				}
				break;
			case 'q':
				if (!strncasecmp("der", optarg, 3)) {
					sig_scheme = TSS_SS_RSASSAPKCS1V15_SHA1;
				} else if (strncasecmp("sha", optarg, 3)) {
					usage(argv[0]);
				}
				break;
			case 'p':
				initFlags |= TSS_KEY_AUTHORIZATION;
				auth = 1;
				popup = 1;
				break;
			case 'w':
				initFlags |= TSS_KEY_MIGRATABLE;
				wrap = 1;
				openssl_key = optarg;
				break;
			default:
				usage(argv[0]);
				break;
		}
	}

	/* set up the key option flags */
	switch (key_size) {
		case 512:
			initFlags |= TSS_KEY_SIZE_512;
			break;
		case 1024:
			initFlags |= TSS_KEY_SIZE_1024;
			break;
		case 2048:
			initFlags |= TSS_KEY_SIZE_2048;
			break;
		case 4096:
			initFlags |= TSS_KEY_SIZE_4096;
			break;
		case 8192:
			initFlags |= TSS_KEY_SIZE_8192;
			break;
		case 16384:
			initFlags |= TSS_KEY_SIZE_16384;
			break;
		default:
			usage(argv[0]);
			break;
	}
#if 0
	while (argc--) {
		printf("argv[%d] = \"%s\"\n", argc, argv[argc]);
	}
	exit(1);
#endif
	filename = argv[argc - 1];
	if (argc < 2 || filename[0] == '-')
		usage(argv[0]);

		//Create Context
	if ((result = Tspi_Context_Create(&hContext))) {
		print_error("Tspi_Context_Create", result);
		exit(result);
	}
		//Connect Context
	if ((result = Tspi_Context_Connect(hContext, NULL))) {
		print_error("Tspi_Context_Connect", result);
		Tspi_Context_Close(hContext);
		exit(result);
	}

		//Create Object
	if ((result = Tspi_Context_CreateObject(hContext,
						TSS_OBJECT_TYPE_RSAKEY,
						initFlags, &hKey))) {
		print_error("Tspi_Context_CreateObject", result);
		Tspi_Context_Close(hContext);
		exit(result);
	}

	if ((result = Tspi_SetAttribUint32(hKey, TSS_TSPATTRIB_KEY_INFO,
					   TSS_TSPATTRIB_KEYINFO_SIGSCHEME,
					   sig_scheme))) {
		print_error("Tspi_SetAttribUint32", result);
		Tspi_Context_CloseObject(hContext, hKey);
		Tspi_Context_Close(hContext);
		exit(result);
	}

	if ((result = Tspi_SetAttribUint32(hKey, TSS_TSPATTRIB_KEY_INFO,
					   TSS_TSPATTRIB_KEYINFO_ENCSCHEME,
					   enc_scheme))) {
		print_error("Tspi_SetAttribUint32", result);
		Tspi_Context_CloseObject(hContext, hKey);
		Tspi_Context_Close(hContext);
		exit(result);
	}

		//Load Key By UUID
	if ((result = Tspi_Context_LoadKeyByUUID(hContext, TSS_PS_TYPE_SYSTEM,
						 SRK_UUID, &hSRK))) {
		print_error("Tspi_Context_LoadKeyByUUID", result);
		Tspi_Context_CloseObject(hContext, hKey);
		Tspi_Context_Close(hContext);
		exit(result);
	}

	if ((result = Tspi_GetAttribUint32(hSRK, TSS_TSPATTRIB_KEY_INFO,
					   TSS_TSPATTRIB_KEYINFO_AUTHUSAGE,
					   &srk_authusage))) {
		print_error("Tspi_GetAttribUint32", result);
		Tspi_Context_CloseObject(hContext, hKey);
		Tspi_Context_Close(hContext);
		exit(result);
	}

	if (srk_authusage) {
		char *authdata = calloc(1, 128);

		if (!authdata) {
			fprintf(stderr, "malloc failed.\n");
			Tspi_Context_Close(hContext);
			exit(result);
		}

		if ((result = Tspi_GetPolicyObject(hSRK, TSS_POLICY_USAGE,
						   &srkUsagePolicy))) {
			print_error("Tspi_GetPolicyObject", result);
			Tspi_Context_CloseObject(hContext, hKey);
			Tspi_Context_Close(hContext);
			free(authdata);
			exit(result);
		}

		if (EVP_read_pw_string(authdata, 128, "SRK Password: ", 0)) {
			Tspi_Context_CloseObject(hContext, hKey);
			Tspi_Context_Close(hContext);
			free(authdata);
			exit(result);
		}

		//Set Secret
		if ((result = Tspi_Policy_SetSecret(srkUsagePolicy,
						    TSS_SECRET_MODE_PLAIN,
						    strlen(authdata),
						    (BYTE *)authdata))) {
			print_error("Tspi_Policy_SetSecret", result);
			free(authdata);
			Tspi_Context_Close(hContext);
			exit(result);
		}

		free(authdata);
	}

	if (auth) {
		if ((result = Tspi_Context_CreateObject(hContext,
							TSS_OBJECT_TYPE_POLICY,
							TSS_POLICY_USAGE,
							&keyUsagePolicy))) {
			print_error("Tspi_Context_CreateObject", result);
			Tspi_Context_Close(hContext);
			exit(result);
		}

		if (popup) {
			//Set Secret
			if ((result = Tspi_Policy_SetSecret(keyUsagePolicy,
							    TSS_SECRET_MODE_POPUP,
							    0, NULL))) {
				print_error("Tspi_Policy_SetSecret", result);
				Tspi_Context_Close(hContext);
				exit(result);
			}
		} else {
			char *authdata = calloc(1, 128);

			if (!authdata) {
				fprintf(stderr, "malloc failed.\n");
				Tspi_Context_Close(hContext);
				exit(result);
			}

			if (EVP_read_pw_string(authdata, 128,
						"Enter Key Usage Password: ", 1)) {
				printf("Passwords do not match.\n");
				free(authdata);
				Tspi_Context_Close(hContext);
				exit(result);
			}

			//Set Secret
			if ((result = Tspi_Policy_SetSecret(keyUsagePolicy,
							    TSS_SECRET_MODE_PLAIN,
							    strlen(authdata),
							    (BYTE *)authdata))) {
				print_error("Tspi_Policy_SetSecret", result);
				free(authdata);
				Tspi_Context_Close(hContext);
				exit(result);
			}

			free(authdata);
		}

		if ((result = Tspi_Policy_AssignToObject(keyUsagePolicy, hKey))) {
			print_error("Tspi_Policy_AssignToObject", result);
			Tspi_Context_CloseObject(hContext, hKey);
			Tspi_Context_Close(hContext);
			exit(result);
		}
	}

	// Create or Wrap Key
	if (wrap) {
		char n[256], p[128];
		unsigned int size_n, size_p;
		BYTE *pubSRK;

		/*Set migration policy needed to wrap the key*/
		if ((result = Tspi_Context_CreateObject(hContext,
						TSS_OBJECT_TYPE_POLICY,
						TSS_POLICY_MIGRATION,
						&keyMigrationPolicy))) {
			print_error("Tspi_Context_CreateObject", result);
			Tspi_Context_Close(hContext);
			exit(result);
		}
		if (auth) {
			char *authdata = calloc(1, 128);

			if (!authdata) {
				fprintf(stderr, "malloc failed.\n");
				Tspi_Context_Close(hContext);
				exit(result);
			}

			if (EVP_read_pw_string(authdata, 128,
						"Enter Key Migration Password: ", 1)) {
				printf("Passwords do not match.\n");
				free(authdata);
				Tspi_Context_Close(hContext);
				exit(result);
			}

			if ((result = Tspi_Policy_SetSecret(keyMigrationPolicy,
							    TSS_SECRET_MODE_PLAIN,
							    strlen(authdata),
							    (BYTE *)authdata))) {
				print_error("Tspi_Policy_SetSecret", result);
				Tspi_Context_Close(hContext);
				exit(result);
			}

			free(authdata);
		}

		if ((result = Tspi_Policy_AssignToObject(keyMigrationPolicy, hKey))) {
			print_error("Tspi_Policy_AssignToObject", result);
			Tspi_Context_CloseObject(hContext, hKey);
			Tspi_Context_Close(hContext);
			exit(result);
		}

		/* Pull the PubKRK out of the TPM */
		if ((result = Tspi_Key_GetPubKey(hSRK, &size_n, &pubSRK))) {
			print_error("Tspi_Key_WrapKey", result);
			Tspi_Context_CloseObject(hContext, hKey);
			Tspi_Context_Close(hContext);
			exit(result);
		}
		Tspi_Context_FreeMemory(hContext, pubSRK);

		if ((rsa = openssl_read_key(openssl_key)) == NULL) {
			Tspi_Context_CloseObject(hContext, hKey);
			Tspi_Context_Close(hContext);
			exit(-1);
		}

		if (RSA_size(rsa) != key_size / 8) {
			fprintf(stderr,
				"Error, key size is incorrect, please use the '-s' option\n");
			RSA_free(rsa);
			Tspi_Context_CloseObject(hContext, hKey);
			Tspi_Context_Close(hContext);
			exit(-1);
		}

		if (openssl_get_modulus_and_prime(rsa, &size_n, (unsigned char *)n,
						  &size_p, (unsigned char *)p)) {
			fprintf(stderr, "Error getting modulus and prime!\n");
			RSA_free(rsa);
			Tspi_Context_CloseObject(hContext, hKey);
			Tspi_Context_Close(hContext);
			exit(-1);
		}

		if ((result = Tspi_SetAttribData(hKey, TSS_TSPATTRIB_RSAKEY_INFO,
						 TSS_TSPATTRIB_KEYINFO_RSA_MODULUS,
						 size_n, (BYTE *)n))) {
			print_error("Tspi_SetAttribData (RSA modulus)", result);
			RSA_free(rsa);
			Tspi_Context_CloseObject(hContext, hKey);
			Tspi_Context_Close(hContext);
			exit(-1);
		}

		if ((result = Tspi_SetAttribData(hKey, TSS_TSPATTRIB_KEY_BLOB,
						 TSS_TSPATTRIB_KEYBLOB_PRIVATE_KEY,
						 size_p, (BYTE *)p))) {
			print_error("Tspi_SetAttribData (private key)", result);
			RSA_free(rsa);
			Tspi_Context_CloseObject(hContext, hKey);
			Tspi_Context_Close(hContext);
			exit(-1);
		}

		if ((result = Tspi_Key_WrapKey(hKey, hSRK, 0))) {
			print_error("Tspi_Key_WrapKey", result);
			Tspi_Context_CloseObject(hContext, hKey);
			Tspi_Context_Close(hContext);
			exit(result);
		}
	} else {
		if ((result = Tspi_Key_CreateKey(hKey, hSRK, 0))) {
			print_error("Tspi_Key_CreateKey", result);
			Tspi_Context_CloseObject(hContext, hKey);
			Tspi_Context_Close(hContext);
			exit(result);
		}
	}

	if ((result = Tspi_GetAttribData(hKey, TSS_TSPATTRIB_KEY_BLOB,
					 TSS_TSPATTRIB_KEYBLOB_BLOB,
					 &blob_size, &blob))) {
		print_error("Tspi_GetAttribData", result);
		Tspi_Context_CloseObject(hContext, hKey);
		Tspi_Context_Close(hContext);
		exit(result);
	}

	if ((outb = BIO_new_file(filename, "w")) == NULL) {
                fprintf(stderr, "Error opening file for write: %s\n", filename);
		Tspi_Context_CloseObject(hContext, hKey);
		Tspi_Context_Close(hContext);
		exit(-1);
	}
	blob_str = ASN1_OCTET_STRING_new();
	if (!blob_str) {
                fprintf(stderr, "Error allocating ASN1_OCTET_STRING\n");
		Tspi_Context_CloseObject(hContext, hKey);
		Tspi_Context_Close(hContext);
		exit(-1);
	}		

	ASN1_STRING_set(blob_str, blob, blob_size);
	asn1_len = i2d_ASN1_OCTET_STRING(blob_str, &blob_asn1);
	PEM_write_bio(outb, "TSS KEY BLOB", "", blob_asn1, asn1_len);

	BIO_free(outb);
	Tspi_Context_Close(hContext);

	printf("Success.\n");

	return 0;
}
