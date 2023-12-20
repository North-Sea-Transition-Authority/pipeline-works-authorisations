package uk.co.ogauthority.pwa.features.application.tasks.fieldinfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.PadProjectInformationService;
import uk.co.ogauthority.pwa.integrations.energyportal.devukfields.external.DevukField;
import uk.co.ogauthority.pwa.integrations.energyportal.devukfields.external.DevukFieldId;
import uk.co.ogauthority.pwa.integrations.energyportal.devukfields.external.DevukFieldService;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetailArea;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.searchselector.SearchSelectable;
import uk.co.ogauthority.pwa.model.view.StringWithTag;
import uk.co.ogauthority.pwa.model.view.Tag;
import uk.co.ogauthority.pwa.service.entitycopier.EntityCopyingService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.searchselector.SearchSelectorService;

@Service
public class PadAreaService implements ApplicationFormSectionService {

  private static final Logger LOGGER = LoggerFactory.getLogger(PadAreaService.class);

  private final PadAreaRepository padAreaRepository;
  private final PwaApplicationDetailService pwaApplicationDetailService;
  private final PadProjectInformationService projectInformationService;
  private final DevukFieldService devukFieldService;
  private final SearchSelectorService searchSelectorService;
  private final PwaAreaFormValidator pwaAreaFormValidator;
  private final EntityCopyingService entityCopyingService;

  @Autowired
  public PadAreaService(PadAreaRepository padAreaRepository,
                        PwaApplicationDetailService pwaApplicationDetailService,
                        PadProjectInformationService projectInformationService,
                        DevukFieldService devukFieldService,
                        SearchSelectorService searchSelectorService,
                        PwaAreaFormValidator pwaAreaFormValidator,
                        EntityCopyingService entityCopyingService) {
    this.padAreaRepository = padAreaRepository;
    this.pwaApplicationDetailService = pwaApplicationDetailService;
    this.projectInformationService = projectInformationService;
    this.devukFieldService = devukFieldService;
    this.searchSelectorService = searchSelectorService;
    this.pwaAreaFormValidator = pwaAreaFormValidator;
    this.entityCopyingService = entityCopyingService;
  }

  public List<PadLinkedArea> getActiveFieldsForApplicationDetail(PwaApplicationDetail pwaApplicationDetail) {
    return padAreaRepository.getAllByPwaApplicationDetail(pwaApplicationDetail);
  }

  /**
   * Add fields to an application detail.
   *
   * @param pwaApplicationDetail The current application detail.
   * @param fields               A list of DevukFields to link to.
   */
  private void addFields(PwaApplicationDetail pwaApplicationDetail, List<DevukField> fields) {

    List<PadLinkedArea> newPadLinkedAreas = fields.stream()
        .map(devukField -> {
          var padLinkedArea = new PadLinkedArea();
          padLinkedArea.setPwaApplicationDetail(pwaApplicationDetail);
          padLinkedArea.setDevukField(devukField);
          padLinkedArea.setAreaType(LinkedAreaType.findAreaTypeByResourceType(pwaApplicationDetail.getResourceType()));
          return padLinkedArea;
        })
        .collect(Collectors.toList());

    padAreaRepository.saveAll(newPadLinkedAreas);

  }

  /**
   * Add manually entered field names to an application detail.
   *
   * @param pwaApplicationDetail The current application detail.
   * @param fieldNames           A list of field names to save as PadFields.
   */
  private void addManuallyEnteredAreas(PwaApplicationDetail pwaApplicationDetail, List<String> fieldNames, boolean removePrefix) {

    List<PadLinkedArea> newPadLinkedAreas = fieldNames.stream()
        .map(fieldName -> {
          var padField = new PadLinkedArea();
          padField.setPwaApplicationDetail(pwaApplicationDetail);
          padField.setAreaName(removePrefix ? searchSelectorService.removePrefix(fieldName) : fieldName);
          padField.setAreaType(LinkedAreaType.findAreaTypeByResourceType(pwaApplicationDetail.getResourceType()));
          return padField;
        })
        .collect(Collectors.toList());

    padAreaRepository.saveAll(newPadLinkedAreas);

  }

  /**
   * Provides a quick way to close off all fields on the current application detail.
   *
   * @param pwaApplicationDetail Current application detail.
   */
  private void removeAllFields(PwaApplicationDetail pwaApplicationDetail) {

    var fieldsToEnd = getActiveFieldsForApplicationDetail(pwaApplicationDetail);

    padAreaRepository.deleteAll(fieldsToEnd);

  }

  private void removeFdpDataFromProjectInfo(PwaApplicationDetail applicationDetail, Boolean isLinkedtoField) {
    if (!isLinkedtoField) {
      projectInformationService.removeFdpQuestionData(applicationDetail);
    }
  }

