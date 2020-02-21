package uk.co.ogauthority.pwa.temp;

import java.io.Serializable;
import java.lang.reflect.Field;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.co.ogauthority.pwa.temp.model.admindetails.WithinSafetyZone;
import uk.co.ogauthority.pwa.temp.model.form.AdministrativeDetailsForm;
import uk.co.ogauthority.pwa.temp.model.form.ProjectInformationForm;

@Component
@Scope("session")
public class PwaApplicationScope implements Serializable {

  // Admin details
  private String projectDescription;
  private Object projectDiagram;
  private Boolean agreesToFdp;
  private String locationFromShore;
  private WithinSafetyZone withinSafetyZone;
  private String structureNameIfYes;
  private String structureNameIfPartially;
  private String methodOfTransportation;
  private String landfallDetails;
  private Boolean acceptFundsLiability;
  private Boolean acceptOpolLiability;
  private Boolean whollyOffshore;

  // Project Information
  private String workStartDay;
  private String workStartMonth;
  private String workStartYear;

  private String earliestCompletionDay;
  private String earliestCompletionMonth;
  private String earliestCompletionYear;

  private String latestCompletionDay;
  private String latestCompletionMonth;
  private String latestCompletionYear;

  private String field;
  private String description;

  private void save(Object form, Class reflectClass) {
    try {
      for (Field field : reflectClass.getDeclaredFields()) {
        field.setAccessible(true);
        this.getClass().getDeclaredField(field.getName()).set(this, field.get(form));
      }
    } catch (NoSuchFieldException | IllegalAccessException exception) {
      exception.printStackTrace();
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
      for (Field field : reflectClass.getDeclaredFields()) {
        field.setAccessible(true);
        field.set(form, this.getClass().getDeclaredField(field.getName()).get(this));
      }
    } catch (NoSuchFieldException | IllegalAccessException exception) {
      exception.printStackTrace();
    }
  }

  public void apply(AdministrativeDetailsForm administrativeDetailsForm) {
    apply(administrativeDetailsForm, AdministrativeDetailsForm.class);
  }

  public void apply(ProjectInformationForm projectInformationForm) {
    apply(projectInformationForm, ProjectInformationForm.class);
  }

}
