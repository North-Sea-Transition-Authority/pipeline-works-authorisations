package uk.co.ogauthority.pwa.model.view;


import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StringWithTagTest {

  @Test
  void equalsAndHashcode(){
    EqualsVerifier.forClass(StringWithTag.class)
        .verify();
  }
}