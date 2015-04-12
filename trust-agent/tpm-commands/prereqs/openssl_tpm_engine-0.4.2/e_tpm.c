
/*
 * Licensed Materials - Property of IBM
 *
 * OpenSSL TPM engine
 *
 * Copyright (C) International Business Machines  Corp., 2005
 *
 */

/*
 * e_tpm.c
 *
 * Kent Yoder <yoder1@us.ibm.com>
 *
 */

#include <stdio.h>
#include <string.h>

#include <openssl/crypto.h>
#include <openssl/dso.h>
#include <openssl/engine.h>
#include <openssl/evp.h>
#include <openssl/objects.h>
#include <openssl/sha.h>
#include <openssl/bn.h>
#include <openssl/pem.h>

#include <tss/platform.h>
#include <tss/tss_defines.h>
#include <tss/tss_typedef.h>
#include <tss/tss_structs.h>
#include <tss/tss_error.h>
#include <tss/tspi.h>

#include <trousers/trousers.h>  // XXX DEBUG

#include "e_tpm.h"

//#define DLOPEN_TSPI

#ifndef OPENSSL_NO_HW
#ifndef OPENSSL_NO_HW_TPM

/* engine specific functions */
static int tpm_engine_destroy(ENGINE *);
static int tpm_engine_init(ENGINE *);
static int tpm_engine_finish(ENGINE *);
static int tpm_engine_ctrl(ENGINE *, int, long, void *, void (*)());
static EVP_PKEY *tpm_engine_load_key(ENGINE *, const char *, UI_METHOD *, void *);
static char *tpm_engine_get_auth(UI_METHOD *, char *, int, char *, void *);

#ifndef OPENSSL_NO_RSA
/* rsa functions */
static int tpm_rsa_init(RSA *rsa);
static int tpm_rsa_finish(RSA *rsa);
static int tpm_rsa_pub_dec(int, const unsigned char *, unsigned char *, RSA *, int);
static int tpm_rsa_pub_enc(int, const unsigned char *, unsigned char *, RSA *, int);
static int tpm_rsa_priv_dec(int, const unsigned char *, unsigned char *, RSA *, int);
static int tpm_rsa_priv_enc(int, const unsigned char *, unsigned char *, RSA *, int);
//static int tpm_rsa_sign(int, const unsigned char *, unsigned int, unsigned char *, unsigned int *, const RSA *);
static int tpm_rsa_keygen(RSA *, int, BIGNUM *, BN_GENCB *);
#endif

/* random functions */
static int tpm_rand_bytes(unsigned char *, int);
static int tpm_rand_status(void);
static void tpm_rand_seed(const void *, int);

/* The definitions for control commands specific to this engine */
#define TPM_CMD_SO_PATH		ENGINE_CMD_BASE
#define TPM_CMD_PIN		ENGINE_CMD_BASE+1
#define TPM_CMD_SECRET_MODE	ENGINE_CMD_BASE+2
static const ENGINE_CMD_DEFN tpm_cmd_defns[] = {
	{TPM_CMD_SO_PATH,
	 "SO_PATH",
	 "Specifies the path to the libtspi.so shared library",
	 ENGINE_CMD_FLAG_STRING},
	{TPM_CMD_PIN,
	 "PIN",
	 "Specifies the secret for the SRK (default is plaintext, else set SECRET_MODE)",
	 ENGINE_CMD_FLAG_STRING},
	{TPM_CMD_SECRET_MODE,
	 "SECRET_MODE",
	 "The TSS secret mode for all secrets",
	 ENGINE_CMD_FLAG_NUMERIC},
	{0, NULL, NULL, 0}
};

#ifndef OPENSSL_NO_RSA
static RSA_METHOD tpm_rsa = {
	"TPM RSA method",
	tpm_rsa_pub_enc,
	tpm_rsa_pub_dec,
	tpm_rsa_priv_enc,
	tpm_rsa_priv_dec,
	NULL, /* set in tpm_engine_init */
	BN_mod_exp_mont,
	tpm_rsa_init,
	tpm_rsa_finish,
	(RSA_FLAG_SIGN_VER | RSA_FLAG_NO_BLINDING),
	NULL,
	NULL, /* sign */
	NULL, /* verify */
	tpm_rsa_keygen
};
#endif

static RAND_METHOD tpm_rand = {
	/* "TPM RAND method", */
	tpm_rand_seed,
	tpm_rand_bytes,
	NULL,
	NULL,
	tpm_rand_bytes,
	tpm_rand_status,
};

/* Constants used when creating the ENGINE */
static const char *engine_tpm_id = "tpm";
static const char *engine_tpm_name = "TPM hardware engine support";
static const char *TPM_LIBNAME = "tspi";

static TSS_HCONTEXT hContext    = NULL_HCONTEXT;
static TSS_HKEY     hSRK        = NULL_HKEY;
static TSS_HPOLICY  hSRKPolicy  = NULL_HPOLICY;
static TSS_HTPM     hTPM        = NULL_HTPM;
static TSS_UUID     SRK_UUID    = TSS_UUID_SRK;
static UINT32       secret_mode = TSS_SECRET_MODE_PLAIN;

/* varibles used to get/set CRYPTO_EX_DATA values */
int  ex_app_data = TPM_ENGINE_EX_DATA_UNINIT;

#ifdef DLOPEN_TSPI
/* This is a process-global DSO handle used for loading and unloading
 * the TSS library. NB: This is only set (or unset) during an
 * init() or finish() call (reference counts permitting) and they're
 * operating with global locks, so this should be thread-safe
 * implicitly. */

static DSO *tpm_dso = NULL;

/* These are the function pointers that are (un)set when the library has
 * successfully (un)loaded. */
static unsigned int (*p_tspi_Context_Create)();
static unsigned int (*p_tspi_Context_Close)();
static unsigned int (*p_tspi_Context_Connect)();
static unsigned int (*p_tspi_Context_FreeMemory)();
static unsigned int (*p_tspi_Context_CreateObject)();
static unsigned int (*p_tspi_Context_LoadKeyByUUID)();
static unsigned int (*p_tspi_Context_LoadKeyByBlob)();
static unsigned int (*p_tspi_Context_GetTpmObject)();
static unsigned int (*p_tspi_TPM_GetRandom)();
static unsigned int (*p_tspi_TPM_StirRandom)();
static unsigned int (*p_tspi_Key_CreateKey)();
static unsigned int (*p_tspi_Key_LoadKey)();
static unsigned int (*p_tspi_Data_Bind)();
static unsigned int (*p_tspi_Data_Unbind)();
static unsigned int (*p_tspi_GetAttribData)();
static unsigned int (*p_tspi_SetAttribData)();
static unsigned int (*p_tspi_SetAttribUint32)();
static unsigned int (*p_tspi_GetAttribUint32)();
static unsigned int (*p_tspi_Context_CloseObject)();
static unsigned int (*p_tspi_Hash_Sign)();
static unsigned int (*p_tspi_Hash_SetHashValue)();
static unsigned int (*p_tspi_GetPolicyObject)();
static unsigned int (*p_tspi_Policy_SetSecret)();
static unsigned int (*p_tspi_Policy_AssignToObject)();