  @Transactional
  public void updateFieldInformation(PwaApplicationDetail applicationDetail, PwaAreaForm form) {

    // if they've said yes or no to the field link question, we have things to do
    if (form.getLinkedToArea() != null) {

      pwaApplicationDetailService.setLinkedToFields(applicationDetail, form.getLinkedToArea());
      removeAllFields(applicationDetail);

      // if they've said yes to field link and selected a field, add field
      if (form.getLinkedToArea() && form.getLinkedAreas() != null) {
        if (applicationDetail.getResourceType().equals(PwaResourceType.CCUS)) {
          addManuallyEnteredAreas(applicationDetail, form.getLinkedAreas(), false);
        } else {
          //differentiate between existing devUkFields and manually entered field names
          var reconciledOptions = devukFieldService.getLinkedAndManualFieldEntries(form.getLinkedAreas());
          addFields(applicationDetail, reconciledOptions.getLinkedEntries());
          addManuallyEnteredAreas(applicationDetail, reconciledOptions.getManualEntries(), true);
        }
      } else if (!form.getLinkedToArea()) {
        // otherwise they've said no to field link, update linked field description
        pwaApplicationDetailService.setNotLinkedFieldDescription(applicationDetail, form.getNoLinkedAreaDescription());
      }

      // clear FDP answers on project info section based on whether or not we're linked to a field
      removeFdpDataFromProjectInfo(applicationDetail, form.getLinkedToArea());

    }

  }


  public void createAndSavePadFieldsFromMasterPwa(PwaApplicationDetail pwaApplicationDetail,
                                                  MasterPwaDetail masterPwaDetail,
                                                  List<MasterPwaDetailArea> masterPwaDetailAreas) {

    if (masterPwaDetail.getLinkedToFields() != null) {
      pwaApplicationDetailService.setLinkedToFields(pwaApplicationDetail, masterPwaDetail.getLinkedToFields());

      if (BooleanUtils.isTrue(masterPwaDetail.getLinkedToFields())) {

        var devUkFieldIds = new ArrayList<DevukFieldId>();
        var manuallyEnteredFields = new ArrayList<String>();
        masterPwaDetailAreas.forEach(pwaDetailField -> {
          if (pwaDetailField.getDevukFieldId() != null) {
            devUkFieldIds.add(pwaDetailField.getDevukFieldId());
          } else {
            manuallyEnteredFields.add(pwaDetailField.getManualFieldName());
          }
        });

        addFields(pwaApplicationDetail, devukFieldService.findByDevukFieldIds(devUkFieldIds));
        addManuallyEnteredAreas(pwaApplicationDetail, manuallyEnteredFields, false);

      } else {
        pwaApplicationDetailService.setNotLinkedFieldDescription(pwaApplicationDetail,
            masterPwaDetail.getPwaLinkedToDescription());
      }
    }

  }


  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    var form = new PwaAreaForm();
    mapEntityToForm(detail, form);
    BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");
    bindingResult = validate(form, bindingResult, ValidationType.FULL, detail);
    return !bindingResult.hasErrors();
  }

  public void mapEntityToForm(PwaApplicationDetail pwaApplicationDetail, PwaAreaForm form) {
    var areas = getActiveFieldsForApplicationDetail(pwaApplicationDetail);
    form.setLinkedToArea(pwaApplicationDetail.getLinkedToArea());
    form.setNoLinkedAreaDescription(pwaApplicationDetail.getNotLinkedDescription());

    if (!areas.isEmpty()) {
      if (pwaApplicationDetail.getResourceType().equals(PwaResourceType.CCUS)) {
        form.setLinkedAreas(areas.stream()
            .map(PadLinkedArea::getAreaName)
            .collect(Collectors.toList()));
      } else {
        form.setLinkedAreas(areas.stream()
            .map(area -> area.isLinkedToDevuk() ? area.getDevukField().getFieldId().toString()
                : SearchSelectable.FREE_TEXT_PREFIX + area.getAreaName())
            .collect(Collectors.toList()));
      }
    }
  }

  public Map<String, String> getPreSelectedApplicationFields(PwaApplicationDetail pwaApplicationDetail) {
    var fields = getActiveFieldsForApplicationDetail(pwaApplicationDetail);
    Map<String, String> preSelectedItems = new HashMap<>();

    fields.forEach(field -> {
      if (field.isLinkedToDevuk()) {
        preSelectedItems.put(field.getDevukField().getFieldId().toString(), field.getDevukField().getFieldName());
      } else {
        preSelectedItems.put(SearchSelectable.FREE_TEXT_PREFIX + field.getAreaName(), field.getAreaName());
      }
    });

    return preSelectedItems;
  }


  @Override
  public BindingResult validate(Object form, BindingResult bindingResult, ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {
    pwaAreaFormValidator.validate(form, bindingResult, validationType, pwaApplicationDetail);
    return bindingResult;
  }

  @Override
  public void copySectionInformation(PwaApplicationDetail fromDetail, PwaApplicationDetail toDetail) {
    entityCopyingService.duplicateEntitiesAndSetParent(
        () -> padAreaRepository.getAllByPwaApplicationDetail(fromDetail),
        toDetail,
        PadLinkedArea.class
    );
  }

  public PwaAreaLinksView getApplicationAreaLinksView(PwaApplicationDetail pwaApplicationDetail) {

    var linkedFieldNames = padAreaRepository.getAllByPwaApplicationDetail(pwaApplicationDetail)
        .stream()
        .map(pf -> pf.getDevukField() != null
            ? new StringWithTag(pf.getDevukField().getFieldName())
            : new StringWithTag(pf.getAreaName(), Tag.NOT_FROM_PORTAL))
        .collect(Collectors.toList());

    return new PwaAreaLinksView(
        pwaApplicationDetail.getLinkedToArea(),
        pwaApplicationDetail.getNotLinkedDescription(),
        linkedFieldNames
    );

  }

}
