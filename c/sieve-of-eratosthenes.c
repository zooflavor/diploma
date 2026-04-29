#include "emulator.h"

void start() {
	// end of the interval
	// not inclusive
	uint64_t nn=read_uint64();
	
	// large enough array to directly index all numbers from 0 to (n-1)
	uint8_t *primes=malloc(nn*sizeof(uint8_t));
	if (!primes) {
		exit(1);
		return;
	}
	
	memory_access_log_enable();
	
	// mark every number potentially prime
	for (uint64_t ii=0ULL; nn>ii; ++ii) {
		primes[ii]=1;
	}
	
	for (uint64_t ii=2ULL; nn>ii; ++ii) {
		if (primes[ii]) {
			// ii has no primes divisor smaller than ii
			// ii is a prime
			// print output
			write_uint64(ii);
			// mark all multiply if ii starting from 2*ii
			for (uint64_t jj=2ULL*ii; nn>jj; jj+=ii) {
				primes[jj]=0;
			}
		}
	}
	
	memory_access_log_disable();
	
	free(primes);
}
