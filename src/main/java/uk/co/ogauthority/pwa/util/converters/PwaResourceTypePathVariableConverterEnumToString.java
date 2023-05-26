package uk.co.ogauthority.pwa.util.converters;


import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalConverter;
import org.springframework.core.convert.converter.Converter;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;

/**
 * Conditionally converts between a string used in urls to differentiate application types and the enum value.
 * Only do conversion when source is annotated with @ApplicationTypeUrl
 */
public class PwaResourceTypePathVariableConverterEnumToString implements Converter<PwaResourceType, String>, ConditionalConverter {

  @Override
  public String convert(@ResourceTypeUrl PwaResourceType source) {
    return source.name().toLowerCase();
  }

  @Override
  public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
    return sourceType.hasAnnotation(ResourceTypeUrl.class);
  }
}
