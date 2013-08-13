#!/bin/sh
openssl x509 -in $1 -noout -pubkey>$2
chmod g-rwx o-rwx openssl.sh
