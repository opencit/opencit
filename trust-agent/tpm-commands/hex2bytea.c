// NOTE  the hex2bytea function is also added to tpm_utils.h / tpm_utils.c  in tpm-tools-1.3.8-patched   ( i added )

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
//#include <unistd.h>
// the BYTE type is defined in tss.h
#include <trousers/tss.h>
//#include <trousers/trousers.h>

//#include "tpm_tspi.h"
//#include "tpm_utils.h"


/*
 * You must free the memory allocated to a_pDecoded when you are done with it.
 * Returns 0 for success, non-zero for failure.
 */
int hex2bytea( const char *a_pszHex, BYTE **a_pDecoded, int *a_iDecodedLen ) {
        BYTE *pDecoded;
        int iDecodedLen, iByte, i;
        int iHexLen = strlen(a_pszHex);
        if( iHexLen % 2 != 0 ) {
                *a_pDecoded = NULL;
                *a_iDecodedLen = 0;
                return 1; // invalid length for hex
        }
        iDecodedLen = iHexLen / 2;
        pDecoded = malloc( sizeof(char) * iDecodedLen );
        for(i=0; i<iHexLen/2; i++) {
                if( sscanf(a_pszHex+(i*2), "%2x", &iByte) != 1 ) {
                        free(pDecoded);
                        *a_pDecoded = NULL;
                        *a_iDecodedLen = 0;
                        return 2; // invalid hex digit
                }
                pDecoded[i] = iByte & 0xFF;
        }
        *a_pDecoded = pDecoded;
        *a_iDecodedLen = iDecodedLen;
        return 0;
}
