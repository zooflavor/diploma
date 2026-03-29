#!/bin/python3
import random
import sys

ll = int(sys.argv[1])
mm = int(sys.argv[2])
nn = int(sys.argv[3])

print(ll)
print(mm)
print(nn)

random.seed()

for rr in range(0, ll):
    for cc in range(0, mm):
        print(2048*random.random()-1024)

for rr in range(0, mm):
    for cc in range(0, nn):
        print(2048*random.random()-1024)
