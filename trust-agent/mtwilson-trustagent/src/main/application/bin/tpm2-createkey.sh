#!/bin/bash

if [[ $# != 4 ]]; then
    echo "Usage: tpm2-createkey.sh <bind|sign> <parenthandle> <keyauth>s <out.pub> <out.priv>"
    exit -1
fi

if [[ $1 == "bind" ]]; then
    attr="0x00020072"
elif [[ $1 == "sign" ]]; then
    attr="0x00040072"
fi


handle=$2
outpub=$3
outpriv=$4

tpm2_create -H $handle -g 0x000B -G 0x0001 -A $attr -o $outpub -O $outpriv -X > /dev/null

exit $?