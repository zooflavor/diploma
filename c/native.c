// implements the system functions of the emulator using the native host system

#include "emulator.h"
#include <stdio.h>

#pragma GCC diagnostic ignored "-Wunused-parameter"
int main(int argc, char *argv[]) {
	start();
	return 0;
}

void memory_access_log_disable() {
}

void memory_access_log_enable() {
}

#pragma GCC diagnostic ignored "-Wunused-parameter"
void memory_access_log_user_data(uint64_t data) {
}

double read_double() {
	double value;
	scanf("%lf", &value);
	return value;
}

float read_float() {
	float value;
	scanf("%f", &value);
	return value;
}

int16_t read_int16() {
	int16_t value;
	scanf("%"SCNd16, &value);
	return value;
}

int32_t read_int32() {
	int32_t value;
	scanf("%"SCNd32, &value);
	return value;
}

int64_t read_int64() {
	int64_t value;
	scanf("%"SCNd64, &value);
	return value;
}

int8_t read_int8() {
	int8_t value;
	scanf("%"SCNd8, &value);
	return value;
}

uint16_t read_uint16() {
	uint16_t value;
	scanf("%"SCNu16, &value);
	return value;
}

uint32_t read_uint32() {
	uint32_t value;
	scanf("%"SCNu32, &value);
	return value;
}

uint64_t read_uint64() {
	uint64_t value;
	scanf("%"SCNu64, &value);
	return value;
}

uint8_t read_uint8() {
	uint8_t value;
	scanf("%"SCNu8, &value);
	return value;
}

void write_double(double value) {
	printf("%f\n", value);
}

void write_float(float value) {
	printf("%f\n", value);
}

void write_int16(int16_t value) {
	printf("%"PRId16"\n", value);
}

void write_int32(int32_t value) {
	printf("%"PRId32"\n", value);
}

void write_int64(int64_t value) {
	printf("%"PRId64"\n", value);
}

void write_int8(int8_t value) {
	printf("%"PRId8"\n", value);
}

void write_uint16(uint16_t value) {
	printf("%"PRIu16"\n", value);
}

void write_uint32(uint32_t value) {
	printf("%"PRIu32"\n", value);
}

void write_uint64(uint64_t value) {
	printf("%"PRIu64"\n", value);
}

void write_uint8(uint8_t value) {
	printf("%"PRIu8"\n", value);
}
