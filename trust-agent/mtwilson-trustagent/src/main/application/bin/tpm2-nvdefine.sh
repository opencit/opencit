#!/bin/bash
# WARNING:
# *** do NOT use TABS for indentation, use SPACES
# *** TABS will cause errors in some linux distributions

if [[ $# < 4 || $# > 5 ]]; then
  echo -e "usage: \n  $0 <ownerpasswd> <indexpasswd> <index> <size> <attributes>\n or\n  $0 <ownerpasswd> <indexpasswd> <index> <size> <attributes> verbose"
  exit 2
fi

ownerPasswd=$1
indexPasswd=$2
index=$3
size=$4
attributes=$5
verbose=$6

if [[ $verbose == "verbose" ]]; then
  echo -n "Define NV Index ($index)"
  tpm2_nvdefine -x $index -a 0x40000001 -P $ownerPasswd -s $size -t $attributes -I $indexPasswd -X
else
  tpm2_nvdefine -x $index -a 0x40000001 -P $ownerPasswd -s $size -t $attributes -I $indexPasswd -X > /dev/null
fi


if [[ $? != 0 ]]; then
  echo "failed"
  exit 1
fi

if [[ $verbose == "verbose" ]]; then
  echo "done. Defined @ $index"
fi
