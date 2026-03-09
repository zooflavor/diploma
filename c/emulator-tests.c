#include "emulator.h"

typedef volatile uint16_t vuint16_t;
typedef volatile uint32_t vuint32_t;
typedef volatile uint64_t vuint64_t;
typedef volatile uint8_t vuint8_t;

struct casts {
    double doubleValue;   //  0
    float floatValue;     //  8
    int16_t int16Value;   // 12
    int32_t int32Value;   // 16
    int64_t int64Value;   // 24
    int8_t int8Value;     // 32
    void *ptrValue;       // 40
    uint16_t uint16Value; // 48
    uint32_t uint32Value; // 52
    uint64_t uint64Value; // 56
    uint8_t uint8Value;   // 64
};

#define ADD_ARRAY(name, type) \
    void name(type input0[], type input1[], type *output, uint32_t size) { \
        for (uint32_t ii=0; size>ii; ++ii) { \
            output[ii]=*(&(input0[13])+ii-13)+input1[ii]; \
        } \
    }

#define BINARY_OP(name, type, op) \
    type name(type value0, type value1) { \
        return value0 op value1; \
    }

#define CAST(name, type) \
    void name(struct casts *casts, type value) { \
        casts->doubleValue=value; \
        casts->floatValue=value; \
        casts->int16Value=value; \
        casts->int32Value=value; \
        casts->int64Value=value; \
        casts->int8Value=value; \
        casts->ptrValue=(void*)(uint64_t)value; \
        casts->uint16Value=value; \
        casts->uint32Value=value; \
        casts->uint64Value=value; \
        casts->uint8Value=value; \
    }

#define CONDITIONAL_EXPRESSION(name, type) \
    type name(type condition, type trueValue, type falseValue) { \
        return condition?trueValue:falseValue; \
    }

#define CONDITIONAL_STATEMENT1(name, type) \
    type name(type condition, type trueValue, type falseValue) { \
        type result; \
        if (condition) { \
            result=trueValue; \
        } \
        else { \
            result=falseValue; \
        } \
        return result; \
    }

#define CONDITIONAL_STATEMENT2(name, type) \
    type name(type condition, type trueValue, type falseValue) { \
        if (condition) { \
            return trueValue; \
        } \
        return falseValue; \
    }

#define CONST(name, type, value) \
    type name() { \
        return value; \
    }

#define NOT_NOT(name, type) \
    type name(type value) { \
        return !!value; \
    }

#define SHIFT(name, type, op) \
    type name(type value0, int32_t value1) { \
        return value0 op value1; \
    }

#define UNARY_OP(name, type, op) \
    type name(type value) { \
        return op value; \
    }

BINARY_OP(add_double, double,   +);
BINARY_OP(add_float,  float,    +);
BINARY_OP(add_int16,  int16_t,  +);
BINARY_OP(add_int32,  int32_t,  +);
BINARY_OP(add_int64,  int64_t,  +);
BINARY_OP(add_int8,   int8_t,   +);
BINARY_OP(add_uint16, uint16_t, +);
BINARY_OP(add_uint32, uint32_t, +);
BINARY_OP(add_uint64, uint64_t, +);
BINARY_OP(add_uint8,  uint8_t,  +);

ADD_ARRAY(add_array_double,  double)
ADD_ARRAY(add_array_float,  float)
ADD_ARRAY(add_array_int16,  int16_t)
ADD_ARRAY(add_array_int32,  int32_t)
ADD_ARRAY(add_array_int64,  int64_t)
ADD_ARRAY(add_array_int8,   int8_t)
ADD_ARRAY(add_array_uint16, uint16_t)
ADD_ARRAY(add_array_uint32, uint32_t)
ADD_ARRAY(add_array_uint64, uint64_t)
ADD_ARRAY(add_array_uint8,  uint8_t)

