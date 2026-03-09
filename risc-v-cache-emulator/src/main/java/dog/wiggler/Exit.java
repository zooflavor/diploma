package dog.wiggler;

public class Exit {
    private Integer code;

    public void clear() {
        code=null;
    }

    public int code() {
        return code;
    }

    public boolean set() {
        return null!=code;
    }

    public void set(int code) {
        this.code=code;
    }

    public void setOk() {
        set(0);
    }
}
