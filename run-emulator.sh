#!/bin/bash
DIR=$(cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd)
java -jar ${DIR}/risc-v-cache-emulator/target/risc-v-cache-emulator*.jar "$@"