BINARY_OP(and_bitwise_int16,  int16_t,  &);
BINARY_OP(and_bitwise_int32,  int32_t,  &);
BINARY_OP(and_bitwise_int64,  int64_t,  &);
BINARY_OP(and_bitwise_int8,   int8_t,   &);
BINARY_OP(and_bitwise_uint16, uint16_t, &);
BINARY_OP(and_bitwise_uint32, uint32_t, &);
BINARY_OP(and_bitwise_uint64, uint64_t, &);
BINARY_OP(and_bitwise_uint8,  uint8_t,  &);

BINARY_OP(and_logical_double, double,   &&);
BINARY_OP(and_logical_float,  float,    &&);
BINARY_OP(and_logical_int16,  int16_t,  &&);
BINARY_OP(and_logical_int32,  int32_t,  &&);
BINARY_OP(and_logical_int64,  int64_t,  &&);
BINARY_OP(and_logical_int8,   int8_t,   &&);
BINARY_OP(and_logical_uint16, uint16_t, &&);
BINARY_OP(and_logical_uint32, uint32_t, &&);
BINARY_OP(and_logical_uint64, uint64_t, &&);
BINARY_OP(and_logical_uint8,  uint8_t,  &&);
void *and_logical_ptr(void *value0, void *value1) {
    return (void*)(uint64_t)(value0 && value1);
}

CAST(cast_double, double)
CAST(cast_float,  float)
CAST(cast_int16,  int16_t)
CAST(cast_int32,  int32_t)
CAST(cast_int64,  int64_t)
CAST(cast_int8,   int8_t)
CAST(cast_uint16, uint16_t)
CAST(cast_uint32, uint32_t)
CAST(cast_uint64, uint64_t)
CAST(cast_uint8,  uint8_t)
void cast_ptr(struct casts *casts, void *value) {
    casts->doubleValue=(uint64_t)value;
    casts->floatValue=(uint64_t)value;
    casts->int16Value=(uint64_t)value;
    casts->int32Value=(uint64_t)value;
    casts->int64Value=(uint64_t)value;
    casts->int8Value=(uint64_t)value;
    casts->ptrValue=value;
    casts->uint16Value=(uint64_t)value;
    casts->uint32Value=(uint64_t)value;
    casts->uint64Value=(uint64_t)value;
    casts->uint8Value=(uint64_t)value;
}

CONDITIONAL_EXPRESSION(conditional_expression_double, double);
CONDITIONAL_EXPRESSION(conditional_expression_float,  float);
CONDITIONAL_EXPRESSION(conditional_expression_int16,  int16_t);
CONDITIONAL_EXPRESSION(conditional_expression_int32,  int32_t);
CONDITIONAL_EXPRESSION(conditional_expression_int64,  int64_t);
CONDITIONAL_EXPRESSION(conditional_expression_int8,   int8_t);
CONDITIONAL_EXPRESSION(conditional_expression_ptr,    void*);
CONDITIONAL_EXPRESSION(conditional_expression_uint16, uint16_t);
CONDITIONAL_EXPRESSION(conditional_expression_uint32, uint32_t);
CONDITIONAL_EXPRESSION(conditional_expression_uint64, uint64_t);
CONDITIONAL_EXPRESSION(conditional_expression_uint8,  uint8_t);
CONDITIONAL_STATEMENT1(conditional_statement1_double, double);
CONDITIONAL_STATEMENT1(conditional_statement1_float,  float);
CONDITIONAL_STATEMENT1(conditional_statement1_int16,  int16_t);
CONDITIONAL_STATEMENT1(conditional_statement1_int32,  int32_t);
CONDITIONAL_STATEMENT1(conditional_statement1_int64,  int64_t);
CONDITIONAL_STATEMENT1(conditional_statement1_int8,   int8_t);
CONDITIONAL_STATEMENT1(conditional_statement1_ptr,    void*);
CONDITIONAL_STATEMENT1(conditional_statement1_uint16, uint16_t);
CONDITIONAL_STATEMENT1(conditional_statement1_uint32, uint32_t);
CONDITIONAL_STATEMENT1(conditional_statement1_uint64, uint64_t);
CONDITIONAL_STATEMENT1(conditional_statement1_uint8,  uint8_t);
CONDITIONAL_STATEMENT2(conditional_statement2_double, double);
CONDITIONAL_STATEMENT2(conditional_statement2_float,  float);
CONDITIONAL_STATEMENT2(conditional_statement2_int16,  int16_t);
CONDITIONAL_STATEMENT2(conditional_statement2_int32,  int32_t);
CONDITIONAL_STATEMENT2(conditional_statement2_int64,  int64_t);
CONDITIONAL_STATEMENT2(conditional_statement2_int8,   int8_t);
CONDITIONAL_STATEMENT2(conditional_statement2_ptr,    void*);
CONDITIONAL_STATEMENT2(conditional_statement2_uint16, uint16_t);
CONDITIONAL_STATEMENT2(conditional_statement2_uint32, uint32_t);
CONDITIONAL_STATEMENT2(conditional_statement2_uint64, uint64_t);
CONDITIONAL_STATEMENT2(conditional_statement2_uint8,  uint8_t);

