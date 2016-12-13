#!/bin/bash
# WARNING:
# *** do NOT use TABS for indentation, use SPACES
# *** TABS will cause errors in some linux distributions

echo -n "TPM device presence: "
if [ -c "/dev/tpm0" ]; then echo "yes"; exit 0; else echo "no"; exit 1; fi