/* Override the real function calls to use our indirect pointers */
#define Tspi_Context_Create p_tspi_Context_Create
#define Tspi_Context_Close p_tspi_Context_Close
#define Tspi_Context_Connect p_tspi_Context_Connect
#define Tspi_Context_CreateObject p_tspi_Context_CreateObject
#define Tspi_Context_CloseObject p_tspi_Context_CloseObject
#define Tspi_Context_FreeMemory p_tspi_Context_FreeMemory
#define Tspi_Context_LoadKeyByBlob p_tspi_Context_LoadKeyByBlob
#define Tspi_Context_LoadKeyByUUID p_tspi_Context_LoadKeyByUUID
#define Tspi_Context_GetTpmObject p_tspi_Context_GetTpmObject
#define Tspi_TPM_GetRandom p_tspi_TPM_GetRandom
#define Tspi_TPM_StirRandom p_tspi_TPM_StirRandom
#define Tspi_Key_CreateKey p_tspi_Key_CreateKey
#define Tspi_Key_LoadKey p_tspi_Key_LoadKey
#define Tspi_Data_Bind p_tspi_Data_Bind
#define Tspi_Data_Unbind p_tspi_Data_Unbind
#define Tspi_GetAttribData p_tspi_GetAttribData
#define Tspi_SetAttribData p_tspi_SetAttribData
#define Tspi_GetAttribUint32 p_tspi_GetAttribUint32
#define Tspi_SetAttribUint32 p_tspi_SetAttribUint32
#define Tspi_GetPolicyObject p_tspi_GetPolicyObject
#define Tspi_Hash_Sign p_tspi_Hash_Sign
#define Tspi_Hash_SetHashValue p_tspi_Hash_SetHashValue
#define Tspi_Policy_SetSecret p_tspi_Policy_SetSecret
#define Tspi_Policy_AssignToObject p_tspi_Policy_AssignToObject
#endif /* DLOPEN_TSPI */

/* This internal function is used by ENGINE_tpm() and possibly by the
 * "dynamic" ENGINE support too */
static int bind_helper(ENGINE * e)
{
	if (!ENGINE_set_id(e, engine_tpm_id) ||
	    !ENGINE_set_name(e, engine_tpm_name) ||
#ifndef OPENSSL_NO_RSA
	    !ENGINE_set_RSA(e, &tpm_rsa) ||
#endif
	    !ENGINE_set_RAND(e, &tpm_rand) ||
	    !ENGINE_set_destroy_function(e, tpm_engine_destroy) ||
	    !ENGINE_set_init_function(e, tpm_engine_init) ||
	    !ENGINE_set_finish_function(e, tpm_engine_finish) ||
	    !ENGINE_set_ctrl_function(e, tpm_engine_ctrl) ||
	    !ENGINE_set_load_pubkey_function(e, tpm_engine_load_key) ||
	    !ENGINE_set_load_privkey_function(e, tpm_engine_load_key) ||
	    !ENGINE_set_cmd_defns(e, tpm_cmd_defns))
		return 0;

	/* Ensure the tpm error handling is set up */
	ERR_load_TPM_strings();
	return 1;
}

static ENGINE *engine_tpm(void)
{
	ENGINE *ret = ENGINE_new();
	DBG("%s", __FUNCTION__);
	if (!ret)
		return NULL;
	if (!bind_helper(ret)) {
		ENGINE_free(ret);
		return NULL;
	}
	return ret;
}

void ENGINE_load_tpm(void)
{
	/* Copied from eng_[openssl|dyn].c */
	ENGINE *toadd = engine_tpm();
	if (!toadd)
		return;
	ENGINE_add(toadd);
	ENGINE_free(toadd);
	ERR_clear_error();
}

int tpm_load_srk(UI_METHOD *ui, void *cb_data)
{
	TSS_RESULT result;
	UINT32 authusage;
	BYTE *auth;

	if (hSRK != NULL_HKEY) {
		DBGFN("SRK is already loaded.");
		return 1;
	}

	if ((result = Tspi_Context_LoadKeyByUUID(hContext, TSS_PS_TYPE_SYSTEM,
						   SRK_UUID, &hSRK))) {
		TSSerr(TPM_F_TPM_LOAD_SRK, TPM_R_REQUEST_FAILED);
		return 0;
	}

	if ((result = Tspi_GetAttribUint32(hSRK, TSS_TSPATTRIB_KEY_INFO,
					     TSS_TSPATTRIB_KEYINFO_AUTHUSAGE,
					     &authusage))) {
		Tspi_Context_CloseObject(hContext, hSRK);
		TSSerr(TPM_F_TPM_LOAD_SRK, TPM_R_REQUEST_FAILED);
		return 0;
	}

	if (!authusage) {
		DBG("SRK has no auth associated with it.");
		return 1;
	}

	/* If hSRKPolicy is non 0, then a policy object for the SRK has already
	 * been set up by engine pre/post commands. Just assign it to the SRK.
	 * Otherwise, we need to get the SRK's implicit policy and prompt for a
	 * secret */
	if (hSRKPolicy) {
		DBG("Found an already initialized SRK policy, using it");
		if ((result = Tspi_Policy_AssignToObject(hSRKPolicy, hSRK))) {
			TSSerr(TPM_F_TPM_LOAD_SRK, TPM_R_REQUEST_FAILED);
			return 0;
		}

		return 1;
	}

	if ((result = Tspi_GetPolicyObject(hSRK, TSS_POLICY_USAGE,
					&hSRKPolicy))) {
		Tspi_Context_CloseObject(hContext, hSRK);
		TSSerr(TPM_F_TPM_LOAD_SRK, TPM_R_REQUEST_FAILED);
		return 0;
	}

	if ((auth = calloc(1, 128)) == NULL) {
		TSSerr(TPM_F_TPM_LOAD_SRK, ERR_R_MALLOC_FAILURE);
		return 0;
	}

	if (!tpm_engine_get_auth(ui, (char *)auth, 128, "SRK authorization: ",
				cb_data)) {
		Tspi_Context_CloseObject(hContext, hSRK);
		free(auth);
		TSSerr(TPM_F_TPM_LOAD_SRK, TPM_R_REQUEST_FAILED);
		return 0;
	}

	/* secret_mode is a global that may be set by engine ctrl
	 * commands.  By default, its set to TSS_SECRET_MODE_PLAIN */
	if ((result = Tspi_Policy_SetSecret(hSRKPolicy, secret_mode,
					      strlen((char *)auth), auth))) {
		Tspi_Context_CloseObject(hContext, hSRK);
		free(auth);
		TSSerr(TPM_F_TPM_LOAD_SRK, TPM_R_REQUEST_FAILED);
		return 0;
	}

	free(auth);

	return 1;
}


