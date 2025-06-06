package uk.co.ogauthority.pwa.util.converters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;

@ExtendWith(MockitoExtension.class)
class PwaStringToCollectionConverterTest {

  @Mock
  private ConversionService conversionService;

  private PwaStringToCollectionConverter pwaStringToCollectionConverter;

  @BeforeEach
  void setup() {
    pwaStringToCollectionConverter = new PwaStringToCollectionConverter(conversionService);
  }

  @Test
  void converter_convertTextArea_ToString() {
    var testString = "This is a test string, it contains commas as if it came from a test area";
    var sourceType = TypeDescriptor.forObject(testString);
    var destinationType = TypeDescriptor.collection(List.class, TypeDescriptor.forObject(testString));

    var convertedString = pwaStringToCollectionConverter.convert(testString, sourceType, destinationType);
    assertTrue(convertedString instanceof List);
    assertThat(((List<?>) convertedString)).hasSize(1);
  }

  @Test
  void converter_convertTextArea_ToInts() {
    var testString = "1,2,3,4";
    var sourceType = TypeDescriptor.forObject(testString);
    var destinationType = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(Integer.class));

    var convertedString = pwaStringToCollectionConverter.convert(testString, sourceType, destinationType);
    assertTrue(convertedString instanceof List);
    assertThat(((List<?>) convertedString)).hasSize(4);
  }
}
