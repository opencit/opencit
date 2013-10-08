/*
 * aikquote.c
 *
 * Produce a PCR quote using an AIK.
 *
 * PCRs are specified on command line. Nonce is fixed at 0 for now.
 *
 * The format of the quote file output is as follows:
 * 2 bytes of PCR bitmask length (big-endian)
 * PCR bitmask (LSB of 1st byte is PCR0, MSB is PCR7; LSB of 2nd byte is PCR8, etc)
 * 4 bytes of PCR value length (20 times number of PCRs) (big-endian)
 * PCR values
 * 256 bytes of Quote signature
 *
 * Note that the 1st portion is the serialized TPM_PCR_SELECTION that gets hashed.
 *
 * Takes an optional challenge file to be hashed as the externalData input
 * to the Quote. This would typically be supplied by the challenger to prevent
 * replay of old Quote output. If no file is specified the challenge is zeros.
 */

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
#include <trousers/tss.h>

#define CKERR(x)	if (result != TSS_SUCCESS) { printf("Error: "); printf((x)); printf("\n"); goto error; }

static void sha1(TSS_HCONTEXT hContext, void *buf, UINT32 bufLen, BYTE *digest);

int
main (int ac, char **av)
{
	TSS_HCONTEXT	hContext;
	TSS_HTPM	hTPM;
	TSS_HKEY	hSRK;
	TSS_HKEY	hAIK;
	TSS_HPOLICY	hSrkPolicy;
	TSS_HPOLICY	hAIKPolicy;
	TSS_HPCRS	hPCRs;
	TSS_UUID	SRK_UUID = TSS_UUID_SRK;
	TSS_VALIDATION	valid;
	TPM_QUOTE_INFO	*quoteInfo;
	BYTE		srkSecret[] = TSS_WELL_KNOWN_SECRET;
	FILE		*f_in;
	FILE		*f_out;
	char		*chalfile = NULL;
	char		*pass = NULL;
	UINT32		tpmProp;
	UINT32		npcrMax;
	UINT32		npcrBytes;
	UINT32		npcrs = 0;
	BYTE		*buf;
	UINT32		bufLen;
	BYTE		*bp;
	BYTE		*tmpbuf;
	UINT32		tmpbufLen;
	BYTE		chalmd[20];
	BYTE		pcrmd[20];
	int		i;
	int		result;

	while (ac > 3) {
		if (0 == strcmp(av[1], "-p")) {
			pass = av[2];
			for (i=3; i<ac; i++)
				av[i-2] = av[i];
			ac -= 2;
		} else if (0 == strcmp(av[1], "-c")) {
			chalfile = av[2];
			for (i=3; i<ac; i++)
				av[i-2] = av[i];
			ac -= 2;
		} else
			break;
	}

	if (ac < 4) {
		fprintf (stderr, "Usage: %s [-p password] [-c challengefile] aikblobfile pcrnumber [pcrnumber] outquotefile\n", av[0]);
		exit (1);
	}

	result = Tspi_Context_Create(&hContext); CKERR("Tspi_Context_Create");
	result = Tspi_Context_Connect(hContext, NULL); CKERR("Tspi_Context_Connect");
	result = Tspi_Context_LoadKeyByUUID(hContext,
			TSS_PS_TYPE_SYSTEM, SRK_UUID, &hSRK); CKERR("Tspi_Context_LoadKeyByUUID(srk)");
	result = Tspi_GetPolicyObject (hSRK, TSS_POLICY_USAGE, &hSrkPolicy); CKERR("Tspi_GetPolicyObject(srk)");
	result = Tspi_Policy_SetSecret(hSrkPolicy, TSS_SECRET_MODE_SHA1,
			sizeof(srkSecret), srkSecret); CKERR("Tspi_Policy_SetSecret(srk)");
	result = Tspi_Context_GetTpmObject (hContext, &hTPM); CKERR("Tspi_Context_GetTpmObject");

	/* Hash challenge file if present */
	if (chalfile) {
		if ((f_in = fopen(chalfile, "rb")) == NULL) {
			fprintf (stderr, "Unable to open file %s\n", chalfile);
			exit (1);
		}
		fseek (f_in, 0, SEEK_END);
		bufLen = ftell (f_in);
		fseek (f_in, 0, SEEK_SET);
		buf = malloc (bufLen);
		if (fread(buf, 1, bufLen, f_in) != bufLen) {
			fprintf (stderr, "Unable to readn file %s\n", chalfile);
			exit (1);
		}
		fclose (f_in);
		sha1 (hContext, buf, bufLen, chalmd);
		free (buf);
	} else {
		memset (chalmd, 0, sizeof(chalmd));
	}

	/* Read AIK blob */
	if ((f_in = fopen(av[1], "rb")) == NULL) {
		fprintf (stderr, "Unable to open file %s\n", av[1]);
		exit (1);
	}
	fseek (f_in, 0, SEEK_END);
	bufLen = ftell (f_in);
	fseek (f_in, 0, SEEK_SET);
	buf = malloc (bufLen);
	if (fread(buf, 1, bufLen, f_in) != bufLen) {
		fprintf (stderr, "Unable to readn file %s\n", av[1]);
		exit (1);
	}
	fclose (f_in);

	result = Tspi_Context_LoadKeyByBlob (hContext, hSRK, bufLen, buf, &hAIK); CKERR("Tspi_Context_LoadKeyByBlob(aik)");
	free (buf);

	if (pass) {
		result = Tspi_Context_CreateObject(hContext, TSS_OBJECT_TYPE_POLICY,
				TSS_POLICY_USAGE, &hAIKPolicy); CKERR("Tspi_Context_CreateObject(aik)");
		result = Tspi_Policy_AssignToObject(hAIKPolicy, hAIK);
		result = Tspi_Policy_SetSecret (hAIKPolicy, TSS_SECRET_MODE_PLAIN,
				strlen(pass), pass); CKERR("Tspi_Policy_SetSecret(aik)"); // do not include null terminator in password -jabuhacx 20120615
	}

	/* Create PCR list to be quoted */
	tpmProp = TSS_TPMCAP_PROP_PCR;
	result = Tspi_TPM_GetCapability(hTPM, TSS_TPMCAP_PROPERTY,
		sizeof(tpmProp), (BYTE *)&tpmProp, &tmpbufLen, &tmpbuf); CKERR("Tspi_TPM_GetCapability");
	npcrMax = *(UINT32 *)tmpbuf;
	Tspi_Context_FreeMemory(hContext, tmpbuf);
	npcrBytes = (npcrMax + 7) / 8;
	result = Tspi_Context_CreateObject(hContext, TSS_OBJECT_TYPE_PCRS,
		TSS_PCRS_STRUCT_INFO, &hPCRs); CKERR("Tspi_Context_CreateObject(pcrs)");
	
	/* Also PCR buffer */
	buf = malloc (2 + npcrBytes + 4 + 20 * npcrMax);
	*(UINT16 *)buf = htons(npcrBytes);
	for (i=0; i<npcrBytes; i++)
		buf[2+i] = 0;

	for (i=2; i<ac-1; i++) {
		char *endptr;
		long pcr = strtol (av[i], &endptr, 10);
		if (pcr < 0 || pcr > npcrMax || *av[i] == 0 || *endptr != 0) {
			fprintf (stderr, "Illegal PCR value %s\n", av[i]);
			exit (1);
		}
		result = Tspi_PcrComposite_SelectPcrIndex(hPCRs, pcr); CKERR("Tspi_PcrComposite_SelectPcrIndex");
		++npcrs;
		buf[2+(pcr/8)] |= 1 << (pcr%8);
	}

	/* Create TSS_VALIDATION struct for Quote */
	valid.ulExternalDataLength = sizeof(chalmd);
	valid.rgbExternalData = chalmd;

	/* Perform Quote */
	result = Tspi_TPM_Quote(hTPM, hAIK, hPCRs, &valid); CKERR("Tspi_TPM_Quote");
	quoteInfo = (TPM_QUOTE_INFO *)valid.rgbData;

	/* Fill in rest of PCR buffer */
	bp = buf + 2 + npcrBytes;
	*(UINT32 *)bp = htonl (20*npcrs);
	bp += sizeof(UINT32);
	for (i=0; i<npcrMax; i++) {
		if (buf[2+(i/8)] & (1 << (i%8))) {
			result = Tspi_PcrComposite_GetPcrValue(hPCRs,
				i, &tmpbufLen, &tmpbuf); CKERR("Tspi_PcrComposite_GetPcrValue");
			memcpy (bp, tmpbuf, tmpbufLen);
			bp += tmpbufLen;
			Tspi_Context_FreeMemory(hContext, tmpbuf);
		}
	}
	bufLen = bp - buf;

	/* Test the hash */
	sha1 (hContext, buf, bufLen, pcrmd);
	if (memcmp (pcrmd, quoteInfo->compositeHash.digest, sizeof(pcrmd)) != 0) {
		/* Try with smaller digest length */
		*(UINT16 *)buf = htons(npcrBytes-1);
		memmove (buf+2+npcrBytes-1, buf+2+npcrBytes, bufLen-2-npcrBytes);
		bufLen -= 1;
		sha1 (hContext, buf, bufLen, pcrmd);
		if (memcmp (pcrmd, quoteInfo->compositeHash.digest, sizeof(pcrmd)) != 0) {
			fprintf (stderr, "Inconsistent PCR hash in output of quote\n");
			exit (1);
		}
	}
	Tspi_Context_FreeMemory(hContext, tmpbuf);

	/* Create output file */

	if ((f_out = fopen (av[ac-1], "wb")) == NULL) {
		fprintf (stderr, "Unable to create file %s\n", av[ac-1]);
		exit (1);
	}
	if (fwrite (buf, 1, bufLen, f_out) != bufLen) {
		fprintf (stderr, "Unable to write to file %s\n", av[ac-1]);
		exit (1);
	}
	if (fwrite (valid.rgbValidationData, 1, valid.ulValidationDataLength, f_out)
			!= valid.ulValidationDataLength) {
		fprintf (stderr, "Unable to write to file %s\n", av[ac-1]);
		exit (1);
	}
	fclose (f_out);

	printf ("Success!\n");
	return 0;

error:
	fprintf (stderr, "Failure, error code: 0x%x\n", result);
	return 1;
}

static void
sha1(TSS_HCONTEXT hContext, void *buf, UINT32 bufLen, BYTE *digest)
{
	TSS_HHASH	hHash;
	BYTE		*tmpbuf;
	UINT32		tmpbufLen;

	Tspi_Context_CreateObject(hContext, TSS_OBJECT_TYPE_HASH,
		TSS_HASH_DEFAULT, &hHash);
	Tspi_Hash_UpdateHashValue(hHash, bufLen, (BYTE *)buf);
	Tspi_Hash_GetHashValue(hHash, &tmpbufLen, &tmpbuf);
	memcpy (digest, tmpbuf, tmpbufLen);
	Tspi_Context_FreeMemory(hContext, tmpbuf);
	Tspi_Context_CloseObject(hContext, hHash);
}