/* Destructor (complements the "ENGINE_tpm()" constructor) */
static int tpm_engine_destroy(ENGINE * e)
{
	/* Unload the tpm error strings so any error state including our
	 * functs or reasons won't lead to a segfault (they simply get displayed
	 * without corresponding string data because none will be found). */
	ERR_unload_TPM_strings();
	return 1;
}

/* initialisation function */
static int tpm_engine_init(ENGINE * e)
{
	TSS_RESULT result;

	DBG("%s", __FUNCTION__);

#ifdef DLOPEN_TSPI
	if (tpm_dso != NULL) {
		TSSerr(TPM_F_TPM_ENGINE_INIT, TPM_R_ALREADY_LOADED);
		return 1;
	}

	if ((tpm_dso = DSO_load(NULL, TPM_LIBNAME, NULL, 0)) == NULL) {
		TSSerr(TPM_F_TPM_ENGINE_INIT, TPM_R_DSO_FAILURE);
		goto err;
	}

#define bind_tspi_func(dso, func) (p_tspi_##func = (void *)DSO_bind_func(dso, "Tspi_" #func))

	if (!bind_tspi_func(tpm_dso, Context_Create) ||
	    !bind_tspi_func(tpm_dso, Context_Close) ||
	    !bind_tspi_func(tpm_dso, Context_Connect) ||
	    !bind_tspi_func(tpm_dso, TPM_GetRandom) ||
	    !bind_tspi_func(tpm_dso, Key_CreateKey) ||
	    !bind_tspi_func(tpm_dso, Data_Bind) ||
	    !bind_tspi_func(tpm_dso, Data_Unbind) ||
	    !bind_tspi_func(tpm_dso, Context_CreateObject) ||
	    !bind_tspi_func(tpm_dso, Context_FreeMemory) ||
	    !bind_tspi_func(tpm_dso, Key_LoadKey) ||
	    !bind_tspi_func(tpm_dso, Context_LoadKeyByUUID) ||
	    !bind_tspi_func(tpm_dso, GetAttribData) ||
	    !bind_tspi_func(tpm_dso, Hash_Sign) ||
	    !bind_tspi_func(tpm_dso, Context_CloseObject) ||
	    !bind_tspi_func(tpm_dso, Hash_SetHashValue) ||
	    !bind_tspi_func(tpm_dso, SetAttribUint32) ||
	    !bind_tspi_func(tpm_dso, GetPolicyObject) ||
	    !bind_tspi_func(tpm_dso, Policy_SetSecret) ||
	    !bind_tspi_func(tpm_dso, TPM_StirRandom) ||
	    !bind_tspi_func(tpm_dso, Context_LoadKeyByBlob) ||
	    !bind_tspi_func(tpm_dso, Context_GetTpmObject) ||
	    !bind_tspi_func(tpm_dso, GetAttribUint32) ||
	    !bind_tspi_func(tpm_dso, SetAttribData) ||
	    !bind_tspi_func(tpm_dso, Policy_AssignToObject)
	    ) {
		TSSerr(TPM_F_TPM_ENGINE_INIT, TPM_R_DSO_FAILURE);
		goto err;
	}
#endif /* DLOPEN_TSPI */

	if ((result = Tspi_Context_Create(&hContext))) {
		TSSerr(TPM_F_TPM_ENGINE_INIT, TPM_R_UNIT_FAILURE);
		goto err;
	}

	/* XXX allow dest to be specified through pre commands */
	if ((result = Tspi_Context_Connect(hContext, NULL))) {
		TSSerr(TPM_F_TPM_ENGINE_INIT, TPM_R_UNIT_FAILURE);
		goto err;
	}

	if ((result = Tspi_Context_GetTpmObject(hContext, &hTPM))) {
		TSSerr(TPM_F_TPM_ENGINE_INIT, TPM_R_UNIT_FAILURE);
		goto err;
	}

	tpm_rsa.rsa_mod_exp = RSA_PKCS1_SSLeay()->rsa_mod_exp;

	return 1;
err:
	if (hContext != NULL_HCONTEXT) {
		Tspi_Context_Close(hContext);
		hContext = NULL_HCONTEXT;
		hTPM = NULL_HTPM;
	}

#ifdef DLOPEN_TSPI
	if (tpm_dso) {
		DSO_free(tpm_dso);
		tpm_dso = NULL;
	}

	p_tspi_Context_Create = NULL;
	p_tspi_Context_Close = NULL;
	p_tspi_Context_Connect = NULL;
	p_tspi_Context_FreeMemory = NULL;
	p_tspi_Context_LoadKeyByBlob = NULL;
	p_tspi_Context_LoadKeyByUUID = NULL;
	p_tspi_Context_GetTpmObject = NULL;
	p_tspi_Context_CloseObject = NULL;
	p_tspi_Key_CreateKey = NULL;
	p_tspi_Key_LoadKey = NULL;
	p_tspi_Data_Bind = NULL;
	p_tspi_Data_Unbind = NULL;
	p_tspi_Hash_SetHashValue = NULL;
	p_tspi_Hash_Sign = NULL;
	p_tspi_GetAttribData = NULL;
	p_tspi_SetAttribData = NULL;
	p_tspi_GetAttribUint32 = NULL;
	p_tspi_SetAttribUint32 = NULL;
	p_tspi_GetPolicyObject = NULL;
	p_tspi_Policy_SetSecret = NULL;
	p_tspi_Policy_AssignToObject = NULL;
	p_tspi_TPM_StirRandom = NULL;
	p_tspi_TPM_GetRandom = NULL;
#endif
	return 0;
}

