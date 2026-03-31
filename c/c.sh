#!/bin/bash
set -e

OUTPUT_DIR="./out"

rm -rf "${OUTPUT_DIR}"
mkdir "${OUTPUT_DIR}"

function compile() {
  compile_clang "${1}" "0"
  compile_clang "${1}" "1"
  compile_clang "${1}" "2"
  compile_gcc "${1}" "0"
  compile_gcc "${1}" "1"
  compile_gcc "${1}" "2"
}

function compile_clang() {
  INPUT="${1}"
  OPTIMIZE="${2}"
  OUTPUT_ELF="${OUTPUT_DIR}/${INPUT}.riscv64-clang-O${OPTIMIZE}.elf"
  OUTPUT_DISASSEMBLY="${OUTPUT_ELF}.disassembly"
  echo $OUTPUT_ELF
  clang --target=riscv64-linux-gnu -march=rv64imfd -mabi=lp64d "-O${OPTIMIZE}" -std=c17 -Wall -Wextra \
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
  riscv64-linux-gnu-gcc -march=rv64imfd -mabi=lp64d "-O${OPTIMIZE}" -std=c17 -Wall -Wextra \
                -T riscv64.ld -nostdlib -ffreestanding  \
                -DEMULATED=1 -o "${OUTPUT_ELF}" "${INPUT}.c"
  riscv64-linux-gnu-objdump -d "${OUTPUT_ELF}" > "${OUTPUT_DISASSEMBLY}"
}

compile "bucket-sieve-cache-aware"
compile "bucket-sieve-cache-oblivious"
compile "emulator-tests"
compile "matrix-multiplication-by-definition"
compile "matrix-multiplication-halving"
compile "matrix-multiplication-power-of-2"
