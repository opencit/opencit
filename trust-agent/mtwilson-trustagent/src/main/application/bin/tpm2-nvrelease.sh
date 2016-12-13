#!/bin/bash
# WARNING:
# *** do NOT use TABS for indentation, use SPACES
# *** TABS will cause errors in some linux distributions

if [[ $# < 2 || $# > 3 ]]; then
  echo -e "usage: \n  $0 <ownerpasswd> <index>\n or\n  $0 <ownerpasswd> <index> verbose"
  exit 2
fi

ownerPasswd=$1
index=$2
verbose=$3

if [[ $verbose == "verbose" ]]; then
  echo -n "Release NV Index ($index)"
  tpm2_nvrelease -x $index -a 0x40000001 -P $ownerPasswd -X
else
  tpm2_nvrelease -x $index -a 0x40000001 -P $ownerPasswd -X > /dev/null
fi

if [[ $? != 0 ]]; then
  echo "failed"
  exit 1
fi

if [[ $verbose == "verbose" ]]; then
  echo "done. Released @ $index"
fi
