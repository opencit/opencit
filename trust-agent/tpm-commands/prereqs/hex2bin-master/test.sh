#!/bin/sh

./hex2bin 00 > 0.bin

./hex2bin 00 0.bin

echo 00 | ./hex2bin > 0.bin

echo 00 | ./hex2bin -stdin 0.bin

