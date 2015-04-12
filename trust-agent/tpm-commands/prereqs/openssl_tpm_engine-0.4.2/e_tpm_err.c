
/*
 * Licensed Materials - Property of IBM
 *
 * OpenSSL TPM engine
 *
 * Copyright (C) International Business Machines  Corp., 2005
 *
 */

/*
 * e_tpm_err.c
 *
 * Kent Yoder <yoder1@us.ibm.com>
 *
 */

#include <stdio.h>

#include <openssl/err.h>
#include <openssl/dso.h>
#include <openssl/engine.h>

#include <tss/platform.h>
#include <tss/tss_defines.h>
#include <tss/tss_typedef.h>
#include <tss/tss_structs.h>
#include <tss/tss_error.h>
#include <tss/tspi.h>

#include "e_tpm.h"

#ifdef DEBUG
#include <trousers/tss.h>
#include <trousers/trousers.h>

#define TSS_ERROR_LAYER(x)      (x & 0x3000)
#define TSS_ERROR_CODE(x)       (x & TSS_MAX_ERROR)

/* function to mimic strerror with TSS error codes */
char *
Trspi_Error_String(TSS_RESULT r)
{
	/* Check the return code to see if it is common to all layers.
	 * If so, return it.
	 */
	switch (TSS_ERROR_CODE(r)) {
		case TSS_SUCCESS:			return "Success";
		default:
			break;
	}

	/* The return code is either unknown, or specific to a layer */
	if (TSS_ERROR_LAYER(r) == TSS_LAYER_TPM) {
		switch (TSS_ERROR_CODE(r)) {
			case TCPA_E_AUTHFAIL:		return "Authentication failed";
			case TCPA_E_BAD_PARAMETER:	return "Bad Parameter";
			case TCPA_E_BADINDEX:		return "Bad index";
			case TCPA_E_AUDITFAILURE:	return "Audit failure";
			case TCPA_E_CLEAR_DISABLED:	return "Clear has been disabled";
			case TCPA_E_DEACTIVATED:	return "TPM is deactivated";
			case TCPA_E_DISABLED:		return "TPM is disabled";
			case TCPA_E_DISABLED_CMD:	return "Disabled command";
			case TCPA_E_FAIL:		return "Operation failed";
			case TCPA_E_INACTIVE:		return "Bad ordinal or unknown command";
			case TCPA_E_INSTALL_DISABLED:	return "Owner install disabled";
			case TCPA_E_INVALID_KEYHANDLE:	return "Invalid keyhandle";
			case TCPA_E_KEYNOTFOUND:	return "Key not found";
			case TCPA_E_NEED_SELFTEST:	return "Bad encryption scheme or need self test";
			case TCPA_E_MIGRATEFAIL:	return "Migration authorization failed";
			case TCPA_E_NO_PCR_INFO:	return "PCR information uninterpretable";
			case TCPA_E_NOSPACE:		return "No space to load key";
			case TCPA_E_NOSRK:		return "No SRK";
			case TCPA_E_NOTSEALED_BLOB:	return "Encrypted blob invalid";
			case TCPA_E_OWNER_SET:		return "Owner already set";
			case TCPA_E_RESOURCES:		return "Insufficient TPM resources";
			case TCPA_E_SHORTRANDOM:	return "Random string too short";
			case TCPA_E_SIZE:		return "TPM out of space";
			case TCPA_E_WRONGPCRVAL:	return "Wrong PCR value";
			case TCPA_E_BAD_PARAM_SIZE:	return "Bad input size";
			case TCPA_E_SHA_THREAD:		return "No existing SHA-1 thread";
			case TCPA_E_SHA_ERROR:		return "SHA-1 error";
			case TCPA_E_FAILEDSELFTEST:	return "Self-test failed, TPM shutdown";
			case TCPA_E_AUTH2FAIL:		return "Second authorization session failed";
			case TCPA_E_BADTAG:		return "Invalid tag";
			case TCPA_E_IOERROR:		return "I/O error";
			case TCPA_E_ENCRYPT_ERROR:	return "Encryption error";
			case TCPA_E_DECRYPT_ERROR:	return "Decryption error";
			case TCPA_E_INVALID_AUTHHANDLE:	return "Invalid authorization handle";
			case TCPA_E_NO_ENDORSEMENT:	return "No EK";
			case TCPA_E_INVALID_KEYUSAGE:	return "Invalid key usage";
			case TCPA_E_WRONG_ENTITYTYPE:	return "Invalid entity type";
			case TCPA_E_INVALID_POSTINIT:	return "Invalid POST init sequence";
			case TCPA_E_INAPPROPRIATE_SIG:	return "Invalid signature format";
			case TCPA_E_BAD_KEY_PROPERTY:	return "Unsupported key parameters";
			case TCPA_E_BAD_MIGRATION:	return "Invalid migration properties";
			case TCPA_E_BAD_SCHEME:		return "Invalid signature or encryption scheme";
			case TCPA_E_BAD_DATASIZE:	return "Invalid data size";
			case TCPA_E_BAD_MODE:		return "Bad mode parameter";
			case TCPA_E_BAD_PRESENCE:	return "Bad physical presence value";
			case TCPA_E_BAD_VERSION:	return "Invalid version";
			case TCPA_E_RETRY:		return "TPM busy: Retry command at a later time";
			default:			return "Unknown error";
		}
	} else if (TSS_ERROR_LAYER(r) == TSS_LAYER_TDDL) {
		switch (TSS_ERROR_CODE(r)) {
			case TSS_E_FAIL:			return "General failure";
			case TSS_E_BAD_PARAMETER:		return "Bad parameter";
			case TSS_E_INTERNAL_ERROR:		return "Internal software error";
			case TSS_E_NOTIMPL:			return "Not implemented";
			case TSS_E_PS_KEY_NOTFOUND:		return "Key not found in persistent storage";
			case TSS_E_KEY_ALREADY_REGISTERED:	return "UUID already registered";
			case TSS_E_CANCELED:			return "The action was cancelled by request";
			case TSS_E_TIMEOUT:			return "The operation has timed out";
			case TSS_E_OUTOFMEMORY:			return "Out of memory";
			case TSS_E_TPM_UNEXPECTED:		return "Unexpected TPM output";
			case TSS_E_COMM_FAILURE:		return "Communication failure";
			case TSS_E_TPM_UNSUPPORTED_FEATURE:	return "Unsupported feature";
			case TDDL_E_COMPONENT_NOT_FOUND:	return "Connection to TPM device failed";
			case TDDL_E_ALREADY_OPENED:		return "Device already opened";
			case TDDL_E_BADTAG:			return "Invalid or unsupported capability";
			case TDDL_E_INSUFFICIENT_BUFFER:	return "Receive buffer too small";
			case TDDL_E_COMMAND_COMPLETED:		return "Command has already completed";
			case TDDL_E_ALREADY_CLOSED:		return "Device driver already closed";
			case TDDL_E_IOERROR:			return "I/O error";
			//case TDDL_E_COMMAND_ABORTED:		return "TPM aborted processing of command";
			default:				return "Unknown";
		}
	} else if (TSS_ERROR_LAYER(r) == TSS_LAYER_TCS) {
		switch (TSS_ERROR_CODE(r)) {
			case TSS_E_FAIL:			return "General failure";
			case TSS_E_BAD_PARAMETER:		return "Bad parameter";
			case TSS_E_INTERNAL_ERROR:		return "Internal software error";
			case TSS_E_NOTIMPL:			return "Not implemented";
			case TSS_E_PS_KEY_NOTFOUND:		return "Key not found in persistent storage";
			case TSS_E_KEY_ALREADY_REGISTERED:	return "UUID already registered";
			case TSS_E_CANCELED:			return "The action was cancelled by request";
			case TSS_E_TIMEOUT:			return "The operation has timed out";
			case TSS_E_OUTOFMEMORY:			return "Out of memory";
			case TSS_E_TPM_UNEXPECTED:		return "Unexpected TPM output";
			case TSS_E_COMM_FAILURE:		return "Communication failure";
			case TSS_E_TPM_UNSUPPORTED_FEATURE:	return "Unsupported feature";
			case TCS_E_KEY_MISMATCH:		return "UUID does not match key handle";
			case TCS_E_KM_LOADFAILED:		return "Key load failed: parent key requires authorization";
			case TCS_E_KEY_CONTEXT_RELOAD:		return "Reload of key context failed";
			case TCS_E_INVALID_CONTEXTHANDLE:	return "Invalid context handle";
			case TCS_E_INVALID_KEYHANDLE:		return "Invalid key handle";
			case TCS_E_INVALID_AUTHHANDLE:		return "Invalid authorization session handle";
			case TCS_E_INVALID_AUTHSESSION:		return "Authorization session has been closed by TPM";
			case TCS_E_INVALID_KEY:			return "Invalid key";
			default:				return "Unknown";
		}
	} else {
		switch (TSS_ERROR_CODE(r)) {
			case TSS_E_FAIL:			return "General failure";
			case TSS_E_BAD_PARAMETER:		return "Bad parameter";
			case TSS_E_INTERNAL_ERROR:		return "Internal software error";
			case TSS_E_NOTIMPL:			return "Not implemented";
			case TSS_E_PS_KEY_NOTFOUND:		return "Key not found in persistent storage";
			case TSS_E_KEY_ALREADY_REGISTERED:	return "UUID already registered";
			case TSS_E_CANCELED:			return "The action was cancelled by request";
			case TSS_E_TIMEOUT:			return "The operation has timed out";
			case TSS_E_OUTOFMEMORY:			return "Out of memory";
			case TSS_E_TPM_UNEXPECTED:		return "Unexpected TPM output";
			case TSS_E_COMM_FAILURE:		return "Communication failure";
			case TSS_E_TPM_UNSUPPORTED_FEATURE:	return "Unsupported feature";
			case TSS_E_INVALID_OBJECT_TYPE:		return "Object type not valid for this operation";
			case TSS_E_INVALID_OBJECT_INITFLAG:	return "Wrong flag creation for object creation";
			case TSS_E_INVALID_HANDLE:		return "Invalid handle";
			case TSS_E_NO_CONNECTION:		return "Core service connection doesn't exist";
			case TSS_E_CONNECTION_FAILED:		return "Core service connection failed";
			case TSS_E_CONNECTION_BROKEN:		return "Communication with core services failed";
			case TSS_E_HASH_INVALID_ALG:		return "Invalid hash algorithm";
			case TSS_E_HASH_INVALID_LENGTH:		return "Hash length is inconsistent with algorithm";
			case TSS_E_HASH_NO_DATA:		return "Hash object has no internal hash value";
			case TSS_E_SILENT_CONTEXT:		return "A silent context requires user input";
			case TSS_E_INVALID_ATTRIB_FLAG:		return "Flag value for attrib-functions inconsistent";
			case TSS_E_INVALID_ATTRIB_SUBFLAG:	return "Sub-flag value for attrib-functions inconsistent";
			case TSS_E_INVALID_ATTRIB_DATA:		return "Data for attrib-functions invalid";
			case TSS_E_NO_PCRS_SET:			return "No PCR registers are selected or set";
			case TSS_E_KEY_NOT_LOADED:		return "The addressed key is not currently loaded";
			case TSS_E_KEY_NOT_SET:			return "No key informatio is currently available";
			case TSS_E_VALIDATION_FAILED:		return "Internal validation of data failed";
			case TSS_E_TSP_AUTHREQUIRED:		return "Authorization is required";
			case TSS_E_TSP_AUTH2REQUIRED:		return "Multiple authorizations are required";
			case TSS_E_TSP_AUTHFAIL:		return "Authorization failed";
			case TSS_E_TSP_AUTH2FAIL:		return "Multiple authorization failed";
			case TSS_E_KEY_NO_MIGRATION_POLICY:	return "Addressed key has no migration policy";
			case TSS_E_POLICY_NO_SECRET:		return "No secret information available for the address policy";
			case TSS_E_INVALID_OBJ_ACCESS:		return "Accessed object is in an inconsistent state";
			case TSS_E_INVALID_ENCSCHEME:		return "Invalid encryption scheme";
			case TSS_E_INVALID_SIGSCHEME:		return "Invalid signature scheme";
			case TSS_E_ENC_INVALID_LENGTH:		return "Invalid length for encrypted data object";
			case TSS_E_ENC_NO_DATA:			return "Encrypted data object contains no data";
			case TSS_E_ENC_INVALID_TYPE:		return "Invalid type for encrypted data object";
			case TSS_E_INVALID_KEYUSAGE:		return "Invalid usage of key";
			case TSS_E_VERIFICATION_FAILED:		return "Internal validation of data failed";
			case TSS_E_HASH_NO_IDENTIFIER:		return "Hash algorithm identifier not set";
			default:				return "Unknown";
		}
	}
}

