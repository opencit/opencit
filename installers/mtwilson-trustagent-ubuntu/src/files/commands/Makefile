# Important! Ensure that the tabs are maintained.
# Converting tabs to spaces is an error in Makefiles
all: aikquote

.PHONY: all

aikquote: aikquote.o
	gcc aikquote.o -o aikquote -lcrypto -ltspi

aikquote.o: $(wildcard aikquote.c)
	gcc -c aikquote.c

clean:
	rm -f aikquote

purge:
	rm -f aikquote aikquote.o


# Static linking (Not working yet, missing gtk libraries and their dependencies...) gcc -static aikquote.o -o aikquote -lcrypto -ltspi -pthread  `pkg-config --cflags --libs gtk+-2.0`
