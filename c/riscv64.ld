/*
Linker script for freestanding linking.
*/

ENTRY(start)

SECTIONS
{
    . = 0x10000;

    .text (READONLY) : ALIGN(CONSTANT (COMMONPAGESIZE))
    {
        *(.text*)
    }

    .rodata (READONLY) : ALIGN(CONSTANT (COMMONPAGESIZE))
    {
        *(.rodata)
    }

    . = ALIGN (CONSTANT (COMMONPAGESIZE));

    .data : ALIGN(CONSTANT (COMMONPAGESIZE))
    {
        *(.data)
    }

    .bss : ALIGN(CONSTANT (COMMONPAGESIZE))
    {
        *(COMMON)
        *(.bss)
    }
}

