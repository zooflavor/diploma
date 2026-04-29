// Mergesort using a temporary array to merge sorted subarrays.

#include "emulator.h"

// The recursion of mergesort.
// size is the size of the current subarray
void mergesort(
		uint64_t *array,
		uint64_t size,
		uint64_t *temp) {
	if (1>=size) {
		// empty and singleton arrays are sorted
		return;
	}
	
	uint64_t size0=size/2;
	uint64_t size1=size-size0;
	uint64_t *array1=array+size0;
	
	mergesort(array, size0, temp);
	mergesort(array1, size1, temp);
	
	uint64_t index0=0;
	uint64_t index1=0;
	uint64_t tempIndex=0;
	while ((size0>index0) && (size1>index1)) {
		// prefers the left side for stability
		if (array[index0]<=array1[index1]) {
			temp[tempIndex]=array[index0];
			++index0;
			++tempIndex;
		}
		else {
			temp[tempIndex]=array1[index1];
			++index1;
			++tempIndex;
		}
	}
	while (size0>index0) {
		temp[tempIndex]=array[index0];
		++index0;
		++tempIndex;
	}
	while (size1>index1) {
		temp[tempIndex]=array1[index1];
		++index1;
		++tempIndex;
	}
	for (uint64_t ii=0; size>ii; ++ii) {
		array[ii]=temp[ii];
	}
}

void start() {
	// the number of elements on the list to be sorted
	uint64_t size=read_uint64();
	
	// allocate 2 array of size
	uint64_t *array=malloc(2*size*sizeof(uint64_t));
	if (0==array) {
		exit(1);
		return;
	}
	uint64_t *temp=array+size;
	
	// read input
	for (uint64_t ii=0; size>ii; ++ii) {
		array[ii]=read_uint64();
	}
	
	memory_access_log_enable();
	
	// sort array
	mergesort(array, size, temp);
	
	// print result
	for (uint64_t ii=0; size>ii; ++ii) {
		write_uint64(array[ii]);
	}
	
	memory_access_log_disable();
}
