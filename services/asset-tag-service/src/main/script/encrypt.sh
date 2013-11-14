#!/bin/sh
# encrypt.sh - encrypt and sign with hmac
# USAGE:
#       encrypt.sh --infile plain.txt --outfile encrypted.txt --enc-passwd 
# NOTE: openssl password based key derivation function needs to be configured with
#       number of iterations but the openssl command line program does not implement
#       this option so only ONE iteration is used, which is not good enough.
# SEE ALSO:
#       OpenSSL support for RFC2898 / PBKDF2  (and how the tool doesn't implement the iteration count, which is important and is a reason to use another program until openssl fixes the tool)
#       http://www.mail-archive.com/openssl-users@openssl.org/msg54143.html
#       

parse_args() {
  if ! options=$(getopt -n encrypt.sh -l infile:,outfile:,enc:,auth: -- "$@"); then exit 1; fi
  eval set -- "$options"
  while [ $# -gt 0 ]
  do
    case $1 in
      --infile) INFILE="$2"; shift;;
      --outfile) OUTFILE="$2"; shift;;
      --enc) ENC_PASSWORD="$2"; shift;;
      --auth) AUTH_PASSWORD="$2"; shift;;
    esac
    shift
  done
}

parse_args $@

echo "Content-Type: encrypted/openssl; alg=\"aes-256-ofb\"; digest-alg=\"sha256\"" > $OUTFILE
echo "Date: $(date)" >> $OUTFILE
echo >> $OUTFILE
openssl enc -aes-256-ofb -in $INFILE -pass $ENC_PASSWORD -md sha256 -base64 >> $OUTFILE
echo ----- >> $OUTFILE
echo "Content-Type: application/signature.openssl; alg=\"hmac\"; digest-alg=\"sha256\"" >> $OUTFILE
echo >> $OUTFILE
openssl dgst -sha256 -hmac $AUTH_PASSWORD -hex $OUTFILE | awk '{ print $2 }' >> $OUTFILE