CONST(const_double_0, double,  0.0L)
CONST(const_double_1, double,  -0.0L)
CONST(const_double_2, double,  1.0L)
CONST(const_double_3, double,  -1.0L)
CONST(const_double_4, double,  1.0L/0.0L)
CONST(const_double_5, double,  -1.0L/0.0L)
CONST(const_double_6, double,  333333.33333333333333333L)
CONST(const_double_7, double,  3.1415926535897932384626433L)
CONST(const_float_0,  float,   0.0)
CONST(const_float_1,  float,   -0.0)
CONST(const_float_2,  float,   1.0)
CONST(const_float_3,  float,   -1.0)
CONST(const_float_4,  float,   1.0/0.0)
CONST(const_float_5,  float,   -1.0/0.0)
CONST(const_float_6,  float,   333333.33333333333333333)
CONST(const_float_7,  float,   3.1415926535897932384626433)
CONST(const_int16_0,  int16_t, 0x0000)
CONST(const_int16_1,  int16_t, 0xffff)
CONST(const_int16_2,  int16_t, 0x0001)
CONST(const_int16_3,  int16_t, 0x8000)
CONST(const_int32_0,  int32_t, 0x00000000)
CONST(const_int32_1,  int32_t, 0xffffffff)
CONST(const_int32_2,  int32_t, 0x00000001)
CONST(const_int32_3,  int32_t, 0x80000000)
CONST(const_int64_0,  int64_t, 0x0000000000000000LL)
CONST(const_int64_1,  int64_t, 0xffffffffffffffffLL)
CONST(const_int64_2,  int64_t, 0x0000000000000001LL)
CONST(const_int64_3,  int64_t, 0x8000000000000000LL)
CONST(const_int8_0,   int8_t,  0x00)
CONST(const_int8_1,   int8_t,  0xff)
CONST(const_int8_2,   int8_t,  0x01)
CONST(const_int8_3,   int8_t,  0x80)
CONST(const_uint16_0, int16_t, 0x0000U)
CONST(const_uint16_1, int16_t, 0xffffU)
CONST(const_uint16_2, int16_t, 0x0001U)
CONST(const_uint16_3, int16_t, 0x8000U)
CONST(const_uint32_0, int32_t, 0x00000000U)
CONST(const_uint32_1, int32_t, 0xffffffffU)
CONST(const_uint32_2, int32_t, 0x00000001U)
CONST(const_uint32_3, int32_t, 0x80000000U)
CONST(const_uint64_0, int64_t, 0x0000000000000000ULL)
CONST(const_uint64_1, int64_t, 0xffffffffffffffffULL)
CONST(const_uint64_2, int64_t, 0x0000000000000001ULL)
CONST(const_uint64_3, int64_t, 0x8000000000000000ULL)
CONST(const_uint8_0,  int8_t,  0x00U)
CONST(const_uint8_1,  int8_t,  0xffU)
CONST(const_uint8_2,  int8_t,  0x01U)
CONST(const_uint8_3,  int8_t,  0x80U)
CONST(const_ptr_0,    void*,   0)
CONST(const_ptr_1,    void*,   (void*)0x0000000000000000ULL)
CONST(const_ptr_2,    void*,   (void*)0xffffffffffffffffULL)
CONST(const_ptr_3,    void*,   (void*)0x0000000000000001ULL)
CONST(const_ptr_4,    void*,   (void*)0x8000000000000000ULL)

