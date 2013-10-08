// written by jbuhacoff

#ifndef __HEX_H__
#define __HEX_H__


#define SHA1_LENGTH 20

typedef unsigned char byte;

struct chararray {
  char *data;
  unsigned int length;
};


struct bytearray {
  byte *data;
  unsigned int length;
};

struct pcr {
  unsigned int index;
  struct bytearray hash;
};

struct pcr_list {
  struct pcr *value;
  unsigned int length;
};

struct chararray create_chararray(int length);
void zero_chararray(struct chararray array);
void free_chararray(struct chararray array);
void zero_bytearray(struct bytearray array);
struct bytearray create_sha1_bytearray();
void free_bytearray(struct bytearray array);
struct pcr create_pcr(unsigned int index);
void free_pcr(struct pcr hPcr);
struct pcr_list create_pcr_list(unsigned int size);
void free_pcr_list(struct pcr_list pcrs);
void unsignedchararray2hex(unsigned char *source, unsigned int srclen, char *target);
void bytearray2hex(struct bytearray source, char *target);
void hex2bytearray(char *source, unsigned int srclen, struct bytearray target);
struct pcr_list read_pcr_list(FILE *in);
void print_pcr_list(struct pcr_list pcrs);

#endif

