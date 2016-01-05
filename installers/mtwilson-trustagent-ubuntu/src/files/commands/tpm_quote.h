/*
 * Declares the function prototypes exported by libtpm_quote.a.
 * Copyright (C) 2010 The MITRE Corporation
 *
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the BSD License as published by the
 * University of California.
 */

#if !defined _TPM_QUOTE_H
#define  _TPM_QUOTE_H

const char *tss_result(TSS_RESULT result);
int tss_err(TSS_RESULT rc, const char *msg);
int tidy(TSS_HCONTEXT hContext, int code);
int pcr_mask(UINT32 *pcrs, UINT32 npcrs, char **mask);
int loadkey(TSS_HCONTEXT hContext,
	    BYTE *blob, UINT32 blobLen,
	    TSS_UUID uuid);
int quote(TSS_HCONTEXT hContext, TSS_UUID uuid,
	  UINT32 *pcrs, UINT32 npcrs,
	  TSS_VALIDATION *valid);
TPM_NONCE *quote_nonce(BYTE *info);
char *toutf16le(char *src);
size_t utf16lelen(const char *src);

#endif /* _TPM_QUOTE_H */
