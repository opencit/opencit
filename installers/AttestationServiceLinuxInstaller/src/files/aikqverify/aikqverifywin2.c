/* Verify a quote issued by an AIK */

/*
 * Copyright (c) 2015 Intel
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

#ifndef UINT8
#define UINT8 unsigned char
#endif

#ifndef UINT16
#define UINT16 unsigned short
#endif

#ifndef UINT32
#define UINT32 unsigned
#endif

typedef struct {
        UINT16 size;
        BYTE *buffer;
} TPM2B_NAME;

typedef struct {
        UINT16 size;
        BYTE *buffer;
} TPM2B_DATA;

typedef struct {
        UINT16 size;
        BYTE *digest;
} TPM2B_DIGEST;

typedef struct {
        UINT16 hashAlg;
        UINT8 size;
        BYTE *pcrSelected;
} TPMS_PCR_SELECTION;

typedef struct {
        UINT16 signAlg;
        UINT16 hashAlg;
        UINT16 size;
        BYTE *signature;
} TPMT_SIGNATURE;


#define SHA1_SIZE 20 // 20 bytes
#define SHA256_SIZE 32 // 32 bytes
#define MAX_BANKS 3  // support up to 3 pcr banks

/* this is the header structure in the quote file -- Microsoft PCPtool uses */
typedef struct _PCP_PLATFORM_ATTESTATION_BLOB {
  UINT32 Magic;
  UINT32 Platform;
  UINT32 HeaderSize;
  UINT32 cbPcrValues;
  UINT32 cbQuote;
  UINT32 cbSignature;
  UINT32 cbLog;
} PCP_PLATFORM_ATTESTATION_BLOB, *PPCP_PLATFORM_ATTESTATION_BLOB;

