#!/bin/bash

LOG="$1"
CACHE="data/cache.log"
TEMP="data/temp.log"

function opt() {
	rm -f "$CACHE"
	../run-emulator.sh cache opt "$1" "$2" data "$LOG" "$CACHE" "$TEMP"
	statistics "$CACHE" "opt, cache lines: $1, line size: $2"
}

function lru() {
	rm -f "$CACHE"
	../run-emulator.sh cache lru "$1" "$2" "$3" data allocate back "$LOG" "$CACHE" 1234567
	statistics "$CACHE" "lru, cache lines: $1, associativity: $2, line size: $3"
}

function statistics() {
	echo -n "file: $LOG, policy: $2, data accesses "
	../run-emulator.sh statistics "$1" | grep "end" | cut -d "," -f 6
}

date
statistics "$LOG" "no cache"
opt 128 8
opt 1024 16
lru 128 1 8
lru 128 2 8
lru 128 4 8
lru 128 128 8
lru 1024 1 16
lru 1024 2 16
lru 1024 4 16
lru 1024 1024 16
date

rm -f "$CACHE"
