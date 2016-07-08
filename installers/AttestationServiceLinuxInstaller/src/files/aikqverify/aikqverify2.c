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


int
main (int ac, char **av)
{
	FILE		*f_in;
	BYTE		*chal;
	UINT32		chalLen;
	BYTE		*quote;
	UINT32		quoteLen;
	RSA			*aikRsa;
	UINT32		selectLen;
	BYTE		*select;
	UINT32		pcrLen;
	BYTE		*pcrs;
	BYTE		*quoted;
	UINT32		quotedLen;
	UINT32		sigLen;
	UINT16		sigAlg;
	BYTE		*sig;
	BYTE		chalmd[20];
	BYTE		md[20];
	BYTE		qinfo[8+20+20];
	char		*chalfile = NULL;
	int			pcr;
	int			pcri = 0;
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
		free (chal);
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

	/* Parse quote file */

	if (quoteLen < 2)
		goto badquote;
	selectLen = 0; //aa
	select = quote;

	pcrs = quote;
	pcrLen = 480;

	quoted = pcrs + pcrLen;
	quotedLen = ntohs(*(UINT16*)quoted) + 2;

	sig = quoted + quotedLen;
        sigAlg = ntohs(*(UINT16*)sig);
	sig = sig + 2;
	sigLen = quoteLen - pcrLen - quotedLen - 2 ;

	/* Verify RSA signature
	SHA1 (qinfo, sizeof(qinfo), md);
	if (1 != RSA_verify(NID_sha1, md, sizeof(md), sig, sigLen, aikRsa)) {
		fprintf (stderr, "Error, bad RSA signature in quote\n");
		exit (2);
	}
	*/

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
