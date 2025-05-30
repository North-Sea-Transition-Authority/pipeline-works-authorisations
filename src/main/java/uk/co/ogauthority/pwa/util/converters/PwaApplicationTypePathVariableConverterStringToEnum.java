package uk.co.ogauthority.pwa.util.converters;


import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalConverter;
import org.springframework.core.convert.converter.Converter;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;

/**
 * Conditionally converts between a string used in urls to differentiate application types and the enum value.
 * Only do conversion when target is annotated with @ApplicationTypeUrl
 */
public class PwaApplicationTypePathVariableConverterStringToEnum implements Converter<String, PwaApplicationType>, ConditionalConverter {

  @Override
  public PwaApplicationType convert(String source) {
    return PwaApplicationType.getFromUrlPathString(source)
        .orElseThrow(() -> new IllegalArgumentException(String.format("could not map %s to application type", source)));
  }

  @Override
  public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
    return targetType.hasAnnotation(ApplicationTypeUrl.class);
  }
}