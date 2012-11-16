/* Get EK certificate */

#include <stdio.h>
#include <string.h>
#include <trousers/tss.h>

#define BSIZE	128
#define CKERR	if (result != TSS_SUCCESS) goto error

/* Definitions from section 7 of
 * TCG PC Client Specific Implementation Specification
 * For Conventional BIOS
 */
#define TCG_TAG_PCCLIENT_STORED_CERT		0x1001
#define TCG_TAG_PCCLIENT_FULL_CERT		0x1002
#define TCG_TAG_PCCLIENT_PART_SMALL_CERT	0x1003
#define TCG_FULL_CERT				0
#define TCG_PARTIAL_SMALL_CERT			1

int
main (int argc, char **argv)
{
	TSS_HCONTEXT	hContext;
	TSS_HNVSTORE	hNV;
	FILE		*f_out;
	UINT32		blobLen;
	UINT32		nvIndex = TSS_NV_DEFINED|TPM_NV_INDEX_EKCert;
	UINT32		offset;
	UINT32		ekOffset;
	UINT32		ekbufLen;
	BYTE		*ekbuf;
	BYTE		*blob;
	UINT32		tag, certType;
	int		result;

	if (argc != 2) {
		printf ("Usage: %s outfilename\n", argv[0]);
		exit (1);
	}

	if ((f_out = fopen (argv[1], "wb")) == NULL) {
		printf ("Unable to open '%s' for output\n", argv[1]);
		exit (1);
	}

	result = Tspi_Context_Create(&hContext); CKERR;
	result = Tspi_Context_Connect(hContext, NULL); CKERR;
	result = Tspi_Context_CreateObject(hContext, TSS_OBJECT_TYPE_NV, 0, &hNV); CKERR;
	result = Tspi_SetAttribUint32(hNV, TSS_TSPATTRIB_NV_INDEX, 0, nvIndex); CKERR;

	/* Try reading certificate header from NV memory */
	blobLen = 5;
	result = Tspi_NV_ReadValue(hNV, 0, &blobLen, &blob);
        printf("result before rerun = %d\n",result);
	if (result != TSS_SUCCESS) {
		/* Try again with authorization */
		TSS_HPOLICY	hNVPolicy;
		result = Tspi_Context_CreateObject(hContext, TSS_OBJECT_TYPE_POLICY, TSS_POLICY_USAGE, &hNVPolicy); CKERR;
		result = Tspi_Policy_SetSecret(hNVPolicy, TSS_SECRET_MODE_POPUP, 0, NULL); CKERR;
		result = Tspi_Policy_AssignToObject(hNVPolicy, hNV); CKERR;
		blobLen = 5;
		result = Tspi_NV_ReadValue(hNV, 0, &blobLen, &blob);
                printf("result after rerun = %d\n",result);
	}
	if (result != TSS_SUCCESS) {
		printf ("Unable to read EK Certificate from TPM\n");
		goto error;
	}
	if (blobLen < 5)
		goto parseerr;
	tag = (blob[0]<<8) | blob[1];
	if (tag != TCG_TAG_PCCLIENT_STORED_CERT)
		goto parseerr;
	certType = blob[2];
	if (certType != TCG_FULL_CERT)
		goto parseerr;
	ekbufLen = (blob[3]<<8) | blob[4];
/*	result = Tspi_Context_FreeMemory (hContext, blob); CKERR; */
	offset = 5;
	blobLen = 2;
	result = Tspi_NV_ReadValue(hNV, offset, &blobLen, &blob); CKERR;
	if (blobLen < 2)
		goto parseerr;
	tag = (blob[0]<<8) | blob[1];
	if (tag == TCG_TAG_PCCLIENT_FULL_CERT) {
		offset += 2;
		ekbufLen -= 2;
	} else if (blob[0] != 0x30)	/* Marker of cert structure */
		goto parseerr;
/*	result = Tspi_Context_FreeMemory (hContext, blob); CKERR; */

	/* Read cert from chip in pieces - too large requests may fail */
	ekbuf = malloc(ekbufLen);
	ekOffset = 0;
	while (ekOffset < ekbufLen) {
		blobLen = ekbufLen-ekOffset;
		if (blobLen > BSIZE)
			blobLen = BSIZE;
		result = Tspi_NV_ReadValue(hNV, offset, &blobLen, &blob); CKERR;
		memcpy (ekbuf+ekOffset, blob, blobLen);
/*		result = Tspi_Context_FreeMemory (hContext, blob); CKERR; */
		offset += blobLen;
		ekOffset += blobLen;
	}

	fwrite (ekbuf, 1, ekbufLen, f_out);
	fclose (f_out);
	printf ("Success!\n");
	return 0;

error:
	printf ("Failure, error code: 0x%x\n", result);
	return 1;
parseerr:
	printf ("Failure, unable to parse certificate store structure\n");
	return 2;
}