BINARY_OP(divide_double, double,   /);
BINARY_OP(divide_float,  float,    /);
BINARY_OP(divide_int16,  int16_t,  /);
BINARY_OP(divide_int32,  int32_t,  /);
BINARY_OP(divide_int64,  int64_t,  /);
BINARY_OP(divide_int8,   int8_t,   /);
BINARY_OP(divide_uint16, uint16_t, /);
BINARY_OP(divide_uint32, uint32_t, /);
BINARY_OP(divide_uint64, uint64_t, /);
BINARY_OP(divide_uint8,  uint8_t,  /);

BINARY_OP(equal_double, double,   ==);
BINARY_OP(equal_float,  float,    ==);
BINARY_OP(equal_int16,  int16_t,  ==);
BINARY_OP(equal_int32,  int32_t,  ==);
BINARY_OP(equal_int64,  int64_t,  ==);
BINARY_OP(equal_int8,   int8_t,   ==);
BINARY_OP(equal_uint16, uint16_t, ==);
BINARY_OP(equal_uint32, uint32_t, ==);
BINARY_OP(equal_uint64, uint64_t, ==);
BINARY_OP(equal_uint8,  uint8_t,  ==);
void *equal_ptr(void *value0, void *value1) {
    return (void*)(uint64_t)(value0 == value1);
}

void exit_forever() {
    exit(13);
    while (1);
}

uint64_t factorial0(uint32_t value) {
    uint64_t result=1;
    for (; 0<value; --value) {
        result*=value;
    }
    return result;
}

uint64_t factorial1(uint32_t value) {
    uint64_t result=1;
    while (0<value) {
        result*=value--;
    }
    return result;
}

uint64_t factorial2(uint32_t value) {
    uint64_t result=1;
    while (1) {
        if (0==value) {
            break;
        }
        result*=value--;
    }
    return result;
}

uint64_t factorial3(uint32_t value) {
    uint64_t result=1;
    while (1) {
        if (0<value) {
            result*=value--;
            continue;
        }
        break;
    }
    return result;
}

uint64_t factorial4(uint32_t value) {
    uint64_t result=1;
    if (0<value) {
        do {
            result*=value--;
        } while (0<value);
    }
    return result;
}

uint64_t factorial5(uint32_t value) {
    uint64_t result=1;
    loop:
    if (0==value) {
        goto end;
    }
    result*=value--;
    goto loop;
    end:
    return result;
}

uint64_t factorial6(uint32_t value) {
    uint64_t result=1;
    loop:
    if (0==value) {
        return result;
    }
    result*=value--;
    goto loop;
}

uint64_t factorial7(uint32_t value) {
    if (0==value) {
        return 1;
    }
    return value*factorial7(value-1);
}

uint64_t factorial8rec(uint32_t value, uint64_t result);
uint64_t factorial8(uint32_t value) {
    return factorial8rec(value, 1);
}

uint64_t factorial8rec(uint32_t value, uint64_t result) {
    if (0==value) {
        return result;
    }
    return factorial8rec(value-1, result*value);
}

uint64_t factorial9(uint32_t value) {
    switch (value) {
        case 0:
        case 1:
            break;
        case 2:
            return 2;
        default:
            return value*(value-1)*(value-2)*factorial9(value-3);
    }
    return 1;
}

int64_t function_pointer_call(int64_t (*func)(int64_t, int64_t), int64_t value0, int64_t value1) {
    return func(value0, value1);
}

BINARY_OP(greater_double, double,   >);
BINARY_OP(greater_float,  float,    >);
BINARY_OP(greater_int16,  int16_t,  >);
BINARY_OP(greater_int32,  int32_t,  >);
BINARY_OP(greater_int64,  int64_t,  >);
BINARY_OP(greater_int8,   int8_t,   >);
BINARY_OP(greater_uint16, uint16_t, >);
BINARY_OP(greater_uint32, uint32_t, >);
BINARY_OP(greater_uint64, uint64_t, >);
BINARY_OP(greater_uint8,  uint8_t,  >);
void *greater_ptr(void *value0, void *value1) {
    return (void*)(uint64_t)(value0 > value1);
}

