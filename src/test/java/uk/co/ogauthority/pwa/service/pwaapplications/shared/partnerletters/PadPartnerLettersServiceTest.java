package uk.co.ogauthority.pwa.service.pwaapplications.shared.partnerletters;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.validation.Validation;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationFilePurpose;
import uk.co.ogauthority.pwa.model.entity.files.PadFile;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinetechinfo.PadPipelineTechInfo;
import uk.co.ogauthority.pwa.model.form.files.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.partnerletters.PartnerLettersForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelinetechinfo.PipelineTechInfoForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.PwaApplicationDetailRepository;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelinetechinfo.PadPipelineTechInfoRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinetechinfo.PadPipelineTechInfoService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinetechinfo.PipelineTechInfoMappingService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;
import uk.co.ogauthority.pwa.validators.partnerletters.PartnerLettersValidator;
import uk.co.ogauthority.pwa.validators.pipelinetechinfo.PipelineTechInfoValidator;


@RunWith(MockitoJUnitRunner.class)
public class PadPartnerLettersServiceTest {

  private PadPartnerLettersService padPartnerLettersService;

  @Mock
  private PwaApplicationDetailService applicationDetailService;

  @Mock
  private PadFileService padFileService;

  private PartnerLettersValidator validator;

  private PwaApplicationDetail pwaApplicationDetail;


  @Before
  public void setUp() {
    validator = new PartnerLettersValidator();
    padPartnerLettersService = new PadPartnerLettersService(applicationDetailService,
        validator, padFileService);
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 100);
  }

  private PartnerLettersForm createValidForm() {
    var form = new PartnerLettersForm();
    form.setPartnerLettersRequired(true);
    form.setPartnerLettersConfirmed(true);
    form.setUploadedFileWithDescriptionForms(List.of(new UploadFileWithDescriptionForm()));
    return form;
  }

  private PwaApplicationDetail createValidEntity() {
    PwaApplicationDetail appDetail = new PwaApplicationDetail();
    appDetail.setPartnerLettersRequired(true);
    appDetail.setPartnerLettersConfirmed(true);
    return appDetail;
  }




  @Test
  public void mapEntityToForm_partnerLettersRequired() {
    var actualForm = new PartnerLettersForm();
    padPartnerLettersService.mapEntityToForm(createValidEntity(), actualForm);
    assertThat(actualForm).isEqualTo(createValidForm());
  }

  @Test
  public void mapEntityToForm_partnerLettersNotRequired() {
    var actualForm = new PartnerLettersForm();
    var entity = new PwaApplicationDetail();
    entity.setPartnerLettersRequired(false);
    padPartnerLettersService.mapEntityToForm(entity, actualForm);

    var expectedForm = new PartnerLettersForm();
    expectedForm.setPartnerLettersRequired(false);
    assertThat(actualForm).isEqualTo(expectedForm);
  }



  @Test
  public void saveEntityUsingForm_partnerLettersRequired() {
    padPartnerLettersService.saveEntityUsingForm(new PwaApplicationDetail(), createValidForm(), new WebUserAccount());
    verify(padFileService, times(1)).updateFiles(any(), any() ,any(), any(), any());
  }

  @Test
  public void saveEntityUsingForm_partnerLettersNotRequired() {
    when(padFileService.getAllByPwaApplicationDetailAndPurpose(pwaApplicationDetail, ApplicationFilePurpose.PARTNER_LETTERS))
        .thenReturn(List.of(new PadFile(), new PadFile(), new PadFile()));
    var form = new PartnerLettersForm();
    form.setPartnerLettersRequired(false);
    padPartnerLettersService.saveEntityUsingForm(pwaApplicationDetail, form, new WebUserAccount());
    verify(padFileService, times(3)).processFileDeletion(any(), any());
  }




  @Test
  public void validate_fullValidation_valid() {
    var bindingResult = new BeanPropertyBindingResult(null, "empty");
    padPartnerLettersService.validate(createValidForm(), bindingResult, ValidationType.FULL, pwaApplicationDetail);
    assertFalse(bindingResult.hasErrors());
  }





}