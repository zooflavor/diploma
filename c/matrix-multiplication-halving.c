// Matrix multiplication by recursively halving the largest dimension,
// and using block operations.

#include "emulator.h"

int max(int value0, int value1) {
	return (value0>=value1)
			?value0
			:value1;
}

// The recursion of the multiplication.
// Stride is the distance in memory between two cells
// in the same column and adjecent rows.
// In this implementation it is the same value
// as the real number of columns in a matrix.
// Matrices are laid out in row-major order,
// so stride2 would be always the same stride1.
// matrix0/size0/stride0 and matrix1/size1/stride1 are the input matrix blocks.
// matrix2/size2/stride1 is the output matrix block.
void multiply(
		double *matrix0,
		double *matrix1,
		double *matrix2,
		int size0,
		int size1,
		int size2,
		int stride0,
		int stride1) {
	int maxSize=max(size0, max(size1, size2));
	if (0>=maxSize) {
		// nothing to do
	}
	else if (1==maxSize) {
		// all matrices are 1x1
		matrix2[0]+=matrix0[0]*matrix1[0];
	}
	else if (size0==maxSize) {
		// halve the number of rows of the left matrix
		int halfSize0=size0/2;
		multiply(
				matrix0,
				matrix1,
				matrix2,
				halfSize0,
				size1,
				size2,
				stride0,
				stride1);
		multiply(
				matrix0+halfSize0*stride0,
				matrix1,
				matrix2+halfSize0*stride1,
				size0-halfSize0,
				size1,
				size2,
				stride0,
				stride1);
	}
	else if (size1==maxSize) {
		// halve the number of columns of the left matrix,
		// which is the same as the number of rows of the right matrix
		int halfSize1=size1/2;
		multiply(
				matrix0,
				matrix1,
				matrix2,
				size0,
				halfSize1,
				size2,
				stride0,
				stride1);
		multiply(
				matrix0+halfSize1,
				matrix1+halfSize1*stride1,
				matrix2,
				size0,
				size1-halfSize1,
				size2,
				stride0,
				stride1);
	}
	else {
		// halve the number of columns of the left matrix
		int halfSize2=size2/2;
		multiply(
				matrix0,
				matrix1,
				matrix2,
				size0,
				size1,
				halfSize2,
				stride0,
				stride1);
		multiply(
				matrix0,
				matrix1+halfSize2,
				matrix2+halfSize2,
				size0,
				size1,
				size2-halfSize2,
				stride0,
				stride1);
	}
}

void start() {
	// read matrix sizes
	// rows of left matrix
	int size0=read_int64();
	// columns of left matrix, rows of right matrix
	int size1=read_int64();
	// columns of right matrix
	int size2=read_int64();
	
	// allocate 3 matrices
	double *matrix0=malloc(
			(size0*size1+size1*size2+size0*size2)*sizeof(double));
	if (0==matrix0) {
		exit(1);
		return;
	}
	double *matrix1=matrix0+size0*size1;
	double *matrix2=matrix1+size1*size2;
	
	memory_access_log_enable();
	
	// read input matrices. row-major order
	for (int ii=0; size0*size1>ii; ++ii) {
		matrix0[ii]=read_double();
	}
	for (int ii=0; size1*size2>ii; ++ii) {
		matrix1[ii]=read_double();
	}
	
	// zero the result matrix
	for (int rr=0; size0>rr; ++rr) {
		for (int cc=0; size2>cc; ++cc) {
			matrix2[rr*size2+cc]=0.0;
		}
	}
	
	// compute result
	multiply(
			matrix0,
			matrix1,
			matrix2,
			size0,
			size1,
			size2,
			size1,
			size2);
	
	// print result
	for (int ii=0; size0*size2>ii; ++ii) {
		write_double(matrix2[ii]);
	}
	
	memory_access_log_disable();
	
	free(matrix2);
	free(matrix1);
	free(matrix0);
}
