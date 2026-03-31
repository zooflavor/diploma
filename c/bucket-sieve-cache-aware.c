#include "emulator.h"

struct prime_t {
	uint64_t position;
	uint64_t prime;
};

struct page_t {
	struct page_t *next;
	uint64_t size;
	struct prime_t primes[];
};

#define PAGE_SIZE 4096
#define PAGE_PRIMES ((PAGE_SIZE-sizeof(struct page_t))/sizeof(struct prime_t))

struct page_t *freePages=0;

struct page_t *allocatePage(struct page_t *next);

struct page_t *add(struct page_t *page, struct prime_t *prime) {
	if ((!page) || (PAGE_PRIMES<=page->size)) {
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
		result=malloc(PAGE_SIZE);
		if (!result) {
			exit(3);
			return 0;
		}
	}
	result->next=next;
	result->size=0;
	return result;
}

void freePage(struct page_t *page) {
	page->next=freePages;
	freePages=page;
}

uint64_t power(uint64_t base, uint64_t exponent) {
	if (!exponent) {
		return 1ULL;
	}
	uint64_t result=power(base*base, exponent>>1);
	if (exponent&1ULL) {
		result*=base;
	}
	return result;
}

struct page_t *sieve(
		uint64_t base,
		uint64_t height, // height of the current recursion
		uint64_t interval, // size of the current interval
		uint64_t position, // current position of the sieve
		struct page_t *primes) { // number of primes in memory
	if (0ULL==height) {
		if ((2ULL<=position) && (!primes)) {
			write_uint64(position);
			primes=allocatePage(primes);
			primes->primes[primes->size].position=position;
			primes->primes[primes->size].prime=position;
			++primes->size;
		}
		for (struct page_t *page=primes; page; page=page->next) {
			for (uint64_t ii=0; page->size>ii; ++ii) {
				page->primes[ii].position+=page->primes[ii].prime;
			}
		}
	}
	else {
		uint64_t interval2=interval/base;
		
		struct page_t *buckets[base];
		for (uint64_t ii=0; base>ii; ++ii) {
			buckets[ii]=0;
		}
		
		while (primes) {
			struct page_t *page=primes;
			primes=primes->next;
			for (uint64_t ii=0; page->size>ii; ++ii) {
				uint64_t bb=(page->primes[ii].position-position)/interval2;
				buckets[bb]=add(buckets[bb], page->primes+ii);
			}
			freePage(page);
		}
		
		for (uint64_t bb=0; base>bb; ++bb) {
			buckets[bb]=sieve(
					base,
					height-1,
					interval2,
					position+bb*interval2,
					buckets[bb]);
			while (buckets[bb]) {
				struct page_t *page=buckets[bb];
				buckets[bb]=buckets[bb]->next;
				for (uint64_t ii=0; page->size>ii; ++ii) {
					if (position+interval<=page->primes[ii].position) {
						primes=add(primes, page->primes+ii);
					}
					else {
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
	uint64_t base=read_uint64();
	if (2ULL>base) {
		exit(1);
		return;
	}
	uint64_t height=read_uint64();
	if (63ULL<height) {
		exit(2);
		return;
	}
	
	memory_access_log_enable();
	
	struct page_t *primes=sieve(
			base,
			height,
			power(base, height), // interval
			0ULL, // position
			0); //primes
			
	memory_access_log_disable();
	
	while (0!=primes) {
		struct page_t *page=primes;
		primes=primes->next;
		free(page);
	}
	
	while (0!=freePages) {
		struct page_t *page=freePages;
		freePages=freePages->next;
		free(page);
	}
}
