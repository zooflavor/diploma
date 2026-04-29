#!/bin/bash
rm -f data/sorting-input.txt
rm -f data/funnelsort*.log
rm -f data/mergesort*.log
./sorting-input.py 262144 > data/sorting-input.txt
cat data/sorting-input.txt | ../run-emulator.sh run ../c/out/funnelsort.riscv64-gcc-O2.elf data/funnelsort.log 2gb > /dev/null
cat data/sorting-input.txt | ../run-emulator.sh run ../c/out/mergesort.riscv64-gcc-O2.elf data/mergesort.log 2gb > /dev/null
