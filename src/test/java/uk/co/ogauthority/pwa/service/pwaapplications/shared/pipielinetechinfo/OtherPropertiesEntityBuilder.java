package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipielinetechinfo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import uk.co.ogauthority.pwa.model.entity.enums.pipelineotherproperties.OtherPipelineProperty;
import uk.co.ogauthority.pwa.model.entity.enums.pipelineotherproperties.PropertyAvailabilityOption;
import uk.co.ogauthority.pwa.model.entity.enums.pipelineotherproperties.PropertyPhase;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinetechinfo.PadPipelineOtherProperties;

/*
  An entity builder class for PadPipelineOtherProperties that matches the data created by OtherPropertiesFormBuilder used to aid testing.
 */
public class OtherPropertiesEntityBuilder {

  private final String otherPhaseDescription = "my description";


  public List<PadPipelineOtherProperties> createAllEntities(PwaApplicationDetail detail) {
    var entities = new ArrayList<PadPipelineOtherProperties>();

    entities.add(createFullEntity(1, detail, PropertyAvailabilityOption.NOT_AVAILABLE, OtherPipelineProperty.WAX_CONTENT,
        null, null));
    entities.add(createFullEntity(2, detail, PropertyAvailabilityOption.NOT_AVAILABLE, OtherPipelineProperty.WAX_APPEARANCE_TEMPERATURE,
        null, null));
    entities.add(createFullEntity(3, detail, PropertyAvailabilityOption.NOT_AVAILABLE, OtherPipelineProperty.ACID_NUM,
        null, null));
    entities.add(createFullEntity(4, detail, PropertyAvailabilityOption.NOT_AVAILABLE, OtherPipelineProperty.VISCOSITY,
        null, null));
    entities.add(createFullEntity(5, detail, PropertyAvailabilityOption.NOT_PRESENT, OtherPipelineProperty.DENSITY_GRAVITY,
        null, null));
    entities.add(createFullEntity(6, detail, PropertyAvailabilityOption.NOT_PRESENT, OtherPipelineProperty.SULPHUR_CONTENT,
        null, null));
    entities.add(createFullEntity(7, detail, PropertyAvailabilityOption.NOT_PRESENT, OtherPipelineProperty.POUR_POINT,
        null, null));
    entities.add(createFullEntity(8, detail, PropertyAvailabilityOption.NOT_PRESENT, OtherPipelineProperty.SOLID_CONTENT,
        null, null));
    entities.add(createFullEntity(9, detail, PropertyAvailabilityOption.AVAILABLE, OtherPipelineProperty.MERCURY,
        BigDecimal.valueOf(3), BigDecimal.valueOf(5)));
    entities.add(createFullEntity(10, detail, PropertyAvailabilityOption.AVAILABLE, OtherPipelineProperty.H20,
        BigDecimal.valueOf(12), BigDecimal.valueOf(15)));

    return entities;
  }



  public PadPipelineOtherProperties createFullEntity(int id, PwaApplicationDetail pwaApplicationDetail,
                                                     PropertyAvailabilityOption availabilityOption,  OtherPipelineProperty propertyName,
                                                     BigDecimal minValue, BigDecimal maxValue) {
    var entity = new PadPipelineOtherProperties();
    entity.setId(id);
    entity.setPwaApplicationDetail(pwaApplicationDetail);
    entity.setPropertyName(propertyName);
    entity.setAvailabilityOption(availabilityOption);
    entity.setMinValue(minValue);
    entity.setMaxValue(maxValue);
    return entity;
  }

  public List<PadPipelineOtherProperties> createBlankEntities(PwaApplicationDetail pwaApplicationDetail) {
    List<PadPipelineOtherProperties> pipelineOtherPropertiesList = new ArrayList<>();
    int id = 1;
    for (OtherPipelineProperty property: OtherPipelineProperty.asList()) {
      var padPipelineOtherProperty = new PadPipelineOtherProperties(pwaApplicationDetail, property);
      padPipelineOtherProperty.setId(id++);
      pipelineOtherPropertiesList.add(padPipelineOtherProperty);
    }
    return pipelineOtherPropertiesList;
  }

  public String getPhaseDataForAppDetail() {
    String separator = ",";
    return PropertyPhase.OIL + separator +
        PropertyPhase.CONDENSATE + separator +
        PropertyPhase.OTHER;
  }

  public void setPhaseDataOnAppDetail(PwaApplicationDetail pwaApplicationDetail) {
    String phases = getPhaseDataForAppDetail();
    pwaApplicationDetail.setPipelinePhaseProperties(phases);
    pwaApplicationDetail.setOtherPhaseDescription(getOtherPhaseDescription());
  }

  public void setPhaseDataOnAppDetail_otherPhaseExcluded(PwaApplicationDetail pwaApplicationDetail) {
    String separator = ",";
    String phases = PropertyPhase.OIL + separator +
        PropertyPhase.OIL + separator +
        PropertyPhase.CONDENSATE;

    pwaApplicationDetail.setPipelinePhaseProperties(phases);
    pwaApplicationDetail.setOtherPhaseDescription(getOtherPhaseDescription());
  }

  public String getOtherPhaseDescription() {
    return otherPhaseDescription;
  }


}