#!/bin/bash
#echo -e -n "2\n20\n" | ../run-emulator.sh run ../c/out/bucket-sieve-cache-aware.riscv64-gcc-O2.elf data/bucket-sieve-cache-aware-base-2.log 2gb
#echo -e -n "4\n10\n" | ../run-emulator.sh run ../c/out/bucket-sieve-cache-aware.riscv64-gcc-O2.elf data/bucket-sieve-cache-aware-base-4.log 2gb
echo -e -n "16\n5\n" | ../run-emulator.sh run ../c/out/bucket-sieve-cache-aware.riscv64-gcc-O2.elf data/bucket-sieve-cache-aware-base-16.log 2gb