int
main (int ac, char **av)
{
	FILE		*f_in;
	BYTE		*chal = NULL;
	UINT32		chalLen = 0;;
	BYTE		*quote = NULL;
	UINT32		quoteLen;
	RSA			*aikRsa;
	//UINT32		selectLen;
	//BYTE		*select;
	//UINT32		pcrLen;
	BYTE		chalmd[20];
	BYTE		md[32];
	BYTE		qinfo[8+20+20];
	char		*chalfile = NULL;
	int			pcr;
	int			pcri = 0;
	//int			ind = 0;
	int			i;
	PPCP_PLATFORM_ATTESTATION_BLOB pAttestation;
	UINT32 cursor = 0;
	BYTE *pbPcrValues = NULL;
    	UINT32 cbPcrValues = 0;
    	BYTE *pbQuote = NULL;
    	UINT32 cbQuote = 0;
    	BYTE *pbSignature = NULL;
    	UINT32 cbSignature = 0;
    	BYTE *pbLog = NULL;
    	UINT32 cbLog = 0;
    	//BYTE *pbNonce = NULL;
    	BYTE quoteDigest[20] = {0};
    	//UINT32 cbQuoteDigest = 0;
    	UINT32 tpmVersion = 0;
    	UINT32 returnCode = 0;

	UINT32 index = 0;
        BYTE            *quoted = NULL;
        BYTE            *quotedInfo = NULL;
        //UINT16          quotedInfoLen;
        //UINT16          sigAlg;
        //UINT32          hashAlg;
        //BYTE            *sig;
        BYTE            *recvNonce = NULL;
        UINT32          recvNonceLen;
        TPM2B_NAME      tpm2b_name;
        TPM2B_DATA      tpm2b_data;
        //UINT32          verifiedLen;
        //UINT32          pcrBankCount;
        TPMS_PCR_SELECTION      pcr_selection[MAX_BANKS];
        //TPM2B_DIGEST    tpm2b_digest;
        //TPMT_SIGNATURE  tpmt_signature;
        BYTE            pcrConcat[SHA256_SIZE * 24 * 3]; //allocate 3 SHA256 banks memory to accomodate possible combination
        BYTE            pcrsDigest[SHA256_SIZE];


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
  		if (chal == NULL) {
			fprintf (stderr, "Unable to allocate memory to read file %s\n", chalfile);
            fclose(f_in);
			exit (1);
		}
		if (fread (chal, 1, chalLen, f_in) != chalLen) {
			fprintf (stderr, "Unable to read file %s\n", chalfile);
            fclose(f_in);
			exit (1);
		}
		fclose (f_in);
		SHA1 (chal, chalLen, chalmd);
		free (chal);
        chal = NULL;
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
        fclose (f_in);
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
  	if (quote == NULL) {
		fprintf (stderr, "Unable to allocate memory to read file %s\n", av[2]);
        fclose(f_in);
		returnCode = 1;
		goto badquote;
	}
	if (fread (quote, 1, quoteLen, f_in) != quoteLen) {
		fprintf (stderr, "Unable to read file %s\n", av[2]);
        fclose(f_in);
		returnCode = 1;
		goto badquote;
	}
	fclose (f_in);

	/* Parse quote file */
    pAttestation = (PPCP_PLATFORM_ATTESTATION_BLOB)quote;

    // Unpack the attestation blob
    cursor = pAttestation->HeaderSize;       //to the beginning of PcrValues
    //printf("header size is: %d\n", cursor);
    tpmVersion = pAttestation->Platform;
    pbPcrValues = &quote[cursor];
    cbPcrValues = pAttestation->cbPcrValues;
    cursor += pAttestation->cbPcrValues;     //to the beginning of TPM_QUOTE_INFO2

    //printf("tpmVersion: %d\n", tpmVersion);

    if(pAttestation->cbQuote != 0)
    {
        pbQuote = &quote[cursor];
        cbQuote = pAttestation->cbQuote;
        cursor += pAttestation->cbQuote;     //to the beginning of Signature
    }
    else {
	fprintf (stderr, "Error, cbQuote is 0\n");
	returnCode = 2;
	goto badquote;
    }
    if(pAttestation->cbSignature != 0)
    {
        pbSignature = &quote[cursor];
        cbSignature = pAttestation->cbSignature;
        cursor += pAttestation->cbSignature; //to the beginning of measurement log
    }
    else {
	fprintf (stderr, "Error, cbSignature is 0\n");
	returnCode = 2;
	goto badquote;
    }
    pbLog = &quote[cursor];
    cbLog = pAttestation->cbLog;
    cursor += pAttestation->cbLog;           //to the end of buffer

    // Step 1: calculate the digest of the quote -- MSR PCP tool still uses SHA1 hash for signature
    SHA1(pbQuote, cbQuote, quoteDigest);

    // Step 2: Verify the signature with the public AIK
    if (1 != RSA_verify(NID_sha1, quoteDigest, sizeof(quoteDigest), pbSignature, cbSignature, aikRsa)) {
		fprintf (stderr, "Error, bad RSA signature in quote\n");
		returnCode = 2;
		goto badquote;
    }

    // validate nonce
    if (tpmVersion==2) {
        index = 0;
        quoted = pbQuote + index;
	// !!! the quote received from MSR pcptool does not contain the quoteInfoLen
        //quotedInfoLen = (*(UINT16*)quoted); // This is NOT in network order
        quotedInfo = quoted; // following is the TPMS_ATTEST structure as defined in tpm2.0 spec
        //printf("quoteInfoLen: %02x\n", quotedInfoLen);

        //qualifiedSigner -- skip the magic header and type -- not interested
        //index += 2;
        index += 6;

        // tpm2b_name
        tpm2b_name.size = ntohs(*(UINT16*)(quoted + index));
        index += 2;
        tpm2b_name.buffer = quoted + index;
        //printf("tpm2b_name size: %02x\n", tpm2b_name.size); //This is in Network Order

        //tpm2b_data
        index += tpm2b_name.size; // skip tpm2b_name
        tpm2b_data.size = ntohs(*(UINT16*)(quoted + index));
        recvNonceLen = tpm2b_data.size;
        //printf("Received Nonce Len: %02x\n", recvNonceLen); //This is in Network Order
        index += 2; // skip UINT16
        /* now compare the received nonce with the chal */
        tpm2b_data.buffer = quoted + index;
        recvNonce = tpm2b_data.buffer;
        index += tpm2b_data.size; // skip tpm2b_data
        // First verificaiton is to check if the received nonce matches the challenges sent

        if (memcmp(recvNonce, chalmd, chalLen) != 0) {
                fprintf(stderr, "Error in comparing the received nonce with the challenge");
		returnCode = 3;
                goto badquote;
        } 
    }

	/* Print out PCR values */
	for (pcr=0; pcr < 24; pcr++) {
		//if (select[pcr/8] & (1 << (pcr%8))) {
			printf ("%2d ", pcr);
			for (i=0; i<20; i++) {
				printf ("%02x", pbPcrValues[20*pcri+i]);
			}
			printf ("\n");
			pcri++;
		//}
	}

	fflush (stdout);
	fprintf (stderr, "Success!\n");

	returnCode = 0;

badquote:
	//fprintf (stderr, "Input AIK quote file incorrect format\n");
        
	//clean allocated memory
	if (quote != NULL) free(quote);
	//if (chal != NULL) free(chal);
	return returnCode;
}



