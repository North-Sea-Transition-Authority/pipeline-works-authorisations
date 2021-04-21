package uk.co.ogauthority.pwa.util;

import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Allows a flash card to display above the page header with the corresponding border:
 * success: green
 * error: red
 * info: blue
 * A flash lasts for a single redirect.
 * An endpoint must have a parameter of type RedirectAttributes in order to use this util class.
 */
public class FlashUtils {

  public static void success(RedirectAttributes redirectAttributes, String title) {
    redirectAttributes.addFlashAttribute("flashClass", "fds-flash--green");
    addTextAttributes(redirectAttributes, title, null);
  }

  public static void success(RedirectAttributes redirectAttributes, String title, String message) {
    redirectAttributes.addFlashAttribute("flashClass", "fds-flash--green");
    addTextAttributes(redirectAttributes, title, message);
  }

  public static void error(RedirectAttributes redirectAttributes, String title) {
    redirectAttributes.addFlashAttribute("flashClass", "fds-flash--red");
    addTextAttributes(redirectAttributes, title, null);
  }

  public static void error(RedirectAttributes redirectAttributes, String title, String message) {
    redirectAttributes.addFlashAttribute("flashClass", "fds-flash--red");
    addTextAttributes(redirectAttributes, title, message);
  }

  public static void errorWithBulletPoints(RedirectAttributes redirectAttributes, String title, String message, List<String> bulletPoints) {
    redirectAttributes.addFlashAttribute("flashClass", "fds-flash--red");
    addTextAttributes(redirectAttributes, title, message);
    addBulletListAttribute(redirectAttributes, bulletPoints);
  }

  public static void info(RedirectAttributes redirectAttributes, String title) {
    addTextAttributes(redirectAttributes, title, null);
  }

  public static void info(RedirectAttributes redirectAttributes, String title, String message) {
    addTextAttributes(redirectAttributes, title, message);
  }

  private static void addTextAttributes(RedirectAttributes redirectAttributes, String title, String message) {
    redirectAttributes.addFlashAttribute("flashTitle", title);
    redirectAttributes.addFlashAttribute("flashMessage", message);
  }

  private static void addBulletListAttribute(RedirectAttributes redirectAttributes, List<String> bulletPoints) {
    redirectAttributes.addFlashAttribute("flashBulletList",  bulletPoints);
  }

  /**
   * If a flash message was added by the previous request, flash it again so it is available to the next request.
   * @param inputFlashMap map of flash attributes attached to the request
   * @param redirectAttributes to add flash to
   */
  public static void reFlashIfExists(@Nullable Map<String, ?> inputFlashMap,
                                     RedirectAttributes redirectAttributes) {

    if (inputFlashMap != null) {

      inputFlashMap.entrySet().stream()
          .filter(entry -> List.of("flashTitle", "flashMessage", "flashClass", "flashBulletList").contains(entry.getKey()))
          .forEach(entry -> redirectAttributes.addFlashAttribute(entry.getKey(), entry.getValue()));

    }

  }
}
