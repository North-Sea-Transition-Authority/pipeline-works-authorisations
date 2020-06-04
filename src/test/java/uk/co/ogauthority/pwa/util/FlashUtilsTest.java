package uk.co.ogauthority.pwa.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

public class FlashUtilsTest {

  private RedirectAttributes redirectAttributes;

  @Before
  public void setUp() {
    redirectAttributes = new RedirectAttributesModelMap();
  }

  @Test
  public void success() {
    FlashUtils.success(redirectAttributes, "title", "message");
    assertThat((Map<String, String>) redirectAttributes.getFlashAttributes())
        .containsExactly(
            entry("flashClass", "fds-flash--green"),
            entry("flashTitle", "title"),
            entry("flashMessage", "message")
        );
  }

  @Test
  public void error() {
    FlashUtils.error(redirectAttributes, "title", "message");
    assertThat((Map<String, String>) redirectAttributes.getFlashAttributes())
        .containsExactly(
            entry("flashClass", "fds-flash--red"),
            entry("flashTitle", "title"),
            entry("flashMessage", "message")
        );
  }

  @Test
  public void info() {
    FlashUtils.info(redirectAttributes, "title", "message");
    assertThat((Map<String, String>) redirectAttributes.getFlashAttributes())
        .containsExactly(
            entry("flashTitle", "title"),
            entry("flashMessage", "message")
        );
  }
}