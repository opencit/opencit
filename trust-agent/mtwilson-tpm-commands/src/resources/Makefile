CFLAGS=-fstack-protector -fPIE -fPIC -O2 -D_FORTIFY_SOURCE=2 -Wformat -Wformat-security
LDFLAGS=-z noexecstack -z relro -z now -pie

all: tpm-createkey tpm-signdata tpm-bindaeskey tpm-unbindaeskey

tpm-createkey:
	gcc $(CFLAGS) -g -O0 -DLOCALEDIR='"/usr/share/locale"' -Itpm-tools-1.3.8 -Itpm-tools-1.3.8/include -L/usr/local/ssl/lib -I/usr/local/ssl/include -o tpm_createkey tpm_createkey.c tpm-tools-1.3.8/lib/tpm_tspi.c tpm-tools-1.3.8/lib/tpm_utils.c tpm-tools-1.3.8/lib/tpm_log.c hex2bytea.c -lcrypto -ltspi $(LDFLAGS)

tpm-signdata:
	gcc $(CFLAGS) -g -O0 -DLOCALEDIR='"/usr/share/locale"' -Itpm-tools-1.3.8 -Itpm-tools-1.3.8/include -L/usr/local/ssl/lib -I/usr/local/ssl/include -o tpm_signdata  tpm_signdata.c tpm-tools-1.3.8/lib/tpm_tspi.c tpm-tools-1.3.8/lib/tpm_utils.c tpm-tools-1.3.8/lib/tpm_log.c hex2bytea.c -lcrypto -ltspi $(LDFLAGS)

tpm-bindaeskey:
	gcc $(CFLAGS) -g -O0 -DLOCALEDIR='"/usr/share/locale"' -Itpm-tools-1.3.8 -Itpm-tools-1.3.8/include -L/usr/local/ssl/lib -I/usr/local/ssl/include -o tpm_bindaeskey tpm_bindaeskey.c tpm-tools-1.3.8/lib/tpm_tspi.c tpm-tools-1.3.8/lib/tpm_utils.c tpm-tools-1.3.8/lib/tpm_log.c -lcrypto -ltspi $(LDFLAGS)

tpm-unbindaeskey:
	gcc $(CFLAGS) -g -O0 -DLOCALEDIR='"/usr/share/locale"' -Itpm-tools-1.3.8 -Itpm-tools-1.3.8/include -L/usr/local/ssl/lib -I/usr/local/ssl/include -o tpm_unbindaeskey tpm_unbindaeskey.c tpm-tools-1.3.8/lib/tpm_tspi.c tpm-tools-1.3.8/lib/tpm_utils.c tpm-tools-1.3.8/lib/tpm_log.c hex2bytea.c -lcrypto -ltspi $(LDFLAGS)

clean:
	rm -f tpm_createkey tpm_signdata tpm_bindaeskey tpm_unbindaeskey
