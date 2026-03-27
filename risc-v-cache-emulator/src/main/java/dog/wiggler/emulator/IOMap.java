package dog.wiggler.emulator;

/**
 * Entry points for system calls.
 * These values must much the system calls in emulator.h.
 */
public class IOMap {
    public static final long MALLOC                     =0x1000L;
    public static final long FREE                       =0x1004L;

    public static final long EXIT                       =0x1010L;
    public static final long EXIT_OK                    =0x1014L;

    public static final long MEMORY_ACCESS_LOG_DISABLE  =0x1020L;
    public static final long MEMORY_ACCESS_LOG_ENABLE   =0x1024L;
    public static final long MEMORY_ACCESS_LOG_USER_DATA=0x1028L;

    public static final long READ_DOUBLE                =0x1030L;
    public static final long READ_FLOAT                 =0x1034L;
    public static final long READ_INT16                 =0x1038L;
    public static final long READ_INT32                 =0x103cL;
    public static final long READ_INT64                 =0x1040L;
    public static final long READ_INT8                  =0x1044L;
    public static final long READ_UINT16                =0x1048L;
    public static final long READ_UINT32                =0x104cL;
    public static final long READ_UINT64                =0x1050L;
    public static final long READ_UINT8                 =0x1054L;

    public static final long WRITE_DOUBLE               =0x1060L;
    public static final long WRITE_FLOAT                =0x1064L;
    public static final long WRITE_INT16                =0x1068L;
    public static final long WRITE_INT32                =0x106cL;
    public static final long WRITE_INT64                =0x1070L;
    public static final long WRITE_INT8                 =0x1074L;
    public static final long WRITE_UINT16               =0x1078L;
    public static final long WRITE_UINT32               =0x107cL;
    public static final long WRITE_UINT64               =0x1080L;
    public static final long WRITE_UINT8                =0x1084L;

    private IOMap() {
    }
}
