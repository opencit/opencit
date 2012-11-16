/* Get EK certificate */

#include <stdio.h>
#include <trousers/tss.h>
#include <memory.h>

#define BSIZE   128
#define CKERR   if (result != TSS_SUCCESS) goto error

int
main ()
{
        TSS_HCONTEXT    hContext;
        TSS_HNVSTORE    hNV;
        UINT32          offset;
        UINT32          ekOffset;
        UINT32          nvIndex = TSS_NV_DEFINED|TPM_NV_INDEX_EKCert;
        UINT32          blobLen;
        BYTE            *blob;
        UINT32          ekbufLen;
        BYTE            *ekbuf;
        unsigned        i;
        int             result;

        result = Tspi_Context_Create(&hContext); CKERR;
        result = Tspi_Context_Connect(hContext, NULL); CKERR;
        result = Tspi_Context_CreateObject(hContext,
TSS_OBJECT_TYPE_NV, 0, &hNV); CKERR;
        result = Tspi_SetAttribUint32(hNV, TSS_TSPATTRIB_NV_INDEX, 0,
nvIndex); CKERR;

        /* Try reading cert a piece at a time */
        blobLen = BSIZE;
        result = Tspi_NV_ReadValue(hNV, 0, &blobLen, &blob); CKERR;

        /* Search for pattern at start of cert */
        for (i=0; i<blobLen-5; i++) {
                if (blob[i]==0x30 && blob[i+1]==0x82
                                && blob[i+4]==0x30 && blob[i+5]==0x82)
                        break;
        }
        if (i == blobLen-5) {
                fprintf (stderr, "No certificate header found\n");
                goto error;
        }
        ekbufLen = (blob[i+2] << 8) + blob[i+3] + 4;
        ekbuf = malloc(ekbufLen);
        memcpy (ekbuf, blob+i, blobLen-i);
//      result = Tspi_Context_FreeMemory (hContext, blob); CKERR;
        offset = blobLen;
        ekOffset = blobLen-i;

        while (ekOffset < ekbufLen) {
                blobLen = ekbufLen-ekOffset;
                if (blobLen > BSIZE)
                        blobLen = BSIZE;
                result = Tspi_NV_ReadValue(hNV, offset, &blobLen, &blob); CKERR;
                memcpy (ekbuf+ekOffset, blob, blobLen);
//              result = Tspi_Context_FreeMemory (hContext, blob); CKERR;
                offset += blobLen;
                ekOffset += blobLen;
                if (ekOffset == ekbufLen)
                        break;
        }

        for (i=0; i<ekbufLen; i++)
                printf ("%02x%s", ekbuf[i], (i+1)%16?" ":"\n");

        printf ("\nSuccess!\n");
        return 0;

error:
        printf ("Failure, error code: 0x%x\n", result);
        return 1;
}
 