static char *tpm_engine_get_auth(UI_METHOD *ui_method, char *auth, int maxlen,
				 char *input_string, void *cb_data)
{
	UI *ui;

	DBG("%s", __FUNCTION__);

	ui = UI_new();
	if (ui_method)
		UI_set_method(ui, ui_method);
	UI_add_user_data(ui, cb_data);

	if (!UI_add_input_string(ui, input_string, 0, auth, 0, maxlen)) {
		TSSerr(TPM_F_TPM_ENGINE_GET_AUTH, TPM_R_UI_METHOD_FAILED);
		UI_free(ui);
		return NULL;
	}

	if (UI_process(ui)) {
		TSSerr(TPM_F_TPM_ENGINE_GET_AUTH, TPM_R_UI_METHOD_FAILED);
		UI_free(ui);
		return NULL;
	}

	UI_free(ui);
	return auth;
}

static int tpm_engine_finish(ENGINE * e)
{
	DBG("%s", __FUNCTION__);

#ifdef DLOPEN_TSPI
	if (tpm_dso == NULL) {
		TSSerr(TPM_F_TPM_ENGINE_FINISH, TPM_R_NOT_LOADED);
		return 0;
	}
#endif
	if (hContext != NULL_HCONTEXT) {
		Tspi_Context_Close(hContext);
		hContext = NULL_HCONTEXT;
	}
#ifdef DLOPEN_TSPI
	if (!DSO_free(tpm_dso)) {
		TSSerr(TPM_F_TPM_ENGINE_FINISH, TPM_R_DSO_FAILURE);
		return 0;
	}
	tpm_dso = NULL;
#endif
	return 1;
}

int fill_out_rsa_object(RSA *rsa, TSS_HKEY hKey)
{
	TSS_RESULT result;
	UINT32 pubkey_len, encScheme, sigScheme;
	BYTE *pubkey;
	struct rsa_app_data *app_data;

	DBG("%s", __FUNCTION__);

	if ((result = Tspi_GetAttribUint32(hKey, TSS_TSPATTRIB_KEY_INFO,
					     TSS_TSPATTRIB_KEYINFO_ENCSCHEME,
					     &encScheme))) {
		TSSerr(TPM_F_TPM_FILL_RSA_OBJECT, TPM_R_REQUEST_FAILED);
		return 0;
	}

	if ((result = Tspi_GetAttribUint32(hKey, TSS_TSPATTRIB_KEY_INFO,
					     TSS_TSPATTRIB_KEYINFO_SIGSCHEME,
					     &sigScheme))) {
		TSSerr(TPM_F_TPM_FILL_RSA_OBJECT, TPM_R_REQUEST_FAILED);
		return 0;
	}

	/* pull out the public key and put it into the RSA object */
	if ((result = Tspi_GetAttribData(hKey, TSS_TSPATTRIB_RSAKEY_INFO,
					   TSS_TSPATTRIB_KEYINFO_RSA_MODULUS,
					   &pubkey_len, &pubkey))) {
		TSSerr(TPM_F_TPM_FILL_RSA_OBJECT, TPM_R_REQUEST_FAILED);
		return 0;
	}

	if ((rsa->n = BN_bin2bn(pubkey, pubkey_len, rsa->n)) == NULL) {
		Tspi_Context_FreeMemory(hContext, pubkey);
		TSSerr(TPM_F_TPM_FILL_RSA_OBJECT, TPM_R_BN_CONVERSION_FAILED);
		return 0;
	}

	Tspi_Context_FreeMemory(hContext, pubkey);

	/* set e in the RSA object */
	if (!rsa->e && ((rsa->e = BN_new()) == NULL)) {
		TSSerr(TPM_F_TPM_FILL_RSA_OBJECT, ERR_R_MALLOC_FAILURE);
		return 0;
	}

	if (!BN_set_word(rsa->e, 65537)) {
		TSSerr(TPM_F_TPM_FILL_RSA_OBJECT, TPM_R_REQUEST_FAILED);
		BN_free(rsa->e);
		rsa->e = NULL;
		return 0;
	}

	if ((app_data = OPENSSL_malloc(sizeof(struct rsa_app_data))) == NULL) {
		TSSerr(TPM_F_TPM_FILL_RSA_OBJECT, ERR_R_MALLOC_FAILURE);
		BN_free(rsa->e);
		rsa->e = NULL;
		return 0;
	}

	DBG("Setting hKey(0x%x) in RSA object", hKey);
	DBG("Setting encScheme(0x%x) in RSA object", encScheme);
	DBG("Setting sigScheme(0x%x) in RSA object", sigScheme);

	memset(app_data, 0, sizeof(struct rsa_app_data));
	app_data->hKey = hKey;
	app_data->encScheme = encScheme;
	app_data->sigScheme = sigScheme;
	RSA_set_ex_data(rsa, ex_app_data, app_data);

	return 1;
}

