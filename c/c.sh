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
#  compile_clang_aarch64 "${1}" "0"
#  compile_clang_aarch64 "${1}" "1"
#  compile_clang_aarch64 "${1}" "2"
#  compile_gcc_aarch64 "${1}" "0"
#  compile_gcc_aarch64 "${1}" "1"
#  compile_gcc_aarch64 "${1}" "2"
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

#function compile_clang_aarch64() {
#  INPUT="${1}"
#  OPTIMIZE="${2}"
#  OUTPUT_ELF="${OUTPUT_DIR}/${INPUT}.aarch64-clang-O${OPTIMIZE}.elf"
#  OUTPUT_DISASSEMBLY="${OUTPUT_ELF}.disassembly"
#  echo $OUTPUT_ELF
#  clang --target=aarch64-linux-gnu -mcpu=generic+nosimd "-O${OPTIMIZE}" -std=c17 -Wall -Wextra \
#                -Wl,-Taarch64.ld -nostdlib -ffreestanding -fno-vectorize -fno-slp-vectorize -fno-tree-vectorize \
#                -DEMULATED=1 -o "${OUTPUT_ELF}" "${INPUT}.c"
#  aarch64-linux-gnu-objdump -d "${OUTPUT_ELF}" > "${OUTPUT_DISASSEMBLY}"
#}

function compile_gcc() {
  INPUT="${1}"
  OPTIMIZE="${2}"
  OUTPUT_ELF="${OUTPUT_DIR}/${INPUT}.riscv64-gcc-O${OPTIMIZE}.elf"
  OUTPUT_DISASSEMBLY="${OUTPUT_ELF}.disassembly"
  echo $OUTPUT_ELF
  riscv64-linux-gnu-gcc -march=rv64imfd -mabi=lp64d "-O${OPTIMIZE}" -std=c17 -Wall -Wextra \
                -T riscv64.ld -nostdlib -ffreestanding \
                -DEMULATED=1 -o "${OUTPUT_ELF}" "${INPUT}.c"
  riscv64-linux-gnu-objdump -d "${OUTPUT_ELF}" > "${OUTPUT_DISASSEMBLY}"
}

#function compile_gcc_aarch64() {
#  INPUT="${1}"
#  OPTIMIZE="${2}"
#  OUTPUT_ELF="${OUTPUT_DIR}/${INPUT}.aarch64-gcc-O${OPTIMIZE}.elf"
#  OUTPUT_DISASSEMBLY="${OUTPUT_ELF}.disassembly"
#  echo $OUTPUT_ELF
#  aarch64-linux-gnu-gcc-11 -march=armv8-a -mlittle-endian "-O${OPTIMIZE}" -std=c17 -Wall -Wextra \
#                -T aarch64.ld -nostdlib -ffreestanding -fno-tree-vectorize \
#                -DEMULATED=1 -o "${OUTPUT_ELF}" "${INPUT}.c"
#  aarch64-linux-gnu-objdump -d "${OUTPUT_ELF}" > "${OUTPUT_DISASSEMBLY}"
#}

compile "emulator-tests"
compile "matrix-multiplication"
