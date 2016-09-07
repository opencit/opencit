/*
 * hex2bin - converts data in hexadecimal form to binary form
 *
 * Copyright (C) 2013 Jonathan Buhacoff <jonathan@buhacoff.net>
 *
 * BSD license.
 *
 * Input must contain only hex characters. Any other characters will result in an error
 * printed to stderr and exit code 1.  On success exit code is 0.
 *
 * Would be nice to add an option in the future to support hex files with
 * comment lines starting with #, and to ignore blank lines and newline characters.
 *
 * because of how runCommand function works we now pass the hash on the command line
 * Example:
 *
 * hex2bin hexString binaryFile
 * 
 * change log:
 * 1/24: now pass hex string as argv[1] and the binary file to write to as argv[2]
 * 9/8/16: replacing sscanf banned function by hex2int()
 */

#include <stdio.h>
#include <stdlib.h>

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
int main(int argc, char **argv) {
    if(argc != 3) {
     printf("usuage is: hex2bin hexString binaryFile");
     exit(-1);
    }
    int b, i=0, j=0, hex1, hex2;

    FILE* outFile = fopen(argv[2],"w");
    
    //printf("argv %s\n", argv[1]);
	
	int iHexLen = strlen(argv[1]);
    //printf("iHexlen %d\n", iHexLen);
    if( iHexLen % 2 != 0 ) {
        fprintf(stderr, "invalid hex length\n"); exit(1);  // invalid length for hex
    }

    for(j=0; j<iHexLen-1; j=j+2) {

        hex1 = hex2int(argv[1][j]);
        hex2 = hex2int(argv[1][j+1]);
        if(hex1 == -1 || hex2 == -1) {
                fprintf(stderr, "non-hex input\n"); exit(1);
        }

        b = (hex1*16) + hex2;
		
		if(outFile != NULL)
        	fputc(b, outFile);
        
		//printf("b: %d\n", b);
     }
	 if(outFile != NULL)
     	fclose(outFile);
     exit(0);
}