BINARY_OP(greater_or_equal_double, double,   >=);
BINARY_OP(greater_or_equal_float,  float,    >=);
BINARY_OP(greater_or_equal_int16,  int16_t,  >=);
BINARY_OP(greater_or_equal_int32,  int32_t,  >=);
BINARY_OP(greater_or_equal_int64,  int64_t,  >=);
BINARY_OP(greater_or_equal_int8,   int8_t,   >=);
BINARY_OP(greater_or_equal_uint16, uint16_t, >=);
BINARY_OP(greater_or_equal_uint32, uint32_t, >=);
BINARY_OP(greater_or_equal_uint64, uint64_t, >=);
BINARY_OP(greater_or_equal_uint8,  uint8_t,  >=);
void *greater_or_equal_ptr(void *value0, void *value1) {
    return (void*)(uint64_t)(value0 >= value1);
}

BINARY_OP(less_double, double,   <);
BINARY_OP(less_float,  float,    <);
BINARY_OP(less_int16,  int16_t,  <);
BINARY_OP(less_int32,  int32_t,  <);
BINARY_OP(less_int64,  int64_t,  <);
BINARY_OP(less_int8,   int8_t,   <);
BINARY_OP(less_uint16, uint16_t, <);
BINARY_OP(less_uint32, uint32_t, <);
BINARY_OP(less_uint64, uint64_t, <);
BINARY_OP(less_uint8,  uint8_t,  <);
void *less_ptr(void *value0, void *value1) {
    return (void*)(uint64_t)(value0 < value1);
}

BINARY_OP(less_or_equal_double, double,   <=);
BINARY_OP(less_or_equal_float,  float,    <=);
BINARY_OP(less_or_equal_int16,  int16_t,  <=);
BINARY_OP(less_or_equal_int32,  int32_t,  <=);
BINARY_OP(less_or_equal_int64,  int64_t,  <=);
BINARY_OP(less_or_equal_int8,   int8_t,   <=);
BINARY_OP(less_or_equal_uint16, uint16_t, <=);
BINARY_OP(less_or_equal_uint32, uint32_t, <=);
BINARY_OP(less_or_equal_uint64, uint64_t, <=);
BINARY_OP(less_or_equal_uint8,  uint8_t,  <=);
void *less_or_equal_ptr(void *value0, void *value1) {
    return (void*)(uint64_t)(value0 <= value1);
}

void memory_access(vuint16_t *p0, vuint32_t *p1, vuint64_t *p2, vuint8_t *p3) {
    memory_access_log_disable();
    for (int ii=0; 4>ii; ++ii) {
        if (0==(ii%2)) {
            memory_access_log_disable();
        }
        else {
            memory_access_log_enable();
        }
        memory_access_log_user_data(ii);
        for (int jj=7; 0<jj; --jj) {
            ++(*p0);
            ++(*p1);
            ++(*p2);
            ++(*p3);
        }
    }
    memory_access_log_disable();
}

BINARY_OP(multiply_double, double,   *);
BINARY_OP(multiply_float,  float,    *);
BINARY_OP(multiply_int16,  int16_t,  *);
BINARY_OP(multiply_int32,  int32_t,  *);
BINARY_OP(multiply_int64,  int64_t,  *);
BINARY_OP(multiply_int8,   int8_t,   *);
BINARY_OP(multiply_uint16, uint16_t, *);
BINARY_OP(multiply_uint32, uint32_t, *);
BINARY_OP(multiply_uint64, uint64_t, *);
BINARY_OP(multiply_uint8,  uint8_t,  *);

