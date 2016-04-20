#!/bin/bash
# encrypt.sh - encrypt and sign with hmac
# USAGE:
#       encrypt.sh --enc encpassword --auth authpassword encrypted.zip plain.txt...
#       encrypt.sh -enc encpassword -auth authpassword encrypted.zip plain.txt...
#       encrypt.sh -e encpassword -a authpassword encrypted.zip plain.txt...
# NOTE: Password based key derivation function needs to be configured with
#       number of iterations but the openssl-1.0.1f enc program does not implement
#       this option so only ONE iteration is used, which is not good enough.
#       This script can use a patched version of openssl-1.0.1f which does provide
#       the necessary options. It's enabled by default with USE_PBKDF2=true
#       Disable with USE_PBKDF2=false.
#       Tune the number of iterations by running "openssl speed sha256"
# Sample contents of the generated encrypted.zip:
# plain.txt          // original input file
# plain.txt.sig      // signature over plain.txt
# Sample format of the generated encrypted.zip:
#Content-Type: encrypted/openssl; alg="aes-256-ofb"; digest-alg="sha256"; key-gen="pbkdf2"; iter="2052228"; enclosed="application/zip"
#Date: Tue Jan 21 01:16:01 PST 2014
#
#U2FsdGVkX1+LU5kNud8jedWmWagnq8WR0xf1TVFkBmkLm+wb
#
# Sample format of the generated plain.txt.sig files:
#Content-Type: application/mtwilson-signature; alg="hmac"; digest-alg="sha256"; headers="content-type,link,date"
#Link: <plain.txt>; rel=signed
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
  if ! options=$(getopt -a -n encrypt.sh -l nopbkdf2,passwd:,enc:,auth: -o p:,e:,a: -- "$@"); then exit 1; fi
  eval set -- "$options"
  while [ $# -gt 0 ]
  do
    case $1 in
      --nopbkdf2) USE_PBKDF2=false;;
      -p|--passwd) eval ENC_PASSWORD="\$$2"; eval AUTH_PASSWORD="\$$2"; shift;;
      -e|--enc) eval ENC_PASSWORD="\$$2"; shift;;
      -a|--auth) eval AUTH_PASSWORD="\$$2"; shift;;
      --) OUTFILE="$2"; shift; shift; INFILES="$@"; shift;;
    esac
    shift
  done
}

parse_args $@

export ENC_PASSWORD
export AUTH_PASSWORD

if [[ "$USE_PBKDF2" == "true" ]]; then
  PBKDF2_OPTS="-pbkdf2 -c $PBKDF2_ITER"
  PBKDF2_RFC822="; key-gen=\"pbkdf2\"; iter=\"$PBKDF2_ITER\""
fi

write_signature() {
  local infile="$1"
  local sigfile="$infile.sig"
  local docfile="$infile.sig.doc"
  local infilename=$(basename $infile)

  # The signature document (docfile) consists of:
  # 1. the signature headers, followed by blank line
  # 2. the original input file
  # The signature document is signed and then the signature file (sigfile) is
  # generated and consists of:
  # 1. the signature headers, followed by blank line
  # 2. the signature, base-64 encoded

  # Generate signature headers and trailing blank line:
  echo "Content-Type: application/mtwilson-signature; alg=\"hmac\"; digest-alg=\"sha256\"; headers=\"content-type,link,date\"" > $docfile
  echo "Link: <${infilename}>; rel=signed" >> $docfile
  echo "Date: $(date)" >> $docfile
  local signature_headers=`cat $docfile`
  echo >> $docfile

  # Append the original input file and calculate the signature:
  cat $infile >> $docfile  
  local signature=`openssl dgst -sha256 -hmac $AUTH_PASSWORD -binary $docfile | openssl enc -base64`

  # The signature document file is a temporary file, so delete it:
  rm $docfile

  # Generate the output signature, replacing the signature file
  echo "$signature_headers" > $sigfile
  echo >> $sigfile
  echo "$signature" >> $sigfile
}

encrypt_zipfile() {
  local zipfile="$1"
  local encfile="$zipfile.enc"
  echo "Content-Type: encrypted/openssl; alg=\"aes-256-ofb\"; digest-alg=\"sha256\"$PBKDF2_RFC822; enclosed=\"application/zip\"" > $encfile
  echo "Date: $(date)" >> $encfile
  echo >> $encfile
  openssl enc -aes-256-ofb -in $zipfile -pass env:ENC_PASSWORD -md sha256 $PBKDF2_OPTS -base64 >> $encfile
}

# sign each of the input files and keep a list of the signature files
SIGFILES=""
for infile in $INFILES
do
  echo "input file: $infile"
  write_signature $infile
  SIGFILES="$SIGFILES ${infile}.sig"
done

# create the encrypted zip file
zip -j $OUTFILE.zip $INFILES $SIGFILES
encrypt_zipfile $OUTFILE.zip
mv $OUTFILE.zip.enc $OUTFILE
#rm $OUTFILE.zip

