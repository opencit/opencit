#!/bin/sh
# encrypt.sh - encrypt and sign with hmac
# USAGE:
#       encrypt.sh --in plain.txt --out encrypted.txt --enc encpassword --auth authpassword
#       encrypt.sh -in plain.txt -out encrypted.txt -enc encpassword -auth authpassword
#       encrypt.sh -i plain.txt -o encrypted.txt -e encpassword -a authpassword
# NOTE: Password based key derivation function needs to be configured with
#       number of iterations but the openssl-1.0.1f enc program does not implement
#       this option so only ONE iteration is used, which is not good enough.
#       This script can use a patched version of openssl-1.0.1f which does provide
#       the necessary options. It's enabled by default with USE_PBKDF2=true
#       Disable with USE_PBKDF2=false.
#       Tune the number of iterations by running "openssl speed sha256"
# Example output:
#Content-Type: encrypted/openssl; alg="aes-256-ofb"; digest-alg="sha256"; key-gen="pbkdf2"; iter="2052228"
#Date: Tue Jan 21 01:16:01 PST 2014
#
#U2FsdGVkX1+LU5kNud8jedWmWagnq8WR0xf1TVFkBmkLm+wb
#-----
#Content-Type: application/signature.openssl; alg="hmac"; digest-alg="sha256"
#
#8586ab1b22c774b32d70de87c3f48385efc3e0588a4b5c3ca9fa8f179da3b59b
# <<<< end of example does not include this line >>>>
# SEE ALSO:
#       OpenSSL support for RFC2898 / PBKDF2  (and how the tool doesn't implement the iteration count, which is important and is a reason to use another program until openssl fixes the tool)
#       http://www.mail-archive.com/openssl-users@openssl.org/msg54143.html
#

USE_PBKDF2=true
PBKDF2_ITER=2052228
PBKDF2_OPTS=
PBKDF2_RFC822=

parse_args() {
  if ! options=$(getopt -a -n encrypt.sh -l in:,out:,enc:,auth: -o i:,o:,e:,a: -- "$@"); then exit 1; fi
  eval set -- "$options"
  while [ $# -gt 0 ]
  do
    case $1 in
      -i|--in) INFILE="$2"; shift;;
      -o|--out) OUTFILE="$2"; shift;;
      -e|--enc) ENC_PASSWORD="$2"; shift;;
      -a|--auth) AUTH_PASSWORD="$2"; shift;;
    esac
    shift
  done
}

parse_args $@

export ENC_PASSWORD

if [[ "$USE_PBKDF2" == "true" ]]; then
  PBKDF2_OPTS="-pbkdf2 -c $PBKDF2_ITER"
  PBKDF2_RFC822="; key-gen=\"pbkdf2\"; iter=\"$PBKDF2_ITER\""
fi

echo "Content-Type: encrypted/openssl; alg=\"aes-256-ofb\"; digest-alg=\"sha256\"$PBKDF2_RFC822" > $OUTFILE
echo "Date: $(date)" >> $OUTFILE
echo >> $OUTFILE
openssl enc -aes-256-ofb -in $INFILE -pass env:ENC_PASSWORD -md sha256 $PBKDF2_OPTS -base64 >> $OUTFILE
echo ----- >> $OUTFILE
echo "Content-Type: application/signature.openssl; alg=\"hmac\"; digest-alg=\"sha256\"" >> $OUTFILE
echo >> $OUTFILE
openssl dgst -sha256 -hmac $AUTH_PASSWORD -hex $OUTFILE | awk '{ print $2 }' >> $OUTFILE

# encode the outfile with base64 to prevent line mangling that would invalidate the signature
cat $OUTFILE | base64 > $OUTFILE.base64
