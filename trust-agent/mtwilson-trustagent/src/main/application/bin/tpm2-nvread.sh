#!/bin/bash
# WARNING:
# *** do NOT use TABS for indentation, use SPACES
# *** TABS will cause errors in some linux distributions

if [[ $# < 3 || $# > 4 ]]; then
  echo -e "usage: \n  $0 <authpasswd> <index> <size>\n or\n  $0 <authpasswd> <index> <size> verbose"
  exit 2
fi

authPasswd=$1
index=$2
size=$3
verbose=$4

if [[ $verbose == "verbose" ]]; then
  echo -n "Reading from NV Index ($index)"
fi

result=`tpm2_nvread -x $index -a $index -P $authPasswd -s $size -o 0 -X`

if [[ $? != 0 ]]; then
  echo "failed"
  exit 1
fi
#print the hex
echo "$result" | sed -n 3p | tr -d " \n"
echo

if [[ $verbose == "verbose" ]]; then
  echo "done. Read $size octets @ $index"
fi