static EVP_PKEY *tpm_engine_load_key(ENGINE *e, const char *key_id,
				     UI_METHOD *ui, void *cb_data)
{
	ASN1_OCTET_STRING *blobstr;
	TSS_HKEY hKey;
	TSS_RESULT result;
	UINT32 authusage;
	RSA *rsa;
	EVP_PKEY *pkey;
	BIO *bf;


	DBG("%s", __FUNCTION__);

	if (!key_id) {
		TSSerr(TPM_F_TPM_ENGINE_LOAD_KEY, ERR_R_PASSED_NULL_PARAMETER);
		return NULL;
	}

	if (!tpm_load_srk(ui, cb_data)) {
		TSSerr(TPM_F_TPM_ENGINE_LOAD_KEY, TPM_R_SRK_LOAD_FAILED);
		return NULL;
	}

	if ((bf = BIO_new_file(key_id, "r")) == NULL) {
		TSSerr(TPM_F_TPM_ENGINE_LOAD_KEY,
		       TPM_R_FILE_NOT_FOUND);
		return NULL;
	}

	blobstr = PEM_ASN1_read_bio((void *)d2i_ASN1_OCTET_STRING,
				    "TSS KEY BLOB", bf, NULL, NULL, NULL);
	if (!blobstr) {
		TSSerr(TPM_F_TPM_ENGINE_LOAD_KEY,
		       TPM_R_FILE_READ_FAILED);
		BIO_free(bf);
		return NULL;
	}

	BIO_free(bf);
	DBG("Loading blob of size: %d", blobstr->length);
	if ((result = Tspi_Context_LoadKeyByBlob(hContext, hSRK,
						   blobstr->length,
						   blobstr->data, &hKey))) {
		TSSerr(TPM_F_TPM_ENGINE_LOAD_KEY,
		       TPM_R_REQUEST_FAILED);
		return NULL;
	}
	ASN1_OCTET_STRING_free(blobstr);

	if ((result = Tspi_GetAttribUint32(hKey, TSS_TSPATTRIB_KEY_INFO,
					     TSS_TSPATTRIB_KEYINFO_AUTHUSAGE,
					     &authusage))) {
		Tspi_Context_CloseObject(hContext, hKey);
		TSSerr(TPM_F_TPM_ENGINE_LOAD_KEY,
		       TPM_R_REQUEST_FAILED);
		return NULL;
	}

	if (authusage) {
		TSS_HPOLICY hPolicy;
		BYTE *auth;

		if ((auth = calloc(1, 128)) == NULL) {
			Tspi_Context_CloseObject(hContext, hKey);
			TSSerr(TPM_F_TPM_ENGINE_LOAD_KEY, ERR_R_MALLOC_FAILURE);
			return NULL;
		}

		if (!tpm_engine_get_auth(ui, (char *)auth, 128,
					 "TPM Key Password: ",
					 cb_data)) {
			Tspi_Context_CloseObject(hContext, hKey);
			free(auth);
			TSSerr(TPM_F_TPM_ENGINE_LOAD_KEY, TPM_R_REQUEST_FAILED);
			return NULL;
		}

		if ((result = Tspi_Context_CreateObject(hContext,
							 TSS_OBJECT_TYPE_POLICY,
							 TSS_POLICY_USAGE,
							 &hPolicy))) {
			Tspi_Context_CloseObject(hContext, hKey);
			free(auth);
			TSSerr(TPM_F_TPM_ENGINE_LOAD_KEY, TPM_R_REQUEST_FAILED);
			return 0;
		}

		if ((result = Tspi_Policy_AssignToObject(hPolicy, hKey))) {
			Tspi_Context_CloseObject(hContext, hKey);
			Tspi_Context_CloseObject(hContext, hPolicy);
			free(auth);
			TSSerr(TPM_F_TPM_ENGINE_LOAD_KEY, TPM_R_REQUEST_FAILED);
			return 0;
		}

		if ((result = Tspi_Policy_SetSecret(hPolicy,
						      TSS_SECRET_MODE_PLAIN,
						      strlen((char *)auth), auth))) {
			Tspi_Context_CloseObject(hContext, hKey);
			Tspi_Context_CloseObject(hContext, hPolicy);
			free(auth);
			TSSerr(TPM_F_TPM_ENGINE_LOAD_KEY, TPM_R_REQUEST_FAILED);
			return 0;
		}

		free(auth);
	}

	/* create the new objects to return */
	if ((pkey = EVP_PKEY_new()) == NULL) {
		Tspi_Context_CloseObject(hContext, hKey);
		TSSerr(TPM_F_TPM_ENGINE_LOAD_KEY, ERR_R_MALLOC_FAILURE);
		return NULL;
	}
	pkey->type = EVP_PKEY_RSA;

	if ((rsa = RSA_new()) == NULL) {
		EVP_PKEY_free(pkey);
		Tspi_Context_CloseObject(hContext, hKey);
		TSSerr(TPM_F_TPM_ENGINE_LOAD_KEY, ERR_R_MALLOC_FAILURE);
		return NULL;
	}
	rsa->meth = &tpm_rsa;
	/* call our local init function here */
	rsa->meth->init(rsa);
	pkey->pkey.rsa = rsa;

	if (!fill_out_rsa_object(rsa, hKey)) {
		EVP_PKEY_free(pkey);
		RSA_free(rsa);
		Tspi_Context_CloseObject(hContext, hKey);
		TSSerr(TPM_F_TPM_ENGINE_LOAD_KEY, TPM_R_REQUEST_FAILED);
		return NULL;
	}

	EVP_PKEY_assign_RSA(pkey, rsa);

	return pkey;
}

static int tpm_create_srk_policy(void *secret)
{
	TSS_RESULT result;
	UINT32 secret_len;

	if (secret_mode == TSS_SECRET_MODE_SHA1)
		secret_len = SHA_DIGEST_LENGTH;
	else {
		secret_len = (secret == NULL) ? 0 : strlen((char *)secret);
		DBG("Using SRK secret = %s", (BYTE *)secret);
	}

	if (hSRKPolicy == NULL_HPOLICY) {
		DBG("Creating SRK policy");
		if ((result = Tspi_Context_CreateObject(hContext,
							  TSS_OBJECT_TYPE_POLICY,
							  TSS_POLICY_USAGE,
							  &hSRKPolicy))) {
			TSSerr(TPM_F_TPM_CREATE_SRK_POLICY,
			       TPM_R_REQUEST_FAILED);
			return 0;
		}
	}

	if ((result = Tspi_Policy_SetSecret(hSRKPolicy, secret_mode,
					      secret_len, (BYTE *)secret))) {
		TSSerr(TPM_F_TPM_CREATE_SRK_POLICY, TPM_R_REQUEST_FAILED);
		return 0;
	}

	return 1;
}

static int tpm_engine_ctrl(ENGINE * e, int cmd, long i, void *p, void (*f) ())
{
	int initialised = !!hContext;
	DBG("%s", __FUNCTION__);

	switch (cmd) {
		case TPM_CMD_SO_PATH:
			if (p == NULL) {
				TSSerr(TPM_F_TPM_ENGINE_CTRL,
				       ERR_R_PASSED_NULL_PARAMETER);
				return 0;
			}
			if (initialised) {
				TSSerr(TPM_F_TPM_ENGINE_CTRL,
				       TPM_R_ALREADY_LOADED);
				return 0;
			}
			TPM_LIBNAME = (const char *) p;
			return 1;
		case TPM_CMD_SECRET_MODE:
			switch ((UINT32)i) {
				case TSS_SECRET_MODE_POPUP:
					secret_mode = (UINT32)i;
					return tpm_create_srk_policy(p);
				case TSS_SECRET_MODE_SHA1:
					/* fall through */
				case TSS_SECRET_MODE_PLAIN:
					secret_mode = (UINT32)i;
					break;
				default:
					TSSerr(TPM_F_TPM_ENGINE_CTRL,
					       TPM_R_UNKNOWN_SECRET_MODE);
					return 0;
					break;
			}
			return 1;
		case TPM_CMD_PIN:
			return tpm_create_srk_policy(p);
		default:
			break;
	}
	TSSerr(TPM_F_TPM_ENGINE_CTRL, TPM_R_CTRL_COMMAND_NOT_IMPLEMENTED);

	return 0;
}

