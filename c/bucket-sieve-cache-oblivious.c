#include "emulator.h"

struct prime_t {
	// the next multiple of prime
	uint64_t position;
	uint64_t prime;
};

// Swaps the two values in memory.
void swap(
		struct prime_t *prime0,
		struct prime_t *prime1);

// the next number where the sieve will log a user data
// always a power of 2
uint64_t nextLog;

// Partitions an array of primes.
// Places primes with position >= positionEnd at the start of the array.
// Places primes with position < positionEnd at the end of the array.
// Returns the number of primes with position >= positionEnd,
// aka the size of the left side.
uint64_t partition(
		struct prime_t *memory,
		uint64_t memorySize,
		uint64_t positionEnd) {
	// there are left number of primes
	// already in place at the start of the array.
	uint64_t left=0;
	// there are right number of primes
	// already in place at the end of the array.
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

// Recursively sieves the interval [position, position+interval-1]
// Primes is the number of primes already found.
// All in primes have a multiple in the interval.
// Free is the free slots in memory.
// Memory layout: primes | free.
// Returns the new primes of primes in memory.
uint64_t sieve(
		uint64_t free, // number of free memory
		uint64_t height, // height of the current recursion
		uint64_t interval, // size of the current interval
		struct prime_t *memory, // pointer to the start of primes
		uint64_t position, // current position of the sieve
		uint64_t primes) { // number of primes in memory
	if (0ULL==height) {
		// leaf level
		// skip 0 and 1
		if ((2ULL<=position) && (!primes)) {
			// position has no primes divisor smaller than position
			// position is a prime
			// print output
			write_uint64(position);
			if (!free) {
				// memory is too small
				exit(1);
				return 0ULL;
			}
			// new prime
			memory[primes].position=position;
			memory[primes].prime=position;
			--free;
			++primes;
		}
		// set the next position off all primes dividing position
		// to their next multiple
		for (uint64_t ii=0; primes>ii; ++ii) {
			memory[ii].position+=memory[ii].prime;
		}
		if (position==nextLog) {
			// log user data at power of 2 positions
			memory_access_log_user_data(position);
			nextLog*=2;
		}
	}
	else {
		uint64_t interval2=interval/2;
		
		// left child
		// memory layout: no multiple in left | has multiple in left | free
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
		// memory layout: no multiple in right | has multiple in right | free
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
	// the height of the recursion tree
	uint64_t height=read_uint64();
	if (63ULL<height) {
		// height too large
		exit(2);
		return;
	}
	// the size of memory to use for the list of primes
	uint64_t memorySize=read_uint64();
	memorySize/=sizeof(struct prime_t);
	// allocate memory for the list of primes
	struct prime_t *memory=malloc(sizeof(struct prime_t)*memorySize);
	if (!memory) {
		exit(1);
		return;
	}
	// log user data at 1
	nextLog=1;
	
	memory_access_log_enable();
	
	// recursively sieve the interval
	sieve(
			memorySize, // free
			height,
			1ULL<<height, // interval
			memory,
			0ULL, // position
			0ULL); // primes
			
	memory_access_log_disable();
	
	free(memory);
}

void swap(struct prime_t *prime0, struct prime_t *prime1) {
	struct prime_t temp=*prime0;
	*prime0=*prime1;
	*prime1=temp;
}