char *
Trspi_Error_Layer(TSS_RESULT r)
{
	switch (TSS_ERROR_LAYER(r)) {
		case TSS_LAYER_TPM:	return "tpm";
		case TSS_LAYER_TDDL:	return "tddl";
		case TSS_LAYER_TCS:	return "tcs";
		case TSS_LAYER_TSP:	return "tsp";
		default:		return "unknown";
	}
}
#endif

/* BEGIN ERROR CODES */
#ifndef OPENSSL_NO_ERR
static ERR_STRING_DATA TPM_str_functs[] = {
	{ERR_PACK(0, TPM_F_TPM_ENGINE_CTRL, 0), "TPM_ENGINE_CTRL"},
	{ERR_PACK(0, TPM_F_TPM_ENGINE_FINISH, 0), "TPM_ENGINE_FINISH"},
	{ERR_PACK(0, TPM_F_TPM_ENGINE_INIT, 0), "TPM_ENGINE_INIT"},
	{ERR_PACK(0, TPM_F_TPM_RAND_BYTES, 0), "TPM_RAND_BYTES"},
	{ERR_PACK(0, TPM_F_TPM_RSA_KEYGEN, 0), "TPM_RSA_KEYGEN"},
	{ERR_PACK(0, TPM_F_TPM_RSA_PRIV_ENC, 0), "TPM_RSA_PRIV_ENC"},
	{ERR_PACK(0, TPM_F_TPM_RSA_PRIV_DEC, 0), "TPM_RSA_PRIV_DEC"},
	{ERR_PACK(0, TPM_F_TPM_LOAD_SRK, 0), "TPM_LOAD_SRK"},
	{ERR_PACK(0, TPM_F_TPM_RSA_FINISH, 0), "TPM_RSA_FINISH"},
	{ERR_PACK(0, TPM_F_TPM_RSA_INIT, 0), "TPM_RSA_INIT"},
	{ERR_PACK(0, TPM_F_TPM_RAND_SEED, 0), "TPM_RAND_SEED"},
	{ERR_PACK(0, TPM_F_TPM_ENGINE_LOAD_KEY, 0), "TPM_ENGINE_LOAD_KEY"},
	{ERR_PACK(0, TPM_F_TPM_STIR_RANDOM, 0), "TPM_STIR_RANDOM"},
	{ERR_PACK(0, TPM_F_TPM_RSA_PUB_ENC, 0), "TPM_RSA_PUB_ENC"},
	{ERR_PACK(0, TPM_F_TPM_RSA_PUB_DEC, 0), "TPM_RSA_PUB_DEC"},
	{ERR_PACK(0, TPM_F_TPM_BIND_FN, 0), "TPM_BIND_FN"},
	{ERR_PACK(0, TPM_F_TPM_FILL_RSA_OBJECT, 0), "TPM_FILL_RSA_OBJECT"},
	{ERR_PACK(0, TPM_F_TPM_ENGINE_GET_AUTH, 0), "TPM_ENGINE_GET_AUTH"},
	{0, NULL}
};

