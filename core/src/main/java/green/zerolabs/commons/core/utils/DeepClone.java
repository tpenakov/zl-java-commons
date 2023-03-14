package green.zerolabs.commons.core.utils;

/***
 * Created by Triphon Penakov 2023-03-06
 */
public interface DeepClone {
    <T> T deepClone(Object input, Class<T> resultType);
}
