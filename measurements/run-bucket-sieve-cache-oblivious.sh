#!/bin/bash
echo -e -n "20\n2000000000\n" | ../run-emulator.sh run ../c/out/bucket-sieve-cache-oblivious.riscv64-gcc-O2.elf data/bucket-sieve-cache-oblivious.log 2gb
