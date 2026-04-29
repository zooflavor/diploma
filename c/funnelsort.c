// Funnelsort.

#include "emulator.h"

// One 2-merger
struct merger_t {
	// the output buffer of the merger
	uint64_t *buffer;
	// index of the element after the last element
	// contained in the output buffer
	uint64_t bufferEnd;
	// the maximum size of the output buffer
	uint64_t bufferSize;
	// index of the first element contained in the output buffer
	uint64_t bufferStart;
	// the left input merger/buffer
	struct merger_t *left;
	// the left input will not produce any more element
	uint64_t leftEmpty;
	// the right input merger/buffer
	struct merger_t *right;
	// the right input will not produce any more element
	uint64_t rightEmpty;
};

// Initializes a 2-merger to 0s.
void initMerger(
		struct merger_t *merger);
// Retursn true iff the output buffer of the merger is empty.
int isEmpty(
		struct merger_t *merger);
// Retursn true iff the output buffer of the merger is full.
int isFull(
		struct merger_t *merger);
// Sets the left input merger of merger to child.
void left(
		struct merger_t *merger,
		struct merger_t *child);
// Returns the first element contained in the output buffer of merger.
uint64_t peekFirst(
		struct merger_t *merger);
// Returns and removes the first element
// contained in the output buffer of merger.
uint64_t removeFirst(
		struct merger_t *merger);
// Sets the right input merger of merger to child.
void right(
		struct merger_t *merger,
		struct merger_t *child);
// Rounds the value up to be divisble by 8.
uint64_t roundUp8(
		uint64_t value);
// The recursion of funnelsort.
// memory can be used to lay out the funnel.
// Returns the required minimum size of memory
// to lay out the funnels.
// It sorts the array iff memory is not NULL.
uint64_t sort(
		uint64_t *array,
		uint64_t arraySize,
		uint8_t *memory);
// Returns the smallest value not less than the cube-root of value.
uint64_t sqrt3(
		uint64_t value);

// Adds value to the end of the output buffer of merger.
void addLast(
		struct merger_t *merger,
		uint64_t value) {
	merger->buffer[merger->bufferEnd]=value;
	++merger->bufferEnd;
}

// Fills the output buffer of merger as much as possible.
// Only called when the buffer is empty.
// This will call fill on the input mergers if there are still
// free space in the output buffer of merger,
// but there's an input merger with empty output buffer.
void fill(
		struct merger_t *merger) {
	while (!isFull(merger)) {
		if (!merger->leftEmpty) {
			if (isEmpty(merger->left)) {
				fill(merger->left);
			}
			// if leftEmpty turns true
			// the left input merger will never be accessed
			merger->leftEmpty=isEmpty(merger->left);
		}
		if (!merger->rightEmpty) {
			if (isEmpty(merger->right)) {
				fill(merger->right);
			}
			// if rightEmpty turns true
			// the right input merger will never be accessed
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
			// prefers the left side for stability
			if (peekFirst(merger->left)<=peekFirst(merger->right)) {
				addLast(merger, removeFirst(merger->left));
			}
			else {
				addLast(merger, removeFirst(merger->right));
			}
		}
	}
}

