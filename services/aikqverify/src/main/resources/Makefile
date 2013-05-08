# Important! Ensure that the tabs are maintained.
# Converting tabs to spaces is an error in Makefiles
aikqverify: aikqverify.o
	gcc aikqverify.o -o aikqverify -lcrypto

aikqverify.o: $(wildcard aikqverify.c)
	gcc -c aikqverify.c

clean:
	rm -f aikqverify aikqverify.o

install:
	mkdir -p /var/opt/intel/aikverifyhome/bin
	mkdir -p /var/opt/intel/aikverifyhome/data
	chmod 755 aikqverify openssl.sh
	cp aikqverify /var/opt/intel/aikverifyhome/bin
	cp openssl.sh /var/opt/intel/aikverifyhome/bin
