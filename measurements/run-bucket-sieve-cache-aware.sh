#!/bin/bash
echo -e -n "2\n20\n4096\n" | ../run-emulator.sh run ../c/out/bucket-sieve-cache-aware.riscv64-gcc-O2.elf data/bucket-sieve-cache-aware-base-2.log 2gb > /dev/null
echo -e -n "4\n10\n4096\n" | ../run-emulator.sh run ../c/out/bucket-sieve-cache-aware.riscv64-gcc-O2.elf data/bucket-sieve-cache-aware-base-4.log 2gb > /dev/null
echo -e -n "16\n5\n4096\n" | ../run-emulator.sh run ../c/out/bucket-sieve-cache-aware.riscv64-gcc-O2.elf data/bucket-sieve-cache-aware-base-16.log 2gb > /dev/null
#echo -e -n "16\n5\n1024\n" | ../run-emulator.sh run ../c/out/bucket-sieve-cache-aware.riscv64-gcc-O2.elf data/bucket-sieve-cache-aware-base-16-1024.log 2gb > /dev/null
#echo -e -n "16\n5\n2048\n" | ../run-emulator.sh run ../c/out/bucket-sieve-cache-aware.riscv64-gcc-O2.elf data/bucket-sieve-cache-aware-base-16-2048.log 2gb > /dev/null
#echo -e -n "16\n5\n4096\n" | ../run-emulator.sh run ../c/out/bucket-sieve-cache-aware.riscv64-gcc-O2.elf data/bucket-sieve-cache-aware-base-16-4096.log 2gb > /dev/null
#echo -e -n "16\n5\n8192\n" | ../run-emulator.sh run ../c/out/bucket-sieve-cache-aware.riscv64-gcc-O2.elf data/bucket-sieve-cache-aware-base-16-8192.log 2gb > /dev/null
#echo -e -n "16\n5\n16384\n" | ../run-emulator.sh run ../c/out/bucket-sieve-cache-aware.riscv64-gcc-O2.elf data/bucket-sieve-cache-aware-base-16-16384.log 2gb > /dev/null
