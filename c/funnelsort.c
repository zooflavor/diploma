#include "emulator.h"

struct merger_t {
	uint64_t *buffer;
	uint64_t bufferEnd;
	uint64_t bufferSize;
	uint64_t bufferStart;
	struct merger_t *left;
	uint64_t leftEmpty;
	struct merger_t *right;
	uint64_t rightEmpty;
};

void initMerger(
		struct merger_t *merger);
int isEmpty(
		struct merger_t *merger);
int isFull(
		struct merger_t *merger);
void left(
		struct merger_t *merger,
		struct merger_t *child);
uint64_t peekFirst(
		struct merger_t *merger);
uint64_t removeFirst(
		struct merger_t *merger);
void right(
		struct merger_t *merger,
		struct merger_t *child);
uint64_t roundUp8(
		uint64_t value);
uint64_t sort(
		uint64_t *array,
		uint64_t arraySize,
		uint8_t *memory);
uint64_t sqrt3(
		uint64_t value);

void addLast(
		struct merger_t *merger,
		uint64_t value) {
	merger->buffer[merger->bufferEnd]=value;
	++merger->bufferEnd;
}

void fill(
		struct merger_t *merger) {
	while (!isFull(merger)) {
		if (!merger->leftEmpty) {
			if (isEmpty(merger->left)) {
				fill(merger->left);
			}
			merger->leftEmpty=isEmpty(merger->left);
		}
		if (!merger->rightEmpty) {
			if (isEmpty(merger->right)) {
				fill(merger->right);
			}
			merger->rightEmpty=isEmpty(merger->right);
		}
		if (merger->leftEmpty && merger->rightEmpty) {
			break;
		}
		if (merger->leftEmpty) {
			addLast(merger, removeFirst(merger->right));
		}
		else if (merger->rightEmpty) {
			addLast(merger, removeFirst(merger->left));
		}
		else {
			if (peekFirst(merger->left)<=peekFirst(merger->right)) {
				addLast(merger, removeFirst(merger->left));
			}
			else {
				addLast(merger, removeFirst(merger->right));
			}
		}
	}
}

// leaf nodes size == 2^height
// k-way k = 2^(height+1), left and right for every leaf
// top node is at the start of the memory
uint64_t funnel(
		uint64_t height,
		uint8_t *memory,
		struct merger_t **mergers,
		uint64_t outputBufferSize) {
	if (0==height) {
		uint64_t mergerMemorySize=roundUp8(sizeof(struct merger_t));
		uint64_t bufferMemorySize=roundUp8(outputBufferSize*sizeof(uint64_t));
		if (0!=memory) {
			struct merger_t *merger=(struct merger_t*)memory;
			initMerger(merger);
			merger->buffer=(uint64_t*)(memory+mergerMemorySize);
			merger->bufferSize=outputBufferSize;
			mergers[0]=merger;
		}
		return mergerMemorySize+bufferMemorySize;
	}
	uint64_t topHeight=height/2ULL;
	uint64_t topSize=1<<topHeight;
	uint64_t bottomHeight=height-topHeight-1ULL;
	uint64_t bottomSize=1<<bottomHeight;
	uint64_t bottomWay=1ULL<<bottomHeight;
	uint64_t bottomWay3=bottomWay*bottomWay*bottomWay;
	struct merger_t *topMergers[topSize];
	uint64_t memorySize=funnel(topHeight, memory, topMergers, outputBufferSize);
	if (0!=memory) {
		memory+=memorySize;
	}
	for (uint64_t ii=0; topSize>ii; ++ii) {
		struct merger_t *merger=topMergers[ii];
		for (uint64_t jj=0; 2>jj; ++jj) {
			uint64_t memorySize2=funnel(bottomHeight, memory, mergers, bottomWay3);
			memorySize+=memorySize2;
			if (0!=memory) {
				struct merger_t *child=(struct merger_t*)memory;
				if (jj) {
					right(merger, child);
				}
				else {
					left(merger, child);
				}
				memory+=memorySize2;
			}
			mergers+=bottomSize;
		}
	}
	return memorySize;
}

void initMerger(
		struct merger_t *merger) {
	merger->buffer=0;
	merger->bufferEnd=0ULL;
	merger->bufferSize=0ULL;
	merger->bufferStart=0ULL;
	merger->left=0;
	merger->leftEmpty=1ULL;
	merger->right=0;
	merger->rightEmpty=1ULL;
}

int isEmpty(
		struct merger_t *merger) {
	return merger->bufferStart>=merger->bufferEnd;
}

