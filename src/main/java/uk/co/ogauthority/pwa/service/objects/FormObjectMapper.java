package uk.co.ogauthority.pwa.service.objects;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;

// This was adapted from this stack overflow post: https://stackoverflow.com/questions/6133660/recursive-beanutils-describe
public class FormObjectMapper {

  private FormObjectMapper() {
    throw new AssertionError();
  }

  public static Map<String, String> toMap(Object object) {
    return toMap(object, null, new HashSet<>());
  }

  private static Map<String, String> toMap(Object object, String prefix, Set<Object> cache) {
    if (object == null || cache.contains(object)) {
      return Collections.emptyMap();
    }
    cache.add(object);
    prefix = (prefix != null) ? prefix + "." : "";

    Map<String, String> beanMap = new TreeMap<>();

    Map<String, Object> properties = getProperties(object);
    for (String property : properties.keySet()) {
      Object value = properties.get(property);
      // ignore nulls
      if (value != null) {
        if (Collection.class.isAssignableFrom(value.getClass())) {
          beanMap.putAll(convertCollection((Collection<?>) value, prefix + property, cache));
        } else if (value.getClass().isArray()) {
          beanMap.putAll(convertCollection(Arrays.asList((Object[]) value), prefix + property, cache));
        } else if (Map.class.isAssignableFrom(value.getClass())) {
          beanMap.putAll(convertMap((Map<?, ?>) value, prefix + property, cache));
        } else {
          beanMap.putAll(convertObject(value, prefix + property, cache));
        }
      }
    }
    return beanMap;
  }

  private static Map<String, Object> getProperties(Object object) {
    Map<String, Object> propertyMap = getFieldProperties(object);
    // getters take precedence in case of any name collisions
    propertyMap.putAll(getGetterMethodProperties(object));
    return propertyMap;
  }

  private static Map<String, Object> getGetterMethodProperties(Object object) {
    Map<String, Object> result = new HashMap<>();
    BeanInfo info;
    try {
      info = Introspector.getBeanInfo(object.getClass());
      for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
        Method reader = pd.getReadMethod();
        if (reader != null) {
          String name = pd.getName();
          if (!"class".equals(name)) {
            try {
              Object value = reader.invoke(object);
              result.put(name, value);
            } catch (IllegalAccessException | InvocationTargetException e) {
              throw new RuntimeException("Exception accessing property " + name + " of " + object.getClass().getName(), e);
            }
          }
        }
      }
    } catch (IntrospectionException e) {
      throw new RuntimeException("Exception introspecting class " + object.getClass().getName(), e);
    }
    return result;
  }

  private static Map<String, Object> getFieldProperties(Object object) {
    return getFieldProperties(object, object.getClass());
  }

  private static Map<String, Object> getFieldProperties(Object object, Class<?> classType) {
    Map<String, Object> result = new HashMap<>();

    Class superClass = classType.getSuperclass();
    if (superClass != null) {
      result.putAll(getFieldProperties(object, superClass));
    }

    // get public fields only
    Field[] fields = classType.getFields();
    for (Field field : fields) {
      try {
        result.put(field.getName(), field.get(object));
      } catch (IllegalAccessException e) {
        throw new RuntimeException("Exception accessing property " + field.getName() + " of " + object.getClass().getName(), e);
      }
    }
    return result;
  }

  private static Map<String, String> convertCollection(Collection<?> values, String key, Set<Object> cache) {
    Map<String, String> valuesMap = new HashMap<>();
    Object[] valArray = values.toArray();
    for (int i = 0; i < valArray.length; i++) {
      Object value = valArray[i];
      if (value != null) {
        valuesMap.putAll(convertObject(value, key + "[" + i + "]", cache));
      }
    }
    return valuesMap;
  }

  private static Map<String, String> convertMap(Map<?, ?> values, String key, Set<Object> cache) {
    Map<String, String> valuesMap = new HashMap<>();
    for (Object thisKey : values.keySet()) {
      Object value = values.get(thisKey);
      if (value != null) {
        valuesMap.putAll(convertObject(value, key + "[" + thisKey + "]", cache));
      }
    }
    return valuesMap;
  }

  private static Map<String, String> convertObject(Object value, String key, Set<Object> cache) {
    // if this type has a registered converted, then get the string and return
    ConversionService conversionService = DefaultConversionService.getSharedInstance();
    if (conversionService.canConvert(value.getClass(), String.class)) {
      String stringValue = conversionService.convert(value, String.class);
      Map<String, String> valueMap = new HashMap<>();
      valueMap.put(key, stringValue);
      return valueMap;
    } else {
      // otherwise, treat it as a nested bean that needs to be described itself
      return toMap(value, key, cache);
    }
  }

}
