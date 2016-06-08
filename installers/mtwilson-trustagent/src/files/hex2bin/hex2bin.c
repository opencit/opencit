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
 */

#include <stdio.h>
#include <stdlib.h>

int main(int argc, char **argv) {
    if(argc != 3) {
     printf("usuage is: hex2bin hexString binaryFile");
     exit(-1);
    }
    int b, i=0;
    int scanerr = 0;
        FILE* outFile = fopen(argv[2],"w");
    for(i=0;argv[1]+i != NULL;i+=2) {
        scanerr = sscanf(argv[1]+i, "%2x", &b);
        if( scanerr == EOF ) { break; }
        if( scanerr != 1 ) { fprintf(stderr, "non-hex input\n"); exit(1); }
        fputc(b, outFile);
    }
    fclose(outFile);
    exit(0);
}
