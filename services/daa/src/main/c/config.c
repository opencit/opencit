/*
 * config.c
 *
 * Read TPM-related, TLS-related, and Mt Wilson configuartion files
 *
 * Copyright (c) 2013 Intel Corporation
 * 
 */

#include <stdio.h>
#include <string.h>
#include <memory.h>
#include <trousers/tss.h>
#include <openssl/x509.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>
#include "config.h"
#include "hex.h"

// from create_tpm_key.c: 
#define print_error(a,b) \
        fprintf(stderr, "%s:%d %s result: 0x%x (%s)\n", __FILE__, __LINE__, \
                a, b, Trspi_Error_String(b))


// secrets larger than 20 bytes need to be hashed with sha1 ... we don't implement that yet
#define MAX_SECRET_LENGTH 20

// you must call free() on the return value when you are done with it
char* append_path(char *path, char *filename) {
  size_t newpath_size = snprintf(NULL, 0, "%s/%s", path, filename) + 1; // +1 for null terminator
  char *newpath = malloc( newpath_size * sizeof(char) );
  snprintf(newpath, newpath_size, "%s/%s", path, filename);
  return newpath;
}

// You must call free() on the return value when you are done with it
char* read_owner_secret(char *configPath) {
  char *filename = "tpm.owner.secret";
  char *path = append_path(configPath, filename);
//printf("opening file %s\n", path);
  FILE *file = fopen(path, "rb");
  if (file == NULL) {
      fprintf (stderr, "Unable to read %s\n", path);
      exit (1);
  }
  struct stat fileInfo;
  fstat(fileno(file), &fileInfo);
  if( fileInfo.st_size > MAX_SECRET_LENGTH ) {
      fprintf(stderr, "Owner secret larger than max length %d\n", MAX_SECRET_LENGTH);
      exit(1);
  }
  char *content = malloc( (fileInfo.st_size+1) * sizeof(char) );
  //int count = 0;
  //int n = 0;
  //while(!feof(file) && count < MAX_SECRET_LENGTH) {
    //n = fread(content, sizeof(char), MAX_SECRET_LENGTH, file);
    //count += n;
  //}
  size_t nread = fread(content, sizeof(char), MAX_SECRET_LENGTH, file);
  fclose(file);
  //printf("read %d bytes from %s\n", nread, path);
  free(path);
  return content;
}

// TODO: move this to an io.c file 
// You must call free() on the return value when you are done with it
struct bytearray read_binary_data(char *path) {
//printf("opening file %s\n", path);
  FILE *file = fopen(path, "rb");
  if (file == NULL) {
      fprintf (stderr, "Unable to read %s\n", path);
      exit (1);
  }
  struct stat fileInfo;
  fstat(fileno(file), &fileInfo);
  struct bytearray content;
  content.length = fileInfo.st_size;
  content.data = malloc( fileInfo.st_size * sizeof(unsigned char) );
  size_t nread = fread(content.data, sizeof(unsigned char), fileInfo.st_size, file);
  fclose(file);
  //printf("read %d bytes from %s\n", nread, path);
  return content;
}

// TODO: move this to an io.c file 
// You must call free() on the return value when you are done with it
struct chararray read_character_data(char *path) {
//printf("opening file %s\n", path);
  FILE *file = fopen(path, "rb");
  if (file == NULL) {
      fprintf (stderr, "Unable to read %s\n", path);
      exit (1);
  }
  struct stat fileInfo;
  fstat(fileno(file), &fileInfo);
  struct chararray content = create_chararray(fileInfo.st_size);
  size_t nread = fread(content.data, sizeof(char), fileInfo.st_size, file);
  fclose(file);
  printf("read_character_data: read %d bytes from %s\n", nread, path);
  return content;
}




void read_pcr_manifest(char *configPath, char *filename, TSS_HPCRS *hPcrs) {
  TSS_RESULT result;
  char *path = append_path(configPath, filename);
  FILE *file = fopen(path, "rb");
  if( file == NULL ) {
    fprintf(stderr, "Unable to read %s\n", path);
    exit(1);
  }
  struct pcr_list pcrs = read_pcr_list(file);
  print_pcr_list(pcrs);

  int i;
  for(i=0; i<pcrs.length; i++) {
    result = Tspi_PcrComposite_SetPcrValue(*hPcrs, pcrs.value[i].index, SHA1_LENGTH, (BYTE *)pcrs.value[i].hash.data); 
    if( result != TSS_SUCCESS ) {
      print_error("Tspi_PcrComposite_SetPcrValue", result);
    }
  }

  free(path);
}

