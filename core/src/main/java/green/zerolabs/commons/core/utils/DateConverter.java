package green.zerolabs.commons.core.utils;

/***
 * Created by Triphon Penakov 2023-03-08
 */
public interface DateConverter {
  String epochMilliToString(Long millis);

  Long epochStringToMilli(String instant);

  Long toEpochMilli(String instant);
}
