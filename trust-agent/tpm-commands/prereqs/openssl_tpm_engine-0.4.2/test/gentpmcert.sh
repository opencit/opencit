#!/bin/sh

OPENSSL=/usr/local/ssl/bin/openssl

if test x$1 != x; then
	KEYFILENAME=$1.key
	CERTFILENAME=$1.cert
else
	KEYFILENAME=test.key
	CERTFILENAME=test.cert
fi

if [ -e $KEYFILENAME ]; then
	rm -f $KEYFILENAME
fi

set -x

./create_tpm_key $KEYFILENAME || exit $?

$OPENSSL req -keyform engine -engine tpm -key $KEYFILENAME -new -x509 -days 365 -out $CERTFILENAME

echo $?
