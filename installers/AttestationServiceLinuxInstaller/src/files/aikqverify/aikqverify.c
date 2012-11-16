/* Verify a quote issued by an AIK */
/* See aikquote.c for format of quote data file */

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
//#include <trousers/tss.h>
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
	UINT32		sigLen;
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
	selectLen = ntohs (*(UINT16*)quote);
	if (2 + selectLen + 4 > quoteLen)
		goto badquote;
	select = quote + 2;
	pcrLen = ntohl (*(UINT32*)(quote+2+selectLen));
	if (2 + selectLen + 4 + pcrLen + 20 > quoteLen)
		goto badquote;
	pcrs = select + selectLen + 4;
	sig = pcrs + pcrLen;
	sigLen = quote + quoteLen - sig;

	/* Create TPM_QUOTE_INFO struct */
	qinfo[0] = 1; qinfo[1] = 1; qinfo[2] = 0; qinfo[3] = 0;
	qinfo[4] = 'Q'; qinfo[5] = 'U'; qinfo[6] = 'O'; qinfo[7] = 'T';
	SHA1 (quote, 2+selectLen+4+pcrLen, qinfo+8);
	memcpy (qinfo+8+20, chalmd, 20);

	/* Verify RSA signature */
	SHA1 (qinfo, sizeof(qinfo), md);
	if (1 != RSA_verify(NID_sha1, md, sizeof(md), sig, sigLen, aikRsa)) {
		fprintf (stderr, "Error, bad RSA signature in quote\n");
		exit (2);
	}

	/* Print out PCR values */

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
	fflush (stdout);
	fprintf (stderr, "Success!\n");

	return 0;

badquote:
	fprintf (stderr, "Input AIK quote file incorrect format\n");
	return 1;
}
