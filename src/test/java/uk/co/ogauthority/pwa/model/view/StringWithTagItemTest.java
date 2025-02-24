package uk.co.ogauthority.pwa.model.view;


import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StringWithTagItemTest {

  @Test
  void equalsAndHashcode(){
    EqualsVerifier.forClass(StringWithTagItem.class)
        .withNonnullFields("stringWithTag")
        .verify();
  }
}