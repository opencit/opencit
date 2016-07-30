# Important! Ensure that the tabs are maintained.
# Converting tabs to spaces is an error in Makefiles

all:    clean aikqverify aikqverify2 aikqverifywin

aikqverify: aikqverify.o
	gcc aikqverify.o -o aikqverify -lcrypto

aikqverify.o: $(wildcard aikqverify.c)
	gcc -c aikqverify.c

aikqverifywin: aikqverifywin.o
	gcc aikqverifywin.o -o aikqverifywin -lcrypto

aikqverifywin.o: $(wildcard aikqverifywin.c)
	gcc -c aikqverifywin.c

aikqverify2: aikqverify2.o
	gcc aikqverify2.o -o aikqverify2 -lcrypto

aikqverify2.o: $(wildcard aikqverify2.c)
	gcc -c aikqverify2.c
	
clean:
	rm -f aikqverify aikqverify.o aikqverifywin aikqverifywin.o aikqverify2 aikqverify2.o

install:
	mkdir -p /var/opt/intel/aikverifyhome/bin
	chmod 755 aikqverify aikqverify2 aikqverifywin openssl.sh
	cp aikqverify /var/opt/intel/aikverifyhome/bin
	cp aikqverify2 /var/opt/intel/aikverifyhome/bin
	cp aikqverifywin /var/opt/intel/aikverifyhome/bin
	cp openssl.sh /var/opt/intel/aikverifyhome/bin
