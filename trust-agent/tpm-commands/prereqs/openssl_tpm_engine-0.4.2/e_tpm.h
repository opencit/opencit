/*
 * Licensed Materials - Property of IBM
 *
 * OpenSSL TPM engine
 *
 * Copyright (C) International Business Machines  Corp., 2005
 *
 */

/*
 * e_tpm.h
 *
 * Kent Yoder <yoder1@us.ibm.com>
 *
 */

#ifndef _E_TPM_H
#define _E_TPM_H

#define TPM_LIB_NAME "tpm engine"

#define NULL_HCONTEXT	0
#define NULL_HKEY	0
#define NULL_HTPM	0
#define NULL_HHASH	0
#define NULL_HENCDATA	0
#define NULL_HPOLICY	0
#define NULL_HPCRS	0

void ERR_load_TPM_strings(void);
void ERR_unload_TPM_strings(void);
void ERR_TSS_error(int function, int reason, char *file, int line);
#ifdef DEBUG
#define TSSerr(f,r) \
	do { \
		ERR_TSS_error((f),(r),__FILE__,__LINE__); \
		ERR_print_errors_fp(stderr); \
	} while (0)
#define DBG(x, ...)	fprintf(stderr, "DEBUG %s:%d " x "\n", __FILE__,__LINE__, ##__VA_ARGS__)
#define DBGFN(x, ...)	fprintf(stderr, "DEBUG %s:%d %s " x "\n", __FILE__,__LINE__,__FUNCTION__,##__VA_ARGS__)
#else
#define TSSerr(f,r)	ERR_TSS_error((f),(r),__FILE__,__LINE__)
#define DBG(x, ...)
#define DBGFN(x, ...)
#endif

/* Error codes for the TPM functions. */

/* Function codes. */
#define TPM_F_TPM_ENGINE_CTRL			100
#define TPM_F_TPM_ENGINE_FINISH			101
#define TPM_F_TPM_ENGINE_INIT			102
#define TPM_F_TPM_RAND_BYTES			103
#define TPM_F_TPM_RSA_KEYGEN			104
#define TPM_F_TPM_RSA_PRIV_ENC			105
#define TPM_F_TPM_RSA_PRIV_DEC			106
#define TPM_F_TPM_LOAD_SRK			107
#define TPM_F_TPM_RSA_FINISH			108
#define TPM_F_TPM_RSA_INIT			109
#define TPM_F_TPM_RAND_SEED			110
#define TPM_F_TPM_ENGINE_LOAD_KEY		111
#define TPM_F_TPM_STIR_RANDOM			112
#define TPM_F_TPM_RSA_PUB_ENC			113
#define TPM_F_TPM_RSA_PUB_DEC			114
#define TPM_F_TPM_BIND_FN			115
#define TPM_F_TPM_FILL_RSA_OBJECT		116
#define TPM_F_TPM_ENGINE_GET_AUTH		117
#define TPM_F_TPM_CREATE_SRK_POLICY		118

/* Reason codes. */
#define TPM_R_ALREADY_LOADED			100
#define TPM_R_CTRL_COMMAND_NOT_IMPLEMENTED	101
#define TPM_R_DSO_FAILURE			102
#define TPM_R_MEXP_LENGTH_TO_LARGE		103
#define TPM_R_MISSING_KEY_COMPONENTS		104
#define TPM_R_NOT_INITIALISED			105
#define TPM_R_NOT_LOADED			106
#define TPM_R_OPERANDS_TOO_LARGE		107
#define TPM_R_OUTLEN_TO_LARGE			108
#define TPM_R_REQUEST_FAILED			109
#define TPM_R_UNDERFLOW_CONDITION		110
#define TPM_R_UNDERFLOW_KEYRECORD		111
#define TPM_R_UNIT_FAILURE			112
#define TPM_R_INVALID_KEY_SIZE			113
#define TPM_R_BN_CONVERSION_FAILED		114
#define TPM_R_INVALID_EXPONENT			115
#define TPM_R_REQUEST_TOO_BIG			116
#define TPM_R_NO_APP_DATA			117
#define TPM_R_INVALID_ENC_SCHEME		118
#define TPM_R_INVALID_MSG_SIZE			119
#define TPM_R_INVALID_PADDING_TYPE		120
#define TPM_R_INVALID_KEY			121
#define TPM_R_SRK_LOAD_FAILED			122
#define TPM_R_FILE_NOT_FOUND			123
#define TPM_R_FILE_READ_FAILED			124
#define TPM_R_ID_INVALID			125
#define TPM_R_UI_METHOD_FAILED			126
#define TPM_R_UNKNOWN_SECRET_MODE		127

/* structure pointed to by the RSA object's app_data pointer */
struct rsa_app_data
{
	TSS_HKEY hKey;
	TSS_HHASH hHash;
	TSS_HENCDATA hEncData;
	UINT32 encScheme;
	UINT32 sigScheme;
};

#define TPM_ENGINE_EX_DATA_UNINIT		-1
#define RSA_PKCS1_OAEP_PADDING_SIZE		(2 * SHA_DIGEST_LENGTH + 2)

#ifndef MIN
#define MIN(x,y)	(x > y ? y : x)
#endif

#endif
