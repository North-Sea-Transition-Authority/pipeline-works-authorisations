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
 * Converts a String to a Collection.
 * This implementation differs from the default StringToCollectionConverter
 * as it does not split strings containing commas into multiple values.
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
}
