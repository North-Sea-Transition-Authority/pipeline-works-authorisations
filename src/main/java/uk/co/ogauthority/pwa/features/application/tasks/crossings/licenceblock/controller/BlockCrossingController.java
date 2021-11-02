package uk.co.ogauthority.pwa.features.application.tasks.crossings.licenceblock.controller;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.controller.pwaapplications.rest.PearsRestController;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermission;
import uk.co.ogauthority.pwa.features.application.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.features.application.files.PadFileService;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.licenceblock.AddBlockCrossingForm;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.licenceblock.AddBlockCrossingFormValidator;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.licenceblock.BlockCrossingFileService;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.licenceblock.BlockCrossingService;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.licenceblock.BlockCrossingUrlFactory;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.licenceblock.CrossedBlockOwner;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.licenceblock.EditBlockCrossingForm;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.licenceblock.EditBlockCrossingFormValidator;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.licenceblock.PadCrossedBlock;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.tasklist.CrossingAgreementsTaskListService;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.tasklist.CrossingOverview;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.tasklist.controller.CrossingAgreementsController;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.crossings.CrossingAgreementTask;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.searchselector.SearchSelectorService;
import uk.co.ogauthority.pwa.util.StreamUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/crossings/block")
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.CAT_1_VARIATION,
    PwaApplicationType.CAT_2_VARIATION,
    PwaApplicationType.DEPOSIT_CONSENT,
    PwaApplicationType.DECOMMISSIONING
})
@PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.DRAFT, PwaApplicationStatus.UPDATE_REQUESTED})
@PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
public class BlockCrossingController {
  private final ApplicationBreadcrumbService breadcrumbService;
  private final PortalOrganisationsAccessor portalOrganisationsAccessor;
  private final AddBlockCrossingFormValidator addBlockCrossingFormValidator;
  private final EditBlockCrossingFormValidator editBlockCrossingFormValidator;
  private final BlockCrossingService blockCrossingService;
  private final BlockCrossingFileService blockCrossingFileService;
  private final CrossingAgreementsTaskListService crossingAgreementsTaskListService;
  private final PadFileService padFileService;
  private final ControllerHelperService controllerHelperService;

  @Autowired
  public BlockCrossingController(
      ApplicationBreadcrumbService breadcrumbService,
      PortalOrganisationsAccessor portalOrganisationsAccessor,
      AddBlockCrossingFormValidator addBlockCrossingFormValidator,
      EditBlockCrossingFormValidator editBlockCrossingFormValidator,
      BlockCrossingService blockCrossingService,
      BlockCrossingFileService blockCrossingFileService,
      CrossingAgreementsTaskListService crossingAgreementsTaskListService,
      PadFileService padFileService,
      ControllerHelperService controllerHelperService) {
    this.breadcrumbService = breadcrumbService;
    this.portalOrganisationsAccessor = portalOrganisationsAccessor;
    this.addBlockCrossingFormValidator = addBlockCrossingFormValidator;
    this.editBlockCrossingFormValidator = editBlockCrossingFormValidator;
    this.blockCrossingService = blockCrossingService;
    this.blockCrossingFileService = blockCrossingFileService;
    this.crossingAgreementsTaskListService = crossingAgreementsTaskListService;
    this.padFileService = padFileService;
    this.controllerHelperService = controllerHelperService;
  }

  @GetMapping
  public ModelAndView renderBlockCrossingOverview(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @ModelAttribute("form") AddBlockCrossingForm form,
      PwaApplicationContext applicationContext) {
    return createOverviewModelAndView(applicationContext.getApplicationDetail());
  }

  @PostMapping
  public ModelAndView postOverview(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @ModelAttribute("form") AddBlockCrossingForm form,
      PwaApplicationContext applicationContext) {

    var detail = applicationContext.getApplicationDetail();

    if (blockCrossingService.isDocumentsRequired(detail) && !blockCrossingFileService.isComplete(detail)) {
      return createOverviewModelAndView(detail)
          .addObject("errorMessage", "Add at least one document");
    } else if (!blockCrossingService.isComplete(detail)) {
      return createOverviewModelAndView(detail)
          .addObject("errorMessage", "Add at least one block");
    }
    return ReverseRouter.redirect(on(CrossingAgreementsController.class)
        .renderCrossingAgreementsOverview(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null,
            null));
  }

  @GetMapping("/new")
  public ModelAndView renderAddBlockCrossing(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @ModelAttribute("form") AddBlockCrossingForm form,
      PwaApplicationContext applicationContext) {

    return createAddBlockCrossingFormModelAndView(applicationContext, form);
  }

  @PostMapping("/new")
  public ModelAndView addBlockCrossingFormSave(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @Valid @ModelAttribute("form") AddBlockCrossingForm form,
      BindingResult bindingResult,
      PwaApplicationContext applicationContext) {

    addBlockCrossingFormValidator.validate(form, bindingResult, applicationContext.getApplicationDetail());

    return controllerHelperService.checkErrorsAndRedirect(
        bindingResult,
        createAddBlockCrossingFormModelAndView(applicationContext, form),
        () -> {
          blockCrossingService.createAndSaveBlockCrossingAndOwnersFromForm(applicationContext.getApplicationDetail(),
              form);
          return redirectToCrossingOverview(applicationContext);
        }
    );

  }

  @GetMapping("/{blockCrossingId}/edit")
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

  @PostMapping("/{blockCrossingId}/edit")
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

    editBlockCrossingFormValidator.validate(form, bindingResult, crossedBlock.getLicence());