UNARY_OP(negate_double, double,   -)
UNARY_OP(negate_float,  float,    -)
UNARY_OP(negate_int16,  int16_t,  -)
UNARY_OP(negate_int32,  int32_t,  -)
UNARY_OP(negate_int64,  int64_t,  -)
UNARY_OP(negate_int8,   int8_t,   -)
UNARY_OP(negate_uint16, uint16_t, -)
UNARY_OP(negate_uint32, uint32_t, -)
UNARY_OP(negate_uint64, uint64_t, -)
UNARY_OP(negate_uint8,  uint8_t,  -)

BINARY_OP(not_equal_double, double,   !=);
BINARY_OP(not_equal_float,  float,    !=);
BINARY_OP(not_equal_int16,  int16_t,  !=);
BINARY_OP(not_equal_int32,  int32_t,  !=);
BINARY_OP(not_equal_int64,  int64_t,  !=);
BINARY_OP(not_equal_int8,   int8_t,   !=);
BINARY_OP(not_equal_uint16, uint16_t, !=);
BINARY_OP(not_equal_uint32, uint32_t, !=);
BINARY_OP(not_equal_uint64, uint64_t, !=);
BINARY_OP(not_equal_uint8,  uint8_t,  !=);
void *not_equal_ptr(void *value0, void *value1) {
    return (void*)(uint64_t)(value0 != value1);
}

UNARY_OP(not_bitwise_int16,  int16_t,  ~)
UNARY_OP(not_bitwise_int32,  int32_t,  ~)
UNARY_OP(not_bitwise_int64,  int64_t,  ~)
UNARY_OP(not_bitwise_int8,   int8_t,   ~)
UNARY_OP(not_bitwise_uint16, uint16_t, ~)
UNARY_OP(not_bitwise_uint32, uint32_t, ~)
UNARY_OP(not_bitwise_uint64, uint64_t, ~)
UNARY_OP(not_bitwise_uint8,  uint8_t,  ~)

UNARY_OP(not_logical_double, double,   !)
UNARY_OP(not_logical_float,  float,    !)
UNARY_OP(not_logical_int16,  int16_t,  !)
UNARY_OP(not_logical_int32,  int32_t,  !)
UNARY_OP(not_logical_int64,  int64_t,  !)
UNARY_OP(not_logical_int8,   int8_t,   !)
UNARY_OP(not_logical_uint16, uint16_t, !)
UNARY_OP(not_logical_uint32, uint32_t, !)
UNARY_OP(not_logical_uint64, uint64_t, !)
UNARY_OP(not_logical_uint8,  uint8_t,  !)
void *not_logical_ptr(void *value) {
    return (void*)(uint64_t)!value;
}

NOT_NOT(not_not_logical_double, double)
NOT_NOT(not_not_logical_float,  float)
NOT_NOT(not_not_logical_int16,  int16_t)
NOT_NOT(not_not_logical_int32,  int32_t)
NOT_NOT(not_not_logical_int64,  int64_t)
NOT_NOT(not_not_logical_int8,   int8_t)
NOT_NOT(not_not_logical_uint16, uint16_t)
NOT_NOT(not_not_logical_uint32, uint32_t)
NOT_NOT(not_not_logical_uint64, uint64_t)
NOT_NOT(not_not_logical_uint8,  uint8_t)
void *not_not_logical_ptr(void *value) {
    return (void*)(uint64_t)!!value;
}

BINARY_OP(or_bitwise_int16,  int16_t,  |);
BINARY_OP(or_bitwise_int32,  int32_t,  |);
BINARY_OP(or_bitwise_int64,  int64_t,  |);
BINARY_OP(or_bitwise_int8,   int8_t,   |);
BINARY_OP(or_bitwise_uint16, uint16_t, |);
BINARY_OP(or_bitwise_uint32, uint32_t, |);
BINARY_OP(or_bitwise_uint64, uint64_t, |);
BINARY_OP(or_bitwise_uint8,  uint8_t,  |);

