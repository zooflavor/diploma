#include "emulator.h"

void start() {
	uint64_t nn=read_uint64();
	
	uint8_t *primes=malloc(nn*sizeof(uint8_t));
	if (!primes) {
		exit(1);
		return;
	}
	
	memory_access_log_enable();
	
	for (uint64_t ii=0ULL; nn>ii; ++ii) {
		primes[ii]=1;
	}
	
	for (uint64_t ii=2ULL; nn>ii; ++ii) {
		if (primes[ii]) {
			write_uint64(ii);
			for (uint64_t jj=2ULL*ii; nn>jj; jj+=ii) {
				primes[jj]=0;
			}
		}
	}
	
	memory_access_log_disable();
	
	free(primes);
}
