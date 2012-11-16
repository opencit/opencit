# Important! Ensure that the tabs are maintained.
# Converting tabs to spaces is an error in Makefiles
aikqverify: aikqverify.o
	gcc aikqverify.o -o aikqverify -lcrypto

aikqverify.o: $(wildcard aikqverify.c)
	gcc -c aikqverify.c

clean:
	rm -f aikqverify aikqverify.o