BINARY_OP(or_logical_double, double,   ||);
BINARY_OP(or_logical_float,  float,    ||);
BINARY_OP(or_logical_int16,  int16_t,  ||);
BINARY_OP(or_logical_int32,  int32_t,  ||);
BINARY_OP(or_logical_int64,  int64_t,  ||);
BINARY_OP(or_logical_int8,   int8_t,   ||);
BINARY_OP(or_logical_uint16, uint16_t, ||);
BINARY_OP(or_logical_uint32, uint32_t, ||);
BINARY_OP(or_logical_uint64, uint64_t, ||);
BINARY_OP(or_logical_uint8,  uint8_t,  ||);
void *or_logical_ptr(void *value0, void *value1) {
    return (void*)(uint64_t)(value0 || value1);
}

double parameters_abi8(
        double a0, int64_t a1, float a2, int32_t a3,
        double a4, int64_t a5, float a6, int32_t a7,
        double xx) {
    double result=a7;
    result=result*xx+a6;
    result=result*xx+a5;
    result=result*xx+a4;
    result=result*xx+a3;
    result=result*xx+a2;
    result=result*xx+a1;
    return result*xx+a0;
}

double parameters_abi19(
        double a0, int64_t a1, float a2, int32_t a3,
        double a4, int64_t a5, float a6, int32_t a7,
        double a8, int64_t a9, float a10, int32_t a11,
        double a12, int64_t a13, float a14, int32_t a15,
        double a16, int64_t a17, float a18,
        double xx) {
    double result=a18;
    result=result*xx+a17;
    result=result*xx+a16;
    result=result*xx+a15;
    result=result*xx+a14;
    result=result*xx+a13;
    result=result*xx+a12;
    result=result*xx+a11;
    result=result*xx+a10;
    result=result*xx+a9;
    result=result*xx+a8;
    result=result*xx+a7;
    result=result*xx+a6;
    result=result*xx+a5;
    result=result*xx+a4;
    result=result*xx+a3;
    result=result*xx+a2;
    result=result*xx+a1;
    return result*xx+a0;
}

double parameters_abi20(
        double a0, int64_t a1, float a2, int32_t a3,
        double a4, int64_t a5, float a6, int32_t a7,
        double a8, int64_t a9, float a10, int32_t a11,
        double a12, int64_t a13, float a14, int32_t a15,
        double a16, int64_t a17, float a18, int32_t a19,
        double xx) {
    double result=a19;
    result=result*xx+a18;
    result=result*xx+a17;
    result=result*xx+a16;
    result=result*xx+a15;
    result=result*xx+a14;
    result=result*xx+a13;
    result=result*xx+a12;
    result=result*xx+a11;
    result=result*xx+a10;
    result=result*xx+a9;
    result=result*xx+a8;
    result=result*xx+a7;
    result=result*xx+a6;
    result=result*xx+a5;
    result=result*xx+a4;
    result=result*xx+a3;
    result=result*xx+a2;
    result=result*xx+a1;
    return result*xx+a0;
}

void read_input_write_output() {
    #define IOSIZE 256
    double doubles[IOSIZE];
    float floats[IOSIZE];
    int16_t int16s[IOSIZE];
    int32_t int32s[IOSIZE];
    int64_t int64s[IOSIZE];
    int8_t int8s[IOSIZE];
    uint16_t uint16s[IOSIZE];
    uint32_t uint32s[IOSIZE];
    uint64_t uint64s[IOSIZE];
    uint8_t uint8s[IOSIZE];
    for (int ii=0; IOSIZE>ii; ++ii) {
        doubles[ii]=read_double();
        floats[ii]=read_float();
        int16s[ii]=read_int16();
        int32s[ii]=read_int32();
        int64s[ii]=read_int64();
        int8s[ii]=read_int8();
        uint16s[ii]=read_uint16();
        uint32s[ii]=read_uint32();
        uint64s[ii]=read_uint64();
        uint8s[ii]=read_uint8();
    }
    for (int ii=IOSIZE-1; 0<=ii; --ii) {
        write_uint8(uint8s[ii]);
        write_uint64(uint64s[ii]);
        write_uint32(uint32s[ii]);
        write_uint16(uint16s[ii]);
        write_int8(int8s[ii]);
        write_int64(int64s[ii]);
        write_int32(int32s[ii]);
        write_int16(int16s[ii]);
        write_float(floats[ii]);
        write_double(doubles[ii]);
    }
    #undef IOSIZE
}

