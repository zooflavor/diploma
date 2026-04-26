#!/bin/bash
rm -f data/matrix-multiplication-input.txt
rm -f data/matrix-multiplication-*.log
./matrix-multiplication-input.py 256 256 256 > data/matrix-multiplication-input.txt
#cat data/matrix-multiplication-input.txt | ../run-emulator.sh run ../c/out/matrix-multiplication-by-definition.riscv64-gcc-O2.elf data/matrix-multiplication-by-definition.log 128mb > /dev/null
#cat data/matrix-multiplication-input.txt | ../run-emulator.sh run ../c/out/matrix-multiplication-halving.riscv64-gcc-O2.elf data/matrix-multiplication-halving.log 128mb > /dev/null
#cat data/matrix-multiplication-input.txt | ../run-emulator.sh run ../c/out/matrix-multiplication-power-of-2.riscv64-gcc-O2.elf data/matrix-multiplication-power-of-2.log 128mb > /dev/null
cat data/matrix-multiplication-input.txt | ../run-emulator.sh run ../c/out/matrix-multiplication-power-of-2-strassen.riscv64-gcc-O2.elf data/matrix-multiplication-power-of-2-strassen.log 128mb > /dev/null
