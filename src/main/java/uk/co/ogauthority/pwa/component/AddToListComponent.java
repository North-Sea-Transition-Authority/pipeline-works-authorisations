package uk.co.ogauthority.pwa.component;

public interface AddToListComponent {

  /** Returns the ID of the object,
   * this will be sent to the form when added to the list.
   * @return the object identifier.
   */
  public String getId();

  /** Return the display name of the object,
   * this will be displayed on the screen for the user to identify the object.
   * @return the object display name.
   */
  public String getName();

  /** This method evaluates if the item selected is valid,
   * invalid items will be displayed as form errors to the user.
   * @return if the object is valid.
   */
  public Boolean isValid();
}
