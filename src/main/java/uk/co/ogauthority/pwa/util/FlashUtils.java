package uk.co.ogauthority.pwa.util;

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

}
