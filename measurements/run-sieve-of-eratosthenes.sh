#!/bin/bash
echo -e -n "131072\n" | ../run-emulator.sh run ../c/out/sieve-of-eratosthenes.riscv64-gcc-O2.elf data/sieve-of-eratosthenes-17.log 2gb > /dev/null
echo -e -n "262144\n" | ../run-emulator.sh run ../c/out/sieve-of-eratosthenes.riscv64-gcc-O2.elf data/sieve-of-eratosthenes-18.log 2gb > /dev/null
echo -e -n "524288\n" | ../run-emulator.sh run ../c/out/sieve-of-eratosthenes.riscv64-gcc-O2.elf data/sieve-of-eratosthenes-19.log 2gb > /dev/null
echo -e -n "1048576\n" | ../run-emulator.sh run ../c/out/sieve-of-eratosthenes.riscv64-gcc-O2.elf data/sieve-of-eratosthenes-20.log 2gb > /dev/null
