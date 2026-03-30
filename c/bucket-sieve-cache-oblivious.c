#include "emulator.h"

struct prime_t {
	uint64_t position;
	uint64_t prime;
};

void swap(
		struct prime_t *prime0,
		struct prime_t *prime1);

// partitions an array of primes
// places primes with position >= positionEnd at the start of the array
// places primes with position < positionEnd at the end of the array
// returns the number of primes with position >= positionEnd
uint64_t partition(
		struct prime_t *memory,
		uint64_t memorySize,
		uint64_t positionEnd) {
	uint64_t left=0;
	uint64_t right=0;
	while (1) {
		while ((left+right<memorySize)
				&& (memory[left].position>=positionEnd)) {
			++left;
		}
		while ((left+right<memorySize)
				&& (memory[memorySize-1-right].position<positionEnd)) {
			++right;
		}
		if (left+right>=memorySize) {
			return left;
		}
		swap(memory+left, memory+memorySize-1-right);
		++left;
		++right;
	}
}

// memory layout: primes | free
// returns: new primes
uint64_t sieve(
		uint64_t free, // number of free memory
		uint64_t height, // height of the current recursion
		uint64_t interval, // size of the current interval
		struct prime_t *memory, // pointer to the start of primes
		uint64_t position, // current position of the sieve
		uint64_t primes) { // number of primes in memory
	if (0ULL==height) {
		if ((2ULL<=position) && (!primes)) {
			write_uint64(position);
			if (!free) {
				exit(3);
				return 0ULL;
			}
			memory[primes].position=position;
			memory[primes].prime=position;
			--free;
			++primes;
		}
		for (uint64_t ii=0; primes>ii; ++ii) {
			memory[ii].position+=memory[ii].prime;
		}
	}
	else {
		uint64_t interval2=interval/2;
		
		// left child
		uint64_t notNow=partition(memory, primes, position+interval2);
		uint64_t primes2=primes-notNow;
		uint64_t newPrimes2=sieve(
					free,
					height-1,
					interval2,
					memory+notNow,
					position,
					primes2)
				-primes2;
		free-=newPrimes2;
		primes+=newPrimes2;
		
		// right child
		notNow=partition(memory, primes, position+interval);
		primes2=primes-notNow;
		newPrimes2=sieve(
					free,
					height-1,
					interval2,
					memory+notNow,
					position+interval2,
					primes2)
				-primes2;
		free-=newPrimes2;
		primes+=newPrimes2;
	}
	return primes;
}

void start() {
	uint64_t height=read_uint64();
	if (63ULL<height) {
		exit(1);
		return;
	}
	uint64_t memorySize=read_uint64();
	memorySize/=sizeof(struct prime_t);
	struct prime_t *memory=malloc(sizeof(struct prime_t)*memorySize);
	if (!memory) {
		exit(2);
		return;
	}
	
	memory_access_log_enable();
	
	sieve(
			memorySize, //free
			height,
			1ULL<<height, // interval
			memory,
			0ULL, // position
			0ULL); //primes
			
	memory_access_log_disable();
	
	free(memory);
}

void swap(struct prime_t *prime0, struct prime_t *prime1) {
	struct prime_t temp=*prime0;
	*prime0=*prime1;
	*prime1=temp;
}
