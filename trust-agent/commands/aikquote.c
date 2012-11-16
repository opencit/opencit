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
#include <stdlib.h>
#include <string.h>
#include <memory.h>
#include <trousers/tss.h>
#include <tss/tspi.h>

#define CKERR	if (result != TSS_SUCCESS) goto error

static void sha1(TSS_HCONTEXT hContext, void *buf, UINT32 bufLen, BYTE *digest);

// input is 40 characters, output is 20 characters;
void convert_niarl_password(char *in_var, BYTE *blob) {
	short	temp_size = strlen(in_var);	//get the full string length
	if(temp_size % 2 == 1) {
		printf("password must be even number of characters\n");
		exit(1);
	}

	int size = temp_size / 2;	//base 16 takes 2 digits so reduce length to show real size
	//blob = new BYTE[size];	//create the byte array  // XXX caller must allocate

	UINT32	hex_value = 0;	//accumulates 2 hex characters to load into blob
	short	index, i;			//index that points at correct byte blob index (truncation intentional)

	for(i = 0; i < temp_size; i++)
	{
		switch(in_var[i])
		{
		case 'F':
		case 'f':
			hex_value += 15;
			break;
		case 'E':
		case 'e':
			hex_value += 14;
			break;
		case 'D':
		case 'd':
			hex_value += 13;
			break;
		case 'C':
		case 'c':
			hex_value += 12;
			break;
		case 'B':
		case 'b':
			hex_value += 11;
			break;
		case 'A':
		case 'a':
			hex_value += 10;
			break;
		case '9':
			hex_value += 9;
			break;
		case '8':
			hex_value += 8;
			break;
		case '7':
			hex_value += 7;
			break;
		case '6':
			hex_value += 6;
			break;
		case '5':
			hex_value += 5;
			break;
		case '4':
			hex_value += 4;
			break;
		case '3':
			hex_value += 3;
			break;
		case '2':
			hex_value += 2;
			break;
		case '1':
			hex_value += 1;
			break;
		case '0':
			hex_value += 0;
			break;
		default:
			printf("password must be hex input\n");
			exit(1);
		}

		index = i / 2;		//allow truncation so we get the right spot in the byte array
		if(i % 2 == 0)		//even characters are the first of two hex characters
			hex_value *= 16;
		else
		{
			blob[index] = hex_value;	//we now have 2 hex characters so load them into the byte array
			hex_value = 0;				//reset the accumulator
		}
	}

}

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

	result = Tspi_Context_Create(&hContext); CKERR;
	result = Tspi_Context_Connect(hContext, NULL); CKERR;
	result = Tspi_Context_LoadKeyByUUID(hContext,
			TSS_PS_TYPE_SYSTEM, SRK_UUID, &hSRK); CKERR;
	result = Tspi_GetPolicyObject (hSRK, TSS_POLICY_USAGE, &hSrkPolicy); CKERR;
	result = Tspi_Policy_SetSecret(hSrkPolicy, TSS_SECRET_MODE_PLAIN,
			sizeof(srkSecret), srkSecret); CKERR;
	result = Tspi_Context_GetTpmObject (hContext, &hTPM); CKERR;

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

	result = Tspi_Context_LoadKeyByBlob (hContext, hSRK, bufLen, buf, &hAIK); CKERR;
	free (buf);
	fprintf (stderr, "after Tspi_Context_LoadKeyByBlob \n");
	if (pass) {
		BYTE *binary_password = malloc(sizeof(BYTE)* strlen(pass)/ 2);
		convert_niarl_password(pass, binary_password);
		result = Tspi_Context_CreateObject(hContext, TSS_OBJECT_TYPE_POLICY,
				TSS_POLICY_USAGE, &hAIKPolicy); CKERR;
		result = Tspi_Policy_AssignToObject(hAIKPolicy, hAIK);
		result = Tspi_Policy_SetSecret (hAIKPolicy, TSS_SECRET_MODE_PLAIN,
				strlen(pass)/2, binary_password); CKERR;
	}

	/* Create PCR list to be quoted */
	tpmProp = TSS_TPMCAP_PROP_PCR;
	result = Tspi_TPM_GetCapability(hTPM, TSS_TPMCAP_PROPERTY,
		sizeof(tpmProp), (BYTE *)&tpmProp, &tmpbufLen, &tmpbuf); CKERR;
	npcrMax = *(UINT32 *)tmpbuf;
	Tspi_Context_FreeMemory(hContext, tmpbuf);
	npcrBytes = (npcrMax + 7) / 8;
	result = Tspi_Context_CreateObject(hContext, TSS_OBJECT_TYPE_PCRS,
		TSS_PCRS_STRUCT_INFO, &hPCRs); CKERR;

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
		result = Tspi_PcrComposite_SelectPcrIndex(hPCRs, pcr); CKERR;
		++npcrs;
		buf[2+(pcr/8)] |= 1 << (pcr%8);
	}

	/* Create TSS_VALIDATION struct for Quote */
	valid.ulExternalDataLength = sizeof(chalmd);
	valid.rgbExternalData = chalmd;

	fprintf (stderr, "before tpm quote\n");
	/* Perform Quote */
	result = Tspi_TPM_Quote(hTPM, hAIK, hPCRs, &valid);
	 CKERR;
	quoteInfo = (TPM_QUOTE_INFO *)valid.rgbData;

	fprintf (stderr, "after tpm quote\n");
	/* Fill in rest of PCR buffer */
	bp = buf + 2 + npcrBytes;
	*(UINT32 *)bp = htonl (20*npcrs);
	bp += sizeof(UINT32);
	for (i=0; i<npcrMax; i++) {
		if (buf[2+(i/8)] & (1 << (i%8))) {
			result = Tspi_PcrComposite_GetPcrValue(hPCRs,
				i, &tmpbufLen, &tmpbuf); CKERR;
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
