#!/bin/bash
# WARNING:
# *** do NOT use TABS for indentation, use SPACES
# *** TABS will cause errors in some linux distributions

if [ "$#" -eq "0" ]; then
  echo "Usage: cit_tpm2_takeownership <ownerpass> [verbose]"
  exit 0
fi

ownerPasswd=$1
endorsePasswd=$1
lockPasswd=$1
tmpownerPasswd=abcd
tmpendorsePasswd=abcd
tmplockPasswd=abcd
verbose=$2
nameAlg=0x000B #SHA256
keyType=0x0001 #RSA
spkContext=/tmp/spk.context
spkHandle=0x81000000 #0x81000000~0x810000FF

echo -n "Taking ownership..."
# Take ownership
if [[ $verbose == verbose ]]; then
  tpm2_takeownership -o $ownerPasswd -e $endorsePasswd -l $lockPasswd -X
else
  tpm2_takeownership -o $ownerPasswd -e $endorsePasswd -l $lockPasswd -X > /dev/null
fi

if [[ $? != 0 ]];then
  # the above command failed, ownership is taken
  # now we check if we are the owner by changing password
  tpm2_takeownership -o $tmpownerPasswd -e $tmpendorsePasswd -l $tmplockPasswd -O $ownerPasswd -E $endorsePasswd -L $lockPasswd -X > /dev/null
  ret=$?
  if [[ "$ret" != 0 ]]; then
    # we are not the owner since we cannot change
    echo "ownership was taken with different password"
    exit 1
  else
    # we are the owner, change the passwd back from tmppasswd
    tpm2_takeownership -o $ownerPasswd -e $endorsePasswd -l $lockPasswd -O $tmpownerPasswd -E $tmpendorsePasswd -L $tmplockPasswd -X > /dev/null
    echo "ownership was taken with the same password"
    exit 4
  fi
fi

# Create storage primary key with NULL auth
rm -rf $spkContext
if [[ $verbose == verbose ]]; then
  tpm2_createprimary -A o -P $ownerPasswd -g $nameAlg -G $keyType -C $spkContext -X
else
  tpm2_createprimary -A o -P $ownerPasswd -g $nameAlg -G $keyType -C $spkContext -X > /dev/null
fi

if [[ $? != 0 ]];then
  echo "failed with create storage primary key"
  exit 2
fi

# make storage primary key persistent
if [[ $verbose == verbose ]]; then
  tpm2_evictcontrol -A o -P $ownerPasswd -c $spkContext -S $spkHandle -X
else
  tpm2_evictcontrol -A o -P $ownerPasswd -c $spkContext -S $spkHandle -X > /dev/null
fi

if [[ $? != 0 ]];then
  echo "failed with make storage primary key persistent"
  exit 3
fi

rm -rf $spkContext
echo "done"
