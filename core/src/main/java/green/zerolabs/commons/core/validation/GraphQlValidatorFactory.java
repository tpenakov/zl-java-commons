package green.zerolabs.commons.core.validation;

public interface GraphQlValidatorFactory {
  <T> GraphQlValidator<T> create(Class<T> clazz);
}