#ifndef OPENSSL_NO_RSA
static int tpm_rsa_init(RSA *rsa)
{
	DBG("%s", __FUNCTION__);

	if (ex_app_data == TPM_ENGINE_EX_DATA_UNINIT)
		ex_app_data = RSA_get_ex_new_index(0, NULL, NULL, NULL, NULL);

	if (ex_app_data == TPM_ENGINE_EX_DATA_UNINIT) {
		TSSerr(TPM_F_TPM_RSA_INIT, TPM_R_REQUEST_FAILED);
		return 0;
	}

	return 1;
}

static int tpm_rsa_finish(RSA *rsa)
{
	struct rsa_app_data *app_data = RSA_get_ex_data(rsa, ex_app_data);

	DBG("%s", __FUNCTION__);

	if (!app_data)
		return 1;

	if (app_data->hHash) {
		Tspi_Context_CloseObject(hContext, app_data->hHash);
		app_data->hHash = NULL_HHASH;
	}

	if (app_data->hKey) {
		Tspi_Context_CloseObject(hContext, app_data->hKey);
		app_data->hKey = NULL_HKEY;
	}

	if (app_data->hEncData) {
		Tspi_Context_CloseObject(hContext, app_data->hEncData);
		app_data->hEncData = NULL_HENCDATA;
	}

	OPENSSL_free(app_data);

	return 1;
}

static int tpm_rsa_pub_dec(int flen,
			   const unsigned char *from,
			   unsigned char *to,
			   RSA *rsa,
			   int padding)
{
	int rv;

	DBG("%s", __FUNCTION__);

	if ((rv = RSA_PKCS1_SSLeay()->rsa_pub_dec(flen, from, to, rsa,
						  padding)) < 0) {
		TSSerr(TPM_F_TPM_RSA_PUB_DEC, TPM_R_REQUEST_FAILED);
		return 0;
	}

	return rv;
}

static int tpm_rsa_priv_dec(int flen,
			    const unsigned char *from,
			    unsigned char *to,
			    RSA *rsa,
			    int padding)
{
	struct rsa_app_data *app_data = RSA_get_ex_data(rsa, ex_app_data);
	TSS_RESULT result;
	UINT32 out_len, in_len;
	BYTE *out;
	int rv;

	DBG("%s", __FUNCTION__);

	if (!app_data) {
		DBG("No app data found for RSA object %p. Calling software.",
		    rsa);
		if ((rv = RSA_PKCS1_SSLeay()->rsa_priv_dec(flen, from, to, rsa,
						padding)) < 0) {
			TSSerr(TPM_F_TPM_RSA_PRIV_DEC, TPM_R_REQUEST_FAILED);
		}

		return rv;
	}

	if (app_data->hKey == NULL_HKEY) {
		TSSerr(TPM_F_TPM_RSA_PRIV_DEC, TPM_R_INVALID_KEY);
		return 0;
	}

	if (app_data->hEncData == NULL_HENCDATA) {
		if ((result = Tspi_Context_CreateObject(hContext,
							  TSS_OBJECT_TYPE_ENCDATA,
							  TSS_ENCDATA_BIND,
							  &app_data->hEncData))) {
			TSSerr(TPM_F_TPM_RSA_PRIV_DEC, TPM_R_REQUEST_FAILED);
			return 0;
		}
	}

	if (padding == RSA_PKCS1_PADDING &&
	    app_data->encScheme != TSS_ES_RSAESPKCSV15) {
		TSSerr(TPM_F_TPM_RSA_PRIV_DEC,
		       TPM_R_INVALID_PADDING_TYPE);
		DBG("encScheme(0x%x) in RSA object", app_data->encScheme);
		return 0;
	} else if (padding == RSA_PKCS1_OAEP_PADDING &&
		   app_data->encScheme != TSS_ES_RSAESOAEP_SHA1_MGF1) {
		TSSerr(TPM_F_TPM_RSA_PRIV_DEC,
		       TPM_R_INVALID_PADDING_TYPE);
		DBG("encScheme(0x%x) in RSA object", app_data->encScheme);
		return 0;
	}

	in_len = flen;
	if ((result = Tspi_SetAttribData(app_data->hEncData,
					   TSS_TSPATTRIB_ENCDATA_BLOB,
					   TSS_TSPATTRIB_ENCDATABLOB_BLOB,
					   in_len, from))) {
		TSSerr(TPM_F_TPM_RSA_PRIV_DEC, TPM_R_REQUEST_FAILED);
		return 0;
	}

	if ((result = Tspi_Data_Unbind(app_data->hEncData, app_data->hKey,
				       &out_len, &out))) {
		TSSerr(TPM_F_TPM_RSA_PRIV_DEC, TPM_R_REQUEST_FAILED);
		return 0;
	}

	DBG("%s: writing out %d bytes as a signature", __FUNCTION__, out_len);

	memcpy(to, out, out_len);
	Tspi_Context_FreeMemory(hContext, out);

	return out_len;
}

