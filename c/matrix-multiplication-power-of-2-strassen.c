// Matrix multiplication by the Strassen algorithm.
// This recursively halves all dimensions,
// and uses block operations.
// All matrix dimensions are enlarged to the same power-of-2 number,
// by adding zero rows and columns to the matrices.
// The result is truncated back to the size compatible with the inputs.

#include "emulator.h"

int stride;

// Sets the cells of the matrix to zero.
void zero(
		double *matrix,
		int size);

// Sets destination = source0 + source1.
void copyAdd(
		double *destination,
		double *source0,
		double *source1,
		int size) {
	for (int rr=0; size>rr; ++rr) {
		for (int cc=0; size>cc; ++cc) {
			int ii=rr*stride+cc;
			destination[ii]=source0[ii]+source1[ii];
		}
	}
}

// Sets destination = source0 + source1 + source2 + source3.
void copyAddAddSubtract(
		double *destination,
		double *source0,
		double *source1,
		double *source2,
		double *source3,
		int size) {
	for (int rr=0; size>rr; ++rr) {
		for (int cc=0; size>cc; ++cc) {
			int ii=rr*stride+cc;
			destination[ii]=source0[ii]+source1[ii]+source2[ii]-source3[ii];
		}
	}
}

// Sets destination = source0 - source1.
void copySubtract(
		double *destination,
		double *source0,
		double *source1,
		int size) {
	for (int rr=0; size>rr; ++rr) {
		for (int cc=0; size>cc; ++cc) {
			int ii=rr*stride+cc;
			destination[ii]=source0[ii]-source1[ii];
		}
	}
}

int max(int value0, int value1) {
	return (value0>=value1)
			?value0
			:value1;
}