int isFull(
		struct merger_t *merger) {
	return merger->bufferEnd>=merger->bufferSize;
}

void left(
		struct merger_t *merger,
		struct merger_t *child) {
	merger->left=child;
	merger->leftEmpty=0ULL;
}

uint64_t max(
		uint64_t value0,
		uint64_t value1) {
	return (value0>=value1)
			?value0
			:value1;
}

uint64_t peekFirst(
		struct merger_t *merger) {
	return merger->buffer[merger->bufferStart];
}

uint64_t removeFirst(
		struct merger_t *merger) {
	uint64_t result=peekFirst(merger);
	++merger->bufferStart;
	if (isEmpty(merger)) {
		merger->bufferEnd=0ULL;
		merger->bufferStart=0ULL;
	}
	return result;
}

void right(
		struct merger_t *merger,
		struct merger_t *child) {
	merger->right=child;
	merger->rightEmpty=0ULL;
}

uint64_t roundUp8(
		uint64_t value) {
	uint64_t result=value&~7;
	if (result!=value) {
		++result;
	}
	return result;
}

void start() {
	uint64_t size=read_uint64();
	
	uint64_t *array=malloc(size*sizeof(uint64_t));
	if (!array) {
		exit(1);
		return;
	}
	
	for (uint64_t ii=0; size>ii; ++ii) {
		array[ii]=read_uint64();
	}
	
	uint64_t memorySize=sort(array, size, 0);
	uint8_t *memory=malloc(memorySize);
	if (!memory) {
		exit(1);
		return;
	}
	
	memory_access_log_enable();

	sort(array, size, memory);
	
	for (uint64_t ii=0; size>ii; ++ii) {
		write_uint64(array[ii]);
	}
	
	memory_access_log_disable();
}

// returns: memory size needed to do the sort
uint64_t sort(
		uint64_t *array,
		uint64_t arraySize,
		uint8_t *memory) {
	if (1>=arraySize) {
		return 0ULL;
	}
	uint64_t kWay=sqrt3(arraySize);
	uint64_t k2Way=1ULL;
	uint64_t k2WayHeight=0ULL;
	while (kWay>k2Way) {
		k2Way<<=1ULL;
		++k2WayHeight;
	}
	uint64_t childMemorySize=0ULL;
	if (memory) {
		for (uint64_t ii=0ULL; kWay>ii; ++ii) {
			uint64_t end=(ii+1)*arraySize/kWay;
			uint64_t start=ii*arraySize/kWay;
			uint64_t childMemorySize2=sort(
					array+start,
					end-start,
					memory);
			childMemorySize=max(childMemorySize, childMemorySize2);
		}
	}
	uint64_t mergersSize=k2Way/2ULL;
	struct merger_t *mergers[mergersSize];
	uint64_t memorySize=funnel(k2WayHeight-1, memory, mergers, arraySize);
	struct merger_t *topMerger=(struct merger_t*)memory;
	if (memory) {
		memory+=memorySize;
	}
	for (uint64_t ii=0; mergersSize>ii; ++ii) {
		struct merger_t *merger=mergers[ii];
		for (int jj=0; 2>jj; ++jj) {
			uint64_t kk=2ULL*ii+jj;
			uint64_t end;
			uint64_t start;
			if (kk<kWay) {
				end=(kk+1)*arraySize/kWay;
				start=kk*arraySize/kWay;
			}
			else {
				end=arraySize;
				start=arraySize;
			}
			uint64_t leafBufferSize=end-start;
			uint64_t leafMemorySize=roundUp8(sizeof(struct merger_t));
			struct merger_t *leaf=(struct merger_t*)memory;
			if (memory) {
				memory+=leafMemorySize;
				initMerger(leaf);
				leaf->buffer=array+start;
				leaf->bufferEnd=leafBufferSize;
				leaf->bufferSize=leafBufferSize;
				if (jj) {
					right(merger, leaf);
				}
				else {
					left(merger, leaf);
				}
			}
			memorySize+=leafMemorySize;
		}
	}
	if (memory) {
		fill(topMerger);
		for (uint64_t ii=0; arraySize>ii; ++ii) {
			array[ii]=removeFirst(topMerger);
		}
	}
	return max(childMemorySize, memorySize);
}

uint64_t sqrt3(
		uint64_t value) {
	uint64_t ll=1ULL;
	uint64_t uu=value;
	while (ll+1ULL<uu) {
		uint64_t mm=(ll+uu)/2ULL;
		if (mm*mm*mm<value) {
			ll=mm;
		}
		else {
			uu=mm;
		}
	}
	return uu;
}
