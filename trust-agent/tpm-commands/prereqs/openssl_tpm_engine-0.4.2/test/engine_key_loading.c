
/*
 * TPM engine key loading tests.
 *
 * Kent Yoder <kyoder@users.sf.net>
 *
 * Usage:
 *   ../create_tpm_key key_file
 *   ./engine_key_loading key_file
 *
 * Note that the "post_test_popup", which will test setting the SRK password
 * by setting its secret policy to type "popup", will fail against a 1.1 TSS
 * and succeed (if you click 'OK') against a 1.2 TSS.  This is because in a
 * 1.1 TSS, the default was to include the trailing zero byte(s) in a password
 * typed into a popup dialog.  In TSS 1.2, the default is to not include any
 * trailing zero bytes in the password, which means clicking 'OK' gives a NULL
 * password (zero-length), which matches all other passwords in this test.
 *
 */

#include <stdio.h>
#include <string.h>

#include <openssl/engine.h>
#include <openssl/rsa.h>
#include <openssl/rand.h>

#include <tss/platform.h>
#include <tss/tss_defines.h>
#include <tss/tss_typedef.h>
#include <tss/tss_structs.h>
#include <tss/tss_error.h>
#include <tss/tspi.h>


#define ERR(x, ...)	fprintf(stderr, "%s:%d " x "\n", __FILE__, __LINE__, ##__VA_ARGS__)

char null_sha1_hash[] = { 0xda, 0x39, 0xa3, 0xee, 0x5e, 0x6b, 0x4b, 0x0d, 0x32, 0x55,
			  0xbf, 0xef, 0x95, 0x60, 0x18, 0x90, 0xaf, 0xd8, 0x07, 0x09 };

/* The tests assume that the SRK secret is a hash of 0 bytes */

struct eng_cmd
{
	char *name;
	long long_arg;
	void *void_arg;
};

/* Test setting the SRK plain password explicitly (there should be no prompt) */
struct eng_cmd post_test_pin_only = { "PIN", 0, NULL };
/* Test using a popup secret */
struct eng_cmd post_test_popup = { "SECRET_MODE", (long)TSS_SECRET_MODE_POPUP, NULL };
/* Test setting the mode to plain, then a NULL secret */
struct eng_cmd post_test_plain[] = { { "SECRET_MODE", (long)TSS_SECRET_MODE_PLAIN, NULL },
				      { "PIN", 0, NULL } };
/* Test passing in a SHA1 hashed secret */
struct eng_cmd post_test_sha1[] = { { "SECRET_MODE", (long)TSS_SECRET_MODE_SHA1, NULL },
				     { "PIN", 0, null_sha1_hash } };

struct eng_cmd *test_cmds[] = { &post_test_pin_only, post_test_plain, post_test_sha1,
				&post_test_popup };
int test_num[] = { 1, 1, 2, 2 };

#define DATA_SIZE	33
#define KEY_SIZE_BITS	512
#define ENTROPY_SIZE    4097

#define RAND_DEVICE	"/dev/urandom"

int
run_test(EVP_PKEY *key)
{
	RSA *rsa;
	char signature[256], data_to_sign[DATA_SIZE], data_recovered[DATA_SIZE];
	int sig_size;

	if (RAND_bytes(data_to_sign, sizeof(data_to_sign)) != 1) {
		ERR_print_errors_fp(stderr);
		return 1;
	}

	if (key)
		rsa = key->pkey.rsa;
	else
		rsa = RSA_generate_key(KEY_SIZE_BITS, 65537, NULL, NULL);

	if (!rsa)
		return 1;

	if ((sig_size = RSA_public_encrypt(sizeof(data_to_sign), data_to_sign,
					   signature, rsa, RSA_PKCS1_PADDING)) == -1) {
		ERR_print_errors_fp(stderr);
		if (!key)
			RSA_free(rsa);
		return 1;
	}

	if ((sig_size = RSA_private_decrypt(sig_size, signature, data_recovered,
					    rsa, RSA_PKCS1_PADDING)) != DATA_SIZE) {
		ERR_print_errors_fp(stderr);
		if (!key)
			RSA_free(rsa);
		return 1;
	}

	if (memcmp(data_recovered, data_to_sign, DATA_SIZE)) {
		ERR("recovered data doesn't match!");
		if (!key)
			RSA_free(rsa);
		return 1;
	}

	if ((sig_size = RSA_private_encrypt(sizeof(data_to_sign), data_to_sign,
					    signature, rsa, RSA_PKCS1_PADDING)) == -1) {
		ERR_print_errors_fp(stderr);
		if (!key)
			RSA_free(rsa);
		return 1;
	}

	if ((sig_size = RSA_public_decrypt(sig_size, signature, data_recovered,
					    rsa, RSA_PKCS1_PADDING)) != DATA_SIZE) {
		ERR_print_errors_fp(stderr);
		if (!key)
			RSA_free(rsa);
		return 1;
	}

	if (memcmp(data_recovered, data_to_sign, DATA_SIZE)) {
		ERR("recovered data doesn't match!");
		if (!key)
			RSA_free(rsa);
		return 1;
	}

	if (!key)
		RSA_free(rsa);

	return 0;
}