static int tpm_rsa_pub_enc(int flen,
			   const unsigned char *from,
			   unsigned char *to,
			   RSA *rsa,
			   int padding)
{
	struct rsa_app_data *app_data = RSA_get_ex_data(rsa, ex_app_data);
	TSS_RESULT result;
	UINT32 out_len, in_len;
	BYTE *out;
	int rv;

	DBG("%s", __FUNCTION__);

	if (!app_data) {
		DBG("No app data found for RSA object %p. Calling software.",
		    rsa);
		if ((rv = RSA_PKCS1_SSLeay()->rsa_pub_enc(flen, from, to, rsa,
						padding)) < 0) {
			TSSerr(TPM_F_TPM_RSA_PUB_ENC, TPM_R_REQUEST_FAILED);
		}

		return rv;
	}

	if (app_data->hKey == NULL_HKEY) {
		TSSerr(TPM_F_TPM_RSA_PUB_ENC, TPM_R_INVALID_KEY);
		return 0;
	}

	if (app_data->hEncData == NULL_HENCDATA) {
		if ((result = Tspi_Context_CreateObject(hContext,
							  TSS_OBJECT_TYPE_ENCDATA,
							  TSS_ENCDATA_BIND,
							  &app_data->hEncData))) {
			TSSerr(TPM_F_TPM_RSA_PUB_ENC, TPM_R_REQUEST_FAILED);
			return 0;
		}
		DBG("Setting hEncData(0x%x) in RSA object", app_data->hEncData);
	}

	DBG("flen is %d", flen);

	if (padding == RSA_PKCS1_PADDING) {
		if (app_data->encScheme != TSS_ES_RSAESPKCSV15) {
			TSSerr(TPM_F_TPM_RSA_PUB_ENC,
			       TPM_R_INVALID_PADDING_TYPE);
			DBG("encScheme(0x%x) in RSA object",
			    app_data->encScheme);
			return 0;
		}


		if (flen > (RSA_size(rsa) - RSA_PKCS1_PADDING_SIZE)) {
			TSSerr(TPM_F_TPM_RSA_PUB_ENC,
			       RSA_R_DATA_TOO_LARGE_FOR_KEY_SIZE);
			return 0;
		}
	} else if (padding == RSA_PKCS1_OAEP_PADDING) {
		if (app_data->encScheme != TSS_ES_RSAESOAEP_SHA1_MGF1) {
			TSSerr(TPM_F_TPM_RSA_PUB_ENC,
			       TPM_R_INVALID_PADDING_TYPE);
			DBG("encScheme(0x%x) in RSA object",
			    app_data->encScheme);
			return 0;
		}

		/* subtract an extra 5 for the TCPA_BOUND_DATA structure */
		if (flen > (RSA_size(rsa) - RSA_PKCS1_PADDING_SIZE - 5)) {
			TSSerr(TPM_F_TPM_RSA_PUB_ENC,
			       RSA_R_DATA_TOO_LARGE_FOR_KEY_SIZE);
			return 0;
		}
	} else {
		TSSerr(TPM_F_TPM_RSA_PUB_ENC, TPM_R_INVALID_ENC_SCHEME);
		return 0;
	}

	in_len = flen;
	DBG("Bind: hKey(0x%x) hEncData(0x%x) in_len(%u)", app_data->hKey,
	    app_data->hEncData, in_len);

	if ((result = Tspi_Data_Bind(app_data->hEncData, app_data->hKey,
				       in_len, from))) {
		TSSerr(TPM_F_TPM_RSA_PUB_ENC, TPM_R_REQUEST_FAILED);
		DBG("result = 0x%x (%s)", result,
		    Trspi_Error_String(result));
		return 0;
	}

	/* pull out the bound data and return it */
	if ((result = Tspi_GetAttribData(app_data->hEncData,
					   TSS_TSPATTRIB_ENCDATA_BLOB,
					   TSS_TSPATTRIB_ENCDATABLOB_BLOB,
					   &out_len, &out))) {
		TSSerr(TPM_F_TPM_RSA_KEYGEN, TPM_R_REQUEST_FAILED);
		return 0;
	}

	DBG("%s: writing out %d bytes as bound data", __FUNCTION__, out_len);

	memcpy(to, out, out_len);
	Tspi_Context_FreeMemory(hContext, out);

	return out_len;
}

static int tpm_rsa_priv_enc(int flen,
			    const unsigned char *from,
			    unsigned char *to,
			    RSA *rsa,
			    int padding)
{
	struct rsa_app_data *app_data = RSA_get_ex_data(rsa, ex_app_data);
	TSS_RESULT result;
	UINT32 sig_len;
	BYTE *sig;
	int rv;

	DBG("%s", __FUNCTION__);

	if (!app_data) {
		DBG("No app data found for RSA object %p. Calling software.",
		    rsa);
		if ((rv = RSA_PKCS1_SSLeay()->rsa_priv_enc(flen, from, to, rsa,
							   padding)) < 0) {
			TSSerr(TPM_F_TPM_RSA_PRIV_ENC, TPM_R_REQUEST_FAILED);
		}

		return rv;
	}

	if (padding != RSA_PKCS1_PADDING) {
		TSSerr(TPM_F_TPM_RSA_PRIV_ENC, TPM_R_INVALID_PADDING_TYPE);
		return 0;
	}

	if (app_data->hKey == NULL_HKEY) {
		TSSerr(TPM_F_TPM_RSA_PRIV_ENC, TPM_R_INVALID_KEY);
		return 0;
	}

	if (app_data->hHash == NULL_HHASH) {
		if ((result = Tspi_Context_CreateObject(hContext,
							  TSS_OBJECT_TYPE_HASH,
							  TSS_HASH_OTHER,
							  &app_data->hHash))) {
			TSSerr(TPM_F_TPM_RSA_PRIV_ENC, TPM_R_REQUEST_FAILED);
			return 0;
		}
	}

	if (app_data->sigScheme == TSS_SS_RSASSAPKCS1V15_SHA1) {
		if (flen != SHA_DIGEST_LENGTH) {
			TSSerr(TPM_F_TPM_RSA_PRIV_ENC, TPM_R_INVALID_MSG_SIZE);
			return 0;
		}
	} else if (app_data->sigScheme == TSS_SS_RSASSAPKCS1V15_DER) {
		if (flen > (RSA_size(rsa) - RSA_PKCS1_PADDING_SIZE)) {
			TSSerr(TPM_F_TPM_RSA_PRIV_ENC, TPM_R_INVALID_MSG_SIZE);
			return 0;
		}
	} else {
		TSSerr(TPM_F_TPM_RSA_PRIV_ENC, TPM_R_INVALID_ENC_SCHEME);
		return 0;
	}

	if ((result = Tspi_Hash_SetHashValue(app_data->hHash, flen, from))) {
		TSSerr(TPM_F_TPM_RSA_PRIV_ENC, TPM_R_REQUEST_FAILED);
		return 0;
	}

	if ((result = Tspi_Hash_Sign(app_data->hHash, app_data->hKey,
				       &sig_len, &sig))) {
		TSSerr(TPM_F_TPM_RSA_PRIV_ENC, TPM_R_REQUEST_FAILED);
		DBG("result = 0x%x (%s)", result,
		    Trspi_Error_String(result));
		return 0;
	}

	DBG("%s: writing out %d bytes as a signature", __FUNCTION__, sig_len);

	memcpy(to, sig, sig_len);
	Tspi_Context_FreeMemory(hContext, sig);

	return sig_len;
}

/* create a new key.  we need a way to specify creation of a key with OAEP
 * padding as well as PKCSv1.5, since signatures will need to be done on
 * data larger than 20 bytes, which is the max size *regardless of key size*
 * for an OAEP key signing using the TPM */
