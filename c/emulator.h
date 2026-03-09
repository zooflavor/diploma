#ifndef __EMULATOR_H__
#define __EMULATOR_H__ 1

#include <inttypes.h>

#ifdef EMULATED

#define malloc(size)                      (((void *(*)(uint64_t))0x1000UL)(size))
#define free(ptr)                         (((void (*)(void*))    0x1004UL)(ptr))

#define exit(code)                        (((void (*)(uint32_t)) 0x1010UL)(code))
#define exit_ok()                         (((void (*)())         0x1014UL)(code))

#define memory_access_log_disable()       (((void (*)())         0x1020UL)())
#define memory_access_log_enable()        (((void (*)())         0x1024UL)())
#define memory_access_log_user_data(data) (((void (*)(uint64_t)) 0x1028UL)(data))

#define read_double()                     (((double (*)())       0x1030UL)())
#define read_float()                      (((float (*)())        0x1034UL)())
#define read_int16()                      (((int16_t (*)())      0x1038UL)())
#define read_int32()                      (((int32_t (*)())      0x103cUL)())
#define read_int64()                      (((int64_t (*)())      0x1040UL)())
#define read_int8()                       (((int8_t (*)())       0x1044UL)())
#define read_uint16()                     (((uint16_t (*)())     0x1048UL)())
#define read_uint32()                     (((uint32_t (*)())     0x104cUL)())
#define read_uint64()                     (((uint64_t (*)())     0x1050UL)())
#define read_uint8()                      (((uint8_t (*)())      0x1054UL)())

#define write_double(value)               (((void (*)(double))   0x1060UL)(value))
#define write_float(value)                (((void (*)(float))    0x1064UL)(value))
#define write_int16(value)                (((void (*)(int16_t))  0x1068UL)(value))
#define write_int32(value)                (((void (*)(int32_t))  0x106cUL)(value))
#define write_int64(value)                (((void (*)(int64_t))  0x1070UL)(value))
#define write_int8(value)                 (((void (*)(int8_t))   0x1074UL)(value))
#define write_uint16(value)               (((void (*)(uint16_t)) 0x1078UL)(value))
#define write_uint32(value)               (((void (*)(uint32_t)) 0x107cUL)(value))
#define write_uint64(value)               (((void (*)(uint64_t)) 0x1080UL)(value))
#define write_uint8(value)                (((void (*)(uint8_t))  0x1084UL)(value))

#else

void *malloc(uint64_t size);
void free(void *ptr);

void exit(int code);
void exit_ok();

void memory_access_log_disable();
void memory_access_log_enable();
void memory_access_log_user_data(uint64_t data);

double read_double();
float read_float();
int16_t read_int16();
int32_t read_int32();
int64_t read_int64();
int8_t read_int8();
uint16_t read_uint16();
uint32_t read_uint32();
uint64_t read_uint64();
uint8_t read_uint8();

void write_double(double value);
void write_float(float value);
void write_int16(int16_t value);
void write_int32(int32_t value);
void write_int64(int64_t value);
void write_int8(int8_t value);
void write_uint16(uint16_t value);
void write_uint32(uint32_t value);
void write_uint64(uint64_t value);
void write_uint8(uint8_t value);

#endif

#endif
