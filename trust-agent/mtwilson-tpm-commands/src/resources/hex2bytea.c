// NOTE  the hex2bytea function is also added to tpm_utils.h / tpm_utils.c  in tpm-tools-1.3.8-patched   ( i added )

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
//#include <unistd.h>
// the BYTE type is defined in tss.h
#include <trousers/tss.h>
#include <trousers/trousers.h>
#include <ctype.h>

//#include "tpm_tspi.h"
//#include "tpm_utils.h"


int hex2int(const char c);
/*
/*
 * Input: hexadecimal character in the range 0..9 or A..F case-insensitive
 * Output: decimal value of input in the range 0..15 
 *         or -1 if the input was not a valid hexadecimal character
 */
int hex2int(const char c)
{
    if(c >= '0' && c<= '9') {
  	return c - '0';
    }
    else if( c >= 'A' && c <= 'F' ) {
        return c - 'A' + 10;
    }
    else if( c >= 'a' && c <= 'f' ) {
        return c - 'a' + 10;
    }
    else {
        return -1;
    }
}
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
        int hex1;
        int hex2;
        int pDecoded_index=0;
        for(i=0; i<iHexLen-1; i=i+2) {

                hex1 = hex2int(a_pszHex[i]);
                hex2 = hex2int(a_pszHex[i+1]);

                if(hex1 == -1 || hex2 == -1) {
                        free(pDecoded);
                        *a_pDecoded = NULL;
                        *a_iDecodedLen = 0;
                        return 2; // invalid hex digit
                }

                iByte = (hex1*16) + hex2;

                pDecoded[pDecoded_index++] = iByte & 0xFF;
        }
        *a_pDecoded = pDecoded;
        *a_iDecodedLen = iDecodedLen;
        return 0;
}
