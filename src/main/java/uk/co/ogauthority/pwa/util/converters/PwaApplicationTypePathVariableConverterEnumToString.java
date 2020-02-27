package uk.co.ogauthority.pwa.util.converters;


import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalConverter;
import org.springframework.core.convert.converter.Converter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;

/**
 * Conditionally converts between a string used in urls to differentiate application types and the enum value.
 * Only do conversion when source is annotated with @ApplicationTypeUrl
 */
public class PwaApplicationTypePathVariableConverterEnumToString implements Converter<PwaApplicationType, String>, ConditionalConverter {

  @Override
  public String convert(@ApplicationTypeUrl PwaApplicationType source) {
    return source.getUrlPathString();
  }

  @Override
  public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
    return sourceType.hasAnnotation(ApplicationTypeUrl.class);
  }
}