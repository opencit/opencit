# Important! Ensure that the tabs are maintained.
# Converting tabs to spaces is an error in Makefiles
CC=gcc
CFLAGS=-fstack-protector -fPIE -fPIC -O2 -D_FORTIFY_SOURCE=2 -Wformat -Wformat-security
LDFLAGS=-z noexecstack -z relro -z now -pie

all: aikquote

.PHONY: all

aikquote: aikquote.o
	$(CC) $(CFLAGS) aikquote.o -o aikquote -lcrypto -ltspi $(LDFLAGS)

aikquote.o: $(wildcard aikquote.c)
	$(CC) $(CFLAGS) -c aikquote.c $(LDFLAGS)

clean:
	rm -f aikquote

purge:
	rm -f aikquote aikquote.o


# Static linking (Not working yet, missing gtk libraries and their dependencies...) gcc -static aikquote.o -o aikquote -lcrypto -ltspi -pthread  `pkg-config --cflags --libs gtk+-2.0`
