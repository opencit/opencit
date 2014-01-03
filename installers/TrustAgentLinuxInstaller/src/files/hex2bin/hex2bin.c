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
 * Example:
 * echo 1111111111111111111111111111111111111111 | hex2bin > /tmp/1.bin
 */

#include <stdio.h>
#include <stdlib.h>

int main(int argc, char **argv) {
    int b;
    int scanerr = 0;
    while(1) {
        scanerr = fscanf(stdin, "%2x", &b);
        if( scanerr == EOF ) { break; }
        if( scanerr != 1 ) { fprintf(stderr, "non-hex input\n"); exit(1); }
        fputc(b, stdout);
    }
    exit(0);
}
