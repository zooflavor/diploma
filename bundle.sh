#!/bin/bash

DIR="Diplomamunka - Nagy Péter - MLFKLX"
PROGRAM="$DIR/program"

rm -rf "$DIR"
mkdir "$DIR"
mkdir "$PROGRAM"

cp Nagy* "$DIR"
cp Nyil* "$DIR"
cp diplomamunka/diplomamunka.pdf "$DIR"
cp diplomamunka/diplomamunka.tex "$DIR"
cp -r c "$PROGRAM"
cp -r risc-v-cache-emulator "$PROGRAM"
cp run-emulator.sh "$PROGRAM"
rm -rf "$PROGRAM/c/.gitignore"
rm -rf "$PROGRAM/c/out"
rm -rf "$PROGRAM/risc-v-cache-emulator/.idea"
rm -rf "$PROGRAM/risc-v-cache-emulator/.gitignore"
rm -rf "$PROGRAM/risc-v-cache-emulator/target"

zip -9r "$DIR.zip" "$DIR"
rm -rf "$DIR"
