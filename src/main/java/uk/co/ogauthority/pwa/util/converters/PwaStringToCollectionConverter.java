package uk.co.ogauthority.pwa.util.converters;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import org.springframework.core.CollectionFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.lang.Nullable;

/**
 * Modified version of the default Spring StringToCollectionConverter which does not split strings containing commas
 * into multiple values.
 * https://github.com/spring-projects/spring-framework/blob/main/spring-core/src/main/java/org/springframework/core/convert/support/StringToCollectionConverter.java
 */
public class PwaStringToCollectionConverter implements ConditionalGenericConverter {

  private final ConversionService conversionService;

  public PwaStringToCollectionConverter(ConversionService conversionService) {
    this.conversionService = conversionService;
  }

  @Override
  public Set<ConvertiblePair> getConvertibleTypes() {
    return Collections.singleton(new ConvertiblePair(String.class, Collection.class));
  }

  @Override
  public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
    return (targetType.getElementTypeDescriptor() == null
        || this.conversionService.canConvert(sourceType, targetType.getElementTypeDescriptor()));
  }

  @Override
  @Nullable
  public Object convert(@Nullable Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
    if (source == null) {
      return null;
    }
    var intCollection = convertToInt(source, targetType);
    if (intCollection != null) {
      return intCollection;
    }
    return convertToSingleString(source, sourceType, targetType);
  }

  private Object convertToSingleString(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
    String string = (String) source;

    TypeDescriptor elementDesc = targetType.getElementTypeDescriptor();
    Collection<Object> target = CollectionFactory.createCollection(targetType.getType(),
        (elementDesc != null ? elementDesc.getType() : null), 1);

    // StringUtils.commaDelimitedListToStringArray() returns an empty array for strings with length 0, so we should
    // replicate that behaviour.
    if (string.isEmpty()) {
      return target;
    }
    if (elementDesc == null) {
      target.add(string.trim());
    } else {
      Object targetElement = this.conversionService.convert(string.trim(), sourceType, elementDesc);
      target.add(targetElement);
    }
    return target;
  }

  private Object convertToInt(Object source, TypeDescriptor targetType) {
    String sourceString = (String) source;
    var splitSource = sourceString.split(",");
    var elementDesc = targetType.getElementTypeDescriptor();
    var target = CollectionFactory.createCollection(targetType.getType(),
        (elementDesc != null ? elementDesc.getType() : null), 1);
    for (var split : splitSource) {
      try {
        target.add(Integer.parseInt(split));
      } catch (NumberFormatException e) {
        return null;
      }
    }
    return target;
  }
}
