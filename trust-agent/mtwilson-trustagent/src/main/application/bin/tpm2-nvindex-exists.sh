#!/bin/bash
# WARNING:
# *** do NOT use TABS for indentation, use SPACES
# *** TABS will cause errors in some linux distributions

if [[ $# < 1 || $# > 2 ]]; then
  echo -e "usage: \n  $0 <index>\n or\n  $0 <index> verbose"
  exit 2
fi

index=$1
verbose=$2

if [[ $verbose == "verbose" ]]; then
  echo -n "Checking NV Index ($index)"
fi

list=`tpm2_nvlist`

if [[ $? != 0 ]]; then
  echo "failed"
  exit 1
fi

result=`echo $list | grep -o $index`

if [[ -z $result ]]; then
  echo "0"
else
  echo "1"
fi

exit 0
