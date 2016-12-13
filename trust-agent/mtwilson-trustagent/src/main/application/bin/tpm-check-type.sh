#!/bin/bash
# WARNING:
# *** do NOT use TABS for indentation, use SPACES
# *** TABS will cause errors in some linux distributions

# required pre-condition: /dev/tpm0 exists
echo -n "TPM device type: "
if [[ -f "/sys/class/misc/tpm0/device/caps" || -f "/sys/class/tpm/tpm0/device/caps" ]]; then
  echo "1.2"
else
#  if [[ -f "/sys/class/tpm/tpm0/device/description" && `cat /sys/class/tpm/tpm0/device/description` == "TPM 2.0 Device" ]]; then
    echo "2.0"
#  else
#    echo "n/a"
#    exit -1
#  fi
fi