static ERR_STRING_DATA TPM_str_reasons[] = {
	{TPM_R_ALREADY_LOADED, "already loaded"},
	{TPM_R_CTRL_COMMAND_NOT_IMPLEMENTED, "ctrl command not implemented"},
	{TPM_R_DSO_FAILURE, "dso failure"},
	{TPM_R_MISSING_KEY_COMPONENTS, "missing key components"},
	{TPM_R_NOT_INITIALISED, "not initialised"},
	{TPM_R_NOT_LOADED, "not loaded"},
	{TPM_R_OPERANDS_TOO_LARGE, "operands too large"},
	{TPM_R_OUTLEN_TO_LARGE, "outlen to large"},
	{TPM_R_REQUEST_FAILED, "request failed"},
	{TPM_R_REQUEST_TOO_BIG, "requested number of random bytes > 4096"},
	{TPM_R_UNDERFLOW_CONDITION, "underflow condition"},
	{TPM_R_UNDERFLOW_KEYRECORD, "underflow keyrecord"},
	{TPM_R_UNIT_FAILURE, "unit failure"},
	{TPM_R_INVALID_KEY_SIZE, "invalid key size"},
	{TPM_R_BN_CONVERSION_FAILED, "bn conversion failed"},
	{TPM_R_INVALID_EXPONENT, "invalid exponent"},
	{TPM_R_NO_APP_DATA, "no app data in RSA object"},
	{TPM_R_INVALID_ENC_SCHEME, "invalid encryption scheme"},
	{TPM_R_INVALID_MSG_SIZE, "invalid message size to sign"},
	{TPM_R_INVALID_PADDING_TYPE, "invalid padding type"},
	{TPM_R_INVALID_KEY, "invalid key"},
	{TPM_R_SRK_LOAD_FAILED, "failed loading the SRK"},
	{TPM_R_FILE_NOT_FOUND, "file to load not found"},
	{TPM_R_FILE_READ_FAILED, "failed reading the key file"},
	{TPM_R_ID_INVALID, "engine id doesn't match"},
	{TPM_R_UI_METHOD_FAILED, "ui function failed"},
	{0, NULL}
};

