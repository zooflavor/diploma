#include "emulator.h"

struct prime_t {
	// the next multiple of prime
	uint64_t position;
	uint64_t prime;
};

struct page_t {
	// the next page in the linked list
	// 0 iff there's no more page
	struct page_t *next;
	// the number of filled slots in primes
	// it's filled up starting at 0 index
	uint64_t size;
	struct prime_t primes[];
};

// the branching factor of the recursion tree
uint64_t base;
struct page_t *freePages=0;
// the next number where the sieve will log a user data
// always a power of 2
uint64_t nextLog;
// the maximum number of primes a page can hold
uint64_t pagePrimes;
// the page size for the linked list
uint64_t pageSize;

// Allocates a new page.
// If there's a page on freePages, it will return one removed from freePages.
// Otherwise allocates a new page from heap memory.
// If there's no more memory it will terminate the execution of the program.
// The next member of the result will be set to next.
struct page_t *allocatePage(struct page_t *next);

// Adds a prime to the linked list page.
// Allocates a new page if necessary.
struct page_t *add(struct page_t *page, struct prime_t *prime) {
	if ((!page) || (pagePrimes<=page->size)) {
		page=allocatePage(page);
	}
	page->primes[page->size]=*prime;
	++page->size;
	return page;
}

struct page_t *allocatePage(struct page_t *next) {
	struct page_t *result;
	if (freePages) {
		result=freePages;
		freePages=freePages->next;
	}
	else {
		result=malloc(pageSize);
		if (!result) {
			exit(1);
			return 0;
		}
	}
	result->next=next;
	result->size=0;
	return result;
}

// Frees a page.
// Freed pages go to freePages, and they are reused by allocatePage().
void freePage(struct page_t *page) {
	page->next=freePages;
	freePages=page;
}

// Returns base^exponent.
uint64_t power(uint64_t base, uint64_t exponent) {
	// shift and square method
	uint64_t result=1ULL;
	while (exponent) {
		if (exponent&1ULL) {
			result*=base;
		}
		base*=base;
		exponent>>=1;
	}
	return result;
}

// Recursively sieves the interval [position, position+interval-1]
// Primes is the linked list of primes already found.
// All in primes have a multiple in the interval.
// Returns the new linked list of primes.
struct page_t *sieve(
		uint64_t height, // height of the current recursion
		uint64_t interval, // size of the current interval
		uint64_t position, // current position of the sieve
		struct page_t *primes) { // number of primes in memory
	if (0ULL==height) {
		// leaf level
		// skip 0 and 1
		if ((2ULL<=position) && (!primes)) {
			// position has no primes divisor smaller than position
			// position is a prime
			// print output
			write_uint64(position);
			// new prime
			struct prime_t prime;
			prime.position=position;
			prime.prime=position;
			primes=add(primes, &prime);
		}
		// set the next position off all primes dividing position
		// to their next multiple
		for (struct page_t *page=primes; page; page=page->next) {
			for (uint64_t ii=0; page->size>ii; ++ii) {
				page->primes[ii].position+=page->primes[ii].prime;
			}
		}
		if (position==nextLog) {
			// log user data at power of 2 positions
			memory_access_log_user_data(position);
			nextLog*=2;
		}
	}
	else {
		uint64_t interval2=interval/base;
		
		// a bucket for all children/digits
		struct page_t *buckets[base];
		for (uint64_t ii=0; base>ii; ++ii) {
			buckets[ii]=0;
		}
		
		// sort primes into buckets
		while (primes) {
			struct page_t *page=primes;
			primes=primes->next;
			for (uint64_t ii=0; page->size>ii; ++ii) {
				// each primes goes to the smallest child having a multiple
				// of it
				uint64_t bb=(page->primes[ii].position-position)/interval2;
				buckets[bb]=add(buckets[bb], page->primes+ii);
			}
			freePage(page);
		}
		
		// go through the children
		for (uint64_t bb=0; base>bb; ++bb) {
			// recursively sieve the child
			buckets[bb]=sieve(
					height-1,
					interval2,
					position+bb*interval2,
					buckets[bb]);
			// redistribute the primes returned
			while (buckets[bb]) {
				struct page_t *page=buckets[bb];
				buckets[bb]=buckets[bb]->next;
				for (uint64_t ii=0; page->size>ii; ++ii) {
					if (position+interval<=page->primes[ii].position) {
						// larger than this interval, mark for return
						primes=add(primes, page->primes+ii);
					}
					else {
						// next smallest child
						uint64_t bb=(page->primes[ii].position-position)/interval2;
						buckets[bb]=add(buckets[bb], page->primes+ii);
					}
				}
				freePage(page);
			}
		}
	}
	return primes;
}

void start() {
	// branching factor of the recursion tree
	base=read_uint64();
	if (2ULL>base) {
		exit(3);
		return;
	}
	// the height of the recursion tree
	uint64_t height=read_uint64();
	if (63ULL<height) {
		// height too large
		exit(2);
		return;
	}
	// page size of the linked list
	pageSize=read_uint64();
	if (32ULL>pageSize) {
		// page size too small
		exit(4);
		return;
	}
	pagePrimes=(pageSize-sizeof(struct page_t))/sizeof(struct prime_t);
	// log user data at 1
	nextLog=1;
	
	memory_access_log_enable();
	
	// recursively sieve the interval
	struct page_t *primes=sieve(
			height,
			power(base, height), // interval
			0ULL, // position
			0); //primes
			
	memory_access_log_disable();
	
	// free pages in primes
	while (0!=primes) {
		struct page_t *page=primes;
		primes=primes->next;
		free(page);
	}
	
	// free free pages
	while (0!=freePages) {
		struct page_t *page=freePages;
		freePages=freePages->next;
		free(page);
	}
}
