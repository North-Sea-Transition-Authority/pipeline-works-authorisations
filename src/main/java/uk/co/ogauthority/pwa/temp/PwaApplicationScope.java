package uk.co.ogauthority.pwa.temp;

import java.io.Serializable;
import java.lang.reflect.Field;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.co.ogauthority.pwa.temp.model.form.AdministrativeDetailsForm;
import uk.co.ogauthority.pwa.temp.model.form.ProjectInformationForm;

@Component
@Scope("session")
public class PwaApplicationScope implements Serializable {

  private AdministrativeDetailsForm administrativeDetailsForm;
  private ProjectInformationForm projectInformationForm;

  public PwaApplicationScope() {
    administrativeDetailsForm = new AdministrativeDetailsForm();
    projectInformationForm = new ProjectInformationForm();
  }

  private void save(Object form, Class reflectClass) {
    try {
      for (Field scopeField : this.getClass().getDeclaredFields()) {
        if (scopeField.getType().equals(reflectClass)) {
          scopeField.setAccessible(true);
          var scopedFieldForm = scopeField.get(this);
          for (Field formField : reflectClass.getDeclaredFields()) {
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
    save(administrativeDetailsForm, AdministrativeDetailsForm.class);
  }

  public void save(ProjectInformationForm projectInformationForm) {
    save(projectInformationForm, ProjectInformationForm.class);
  }

  private void apply(Object form, Class reflectClass) {
    try {
      for (Field scopeField : this.getClass().getDeclaredFields()) {
        if (scopeField.getType().equals(reflectClass)) {
          scopeField.setAccessible(true);
          var scopedFieldForm = scopeField.get(this);
          for (Field formField : reflectClass.getDeclaredFields()) {
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
    apply(administrativeDetailsForm, AdministrativeDetailsForm.class);
  }

  public void apply(ProjectInformationForm projectInformationForm) {
    apply(projectInformationForm, ProjectInformationForm.class);
  }

}
