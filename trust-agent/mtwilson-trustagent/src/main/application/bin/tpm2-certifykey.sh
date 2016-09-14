#!/bin/bash

if [[ $# != 7 ]]; then 
    echo "Usage: tpm2-certifykey.sh <parenthandle> <signinghandle> <signingpassword> <in.pub> <in.priv> <out.attest> <out.sig>"
    exit -1
fi

parenthandle=$1
signingHandle=$2
signingPass=$3
inpub=$4
inpriv=$5
outattest=$6
outsig=$7


tpm2_load -H $parenthandle -u $inpub -r $inpriv -C /tmp/object.context -n /tmp/outputfilename.tmp > /dev/null

if [[ $? != 0 ]]; then 
    echo "Failed to load key"
    exit 1
fi

tpm2_certify -k $signingHandle -K $signingPass -g 0x000B -a $outattest -s $outsig -C /tmp/object.context -X > /dev/null

exit $?