int
main(int argc, char **argv)
{
	struct eng_cmd *post_cmds;
	int post_num, failure = 0, i;
	ENGINE *e;
	EVP_PKEY *key;
	FILE *f;
	char entropy[ENTROPY_SIZE];
	int entropy_len = ENTROPY_SIZE;
        const char *engine_id = "tpm";

	if (!argv[1]) {
		fprintf(stderr, "usage: %s: <tpm key file>\n", argv[0]);
		return -1;
	}

        ENGINE_load_builtin_engines();

	e = ENGINE_by_id(engine_id);
	if (!e) {
		/* the engine isn't available */
		ERR_print_errors_fp(stderr);
		ERR("ENGINE_by_id failed.");
		return 1;
	}

	if (!ENGINE_init(e)) {
		/* the engine couldn't initialise, release 'e' */
		ERR_print_errors_fp(stderr);
		ERR("ENGINE_init failed.");
		ENGINE_free(e);
		ENGINE_finish(e);
		return 2;
	}
	if (!ENGINE_set_default_RSA(e) || !ENGINE_set_default_RAND(e)) {
		/* This should only happen when 'e' can't initialise, but the previous
		 * statement suggests it did. */
		ERR_print_errors_fp(stderr);
		ERR("ENGINE_init failed.");
		ENGINE_free(e);
		ENGINE_finish(e);
		return 3;
	}

	/* ENGINE_init() returned a functional reference, so free the */
	/* structural reference with ENGINE_free */
	ENGINE_free(e);

	/* Test 1
	 *
	 *  Load a TPM key from file using the engine load command.
	 *
	 */
	if ((key = ENGINE_load_private_key(e, argv[1], NULL, NULL)) == NULL) {
		ERR_print_errors_fp(stderr);
		ERR("Couldn't load TPM key \"%s\" from file.", argv[1]);
		return 4;
	}

	/*
	 * Test 2
	 *
	 * Do a test run on the loaded TPM key.
	 *
	 */
	printf("%s: Testing loaded TPM key \"%s\"\n", argv[0], argv[1]);
	failure = run_test(key);
	printf("%s: Done.\n", argv[0]);

	/*
	 * Test 3
	 *
	 * Call stir random through the RAND interface. The only "test" being done
	 * here is that the engine doesn't segfault, since there is no return value.
	 *
	 */
	if ((f = fopen(RAND_DEVICE, "r")) == NULL) {
		ERR("Error opering rand device %s to get entropy string", RAND_DEVICE);
		return 5;
	}

	if (fread(entropy, entropy_len, 1, f) != 1) {
		ERR("Error reading from rand device %s to get entropy string", RAND_DEVICE);
		fclose(f);
		return 6;
	}
	fclose(f);

	RAND_seed(entropy, entropy_len);

	/*
	 * Test 4
	 *
	 * Test auth data passthrough to the engine
	 *
	 */
	for (i = 0; i < 4 && !failure; i++) {
		post_cmds = test_cmds[i];
		post_num = test_num[i];
		/* Process post-initialize commands */
		while (post_num--) {
			if (!ENGINE_ctrl_cmd(e, post_cmds->name, post_cmds->long_arg,
					     post_cmds->void_arg, NULL, 0)) {
				ERR_print_errors_fp(stderr);
				ERR("Post command %d failed", i);
				failure = 1;
				ENGINE_finish(e);
				return 7;
			}
			post_cmds++;
		}

		printf("%s: Test %d with generated TPM key\n", argv[0], i);
		failure = run_test(NULL);
		printf("%s: Done.\n", argv[0]);
	}

	/* Release the functional reference from ENGINE_init() */
	ENGINE_finish(e);
	e = NULL;

	return failure ? 8 : 0;
}
