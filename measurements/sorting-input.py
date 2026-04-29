#!/bin/python3
import random
import sys

nn = int(sys.argv[1])

print(nn)

random.seed()

for rr in range(0, nn):
    print(random.randint(0, 2*nn))
