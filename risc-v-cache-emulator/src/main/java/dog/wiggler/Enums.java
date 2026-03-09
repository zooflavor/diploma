package dog.wiggler;

import dog.wiggler.function.Function;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Enums {
    private Enums() {
    }

    public static <E extends Enum<E>, K> Map<K, E> valueMap(
            Function<? super E, ? extends K> keySelector, Class<E> enumType) {
        try {
            Map<K, E> map=new HashMap<>();
            for (Object oo: (Object[])enumType.getMethod("values").invoke(null)) {
                E ee=enumType.cast(oo);
                K kk=keySelector.apply(ee);
                map.put(kk, ee);
            }
            return Collections.unmodifiableMap(new HashMap<>(map));
        }
        catch (Error|RuntimeException ex) {
            throw ex;
        }
        catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }
}
