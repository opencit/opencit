/*
 * Take ownership using the well known secrets.
 * Copyright (C) 2010 The MITRE Corporation
 *
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the BSD License as published by the
 * University of California.
 */

/* For use on systems on which IBM's tpm-tools are not available. */

#if defined HAVE_CONFIG_H
#include "config.h"
#endif
#include <stdio.h>
#include <stddef.h>
#include <tss/tspi.h>
#include "tpm_quote.h"
#include <string.h>

const static char usage[] =
  "Usage: %s\n"
  "On success, takes ownership of the TPM.\n";

int main(int argc, char **argv)
{
  if (argc != 1) {
    fprintf(stderr, usage, argv[0]);
    return 1;
  }

  /* Create context */
  TSS_HCONTEXT hContext;
  int rc = Tspi_Context_Create(&hContext);
  if (rc != TSS_SUCCESS)
    return tss_err(rc, "creating context");

  rc = Tspi_Context_Connect(hContext, NULL);
  if (rc != TSS_SUCCESS)
    return tss_err(rc, "connecting");

  /* Get TPM handle */
  TSS_HTPM hTPM;
  rc = Tspi_Context_GetTpmObject(hContext, &hTPM);
  if (rc != TSS_SUCCESS)
    return tss_err(rc, "getting TPM object");

  TSS_HPOLICY hTPMPolicy;
  rc = Tspi_GetPolicyObject(hTPM, TSS_POLICY_USAGE, &hTPMPolicy);
  if (rc != TSS_SUCCESS)
    return tss_err(rc, "getting TPM policy");


  BYTE srkSecret[] = TSS_WELL_KNOWN_SECRET;
/*  rc = Tspi_Policy_SetSecret(hTPMPolicy, TSS_SECRET_MODE_SHA1,
			     sizeof srkSecret, srkSecret);
*/

  rc = Tspi_Policy_SetSecret(hTPMPolicy, TSS_SECRET_MODE_PLAIN, strlen("intel"), "intel");






  if (rc != TSS_SUCCESS)
    return tss_err(rc, "setting TPM policy secret");

  /* Make SRK */
  TSS_HKEY hSRK;
  rc = Tspi_Context_CreateObject(hContext,
				 TSS_OBJECT_TYPE_RSAKEY,
				 TSS_KEY_TSP_SRK|TSS_KEY_AUTHORIZATION,
				 &hSRK);
  if (rc != TSS_SUCCESS)
    return tss_err(rc, "making SRK");

  TSS_HPOLICY hSrkPolicy;
  rc = Tspi_GetPolicyObject(hSRK, TSS_POLICY_USAGE, &hSrkPolicy);
  if (rc != TSS_SUCCESS)
    return tss_err(rc, "getting SRK policy");

  rc = Tspi_Policy_SetSecret(hSrkPolicy, TSS_SECRET_MODE_SHA1,
			     sizeof srkSecret, srkSecret);
  if (rc != TSS_SUCCESS)
    return tss_err(rc, "setting SRK secret");

  BYTE nonce[20];		/* Value of nonce does not matter */
  TSS_VALIDATION valid;
  valid.ulExternalDataLength = sizeof nonce;
  valid.rgbExternalData = nonce;
  TSS_HKEY hEK;
  rc = Tspi_TPM_GetPubEndorsementKey(hTPM, FALSE, &valid, &hEK);
  if (rc != TSS_SUCCESS)
    return tss_err(rc, "getting endorsement key");

  rc = Tspi_TPM_TakeOwnership(hTPM, hSRK, hEK);
  if (rc != TSS_SUCCESS)
    return tss_err(rc, "taking ownership");

  return 0;
}