// The recursion of the multiplication.
// Matrices are laid out in row-major order.
// matrix0/size/stride and matrix1/size/stride are the input matrix blocks.
// matrix2/size/stride is the output matrix block.
// temp matrices are half wasted.
// The left-upper quarter of every one is used to hold temporary
// results used in this instance of the recursion.
// The left-lower querters are passed on to the lower levels of the recursion.
// The right half of temp matrices are unused.
void multiply(
		double *matrix0,
		double *matrix1,
		double *matrix2,
		double *temp1,
		double *temp2,
		double *temp3,
		double *temp4,
		double *temp5,
		double *temp6,
		double *temp7,
		double *temp8,
		double *temp9,
		int size) {
	if (0>=size) {
		// nothing to do
	}
	else if (1==size) {
		// all matrices are 1x1
		matrix2[0]=matrix0[0]*matrix1[0];
	}
	else {
		// halve all dimensions
		int halfSize=size/2;
		double *matrix011=matrix0;
		double *matrix021=matrix0+halfSize*stride;
		double *matrix012=matrix011+halfSize;
		double *matrix022=matrix021+halfSize;
		double *matrix111=matrix1;
		double *matrix121=matrix1+halfSize*stride;
		double *matrix112=matrix111+halfSize;
		double *matrix122=matrix121+halfSize;
		double *matrix211=matrix2;
		double *matrix221=matrix2+halfSize*stride;
		double *matrix212=matrix211+halfSize;
		double *matrix222=matrix221+halfSize;
		
		copyAdd(temp8, matrix011, matrix022, halfSize);
		copyAdd(temp9, matrix111, matrix122, halfSize);
		multiply(
			temp8,
			temp9,
			temp1,
			temp1+halfSize*stride,
			temp2+halfSize*stride,
			temp3+halfSize*stride,
			temp4+halfSize*stride,
			temp5+halfSize*stride,
			temp6+halfSize*stride,
			temp7+halfSize*stride,
			temp8+halfSize*stride,
			temp9+halfSize*stride,
			halfSize);
		
		copyAdd(temp8, matrix021, matrix022, halfSize);
		multiply(
			temp8,
			matrix111,
			temp2,
			temp1+halfSize*stride,
			temp2+halfSize*stride,
			temp3+halfSize*stride,
			temp4+halfSize*stride,
			temp5+halfSize*stride,
			temp6+halfSize*stride,
			temp7+halfSize*stride,
			temp8+halfSize*stride,
			temp9+halfSize*stride,
			halfSize);
		
		copySubtract(temp9, matrix112, matrix122, halfSize);
		multiply(
			matrix011,
			temp9,
			temp3,
			temp1+halfSize*stride,
			temp2+halfSize*stride,
			temp3+halfSize*stride,
			temp4+halfSize*stride,
			temp5+halfSize*stride,
			temp6+halfSize*stride,
			temp7+halfSize*stride,
			temp8+halfSize*stride,
			temp9+halfSize*stride,
			halfSize);
		
		copySubtract(temp9, matrix121, matrix111, halfSize);
		multiply(
			matrix022,
			temp9,
			temp4,
			temp1+halfSize*stride,
			temp2+halfSize*stride,
			temp3+halfSize*stride,
			temp4+halfSize*stride,
			temp5+halfSize*stride,
			temp6+halfSize*stride,
			temp7+halfSize*stride,
			temp8+halfSize*stride,
			temp9+halfSize*stride,
			halfSize);
		
		copyAdd(temp8, matrix011, matrix012, halfSize);
		multiply(
			temp8,
			matrix122,
			temp5,
			temp1+halfSize*stride,
			temp2+halfSize*stride,
			temp3+halfSize*stride,
			temp4+halfSize*stride,
			temp5+halfSize*stride,
			temp6+halfSize*stride,
			temp7+halfSize*stride,
			temp8+halfSize*stride,
			temp9+halfSize*stride,
			halfSize);
		
		copySubtract(temp8, matrix021, matrix011, halfSize);
		copyAdd(temp9, matrix111, matrix112, halfSize);
		multiply(
			temp8,
			temp9,
			temp6,
			temp1+halfSize*stride,
			temp2+halfSize*stride,
			temp3+halfSize*stride,
			temp4+halfSize*stride,
			temp5+halfSize*stride,
			temp6+halfSize*stride,
			temp7+halfSize*stride,
			temp8+halfSize*stride,
			temp9+halfSize*stride,
			halfSize);
		
		copySubtract(temp8, matrix012, matrix022, halfSize);
		copyAdd(temp9, matrix121, matrix122, halfSize);
		multiply(
			temp8,
			temp9,
			temp7,
			temp1+halfSize*stride,
			temp2+halfSize*stride,
			temp3+halfSize*stride,
			temp4+halfSize*stride,
			temp5+halfSize*stride,
			temp6+halfSize*stride,
			temp7+halfSize*stride,
			temp8+halfSize*stride,
			temp9+halfSize*stride,
			halfSize);
		
		copyAddAddSubtract(matrix211, temp1, temp4, temp7, temp5, halfSize);
		
		copyAdd(matrix212, temp3, temp5, halfSize);
		
		copyAdd(matrix221, temp2, temp4, halfSize);
		
		copyAddAddSubtract(matrix222, temp1, temp3, temp6, temp2, halfSize);
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
	
	// allocate 12 matrices
	double *matrix0=malloc(12*stride*stride*sizeof(double));
	if (0==matrix0) {
		exit(1);
		return;
	}
	double *matrix1=matrix0+stride*stride*sizeof(double);
	double *matrix2=matrix1+stride*stride*sizeof(double);
	double *temp1=matrix2+stride*stride*sizeof(double);
	double *temp2=temp1+stride*stride*sizeof(double);
	double *temp3=temp2+stride*stride*sizeof(double);
	double *temp4=temp3+stride*stride*sizeof(double);
	double *temp5=temp4+stride*stride*sizeof(double);
	double *temp6=temp5+stride*stride*sizeof(double);
	double *temp7=temp6+stride*stride*sizeof(double);
	double *temp8=temp7+stride*stride*sizeof(double);
	double *temp9=temp8+stride*stride*sizeof(double);
	
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
			temp1,
			temp2,
			temp3,
			temp4,
			temp5,
			temp6,
			temp7,
			temp8,
			temp9,
			stride);
	
	// print result
	for (int rr=0; size0>rr; ++rr) {
		for (int cc=0; size2>cc; ++cc) {
			write_double(matrix2[rr*stride+cc]);
		}
	}
	
	memory_access_log_disable();
	
	free(matrix0);
}

void zero(double *matrix, int size) {
	for (int rr=0; size>rr; ++rr) {
		for (int cc=0; size>cc; ++cc) {
			matrix[rr*stride+cc]=0.0;
		}
	}
}
