package uk.co.ogauthority.pwa.temp;

import java.io.Serializable;
import java.lang.reflect.Field;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.co.ogauthority.pwa.temp.model.form.AdministrativeDetailsForm;
import uk.co.ogauthority.pwa.temp.model.form.LocationForm;
import uk.co.ogauthority.pwa.temp.model.form.ProjectInformationForm;

@Component
@Scope("session")
public class FormState implements Serializable {

  private final AdministrativeDetailsForm administrativeDetailsForm;
  private final ProjectInformationForm projectInformationForm;
  private final LocationForm locationForm;

  public FormState() {
    administrativeDetailsForm = new AdministrativeDetailsForm();
    projectInformationForm = new ProjectInformationForm();
    locationForm = new LocationForm();
  }

  private void saveForm(Object form) {
    try {
      for (Field scopeField : this.getClass().getDeclaredFields()) {
        if (scopeField.getType().equals(form.getClass())) {
          scopeField.setAccessible(true);
          var scopedFieldForm = scopeField.get(this);
          for (Field formField : form.getClass().getDeclaredFields()) {
            formField.setAccessible(true);
            var formFieldSetValue = formField.get(form);
            formField.set(scopedFieldForm, formFieldSetValue);
          }
        }
      }
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
  }

  public void save(AdministrativeDetailsForm administrativeDetailsForm) {
    saveForm(administrativeDetailsForm);
  }

  public void save(ProjectInformationForm projectInformationForm) {
    saveForm(projectInformationForm);
  }

  public void save(LocationForm locationForm) {
    saveForm(locationForm);
  }

  private void applyToForm(Object form) {
    try {
      for (Field scopeField : this.getClass().getDeclaredFields()) {
        if (scopeField.getType().equals(form.getClass())) {
          scopeField.setAccessible(true);
          var scopedFieldForm = scopeField.get(this);
          for (Field formField : form.getClass().getDeclaredFields()) {
            formField.setAccessible(true);
            var scopedFieldSetValue = formField.get(scopedFieldForm);
            formField.set(form, scopedFieldSetValue);
          }
        }
      }
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
  }

  public void apply(AdministrativeDetailsForm administrativeDetailsForm) {
    applyToForm(administrativeDetailsForm);
  }

  public void apply(ProjectInformationForm projectInformationForm) {
    applyToForm(projectInformationForm);
  }

  public void apply(LocationForm locationForm) {
    applyToForm(locationForm);
  }

}