#endif

static ERR_STRING_DATA TPM_lib_name[] = {
	{0, TPM_LIB_NAME},
	{0, NULL}
};


static int TPM_lib_error_code = 0;
static int TPM_error_init = 1;

void ERR_load_TPM_strings(void)
{
	DBG("%s", __FUNCTION__);
	if (TPM_lib_error_code == 0) {
		TPM_lib_error_code = ERR_get_next_error_library();
		DBG("TPM_lib_error_code is %d", TPM_lib_error_code);
	}

	if (TPM_error_init) {
		TPM_error_init = 0;
#ifndef OPENSSL_NO_ERR
		ERR_load_strings(TPM_lib_error_code, TPM_str_functs);
		ERR_load_strings(TPM_lib_error_code, TPM_str_reasons);
#endif
		TPM_lib_name[0].error = ERR_PACK(TPM_lib_error_code, 0, 0);
		ERR_load_strings(0, TPM_lib_name);
	}
}

void ERR_unload_TPM_strings(void)
{
	DBG("%s", __FUNCTION__);

	if (TPM_error_init == 0) {
#ifndef OPENSSL_NO_ERR
		ERR_unload_strings(TPM_lib_error_code, TPM_str_functs);
		ERR_unload_strings(TPM_lib_error_code, TPM_str_reasons);
#endif

		ERR_load_strings(0, TPM_lib_name);
		TPM_error_init = 1;
	}
}

void ERR_TSS_error(int function, int reason, char *file, int line)
{
	DBG("%s", __FUNCTION__);

	if (TPM_lib_error_code == 0)
		TPM_lib_error_code = ERR_get_next_error_library();

	ERR_PUT_error(TPM_lib_error_code, function, reason, file, line);
}

