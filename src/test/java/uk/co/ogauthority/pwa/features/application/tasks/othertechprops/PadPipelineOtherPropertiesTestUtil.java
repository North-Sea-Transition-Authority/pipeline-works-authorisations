package uk.co.ogauthority.pwa.features.application.tasks.othertechprops;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.RandomUtils;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinetechinfo.PadPipelineOtherProperties;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinetechinfo.PadPipelineOtherProperties_;
import uk.co.ogauthority.pwa.testutils.ObjectTestUtils;

/*
  An entity builder class for PadPipelineOtherProperties that matches the data created by OtherPropertiesFormBuilder used to aid testing.
 */
public class PadPipelineOtherPropertiesTestUtil {
  public static final String OTHER_PHASE_DESCRIPTION = "my description";

  private PadPipelineOtherPropertiesTestUtil() {
    //no instantiation
  }

  public static PadPipelineOtherProperties createNotAvailableProperty(PwaApplicationDetail pwaApplicationDetail,
                                                                      OtherPipelineProperty otherPipelineProperty) {

    var otherProperties = createFullEntity(
        null,
        pwaApplicationDetail,
        PropertyAvailabilityOption.NOT_AVAILABLE,
        otherPipelineProperty,
        null,
        null
    );

    ObjectTestUtils.assertAllFieldsNotNull(
        otherProperties,
        PadPipelineOtherProperties.class,
        Set.of(
            PadPipelineOtherProperties_.ID,
            PadPipelineOtherProperties_.MIN_VALUE,
            PadPipelineOtherProperties_.MAX_VALUE
        ));

    return otherProperties;

  }

  public static PadPipelineOtherProperties createAvailableProperty(PwaApplicationDetail pwaApplicationDetail,
                                                                   OtherPipelineProperty otherPipelineProperty) {

    var otherProperties = createFullEntity(
        null,
        pwaApplicationDetail,
        PropertyAvailabilityOption.AVAILABLE,
        otherPipelineProperty,
        BigDecimal.valueOf(RandomUtils.nextDouble(0, 50)).setScale(2, RoundingMode.HALF_UP),
        BigDecimal.valueOf(RandomUtils.nextDouble(51, 100)).setScale(2, RoundingMode.HALF_UP)
    );

    ObjectTestUtils.assertAllFieldsNotNull(
        otherProperties,
        PadPipelineOtherProperties.class,
        Set.of(PadPipelineOtherProperties_.ID));

    return otherProperties;
  }


  public static List<PadPipelineOtherProperties> createAllEntities(PwaApplicationDetail detail) {
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

    return entities;
  }


  public static PadPipelineOtherProperties createFullEntity(Integer id,
                                                            PwaApplicationDetail pwaApplicationDetail,
                                                            PropertyAvailabilityOption availabilityOption,
                                                            OtherPipelineProperty propertyName,
                                                            BigDecimal minValue,
                                                            BigDecimal maxValue) {
    var entity = new PadPipelineOtherProperties();
    entity.setId(id);
    entity.setPwaApplicationDetail(pwaApplicationDetail);
    entity.setPropertyName(propertyName);
    entity.setAvailabilityOption(availabilityOption);
    entity.setMinValue(minValue);
    entity.setMaxValue(maxValue);
    return entity;
  }

  public static List<PadPipelineOtherProperties> createBlankEntities(PwaApplicationDetail pwaApplicationDetail) {
    List<PadPipelineOtherProperties> pipelineOtherPropertiesList = new ArrayList<>();
    int id = 1;
    for (OtherPipelineProperty property : OtherPipelineProperty.asList()) {
      var padPipelineOtherProperty = new PadPipelineOtherProperties(pwaApplicationDetail, property);
      padPipelineOtherProperty.setId(id++);
      pipelineOtherPropertiesList.add(padPipelineOtherProperty);
    }
    return pipelineOtherPropertiesList;
  }

  public static Set<PropertyPhase> getPhaseDataForAppDetail() {
    return PropertyPhase.stream().collect(Collectors.toSet());
  }

  public static void setPhaseDataOnAppDetail(PwaApplicationDetail pwaApplicationDetail) {
    pwaApplicationDetail.setPipelinePhaseProperties(getPhaseDataForAppDetail());
    pwaApplicationDetail.setOtherPhaseDescription(getOtherPhaseDescription());
  }

  public static void setPhaseDataOnAppDetail_otherPhaseExcluded(PwaApplicationDetail pwaApplicationDetail) {
    var phases = getPhaseDataForAppDetail();
    phases.remove(PropertyPhase.OTHER);
    pwaApplicationDetail.setPipelinePhaseProperties(phases);
    pwaApplicationDetail.setOtherPhaseDescription(getOtherPhaseDescription());
  }

  public static String getOtherPhaseDescription() {
    return OTHER_PHASE_DESCRIPTION;
  }


}