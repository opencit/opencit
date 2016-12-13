#!/bin/bash
# WARNING:
# *** do NOT use TABS for indentation, use SPACES
# *** TABS will cause errors in some linux distributions

if [[ $# < 3 || $# > 4 ]]; then
  echo -e "usage: \n  $0 <authpasswd> <index> <datafile>\n or\n  $0 <authpasswd> <index> <datafile> verbose"
  exit 2
fi

authPasswd=$1
index=$2
dataFile=$3
verbose=$4

if [[ $verbose == "verbose" ]]; then
  echo -n "Write to NV Index ($index)"
  tpm2_nvwrite -x $index -a 0x40000001 -P $authPasswd -f $dataFile -X
else
  tpm2_nvwrite -x $index -a 0x40000001 -P $authPasswd -f $dataFile -X > /dev/null
fi

if [[ $? != 0 ]]; then
  echo "failed"
  exit 1
fi

if [[ $verbose == "verbose" ]]; then
  echo "done. Wrote @ $index"
fi
