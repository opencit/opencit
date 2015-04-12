SUBDIRS=. test

EXTRA_DIST = README  openssl.cnf.sample

openssl_engine_LTLIBRARIES=libtpm.la
bin_PROGRAMS=create_tpm_key
openssl_enginedir=@libdir@/openssl/engines

libtpm_la_LIBADD=-lcrypto -lc -ltspi
libtpm_la_SOURCES=e_tpm.c e_tpm.h e_tpm_err.c

create_tpm_key_SOURCES=create_tpm_key.c
create_tpm_key_LDADD=-ltspi
