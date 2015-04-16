/*
 * hex2bin - converts data in hexadecimal form to binary form
 *
 * Copyright (C) 2013 Jonathan Buhacoff <jonathan@buhacoff.net>
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
 *
 *
 */

#include <stdio.h>
#include <stdlib.h>
#include <ctype.h>
#include <string.h>

/*
 * returns 0 on success, 1 on failure
 */
int hexfile2binfile(FILE *in, FILE *out) {
    int b; // one byte buffer
	int scanerr;
	for(;;) {
		scanerr = fscanf(in, "%2x", &b);
		if( scanerr == EOF ) { break; }
		if( scanerr != 1 ) { fprintf(stderr, "non-hex input\n"); return 1; }
		fputc(b, out);
	}
	return 0;
}

/*
 * returns 0 on success, 1 on failure
 */
int hexarg2binfile(char *arg, FILE *out) {
	int i; // loop index
    int b; // one byte buffer
	int scanerr;
	for(i=0;arg+i != NULL;i+=2) {
		scanerr = sscanf(arg+i, "%2x", &b);
		if( scanerr == EOF ) { break; }
		if( scanerr != 1 ) { fprintf(stderr, "non-hex input\n"); return 1; }
		fputc(b, out);
	}
	return 0;
}

int help() {
	printf("Usage:\n");
	printf("stdin to stdout: echo 00 | hex2bin > 0.bin\n");
	printf("arg to stdout: hex2bin 00 > 0.bin\n");
	printf("stdin to file: echo 00 | hex2bin -stdin 0.bin\n");
	printf("arg to file: hex2bin 00 0.bin\n");
	printf("\n");
	printf("Input must contain only hex characters.\n");
	return -1;
}

int main(int argc, char **argv) {
    int err = 0;
    if(argc == 1) {
		return hexfile2binfile(stdin, stdout);
	}
	if(argc == 2) {
		if( strncmp(argv[1],"-h",strlen("-h")) == 0 || strncmp(argv[1],"--help",strlen("--help")) == 0 ) {
			return help();
		}
		else {
			return hexarg2binfile(argv[1], stdout);
		}
	}
	if(argc == 3) {
		// arg or stdin to outfile
		FILE* outfile = fopen(argv[2],"w");
		if( strncmp(argv[1],"-stdin",strlen("-stdin")) == 0 ) {
			err = hexfile2binfile(stdin, outfile);
		}
		else {
			err = hexarg2binfile(argv[1], outfile);
		}
		fclose(outfile);
		return err;
	}
	if(argc > 3) {
		return help();
    }
}
/* Notes:
 * Would be nice to add an option in the future to support hex files with
 * comment lines starting with #, and to ignore blank lines and newline characters.
 */
