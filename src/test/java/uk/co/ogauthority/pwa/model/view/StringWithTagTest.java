package uk.co.ogauthority.pwa.model.view;


import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class StringWithTagTest {

  @Test
  public void equalsAndHashcode(){
    EqualsVerifier.forClass(StringWithTag.class)
        .verify();
  }
}