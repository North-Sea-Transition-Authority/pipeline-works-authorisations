package uk.co.ogauthority.pwa.util.converters;


import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalConverter;
import org.springframework.core.convert.converter.Converter;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;

/**
 * Conditionally converts between a string used in urls to differentiate resource types and the enum value.
 * Only do conversion when target is annotated with @ResourceTypeUrl
 */
public class PwaResourceTypePathVariableConverterStringToEnum implements Converter<String, PwaResourceType>, ConditionalConverter {

  @Override
  public PwaResourceType convert(String source) {
    return PwaResourceType.valueOf(source.toUpperCase());
  }

  @Override
  public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
    return targetType.hasAnnotation(ResourceTypeUrl.class);
  }
}
