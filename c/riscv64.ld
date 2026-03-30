ENTRY(start)

SECTIONS
{
    . = 0x10000;

    .text (READONLY) : ALIGN(4K)
    {
        *(.text*)
    }

    .rodata (READONLY) : ALIGN(4K)
    {
        *(.rodata)
    }

    . = ALIGN (CONSTANT (COMMONPAGESIZE));

    .data : ALIGN(4K)
    {
        *(.data)
    }

    .bss : ALIGN(4K)
    {
        *(COMMON)
        *(.bss)
    }
}

