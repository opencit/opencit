#!/bin/bash
# WARNING:
# *** do NOT use TABS for indentation, use SPACES
# *** TABS will cause errors in some linux distributions

if [[ $# > 1 ]]; then
  echo -e "usage: \n  $0\n or\n  $0 verbose"
  exit 2
fi

ownerPasswd=abc
endorsePasswd=abc
lockPasswd=abc
verbose=$1

echo -n "Check ownership: "
# Take ownership
if [[ $verbose == verbose ]]; then
  tpm2_takeownership -o $ownerPasswd -e $endorsePasswd -l $lockPasswd
else
  tpm2_takeownership -o $ownerPasswd -e $endorsePasswd -l $lockPasswd > /dev/null
fi

if [[ $? != 0 ]];then
  echo "owned"
  exit 1
fi

#clear ownership
tpm2_takeownership -O $ownerPasswd -E $endorsePasswd -L $lockPasswd > /dev/null
echo "not owned"
