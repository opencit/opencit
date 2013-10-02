# Important! Ensure that the tabs are maintained.
# Converting tabs to spaces is an error in Makefiles
all: takeownership getcert getcert2 aikpublish aikrespond aikchallenge aikquote

aikpublish:
	gcc -o aikpublish aikpublish.c -ltspi
	chmod +x aikpublish

aikrespond:
	gcc -o aikrespond aikrespond.c -ltspi
	chmod +x aikrespond

aikchallenge:
	gcc -o aikchallenge aikchallenge.c -lcrypto
	chmod +x aikchallenge

.PHONY: all tpm-quote-tools


getcert: getcert.o remote.o tss_err.o
	gcc getcert.o remote.o tss_err.o -o getcert -lcrypto -ltspi
	chmod +x getcert

getcert.o: $(wildcard getcert.c)
	gcc -c getcert.c

getcert2: getcert2.o remote.o tss_err.o
	gcc getcert2.o remote.o tss_err.o -o getcert2 -lcrypto -ltspi
	chmod +x getcert2

getcert2.o: $(wildcard getcert2.c)
	gcc -c getcert2.c

aikquote: aikquote.o remote.o
	gcc aikquote.o remote.o -o aikquote -lcrypto -ltspi

aikquote.o: $(wildcard aikquote.c)
	gcc -c aikquote.c

aikqverify: aikqverify.o
	gcc aikqverify.o -o aikqverify -lcrypto -ltspi

aikqverify.o: $(wildcard aikquote.c)
	gcc -c aikqverify.c

takeownership: tss_err.o takeownership.o
	gcc takeownership.o tss_err.o -o takeownership -lcrypto -ltspi

takeownership_alternative: tpm-quote-tools takeownership.o
	gcc takeownership.o -o takeownership -lcrypto -ltspi -Ltpm-quote-tools-1.0.1 -ltpm_quote

takeownership.o: $(wildcard takeownership.c)
	gcc -c takeownership.c

tss_err.o:
	gunzip -c tpm-quote-tools-1.0.1.tar.gz | tar xf -
	cd tpm-quote-tools-1.0.1 && ./configure && make && cp tss_err.o ..

tpm-quote-tools:
	gunzip -c tpm-quote-tools-1.0.1.tar.gz | tar xf -
	cd tpm-quote-tools-1.0.1 && ./configure && make

remote.o:
	gcc -c remote.c

clean:
	rm -f aikquote getcert getcert2 aikpublish aikchallenge aikrespond takeownership
	rm -rf tpm-quote-tools-1.0.1

purge: clean
	rm -f *.o

install:
	mkdir -p /var/opt/intel/daa
	mkdir -p /opt/intel/daa/bin
	chmod 755 aikquote getcert getcert2 aikpublish aikchallenge aikrespond takeownership
	cp aikquote getcert getcert2 aikpublish aikchallenge aikrespond takeownership /opt/intel/daa/bin

