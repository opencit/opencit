# Important! Ensure that the tabs are maintained.
# Converting tabs to spaces is an error in Makefiles
CC=gcc
CFLAGS=-fstack-protector -fPIE -fPIC -O2 -D_FORTIFY_SOURCE=2 -Wformat -Wformat-security
LDFLAGS=-z noexecstack -z relro -z now -pie

aikqverify: aikqverify.o
	$(CC) $(CFLAGS) aikqverify.o -o aikqverify -lcrypto $(LDFLAGS)

aikqverify.o: $(wildcard aikqverify.c)
	$(CC) $(CFLAGS) -c aikqverify.c $(LDFLAGS)

clean:
	rm -f aikqverify aikqverify.o

install:
	mkdir -p /var/opt/intel/aikverifyhome/bin
	mkdir -p /var/opt/intel/aikverifyhome/data
	chmod 755 aikqverify openssl.sh
	cp aikqverify /var/opt/intel/aikverifyhome/bin
	cp openssl.sh /var/opt/intel/aikverifyhome/bin
