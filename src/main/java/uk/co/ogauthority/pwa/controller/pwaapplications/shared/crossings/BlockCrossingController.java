package uk.co.ogauthority.pwa.controller.pwaapplications.shared.crossings;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Comparator;
import java.util.List;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.controller.files.PwaApplicationDataFileUploadAndDownloadController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.CrossingAgreementsController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.energyportal.service.organisations.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.CrossedBlockOwner;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.PadCrossedBlock;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.crossings.AddBlockCrossingForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.crossings.EditBlockCrossingForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.licence.PearsBlockService;
import uk.co.ogauthority.pwa.service.licence.PickablePearsBlock;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.BlockCrossingService;
import uk.co.ogauthority.pwa.util.ControllerUtils;
import uk.co.ogauthority.pwa.util.StreamUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;
import uk.co.ogauthority.pwa.validators.pwaapplications.shared.crossings.AddBlockCrossingFormValidator;
import uk.co.ogauthority.pwa.validators.pwaapplications.shared.crossings.EditBlockCrossingFormValidator;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/crossings")
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.CAT_1_VARIATION,
    PwaApplicationType.CAT_2_VARIATION,
    PwaApplicationType.DECOMMISSIONING,
    PwaApplicationType.OPTIONS_VARIATION
})
public class BlockCrossingController extends PwaApplicationDataFileUploadAndDownloadController {
  private final ApplicationBreadcrumbService breadcrumbService;
  private final PortalOrganisationsAccessor portalOrganisationsAccessor;
  private final AddBlockCrossingFormValidator addBlockCrossingFormValidator;
  private final EditBlockCrossingFormValidator editBlockCrossingFormValidator;
  private final PearsBlockService pearsBlockService;
  private final BlockCrossingService blockCrossingService;


  @Autowired
  public BlockCrossingController(
      ApplicationBreadcrumbService breadcrumbService,
      PortalOrganisationsAccessor portalOrganisationsAccessor,
      AddBlockCrossingFormValidator addBlockCrossingFormValidator,
      EditBlockCrossingFormValidator editBlockCrossingFormValidator,
      PearsBlockService pearsBlockService,
      BlockCrossingService blockCrossingService) {
    this.breadcrumbService = breadcrumbService;
    this.portalOrganisationsAccessor = portalOrganisationsAccessor;
    this.addBlockCrossingFormValidator = addBlockCrossingFormValidator;
    this.editBlockCrossingFormValidator = editBlockCrossingFormValidator;
    this.pearsBlockService = pearsBlockService;
    this.blockCrossingService = blockCrossingService;
  }

  @GetMapping("/edit-block-crossing/{blockCrossingId}")
  @PwaApplicationStatusCheck(status = PwaApplicationStatus.DRAFT)
  @PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
  public ModelAndView renderEditBlockCrossing(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @PathVariable("blockCrossingId") Integer blockCrossingId,
      @ModelAttribute("form") EditBlockCrossingForm form,
      PwaApplicationContext applicationContext) {
    var crossedBlock = blockCrossingService.getCrossedBlockByIdAndApplicationDetail(
        blockCrossingId,
        applicationContext.getApplicationDetail()
    );
    blockCrossingService.mapBlockCrossingToEditForm(crossedBlock, form);
    return createEditBlockCrossingFormModelAndView(applicationContext, crossedBlock);
  }

  @PostMapping("/edit-block-crossing/{blockCrossingId}")
  @PwaApplicationStatusCheck(status = PwaApplicationStatus.DRAFT)
  @PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
  public ModelAndView editBlockCrossingFormSave(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @PathVariable("blockCrossingId") Integer blockCrossingId,
      @Valid @ModelAttribute("form") EditBlockCrossingForm form,
      BindingResult bindingResult,
      PwaApplicationContext applicationContext) {

    var crossedBlock = blockCrossingService.getCrossedBlockByIdAndApplicationDetail(
        blockCrossingId,
        applicationContext.getApplicationDetail()
    );

    editBlockCrossingFormValidator.validate(form, bindingResult);

    return ControllerUtils.checkErrorsAndRedirect(
        bindingResult,
        createEditBlockCrossingFormModelAndView(applicationContext, crossedBlock),
        () -> {
          blockCrossingService.updateAndSaveBlockCrossingAndOwnersFromForm(crossedBlock,
              form);
          return redirectToCrossingOverview(applicationContext);
        }
    );

  }

