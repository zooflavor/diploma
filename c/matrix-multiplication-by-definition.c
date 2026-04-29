// Matrix multiplication by definition.

#include "emulator.h"

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
	
	// compute result
	for (int rr=0; size0>rr; ++rr) {
		for (int cc=0; size2>cc; ++cc) {
			double dd=0.0;
			for (int ii=0; size1>ii; ++ii) {
				dd+=matrix0[rr*size1+ii]*matrix1[ii*size2+cc];
			}
			matrix2[rr*size2+cc]=dd;
		}
	}
	
	// print result
	for (int ii=0; size0*size2>ii; ++ii) {
		write_double(matrix2[ii]);
	}
	
	memory_access_log_disable();
	
	free(matrix0);
}
