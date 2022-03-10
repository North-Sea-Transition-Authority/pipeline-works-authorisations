package uk.co.ogauthority.pwa.features.webapp.devtools.testharness.appsectiongeneration;

import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasks.permdeposit.MaterialType;
import uk.co.ogauthority.pwa.features.application.tasks.permdeposit.PermanentDepositService;
import uk.co.ogauthority.pwa.features.application.tasks.permdeposit.PermanentDepositsForm;
import uk.co.ogauthority.pwa.util.forminputs.decimal.DecimalInput;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.TwoFieldDateInput;

@Service
@Profile("test-harness")
class PermanentDepositsGeneratorService implements TestHarnessAppFormService {

  private final PermanentDepositService permanentDepositService;

  private static final ApplicationTask LINKED_APP_FORM_TASK = ApplicationTask.PERMANENT_DEPOSITS;

  @Autowired
  public PermanentDepositsGeneratorService(
      PermanentDepositService permanentDepositService) {
    this.permanentDepositService = permanentDepositService;
  }


  @Override
  public ApplicationTask getLinkedAppFormTask() {
    return LINKED_APP_FORM_TASK;
  }


  @Override
  public void generateAppFormData(TestHarnessAppFormServiceParams appFormServiceParams) {
    permanentDepositService.saveEntityUsingForm(appFormServiceParams.getApplicationDetail(), createForm(), appFormServiceParams.getUser());
  }


  private PermanentDepositsForm createForm() {
    var form = new PermanentDepositsForm();
    form.setDepositIsForConsentedPipeline(false);
    form.setDepositReference("Test_Harness_Deposit_Reference");
    form.setDepositIsForPipelinesOnOtherApp(true);
    form.setAppRefAndPipelineNum("App Ref X, Pipeline Num PL12345");

    var fromDate = LocalDate.now();
    form.setFromDate(new TwoFieldDateInput(fromDate));
    form.setToDate(new TwoFieldDateInput(fromDate.plusMonths(3)));

    form.setMaterialType(MaterialType.ROCK);
    form.setRocksSize("Large");
    form.setQuantityRocks(new DecimalInput("50"));

    form.setFromCoordinateForm(TestHarnessAppFormUtil.getRandomCoordinatesForm());
    form.setToCoordinateForm(TestHarnessAppFormUtil.getRandomCoordinatesForm());

    return form;
  }




}
