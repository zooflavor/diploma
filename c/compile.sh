#!/bin/bash

# Compiles the programs listed at the bottom section.
#
# Every program will be compiled to the native host system and also to RISC-V.
#
# RISC-V outputs are generated
# for all combination of compilers gcc and clang,
# and optimization levels 0, 1, and 2.

set -e

OUTPUT_DIR="./out"

rm -rf "${OUTPUT_DIR}"
mkdir "${OUTPUT_DIR}"

function compile() {
  OUTPUT_ELF="${OUTPUT_DIR}/${1}.native-gcc-O2.elf"
  echo $OUTPUT_ELF
  gcc -o "${OUTPUT_ELF}" "${1}.c" native.c \
    -std=c17 -Wall -Wextra -g
  for CO in $(echo -n "compile_clang" "compile_gcc")
  do
    for OL in {0..2}
	do
	  "${CO}" "${1}" "${OL}"
	done
  done
}

function compile_clang() {
  INPUT="${1}"
  OPTIMIZE="${2}"
  OUTPUT_ELF="${OUTPUT_DIR}/${INPUT}.riscv64-clang-O${OPTIMIZE}.elf"
  OUTPUT_DISASSEMBLY="${OUTPUT_ELF}.disassembly"
  echo $OUTPUT_ELF
  clang --target=riscv64-linux-gnu -march=rv64imfd -mabi=lp64d \
    "-O${OPTIMIZE}" -std=c17 -Wall -Wextra \
    -Wl,-Triscv64.ld -nostdlib -ffreestanding \
    -DEMULATED=1 -o "${OUTPUT_ELF}" "${INPUT}.c"
  riscv64-linux-gnu-objdump -d "${OUTPUT_ELF}" > "${OUTPUT_DISASSEMBLY}"
}

function compile_gcc() {
  INPUT="${1}"
  OPTIMIZE="${2}"
  OUTPUT_ELF="${OUTPUT_DIR}/${INPUT}.riscv64-gcc-O${OPTIMIZE}.elf"
  OUTPUT_DISASSEMBLY="${OUTPUT_ELF}.disassembly"
  echo $OUTPUT_ELF
  riscv64-linux-gnu-gcc -march=rv64imfd -mabi=lp64d \
    "-O${OPTIMIZE}" -std=c17 -Wall -Wextra \
    -T riscv64.ld -nostdlib -ffreestanding  \
    -DEMULATED=1 -o "${OUTPUT_ELF}" "${INPUT}.c"
  riscv64-linux-gnu-objdump -d "${OUTPUT_ELF}" > "${OUTPUT_DISASSEMBLY}"
}

compile "bucket-sieve-cache-aware"
compile "bucket-sieve-cache-oblivious"
compile "emulator-tests"
compile "funnelsort"
compile "matrix-multiplication-by-definition"
compile "matrix-multiplication-halving"
compile "matrix-multiplication-power-of-2"
compile "matrix-multiplication-power-of-2-strassen"
compile "mergesort"
compile "sieve-of-eratosthenes"
