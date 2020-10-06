package uk.co.ogauthority.pwa.model.form.location;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
public class CoordinateFormTest {

  @Test
  public void setLatitudeSeconds_noScale_scaleSet() {

    var form = new CoordinateForm();
    form.setLatitudeSeconds(BigDecimal.valueOf(5));

    assertThat(form.getLatitudeSeconds()).isEqualTo(new BigDecimal("5.00"));
    assertThat(form.getLatitudeSeconds().scale()).isEqualTo(2);

  }

  @Test
  public void setLatitudeSeconds_withScale_unchanged() {

    var form = new CoordinateForm();

    form.setLatitudeSeconds(BigDecimal.valueOf(5.1));
    assertThat(form.getLatitudeSeconds()).isEqualTo(BigDecimal.valueOf(5.1));
    assertThat(form.getLatitudeSeconds().scale()).isEqualTo(1);

    form.setLatitudeSeconds(BigDecimal.valueOf(5.11));
    assertThat(form.getLatitudeSeconds()).isEqualTo(BigDecimal.valueOf(5.11));
    assertThat(form.getLatitudeSeconds().scale()).isEqualTo(2);

    form.setLatitudeSeconds(BigDecimal.valueOf(5.111));
    assertThat(form.getLatitudeSeconds()).isEqualTo(BigDecimal.valueOf(5.111));
    assertThat(form.getLatitudeSeconds().scale()).isEqualTo(3);

  }

  @Test
  public void setLongitudeSeconds_noScale_scaleSet() {

    var form = new CoordinateForm();
    form.setLongitudeSeconds(BigDecimal.valueOf(5));

    assertThat(form.getLongitudeSeconds()).isEqualTo(new BigDecimal("5.00"));
    assertThat(form.getLongitudeSeconds().scale()).isEqualTo(2);

  }

  @Test
  public void setLongitudeSeconds_withScale_unchanged() {

    var form = new CoordinateForm();

    form.setLongitudeSeconds(BigDecimal.valueOf(5.1));
    assertThat(form.getLongitudeSeconds()).isEqualTo(BigDecimal.valueOf(5.1));
    assertThat(form.getLongitudeSeconds().scale()).isEqualTo(1);

    form.setLongitudeSeconds(BigDecimal.valueOf(5.11));
    assertThat(form.getLongitudeSeconds()).isEqualTo(BigDecimal.valueOf(5.11));
    assertThat(form.getLongitudeSeconds().scale()).isEqualTo(2);

    form.setLongitudeSeconds(BigDecimal.valueOf(5.111));
    assertThat(form.getLongitudeSeconds()).isEqualTo(BigDecimal.valueOf(5.111));
    assertThat(form.getLongitudeSeconds().scale()).isEqualTo(3);

  }

}
