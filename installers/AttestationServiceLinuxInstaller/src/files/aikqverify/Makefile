# Important! Ensure that the tabs are maintained.
# Converting tabs to spaces is an error in Makefiles

all:    aikqverify aikqverifywin

aikqverify: aikqverify.o
	gcc aikqverify.o -o aikqverify -lcrypto

aikqverify.o: $(wildcard aikqverify.c)
	gcc -c aikqverify.c

aikqverifywin: aikqverifywin.o
	gcc aikqverifywin.o -o aikqverifywin -lcrypto

aikqverifywin.o: $(wildcard aikqverifywin.c)
	gcc -c aikqverifywin.c

clean:
	rm -f aikqverify aikqverify.o aikqverifywin aikverifywin.o

install:
	mkdir /var/opt/intel/aikverifyhome/bin
	chmod 755 aikqverify openssl.sh
	cp aikqverify /var/opt/intel/aikverifyhome/bin
	cp aikqverifywin /var/opt/intel/aikverifyhome/bin
	cp openssl.sh /var/opt/intel/aikverifyhome/bin
