#!/bin/bash
rm -f data/matrix-multiplication-input.txt
rm -f data/matrix-multiplication-*.log
./matrix-multiplication-input.py 100 100 100 > data/matrix-multiplication-input.txt
cat data/matrix-multiplication-input.txt | ../run-emulator.sh run ../c/out/matrix-multiplication-by-definition.riscv64-gcc-O2.elf data/matrix-multiplication-by-definition.log 128mb > /dev/null
cat data/matrix-multiplication-input.txt | ../run-emulator.sh run ../c/out/matrix-multiplication-halving.riscv64-gcc-O2.elf data/matrix-multiplication-halving.log 128mb > /dev/null
cat data/matrix-multiplication-input.txt | ../run-emulator.sh run ../c/out/matrix-multiplication-power-of-2.riscv64-gcc-O2.elf data/matrix-multiplication-power-of-2.log 128mb > /dev/null
