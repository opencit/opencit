/* Tpm2 Verify a quote issued by an AIK
 * This is modified for tpm2 based on aikqverify.c fo tpm 1.2 */
/* See aikquote2.c for format of quote data file */

/*
 * Copyright (c) 2009 Hal Finney
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

#include <stdio.h>
#include <string.h>
#include <memory.h>
#include <openssl/pem.h>
#include <openssl/x509.h>
#include <openssl/sha.h>


#ifndef BYTE
#define BYTE unsigned char
#endif

#ifndef UINT16
#define UINT16 unsigned short
#endif

#ifndef UINT32
#define UINT32 unsigned
#endif

typedef struct {
	UINT16 size;
	BYTE *name;
} TPM2B_NAME;

typedef struct {
	UINT16 size;
	BYTE *buffer;
} TPM2B_DATA;

int
main (int ac, char **av)
{
	FILE		*f_in;
	BYTE		*chal;
	UINT32		chalLen;
	BYTE		*quote;
	BYTE		*ptr;
	UINT32		index =0;
	UINT32		quoteLen;
	RSA			*aikRsa;
	UINT32		selectLen;
	BYTE		*select;
	UINT32		pcrLen;
	BYTE		*pcrs;
	BYTE		*quoted;
	BYTE		*quotedInfo;
	UINT16		quotedInfoLen;
	BYTE		*tpmtSig;
	UINT16		sigAlg;
	UINT16		hashAlg;
	BYTE		*sig;
	UINT32		sigLen;
	BYTE		*recvNonce;
	UINT32		recvNonceLen;
	UINT32		verifiedLen;
	BYTE		chalmd[20];
	BYTE		md[32]; // SHA256 hash
	BYTE		qinfo[8+20+20];
	TPM2B_NAME	*tpm2b_name = NULL;
	TPM2B_DATA	*tpm2b_data = NULL;
	char		*chalfile = NULL;
	int			pcr;
	int			pcri = 0;
	int			ind = 0;
	int			i;

	if (ac == 5 && 0 == strcmp(av[1], "-c")) {
		chalfile = av[2];
		for (i=3; i<ac; i++)
			av[i-2] = av[i];
		ac -= 2;
	}

	if (ac != 3) {
		fprintf (stderr, "Usage: %s [-c challengefile] aikrsafile quotefile\n", av[0]);
		exit (1);
	}

	/* Read challenge file */

	if (chalfile) {
		if ((f_in = fopen (chalfile, "rb")) == NULL) {
			fprintf (stderr, "Unable to open file %s\n", chalfile);
			exit (1);
		}
		fseek (f_in, 0, SEEK_END);
		chalLen = ftell (f_in);
		fseek (f_in, 0, SEEK_SET);
		chal = malloc (chalLen);
		if (fread (chal, 1, chalLen, f_in) != chalLen) {
			fprintf (stderr, "Unable to read file %s\n", chalfile);
			exit (1);
		}
		fclose (f_in);
		SHA1 (chal, chalLen, chalmd);
		//free (chal);
	} else {
		memset (chalmd, 0, sizeof(chalmd));
	}


	/* Read AIK from OpenSSL file */

	if ((f_in = fopen (av[1], "rb")) == NULL) {
		fprintf (stderr, "Unable to open file %s\n", av[1]);
		exit (1);
	}
	if ((aikRsa = PEM_read_RSA_PUBKEY(f_in, NULL, NULL, NULL)) == NULL) {
		fprintf (stderr, "Unable to read RSA file %s\n", av[1]);
		exit (1);
	}
	fclose (f_in);

	/* Read quote file */

	if ((f_in = fopen (av[2], "rb")) == NULL) {
		fprintf (stderr, "Unable to open file %s\n", av[2]);
		exit (1);
	}
	fseek (f_in, 0, SEEK_END);
	quoteLen = ftell (f_in);
	fseek (f_in, 0, SEEK_SET);
	quote = malloc (quoteLen);
	if (fread (quote, 1, quoteLen, f_in) != quoteLen) {
		fprintf (stderr, "Unable to read file %s\n", av[2]);
		exit (1);
	}
	fclose (f_in);

	/* Parse quote file
         * The quote result is constructed as follows for now
	 * pcr values (0-23), sha1 pcr bank. so the length is 20*24=480
	 */
        //printf("quoteLen: %d\n", quoteLen);

	if (quoteLen < 2)
		goto badquote;
	selectLen = 0; //aa
	select = quote;
	ptr = quote;
	index = 0;

	// pcr values for pcr 0-23 in bank of SHA1
	pcrs = quote;
	pcrLen = 480;

	index = index + pcrLen;	
	// quoted infomration structure
	quoted = quote + index; 
	//quotedInfoLen = ntohs(*(UINT16*)quoted);
	quotedInfoLen = (*(UINT16*)quoted); // This is NOT in network order
 	quotedInfo = quoted + 2; // following is the TPMS_ATTEST structure as defined in tpm2.0 spec
        //printf("quoteInfoLen: %d\n", quotedInfoLen);
	
	index = index + 2;
	//qualifiedSigner -- skip the magic header and type
	index = index + 6;
	tpm2b_name = (TPM2B_NAME*)(quote + index);
        //printf("tpm2b_name size: %02x\n", ntohs(tpm2b_name->size)); //This is in Network Order

	//tpm2b_data	
	index = index + 2 + ntohs(tpm2b_name->size); // skip tpm2b_name
	tpm2b_data = (TPM2B_DATA*)(quote + index);
	recvNonceLen = ntohs(tpm2b_data->size);
        //printf("Received Nonce Len: %02x\n", recvNonceLen); //This is in Network Order
	index = index + 2; // skip UINT16

	/* now compare the received nonce with the chal */
	recvNonce = quote + index;
	/* debug purpose
	printf("Received nonce:  ");
        for (ind=0; ind<chalLen; ind++) {
	  printf("%02x", (*(BYTE*)(recvNonce+ind)));
	}
	printf("\n");	
	printf("Challenge nonce: ");
        for (ind=0; ind<chalLen; ind++) {
	  printf("%02x", chal[ind]);
	}
	printf("\n");
	*/	
	if (memcmp(recvNonce, chal, chalLen) != 0) {
		fprintf(stderr, "Error in comparing the received nonce with the challenge");
		free(chal);
		exit(1);
	}
	if (chal != NULL) 
		free(chal);

	index = index + 2 + ntohs(tpm2b_data->size); // skip tpm2b_name
        tpmtSig = quoted + 2 + quotedInfoLen;
        //sigAlg = ntohs(*(UINT16*)tpmtSig);
        sigAlg = (*(UINT16*)tpmtSig); // This is NOT in networ order
        //printf("sigAlg: %02x\n", sigAlg);
        
	//hashAlg = ntohs(*(UINT16*)(tpmtSig+2)); 
	hashAlg = (*(UINT16*)(tpmtSig+2)); // This is NOT in network order
        //printf("hashAlg: %02x\n", hashAlg);
	
	//sigLen = ntohs(*(UINT16*)(tpmtSig+4)); 
	sigLen = (*(UINT16*)(tpmtSig+4)); // This is NOT in network order
        //printf("sigLen: %02x\n", sigLen);
        sig = tpmtSig + 6;

	/* verify the length of data */	
	verifiedLen = sig-quote+sigLen;
	if (verifiedLen != quoteLen) {
		fprintf (stderr, "Error in parsing data structure in quote\n");
		exit(1);
	}
	
	// Verify RSA signature
        // hash
        memset(md, 0, sizeof(md));
	SHA256(quotedInfo, quotedInfoLen, md);
        // signature
	if (1 != RSA_verify(NID_sha256, md, sizeof(md), sig, sigLen, aikRsa)) {
		fprintf (stderr, "Error, bad RSA signature in quote\n");
		exit (2);
	}

	/* Print out PCR values 
	for (pcr=0; pcr < 8*selectLen; pcr++) {
		if (select[pcr/8] & (1 << (pcr%8))) {
			printf ("%2d ", pcr);
			for (i=0; i<20; i++) {
				printf ("%02x", pcrs[20*pcri+i]);
			}
			printf ("\n");
			pcri++;
		}
	}
        */
        for (pcr=0; pcr < 24; pcr++) {
		printf ("%2d ", pcr);
		for (i=0; i<20; i++) {
			printf ("%02x", pcrs[20*pcri+i]);
		}
		printf ("\n");
                pcri++;
	}
         
	fflush (stdout);
	fprintf (stderr, "Success!\n");

	return 0;

badquote:
	fprintf (stderr, "Input AIK quote file incorrect format\n");
	return 1;
}