static int tpm_rsa_keygen(RSA *rsa, int bits, BIGNUM *e, BN_GENCB *cb)
{
	TSS_RESULT result;
	TSS_FLAG initFlags = TSS_KEY_TYPE_LEGACY;
	UINT32 encScheme, sigScheme;
	TSS_HKEY hKey;

	/* XXX allow this to be specified through pre commands */
	sigScheme = TSS_SS_RSASSAPKCS1V15_DER;
	encScheme = TSS_ES_RSAESPKCSV15;

	DBG("%s", __FUNCTION__);

	if (!BN_is_word(e, 65537)) {
		TSSerr(TPM_F_TPM_RSA_KEYGEN, TPM_R_INVALID_EXPONENT);
		return 0;
	}

	/* set e in the RSA object as done in the built-in openssl function */
	if (!rsa->e && ((rsa->e = BN_new()) == NULL)) {
		TSSerr(TPM_F_TPM_RSA_KEYGEN, ERR_R_MALLOC_FAILURE);
		return 0;
	}
	BN_copy(rsa->e, e);

	switch (bits) {
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
			TSSerr(TPM_F_TPM_RSA_KEYGEN, TPM_R_INVALID_KEY_SIZE);
			return 0;
	}

	/* Load the parent key (SRK) which will wrap the new key */
	if (!tpm_load_srk(NULL, NULL)) {
		TSSerr(TPM_F_TPM_RSA_KEYGEN, TPM_R_SRK_LOAD_FAILED);
		return 0;
	}

	/* Create the new key object */
	if ((result = Tspi_Context_CreateObject(hContext,
						  TSS_OBJECT_TYPE_RSAKEY,
						  initFlags, &hKey))) {
		TSSerr(TPM_F_TPM_RSA_KEYGEN, TPM_R_REQUEST_FAILED);
		return 0;
	}

	/* set the signature scheme */
	if ((result = Tspi_SetAttribUint32(hKey, TSS_TSPATTRIB_KEY_INFO,
					     TSS_TSPATTRIB_KEYINFO_SIGSCHEME,
					     sigScheme))) {
		Tspi_Context_CloseObject(hContext, hKey);
		TSSerr(TPM_F_TPM_RSA_KEYGEN, TPM_R_REQUEST_FAILED);
		return 0;
	}

	/* set the encryption scheme */
	if ((result = Tspi_SetAttribUint32(hKey, TSS_TSPATTRIB_KEY_INFO,
					     TSS_TSPATTRIB_KEYINFO_ENCSCHEME,
					     encScheme))) {
		Tspi_Context_CloseObject(hContext, hKey);
		TSSerr(TPM_F_TPM_RSA_KEYGEN, TPM_R_REQUEST_FAILED);
		return 0;
	}

	/* Call create key using the new object */
	if ((result = Tspi_Key_CreateKey(hKey, hSRK, NULL_HPCRS))) {
		Tspi_Context_CloseObject(hContext, hKey);
		TSSerr(TPM_F_TPM_RSA_KEYGEN, TPM_R_REQUEST_FAILED);
		return 0;
	}

	if (!fill_out_rsa_object(rsa, hKey)) {
		Tspi_Context_CloseObject(hContext, hKey);
		TSSerr(TPM_F_TPM_RSA_KEYGEN, TPM_R_REQUEST_FAILED);
		return 0;
	}

	/* Load the key into the chip so other functions don't need to */
	if ((result = Tspi_Key_LoadKey(hKey, hSRK))) {
		Tspi_Context_CloseObject(hContext, hKey);
		TSSerr(TPM_F_TPM_ENGINE_LOAD_KEY, TPM_R_REQUEST_FAILED);
		return 0;
	}

	return 1;
}
#endif

static int tpm_rand_bytes(unsigned char *buf, int num)
{
	TSS_RESULT result;
	BYTE *rand_data;
	UINT32 total_requested = 0;

	DBG("%s getting %d bytes", __FUNCTION__, num);

	if (num - total_requested > 4096) {
		if ((result = Tspi_TPM_GetRandom(hTPM, 4096, &rand_data))) {
			TSSerr(TPM_F_TPM_RAND_BYTES, TPM_R_REQUEST_FAILED);
			return 0;
		}

		memcpy(&buf[total_requested], rand_data, 4096);
		Tspi_Context_FreeMemory(hContext, rand_data);
		total_requested += 4096;
	}

	if ((result = Tspi_TPM_GetRandom(hTPM, num - total_requested, &rand_data))) {
		TSSerr(TPM_F_TPM_RAND_BYTES, TPM_R_REQUEST_FAILED);
		return 0;
	}

	memcpy(buf + total_requested, rand_data, num - total_requested);
	Tspi_Context_FreeMemory(hContext, rand_data);

	return 1;
}

static int tpm_rand_status(void)
{
	DBG("%s", __FUNCTION__);
	return 1;
}

static void tpm_rand_seed(const void *buf, int num)
{
	TSS_RESULT result;
	UINT32 total_stirred = 0;

	DBG("%s", __FUNCTION__);

	/* There's a hard maximum of 255 bytes allowed to be sent to the TPM on a TPM_StirRandom
	 * call.  Use all the bytes in  buf, but break them in to 255 or smaller byte chunks */
	while (num - total_stirred > 255) {
		if ((result = Tspi_TPM_StirRandom(hTPM, 255, buf + total_stirred))) {
			TSSerr(TPM_F_TPM_RAND_SEED, TPM_R_REQUEST_FAILED);
			return;
		}

		total_stirred += 255;
	}

	if ((result = Tspi_TPM_StirRandom(hTPM, num - total_stirred, buf + total_stirred))) {
		TSSerr(TPM_F_TPM_RAND_SEED, TPM_R_REQUEST_FAILED);
	}

	return;
}

/* This stuff is needed if this ENGINE is being compiled into a self-contained
 * shared-library. */
static int bind_fn(ENGINE * e, const char *id)
{
	if (id && (strcmp(id, engine_tpm_id) != 0)) {
		TSSerr(TPM_F_TPM_BIND_FN, TPM_R_ID_INVALID);
		return 0;
	}
	if (!bind_helper(e)) {
		TSSerr(TPM_F_TPM_BIND_FN, TPM_R_REQUEST_FAILED);
		return 0;
	}
	return 1;
}

IMPLEMENT_DYNAMIC_CHECK_FN()
IMPLEMENT_DYNAMIC_BIND_FN(bind_fn)
#endif
#endif
