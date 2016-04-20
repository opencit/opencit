@echo off
set OPENSSL_CONF=C:\OpenSSL-Win32\bin\openssl.cfg
C:\OpenSSL-Win32\bin\openssl.exe x509 -in %1 -noout -pubkey > %2
