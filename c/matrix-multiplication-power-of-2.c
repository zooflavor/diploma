#include "emulator.h"

int stride;

void zero(double *matrix, int size);

int max(int value0, int value1) {
	return (value0>=value1)
			?value0
			:value1;
}

// stride is the distance in memory between two cells
// in the same column and adjecent rows.
// in this implementation is the same value
// as the real number of columns in a matrix
// matrices are laid out in row-major order,
// so all strides are always the chose power of 2 value.
void multiply(
		double *matrix0,
		double *matrix1,
		double *matrix2,
		int size) {
	if (0>=size) {
		// nothing to do
	}
	else if (1==size) {
		matrix2[0]+=matrix0[0]*matrix1[0];
	}
	else { // 1<size
		int halfSize=size/2;
		for (int rr=0; 2>rr; ++rr) {
			for (int cc=0; 2>cc; ++cc) {
				for (int ii=0; 2>ii; ++ii) {
					multiply(
							matrix0+rr*halfSize*stride+ii*halfSize,
							matrix1+ii*halfSize*stride+cc*halfSize,
							matrix2+rr*halfSize*stride+cc*halfSize,
							halfSize);
				}
			}
		}
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
	
	int maxSize=max(size0, max(size1, size2));
	// find smallest power of 2 not smaller than maxSize
	stride=1;
	while (1) {
		if (0>=stride) {
			// overflow
			exit(2);
		}
		if (stride>=maxSize) {
			break;
		}
		stride*=2;
	}
	
	// allocate 3 matrices
	double *matrices=malloc(3*stride*stride*sizeof(double));
	if (0==matrices) {
		exit(1);
		return;
	}
	double *matrix0=matrices;
	double *matrix1=matrix0+stride*stride;
	double *matrix2=matrix1+stride*stride;
	
	memory_access_log_enable();
	
	// zero input matrices
	zero(matrix0, stride);
	zero(matrix1, stride);
	
	// read input matrices. row-major order
	for (int rr=0; size0>rr; ++rr) {
		for (int cc=0; size1>cc; ++cc) {
			matrix0[rr*stride+cc]=read_double();
		}
	}
	for (int rr=0; size1>rr; ++rr) {
		for (int cc=0; size2>cc; ++cc) {
			matrix1[rr*stride+cc]=read_double();
		}
	}
	
	// zero the result matrix
	zero(matrix2, stride);
	
	// compute result
	multiply(
			matrix0,
			matrix1,
			matrix2,
			stride);
	
	// print result
	for (int rr=0; size0>rr; ++rr) {
		for (int cc=0; size2>cc; ++cc) {
			write_double(matrix2[rr*stride+cc]);
		}
	}
	
	memory_access_log_disable();
	
	free(matrices);
}

void zero(double *matrix, int size) {
	for (int rr=0; size>rr; ++rr) {
		for (int cc=0; size>cc; ++cc) {
			matrix[rr*size+cc]=0.0;
		}
	}
}
