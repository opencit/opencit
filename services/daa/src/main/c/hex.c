// written by jbuhacoff

#include <stdio.h>
#include <stdlib.h>
#include <ctype.h>
#include <string.h>
#include "hex.h"

struct chararray create_chararray(int length) {
  struct chararray hash;
  hash.data = malloc( length * sizeof(char) );
  hash.length = length;
  return hash;
}

void zero_chararray(struct chararray array) {
  int i;
  for(i=0; i<array.length; i++) {
    array.data[i] = 0;
  }
}

void free_chararray(struct chararray array) {
  free( (void *)array.data );
}


void zero_bytearray(struct bytearray array) {
  int i;
  for(i=0; i<array.length; i++) {
    array.data[i] = 0;
  }
}

struct bytearray create_sha1_bytearray() {
  struct bytearray hash;
  hash.data = malloc( SHA1_LENGTH * sizeof(byte) );
  hash.length = SHA1_LENGTH;
  return hash;
}
void free_bytearray(struct bytearray array) {
  free( (void *)array.data );
}

struct pcr create_pcr(unsigned int index) {
  struct pcr hPcr;
  hPcr.index = index;
  hPcr.hash = create_sha1_bytearray();
  return hPcr;
}
void free_pcr(struct pcr hPcr) {
  free_bytearray( hPcr.hash );
}
struct pcr_list create_pcr_list(unsigned int size) {
  struct pcr_list pcrs;
  pcrs.length = size;
  pcrs.value = malloc( pcrs.length * sizeof(struct pcr) );
  unsigned int i;
  for(i=0; i<pcrs.length; i++) {
    pcrs.value[i] = create_pcr(i);
  }
  return pcrs;
}
void free_pcr_list(struct pcr_list pcrs) {
  unsigned int i;
  for(i=0; i<pcrs.length; i++) {
    free_pcr(pcrs.value[i]);
  }
}

void unsignedchararray2hex(unsigned char *source, unsigned int srclen, char *target) {
  char buf[3]; // 2 hex digits and null terminator
  unsigned int i;
  for(i=0; i<srclen; i++) {
    sprintf(buf, "%02x", source[i]);
    target[i*2] = buf[0];
    target[(i*2)+1] = buf[1];
  }
  target[i*2] = '\0';
}
// target must have be at least source.length * 2 bytes long and probably should also leave space for null terminator
void bytearray2hex(struct bytearray source, char *target) {
  unsigned int i;
  for(i=0; i<source.length; i++) {
    sprintf(&target[i*2], "%02x", source.data[i]);
  }
  target[i*2] = '\0';
}

// ignores non-hex digits;  requires even-length hex representation (leading zeros to make it line up)
void hex2bytearray(char *source, unsigned int srclen, struct bytearray target) {
  unsigned int s,t=0;
  for(s=0; s<srclen-1; s+=2) {
    if( !isxdigit(source[s]) || !isxdigit(source[s+1]) ) { printf("illegal argument: not a hex: %s\n", source); exit(1); }
    int b;
    sscanf(&source[s], "%2x", &b);
    target.data[t] = b;
    //printf("source: %c%c   target: %d\n", source[s], source[s+1], target.data[t]); // debug
    t++;
  }
  //printf("\n"); // debug
}


/*
Format of pcr list (aka manifest) file:
# optional comment
pcr-index pcr-value
pcr-index pcr-value  # optional comment
...
There can be one or more leading and trailing spaces around
each value.
Blank lines are ignored.
Lines starting with hash # are ignored.
If a hash # apppears at the end of a valid line the rest of the line is ignored.
*/
struct pcr_list read_pcr_list(FILE *in) {
  int pcrset[24] = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
  struct pcr_list pcrs = create_pcr_list(24);
  char linebuf[255];
  fgets(linebuf, 255, in); // read first line
  while( !feof(in) && !ferror(in) ) {
    if( linebuf[0] == '#' || linebuf[0] == '\n' ) { goto readnextline; }
    unsigned int pcrIndex;
    char hex[40];
    sscanf(linebuf, "%d %40s\n", &pcrIndex, hex);
    pcrset[pcrIndex] = 1;
    hex2bytearray(hex, 40, pcrs.value[pcrIndex].hash);

readnextline:
    fgets(linebuf, 255, in);
  }
  // now consolidate into just the pcrs that were read
  int count = 0; int i; for(i=0; i<24; i++) { count += pcrset[i]; }
  struct pcr_list pcrs1 = create_pcr_list(count);
  pcrs1.length = count;
  unsigned int next = 0;
  for(i=0; i<24; i++) {
    if( pcrset[i] ) {
      pcrs1.value[next].index = i;
      pcrs1.value[next].hash = pcrs.value[i].hash;
      next++;
    }
    else {
      free_pcr( pcrs.value[i] );
    }
  }
  return pcrs1;
}

void print_pcr_list(struct pcr_list pcrs) {
  printf("# PCR manifest length: %d\n", pcrs.length);
  unsigned int i;
  for(i=0; i<pcrs.length; i++) {
    char buf[255];
    bytearray2hex(pcrs.value[i].hash, buf);
    printf("%2d %s\n", pcrs.value[i].index, buf); //pcrs.value[i].hash);
  }
}

/*
EXAMPLE CODE:
#include "hex.h"
int main(int argc, char **argv) {
  FILE *in = fopen("pcr.manifest", "rb");
  struct pcr_list pcrs = read_pcr_list(in);
  print_pcr_list(pcrs);
  fclose(in);
}
*/

/*
SAMPLE DATA FILE:  pcr.manifest
[root@localhost ~]# cat pcr.manifest
   0 891eb0b556b83fcef1c10f3fa6464345e34f8f91
17     bfc3ffd7940e9281a3ebfdfa4e0412869a3f55d8
# test whole line comment
18 dc3831d76dbb34f8906c1f9e14fc99ed227bb817  # optional comment
19 064d4c4f0eecb20a1d2b512ae80d7df6af612d01

20 0000000000000000000000000000000000000000


EXAMPLE EXECUTION:
[root@localhost ~]# ./hex
# PCR manifest length: 5
 0 891eb0b556b83fcef1c10f3fa6464345e34f8f91
17 bfc3ffd7940e9281a3ebfdfa4e0412869a3f55d8
18 dc3831d76dbb34f8906c1f9e14fc99ed227bb817
19 064d4c4f0eecb20a1d2b512ae80d7df6af612d01
20 0000000000000000000000000000000000000000

*/