  @PostMapping("/remove-block-crossing/{blockCrossingId}")
  @PwaApplicationStatusCheck(status = PwaApplicationStatus.DRAFT)
  @PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
  public ModelAndView removeBlockCrossing(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @PathVariable("blockCrossingId") Integer blockCrossingId,
      @ModelAttribute("form") AddBlockCrossingForm form,
      PwaApplicationContext applicationContext) {
    var crossedBlock = blockCrossingService.getCrossedBlockByIdAndApplicationDetail(
        blockCrossingId,
        applicationContext.getApplicationDetail()
    );
    blockCrossingService.removeBlockCrossing(crossedBlock);
    return redirectToCrossingOverview(applicationContext);
  }

  @GetMapping("/add-block-crossing")
  @PwaApplicationStatusCheck(status = PwaApplicationStatus.DRAFT)
  @PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
  public ModelAndView renderAddBlockCrossing(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @ModelAttribute("form") AddBlockCrossingForm form,
      PwaApplicationContext applicationContext) {

    return createAddBlockCrossingFormModelAndView(applicationContext);
  }

  @PostMapping("/add-block-crossing")
  @PwaApplicationStatusCheck(status = PwaApplicationStatus.DRAFT)
  @PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
  public ModelAndView addBlockCrossingFormSave(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @Valid @ModelAttribute("form") AddBlockCrossingForm form,
      BindingResult bindingResult,
      PwaApplicationContext applicationContext) {

    addBlockCrossingFormValidator.validate(form, bindingResult);

    return ControllerUtils.checkErrorsAndRedirect(
        bindingResult,
        createAddBlockCrossingFormModelAndView(applicationContext),
        () -> {
          blockCrossingService.createAndSaveBlockCrossingAndOwnersFromForm(applicationContext.getApplicationDetail(),
              form);
          return redirectToCrossingOverview(applicationContext);
        }
    );

  }

  private ModelAndView redirectToCrossingOverview(PwaApplicationContext applicationContext) {
    return ReverseRouter.redirect(on(CrossingAgreementsController.class)
        .renderCrossingAgreementsOverview(
            applicationContext.getApplicationDetail().getPwaApplicationType(),
            null,
            null
        ));
  }

  private void addGenericBlockCrossingModelAttributes(ModelAndView modelAndView) {
    // TODO Convert to search selector when PWA-150 is complete. will improve performance by not loading entire dataset
    var sortedOrganisationUnits = portalOrganisationsAccessor.findOrganisationUnitsWhereNameContains(
        "", PageRequest.of(0, 50)
    )
        .stream()
        .sorted(Comparator.comparing(o -> o.getName().toLowerCase()))
        .collect(StreamUtils.toLinkedHashMap(o -> String.valueOf(o.getOuId()), PortalOrganisationUnit::getName));

    modelAndView.addObject("crossedBlockOwnerOptions", CrossedBlockOwner.asList());
    modelAndView.addObject("orgUnits", sortedOrganisationUnits);

  }

  private ModelAndView createAddBlockCrossingFormModelAndView(PwaApplicationContext applicationContext) {

    // TODO PWA-408 this eventually will be used by a search selector
    var pickableBlocks = pearsBlockService.findOffshorePickablePearsBlocks("%", PageRequest.of(0, 50))
        .stream()
        .collect(StreamUtils.toLinkedHashMap(PickablePearsBlock::getData, PickablePearsBlock::getKey));

    var modelAndView = new ModelAndView("pwaApplication/shared/crossings/addBlockCrossing")
        .addObject("pickableBlocks", pickableBlocks)
        .addObject("errorList", List.of());

    addGenericBlockCrossingModelAttributes(modelAndView);
    breadcrumbService.fromCrossings(applicationContext.getPwaApplication(), modelAndView, "Add block crossing");
    return modelAndView;
  }

  private ModelAndView createEditBlockCrossingFormModelAndView(PwaApplicationContext applicationContext,
                                                               PadCrossedBlock crossedBlock) {

    // TODO PWA-408 this eventually will be used by a search selector
    var pickableBlocks = pearsBlockService.findOffshorePickablePearsBlocks("%", PageRequest.of(0, 50))
        .stream()
        .collect(StreamUtils.toLinkedHashMap(PickablePearsBlock::getData, PickablePearsBlock::getKey));

    var modelAndView = new ModelAndView("pwaApplication/shared/crossings/editBlockCrossing")
        .addObject("pickableBlocks", pickableBlocks)
        .addObject("errorList", List.of())
        .addObject("blockReference", crossedBlock.getBlockReference())
        .addObject("licenceReference",
            crossedBlock.getLicence() != null ? crossedBlock.getLicence().getLicenceName() : "Unlicensed");

    addGenericBlockCrossingModelAttributes(modelAndView);
    breadcrumbService.fromCrossings(applicationContext.getPwaApplication(), modelAndView, "Edit block crossing");
    return modelAndView;
  }

}
