#ifndef __CONFIG_H_
#define __CONFIG_H_

char* read_owner_secret(char *configPath);
void read_pcr_manifest(char *configPath, char *filename, TSS_HPCRS *hPcrs);
struct bytearray read_binary_data(char *path);
struct chararray read_character_data(char *path);

#endif

