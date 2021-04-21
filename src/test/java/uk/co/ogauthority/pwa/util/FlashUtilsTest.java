package uk.co.ogauthority.pwa.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.List;
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
  public void success_onlyTitle() {
    FlashUtils.success(redirectAttributes, "title");
    assertThat(getFlashAttributes())
        .containsExactly(
            entry("flashClass", "fds-flash--green"),
            entry("flashTitle", "title"),
            entry("flashMessage", null)
        );
  }

  @Test
  public void success_titleAndMessage() {
    FlashUtils.success(redirectAttributes, "title", "message");
    assertThat(getFlashAttributes())
        .containsExactly(
            entry("flashClass", "fds-flash--green"),
            entry("flashTitle", "title"),
            entry("flashMessage", "message")
        );
  }

  @Test
  public void error_onlyTitle() {
    FlashUtils.error(redirectAttributes, "title");
    assertThat(getFlashAttributes())
        .containsExactly(
            entry("flashClass", "fds-flash--red"),
            entry("flashTitle", "title"),
            entry("flashMessage", null)
        );
  }

  @Test
  public void error_titleAndMessage() {
    FlashUtils.error(redirectAttributes, "title", "message");
    assertThat(getFlashAttributes())
        .containsExactly(
            entry("flashClass", "fds-flash--red"),
            entry("flashTitle", "title"),
            entry("flashMessage", "message")
        );
  }

  @Test
  public void errorWithBulletPoints_setsAllAttributes() {
    FlashUtils.errorWithBulletPoints(redirectAttributes, "title", "message", List.of("123", "abc"));
    assertThat(getFlashAttributes())
        .containsExactly(
            entry("flashClass", "fds-flash--red"),
            entry("flashTitle", "title"),
            entry("flashMessage", "message"),
            entry("flashBulletList", List.of("123", "abc"))
        );
  }

  @Test
  public void info_onlyTitle() {
    FlashUtils.info(redirectAttributes, "title");
    assertThat(getFlashAttributes())
        .containsExactly(
            entry("flashTitle", "title"),
            entry("flashMessage", null)
        );
  }

  @Test
  public void info_titleAndMessage() {
    FlashUtils.info(redirectAttributes, "title", "message");
    assertThat(getFlashAttributes())
        .containsExactly(
            entry("flashTitle", "title"),
            entry("flashMessage", "message")
        );
  }

  @Test
  public void reFlashIfExists_flashExists() {

    var flashMap = Map.of(
        "flashTitle", "title",
        "flashMessage", "message",
        "flashClass", "success"
    );

    FlashUtils.reFlashIfExists(flashMap, redirectAttributes);

    assertThat(getFlashAttributes())
        .containsExactlyInAnyOrderEntriesOf(flashMap);

  }

  @Test
  public void reFlashIfExists_flashDoesntExist() {

    FlashUtils.reFlashIfExists(Map.of(), redirectAttributes);

    assertThat(getFlashAttributes())
        .isEmpty();

  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> getFlashAttributes() {
    return (Map<String, Object>) redirectAttributes.getFlashAttributes();
  }
}