// Lays out a 2^(height+1) funnel.
// Returns the required size of memory needed to lay out the funnel.
// If memory is NULL it will do nothing but returns the memory size.
// If memory is not NULL it will lay out the funnel.
// The top merger is placed at the start of memory.
// The top merger's output buffer will have size of outputBufferSize.
// Mergers will be set to the bottom 2^height mergers at the leaf level.
// The left and right input buffers of the leaf mergers
// are the 2^(height+1) inputs to the merger.
uint64_t funnel(
		uint64_t height,
		uint8_t *memory,
		struct merger_t **mergers,
		uint64_t outputBufferSize) {
	if (0==height) {
		// base case, a single 2-merger
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
	// top funnel, 2*topSize-way
	uint64_t memorySize=funnel(topHeight, memory, topMergers, outputBufferSize);
	if (0!=memory) {
		memory+=memorySize;
	}
	// bottom funnels
	for (uint64_t ii=0; topSize>ii; ++ii) {
		struct merger_t *merger=topMergers[ii];
		for (uint64_t jj=0; 2>jj; ++jj) {
			// bottom funnel, 2*bottomSize-way
			uint64_t memorySize2
					=funnel(bottomHeight, memory, mergers, bottomWay3);
			memorySize+=memorySize2;
			if (0!=memory) {
				struct merger_t *child=(struct merger_t*)memory;
				// connect the bottom funnel
				// to one of the inputs of the top funnel
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
	// resets the indices when the buffer becomes empty
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
	// the number of elements on the list to be sorted
	uint64_t size=read_uint64();
	
	// allocate the input/output array
	uint64_t *array=malloc(size*sizeof(uint64_t));
	if (!array) {
		exit(1);
		return;
	}
	
	// read input
	for (uint64_t ii=0; size>ii; ++ii) {
		array[ii]=read_uint64();
	}
	
	// determine the memory size needed to lay out all funnels
	uint64_t memorySize=sort(array, size, 0);
	// allocate memory for the funnels
	uint8_t *memory=malloc(memorySize);
	if (!memory) {
		exit(1);
		return;
	}
	
	memory_access_log_enable();

	// recursively sort the array
	sort(array, size, memory);
	
	// write output
	for (uint64_t ii=0; size>ii; ++ii) {
		write_uint64(array[ii]);
	}
	
	memory_access_log_disable();
}

uint64_t sort(
		uint64_t *array,
		uint64_t arraySize,
		uint8_t *memory) {
	if (1>=arraySize) {
		return 0ULL;
	}
	uint64_t kWay=sqrt3(arraySize);
	// a funnel is a perfect binary tree
	// so k-way must a power of 2
	uint64_t k2Way=1ULL;
	uint64_t k2WayHeight=0ULL;
	while (kWay>k2Way) {
		k2Way<<=1ULL;
		++k2WayHeight;
	}
	uint64_t childMemorySize=0ULL;
	if (memory) {
		// recursively sort the subarrays
		for (uint64_t ii=0ULL; kWay>ii; ++ii) {
			// subarray indices
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
	// lay out the k2Way-merger
	uint64_t memorySize=funnel(k2WayHeight-1, memory, mergers, arraySize);
	struct merger_t *topMerger=(struct merger_t*)memory;
	if (memory) {
		memory+=memorySize;
	}
	// connect the subarrays as inputs to the funnel.
	for (uint64_t ii=0; mergersSize>ii; ++ii) {
		struct merger_t *merger=mergers[ii];
		for (int jj=0; 2>jj; ++jj) {
			uint64_t kk=2ULL*ii+jj;
			uint64_t end;
			uint64_t start;
			if (kk<kWay) {
				// subarray indices
				end=(kk+1)*arraySize/kWay;
				start=kk*arraySize/kWay;
			}
			else {
				// supernumerary input buffers are empty
				end=arraySize;
				start=arraySize;
			}
			uint64_t leafBufferSize=end-start;
			uint64_t leafMemorySize=roundUp8(sizeof(struct merger_t));
			struct merger_t *leaf=(struct merger_t*)memory;
			if (memory) {
				memory+=leafMemorySize;
				// create a merger after the funnel
				// but use the input array as an already filled up
				// output buffer
				// the merger has no inputs and both child are marked empty
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
		// fill the top merger
		// the top merger's outout buffer has the same size
		// as the array to be sorted
		// the output buffer will go from empty to full
		fill(topMerger);
		for (uint64_t ii=0; arraySize>ii; ++ii) {
			array[ii]=removeFirst(topMerger);
		}
	}
	return max(childMemorySize, memorySize);
}

uint64_t sqrt3(
		uint64_t value) {
	// binary search for ceiling(sqrt3(value))
	// for all positive numbers 1 is a lover bound
	// for all positive numbers value is an upper bound
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
