CC=gcc
CFLAGS=-fstack-protector -fPIE -fPIC -O2 -D_FORTIFY_SOURCE=2 -Wformat -Wformat-security
LDFLAGS=-z noexecstack -z relro -z now -pie

hex2bin: hex2bin.c
		$(CC) $(CFLAGS) -o hex2bin hex2bin.c $(LDFLAGS)

clean:
		rm -fr hex2bin