BINARY_OP(remainder_int16,  int16_t,  %);
BINARY_OP(remainder_int32,  int32_t,  %);
BINARY_OP(remainder_int64,  int64_t,  %);
BINARY_OP(remainder_int8,   int8_t,   %);
BINARY_OP(remainder_uint16, uint16_t, %);
BINARY_OP(remainder_uint32, uint32_t, %);
BINARY_OP(remainder_uint64, uint64_t, %);
BINARY_OP(remainder_uint8,  uint8_t,  %);

SHIFT(shift_left_int16,  int16_t,  <<)
SHIFT(shift_left_int32,  int32_t,  <<)
SHIFT(shift_left_int64,  int64_t,  <<)
SHIFT(shift_left_int8,   int8_t,   <<)
SHIFT(shift_left_uint16, uint16_t, <<)
SHIFT(shift_left_uint32, uint32_t, <<)
SHIFT(shift_left_uint64, uint64_t, <<)
SHIFT(shift_left_uint8,  uint8_t,  <<)

SHIFT(shift_right_int16,  int16_t,  >>)
SHIFT(shift_right_int32,  int32_t,  >>)
SHIFT(shift_right_int64,  int64_t,  >>)
SHIFT(shift_right_int8,   int8_t,   >>)
SHIFT(shift_right_uint16, uint16_t, >>)
SHIFT(shift_right_uint32, uint32_t, >>)
SHIFT(shift_right_uint64, uint64_t, >>)
SHIFT(shift_right_uint8,  uint8_t,  >>)

uint64_t sizeofs(uint32_t type) {
    switch (type) {
        case 0:
            return sizeof(char);
        case 1:
            return sizeof(short int);
        case 2:
            return sizeof(int);
        case 3:
            return sizeof(long int);
        case 4:
            return sizeof(float);
        case 5:
            return sizeof(double);
        case 6:
            return sizeof(void*);
        case 7:
            return sizeof(char*);
        case 8:
            return sizeof(int*);
        case 9:
            return sizeof(int (*)(int));
        case 10:
            return sizeof(int (**)(int));
        case 11:
            return sizeof(char[47]);
        case 12:
            return sizeof(short int[47]);
        case 13:
            return sizeof(int[47]);
        case 14:
            return sizeof(long int[47]);
        case 15:
            return sizeof(float[47]);
        case 16:
            return sizeof(double[47]);
        case 17:
            return sizeof(void*[47]);
        case 18:
            return sizeof(struct casts);
        default:
            return -1;
    }
}

uint64_t stack_overflow(uint64_t size) {
    if (0xffffffffffffffffULL==size) {
	return 0ULL;
    }
    if (0==malloc(size)) {
        size>>=1;
    }
    return 1+stack_overflow(size)+stack_overflow(size+1)+size;
}

int start() {
    return 0;
}

BINARY_OP(subtract_double, double,   -);
BINARY_OP(subtract_float,  float,    -);
BINARY_OP(subtract_int16,  int16_t,  -);
BINARY_OP(subtract_int32,  int32_t,  -);
BINARY_OP(subtract_int64,  int64_t,  -);
BINARY_OP(subtract_int8,   int8_t,   -);
BINARY_OP(subtract_uint16, uint16_t, -);
BINARY_OP(subtract_uint32, uint32_t, -);
BINARY_OP(subtract_uint64, uint64_t, -);
BINARY_OP(subtract_uint8,  uint8_t,  -);

BINARY_OP(xor_bitwise_int16,  int16_t,  ^);
BINARY_OP(xor_bitwise_int32,  int32_t,  ^);
BINARY_OP(xor_bitwise_int64,  int64_t,  ^);
BINARY_OP(xor_bitwise_int8,   int8_t,   ^);
BINARY_OP(xor_bitwise_uint16, uint16_t, ^);
BINARY_OP(xor_bitwise_uint32, uint32_t, ^);
BINARY_OP(xor_bitwise_uint64, uint64_t, ^);
BINARY_OP(xor_bitwise_uint8,  uint8_t,  ^);
