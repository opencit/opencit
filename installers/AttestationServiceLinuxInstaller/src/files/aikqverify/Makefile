# Important! Ensure that the tabs are maintained.
# Converting tabs to spaces is an error in Makefiles
CC=gcc
CFLAGS=-fstack-protector-strong -fPIE -fPIC -O2 -D_FORTIFY_SOURCE=2 -Wformat -Wformat-security
LDFLAGS=-z noexecstack -z relro -z now -pie

all:    clean aikqverify aikqverify2 aikqverifywin aikqverifywin2

aikqverify: aikqverify.o
	$(CC) $(CFLAGS) aikqverify.o -o aikqverify -lcrypto $(LDFLAGS)

aikqverify.o: $(wildcard aikqverify.c)
	$(CC) $(CFLAGS) -c aikqverify.c $(LDFLAGS)

aikqverifywin: aikqverifywin.o
	$(CC) $(CFLAGS) aikqverifywin.o -o aikqverifywin -lcrypto $(LDFLAGS)

aikqverifywin.o: $(wildcard aikqverifywin.c)
	$(CC) $(CFLAGS) -c aikqverifywin.c $(LDFLAGS)

aikqverifywin2: aikqverifywin2.o
	$(CC) $(CFLAGS) aikqverifywin2.o -o aikqverifywin2 -lcrypto $(LDFLAGS)

aikqverifywin2.o: $(wildcard aikqverifywin2.c)
	$(CC) $(CFLAGS) -c aikqverifywin2.c $(LDFLAGS)

aikqverify2: aikqverify2.o
	$(CC) $(CFLAGS) aikqverify2.o -o aikqverify2 -lcrypto $(LDFLAGS)

aikqverify2.o: $(wildcard aikqverify2.c)
	$(CC) $(CFLAGS) -c aikqverify2.c $(LDFLAGS)
	
clean:
	rm -f aikqverify aikqverify.o aikqverifywin aikqverifywin.o aikqverifywin2 aikqverifywin2.o aikqverify2 aikqverify2.o

install:
	mkdir -p /var/opt/intel/aikverifyhome/bin
	chmod 755 aikqverify aikqverify2 aikqverifywin aikqverifywin2 openssl.sh
	cp aikqverify /var/opt/intel/aikverifyhome/bin
	cp aikqverify2 /var/opt/intel/aikverifyhome/bin
	cp aikqverifywin /var/opt/intel/aikverifyhome/bin
	cp aikqverifywin2 /var/opt/intel/aikverifyhome/bin
	cp openssl.sh /var/opt/intel/aikverifyhome/bin