    return controllerHelperService.checkErrorsAndRedirect(
        bindingResult,
        createEditBlockCrossingFormModelAndView(applicationContext, crossedBlock),
        () -> {
          blockCrossingService.updateAndSaveBlockCrossingAndOwnersFromForm(crossedBlock,
              form);
          return redirectToCrossingOverview(applicationContext);
        }
    );
  }

  @GetMapping("/{blockCrossingId}/remove")
  public ModelAndView renderRemoveBlockCrossing(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @PathVariable("blockCrossingId") Integer blockCrossingId,
      PwaApplicationContext applicationContext) {

    var detail = applicationContext.getApplicationDetail();
    blockCrossingService.errorWhenCrossedBlockDoesNotExist(blockCrossingId, detail);
    var modelAndView = new ModelAndView("pwaApplication/shared/crossings/removeBlockCrossing")
        .addObject("crossing", blockCrossingService.getCrossedBlockView(detail, blockCrossingId))
        .addObject("backUrl",
            crossingAgreementsTaskListService.getRoute(detail, CrossingAgreementTask.LICENCE_AND_BLOCKS));
    breadcrumbService.fromCrossingSection(detail, modelAndView, CrossingAgreementTask.LICENCE_AND_BLOCKS,
        "Remove block crossing");
    return modelAndView;
  }

  @PostMapping("/{blockCrossingId}/remove")
  public ModelAndView removeBlockCrossing(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @PathVariable("blockCrossingId") Integer blockCrossingId,
      PwaApplicationContext applicationContext) {
    var crossedBlock = blockCrossingService.getCrossedBlockByIdAndApplicationDetail(
        blockCrossingId,
        applicationContext.getApplicationDetail()
    );
    blockCrossingService.removeBlockCrossing(crossedBlock);
    return redirectToCrossingOverview(applicationContext);
  }

  private ModelAndView redirectToCrossingOverview(PwaApplicationContext applicationContext) {
    var detail = applicationContext.getApplicationDetail();
    return crossingAgreementsTaskListService.getOverviewRedirect(detail,
        CrossingAgreementTask.LICENCE_AND_BLOCKS);
  }

  private void addGenericBlockCrossingModelAttributes(ModelAndView modelAndView, PwaApplicationContext context) {
    var sortedOrganisationUnits = portalOrganisationsAccessor.getAllActiveOrganisationUnits()
        .stream()
        .sorted(Comparator.comparing(o -> o.getName().toLowerCase()))
        .collect(StreamUtils.toLinkedHashMap(o -> String.valueOf(o.getOuId()), PortalOrganisationUnit::getName));

    modelAndView.addObject("crossedBlockOwnerOptions", CrossedBlockOwner.asList());
    modelAndView.addObject("orgUnits", sortedOrganisationUnits);
    modelAndView.addObject("backUrl", ReverseRouter.route(on(BlockCrossingController.class)
        .renderBlockCrossingOverview(context.getApplicationType(), context.getMasterPwaApplicationId(), null, null)));

  }

  private ModelAndView createOverviewModelAndView(PwaApplicationDetail detail) {
    var modelAndView = new ModelAndView("pwaApplication/shared/crossings/overview")
        .addObject("overview", CrossingOverview.LICENCE_AND_BLOCKS)
        .addObject("blockCrossings", blockCrossingService.getCrossedBlockViews(detail))
        .addObject("blockCrossingUrlFactory", new BlockCrossingUrlFactory(detail))
        .addObject("blockCrossingFiles",
            padFileService.getUploadedFileViews(detail, ApplicationDetailFilePurpose.BLOCK_CROSSINGS,
                ApplicationFileLinkStatus.FULL))
        .addObject("backUrl", ReverseRouter.route(on(CrossingAgreementsController.class)
            .renderCrossingAgreementsOverview(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null,
                null)))
        .addObject("isDocumentsRequired", blockCrossingService.isDocumentsRequired(detail));
    breadcrumbService.fromCrossings(detail.getPwaApplication(), modelAndView, "Licence and blocks");
    return modelAndView;
  }

  private ModelAndView createAddBlockCrossingFormModelAndView(PwaApplicationContext applicationContext,
                                                              AddBlockCrossingForm form) {

    var modelAndView = new ModelAndView("pwaApplication/shared/crossings/addBlockCrossing")
        .addObject("blockSelectorUrl", SearchSelectorService.route(on(PearsRestController.class)
            .searchBlocks(null)))
        .addObject("errorList", List.of());

    if (form.getPickedBlock() != null) {
      blockCrossingService.getPickablePearsBlockFromForm(form)
          .ifPresent(block -> modelAndView.addObject("preselectedBlock", Map.of(block.getSelectionId(), block.getSelectionText())));
    }

    addGenericBlockCrossingModelAttributes(modelAndView, applicationContext);
    breadcrumbService.fromCrossingSection(applicationContext.getApplicationDetail(), modelAndView,
        CrossingAgreementTask.LICENCE_AND_BLOCKS, "Add block crossing");
    return modelAndView;
  }

  private ModelAndView createEditBlockCrossingFormModelAndView(PwaApplicationContext applicationContext,
                                                               PadCrossedBlock crossedBlock) {

    var modelAndView = new ModelAndView("pwaApplication/shared/crossings/editBlockCrossing")
        .addObject("errorList", List.of())
        .addObject("blockReference", crossedBlock.getBlockReference())
        .addObject("licenceReference",
            crossedBlock.getLicence() != null ? crossedBlock.getLicence().getLicenceName() : "Unlicensed");

    addGenericBlockCrossingModelAttributes(modelAndView, applicationContext);
    breadcrumbService.fromCrossingSection(applicationContext.getApplicationDetail(), modelAndView,
        CrossingAgreementTask.LICENCE_AND_BLOCKS, "Edit block crossing");
    return modelAndView;
  }
}
