package uk.co.ogauthority.pwa.service.devuk;

import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.model.entity.devuk.DevukField;
import uk.co.ogauthority.pwa.model.entity.devuk.PadField;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.fields.PwaFieldForm;
import uk.co.ogauthority.pwa.repository.devuk.PadFieldRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.projectinformation.PadProjectInformationService;
import uk.co.ogauthority.pwa.validators.PwaFieldFormValidator;

@Service
public class PadFieldService implements ApplicationFormSectionService {

  private final PadFieldRepository padFieldRepository;
  private final PwaApplicationDetailService pwaApplicationDetailService;
  private final PadProjectInformationService projectInformationService;
  private final DevukFieldService devukFieldService;
  private final PwaFieldFormValidator pwaFieldFormValidator;

  @Autowired
  public PadFieldService(PadFieldRepository padFieldRepository,
                         PwaApplicationDetailService pwaApplicationDetailService,
                         PadProjectInformationService projectInformationService,
                         DevukFieldService devukFieldService,
                         PwaFieldFormValidator pwaFieldFormValidator) {
    this.padFieldRepository = padFieldRepository;
    this.pwaApplicationDetailService = pwaApplicationDetailService;
    this.projectInformationService = projectInformationService;
    this.devukFieldService = devukFieldService;
    this.pwaFieldFormValidator = pwaFieldFormValidator;
  }

  public List<PadField> getActiveFieldsForApplicationDetail(PwaApplicationDetail pwaApplicationDetail) {
    return padFieldRepository.getAllByPwaApplicationDetail(pwaApplicationDetail);
  }

  /**
   * Add fields to an application detail.
   *
   * @param pwaApplicationDetail The current application detail.
   * @param fields A list of DevukFields to link to.
   */
  private void addFields(PwaApplicationDetail pwaApplicationDetail, List<DevukField> fields) {

    List<PadField> newPadFields = fields.stream()
        .map(devukField -> {
          var padField = new PadField();
          padField.setPwaApplicationDetail(pwaApplicationDetail);
          padField.setDevukField(devukField);
          return padField;
        })
        .collect(Collectors.toList());

    padFieldRepository.saveAll(newPadFields);

  }

  /**
   * Provides a quick way to close off all fields on the current application detail.
   *
   * @param pwaApplicationDetail Current application detail.
   */
  private void removeAllFields(PwaApplicationDetail pwaApplicationDetail) {

    var fieldsToEnd = getActiveFieldsForApplicationDetail(pwaApplicationDetail);

    padFieldRepository.deleteAll(fieldsToEnd);

  }

  private void removeFdpDataFromProjectInfo(PwaApplicationDetail applicationDetail, Boolean isLinkedtoField) {
    if (!isLinkedtoField) {
      projectInformationService.removeFdpQuestionData(applicationDetail);
    }
  }

  @Transactional
  public void updateFieldInformation(PwaApplicationDetail applicationDetail, PwaFieldForm form) {

    // if they've said yes or no to the field link question, we have things to do
    if (form.getLinkedToField() != null) {

      pwaApplicationDetailService.setLinkedToFields(applicationDetail, form.getLinkedToField());

      removeAllFields(applicationDetail);

      // if they've said yes to field link and selected a field, add field
      if (form.getLinkedToField() && form.getFieldId() != null) {

        var devukField = devukFieldService.findById(form.getFieldId());
        addFields(applicationDetail, List.of(devukField));

      } else if (!form.getLinkedToField()) {

        // otherwise they've said no to field link, update linked field description
        pwaApplicationDetailService.setNotLinkedFieldDescription(applicationDetail, form.getNoLinkedFieldDescription());

      }

      // clear FDP answers on project info section based on whether or not we're linked to a field
      removeFdpDataFromProjectInfo(applicationDetail, form.getLinkedToField());

    }

  }

  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    var form = new PwaFieldForm();
    mapEntityToForm(detail, form);
    BindingResult bindingResult = new BeanPropertyBindingResult(form, "form");
    bindingResult = validate(form, bindingResult, ValidationType.FULL, detail);
    return !bindingResult.hasErrors();
  }

  public void mapEntityToForm(PwaApplicationDetail pwaApplicationDetail, PwaFieldForm form) {
    var fields = getActiveFieldsForApplicationDetail(pwaApplicationDetail);
    form.setLinkedToField(pwaApplicationDetail.getLinkedToField());
    if (fields.size() == 1) {
      if (fields.get(0).isLinkedToDevuk()) {
        form.setFieldId(fields.get(0).getDevukField().getFieldId());
      }
    } else if (fields.size() == 0) {
      form.setNoLinkedFieldDescription(pwaApplicationDetail.getNotLinkedDescription());
    }
  }


  @Override
  public BindingResult validate(Object form, BindingResult bindingResult, ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {
    pwaFieldFormValidator.validate(form, bindingResult, validationType);
    return bindingResult;
  }

}
