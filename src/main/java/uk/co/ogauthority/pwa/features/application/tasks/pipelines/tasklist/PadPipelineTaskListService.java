package uk.co.ogauthority.pwa.features.application.tasks.pipelines.tasklist;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineMaterial;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineOverview;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.features.application.tasks.optionconfirmation.PadOptionConfirmedService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipelineId;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipelineRepository;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipelineService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.controller.PipelinesController;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.PadPipelineIdentService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.controller.PipelineIdentsController;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.importconsented.controller.ModifyPipelineController;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.setnumber.RegulatorPipelineNumberTaskService;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskInfo;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskListEntry;
import uk.co.ogauthority.pwa.model.entity.enums.mailmerge.MailMergeFieldMnem;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.validation.SummaryScreenValidationResult;
import uk.co.ogauthority.pwa.util.pipelines.PipelineNumberSortingUtil;

@Service
public class PadPipelineTaskListService implements ApplicationFormSectionService {

  private static final Logger LOGGER = LoggerFactory.getLogger(PadPipelineTaskListService.class);

  private final PadPipelineService padPipelineService;
  private final PadPipelineIdentService padPipelineIdentService;
  private final PadOptionConfirmedService padOptionConfirmedService;
  private final PadPipelineRepository padPipelineRepository;
  private final RegulatorPipelineNumberTaskService regulatorPipelineNumberTaskService;
  private final PadPipelineDataCopierService padPipelineDataCopierService;

  @Autowired
  public PadPipelineTaskListService(PadPipelineService padPipelineService,
                                    PadPipelineIdentService padPipelineIdentService,
                                    PadOptionConfirmedService padOptionConfirmedService,
                                    PadPipelineRepository padPipelineRepository,
                                    RegulatorPipelineNumberTaskService regulatorPipelineNumberTaskService,
                                    PadPipelineDataCopierService padPipelineDataCopierService) {
    this.padPipelineService = padPipelineService;
    this.padPipelineIdentService = padPipelineIdentService;
    this.padOptionConfirmedService = padOptionConfirmedService;
    this.padPipelineRepository = padPipelineRepository;
    this.regulatorPipelineNumberTaskService = regulatorPipelineNumberTaskService;
    this.padPipelineDataCopierService = padPipelineDataCopierService;
  }

  @Override
  public boolean canShowInTaskList(PwaApplicationDetail pwaApplicationDetail) {
    // do not do additional type checks as this is covered by the controller markup
    return !PwaApplicationType.OPTIONS_VARIATION.equals(pwaApplicationDetail.getPwaApplicationType())
        || padOptionConfirmedService.approvedOptionConfirmed(pwaApplicationDetail);
  }

  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    return getValidationResult(detail).isSectionComplete();
  }

  @Override
  public BindingResult validate(Object form, BindingResult bindingResult, ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {
    throw new AssertionError("Doesn't make sense to implement this.");
  }

  @Override
  public void copySectionInformation(PwaApplicationDetail fromDetail, PwaApplicationDetail toDetail) {
    padPipelineDataCopierService.copyAllPadPipelineData(fromDetail, toDetail,
        () -> padPipelineService.getPipelines(fromDetail));
  }

  @Override
  public void cleanupData(PwaApplicationDetail detail) {

    var updatedPipelinesList = padPipelineService.getPipelines(detail).stream()
        .filter(pipe -> padPipelineService.isValidationRequiredByStatus(pipe.getPipelineStatus()))
        .peek(padPipeline -> {

          if (!padPipeline.getTrenchedBuriedBackfilled()) {
            padPipeline.setTrenchingMethodsDescription(null);
          }

          if (!padPipeline.getPipelineMaterial().equals(PipelineMaterial.OTHER)) {
            padPipeline.setOtherPipelineMaterialUsed(null);
          }

        })
        .collect(Collectors.toList());

    padPipelineRepository.saveAll(updatedPipelinesList);

  }

  public List<PadPipelineTaskListItem> getSortedPipelineTaskListItems(PwaApplicationContext applicationContext) {

    var taskListItemHeaders = getPipelinesTaskListHeaders(applicationContext.getApplicationDetail());

    return taskListItemHeaders.stream()
        .map(pipelineTaskListHeader -> new PadPipelineTaskListItem(
            pipelineTaskListHeader,
            createTaskListEntries(applicationContext, pipelineTaskListHeader)
        ))
        .sorted(Comparator.comparing(PadPipelineTaskListItem::getPipelineName))
        .collect(Collectors.toList());
  }

  private List<PadPipelineTaskListHeader> getPipelinesTaskListHeaders(PwaApplicationDetail pwaApplicationDetail) {

    var allPadPipelines = padPipelineService.getPipelines(pwaApplicationDetail);
    var padPipelineIdToOverviewMap = padPipelineService.getApplicationPipelineOverviews(pwaApplicationDetail)
        .stream()
        .collect(Collectors.toMap(PipelineOverview::getPadPipelineId, Function.identity()));

    return allPadPipelines.stream()
        .map(padPipeline -> {
          var overviewOpt = Optional.ofNullable(padPipelineIdToOverviewMap.get(padPipeline.getId()));
          LOGGER.debug("Overview for padPipelineId:{} is {}", padPipeline.getId(),
              overviewOpt.isEmpty() ? "not found" : "found");

          var identsNo = overviewOpt
              .map(PipelineOverview::getNumberOfIdents)
              .orElse(0L);

          var pipelineName = overviewOpt
              .map(PipelineOverview::getPipelineName)
              .orElse(padPipeline.getPipelineRef());

          return new PadPipelineTaskListHeader(
              padPipeline,
              identsNo,
              padPipeline.getPipelineStatus(),
              pipelineName
          );

        })
        .collect(Collectors.toList());
  }


  private List<TaskListEntry> createTaskListEntries(PwaApplicationContext applicationContext,
                                                    PadPipelineTaskListHeader padPipelineTaskListHeader) {
    var padPipeline = padPipelineTaskListHeader.getPadPipeline();

    var editPipelineHeaderUrl = getEditPipelineHeaderUrl(
        applicationContext.getMasterPwaApplicationId(),
        applicationContext.getApplicationType(),
        padPipeline.getId());

    var identTaskUrl = getPipelineIdentOverviewUrl(
        applicationContext.getMasterPwaApplicationId(),
        applicationContext.getApplicationType(),
        padPipeline.getId()
    );

    if (!padPipelineService.isValidationRequiredByStatus(padPipeline.getPipelineStatus())) {
      return List.of();
    }

    var entryList = new ArrayList<TaskListEntry>();

    regulatorPipelineNumberTaskService.getTaskListEntry(applicationContext, padPipelineTaskListHeader)
        .ifPresent(entryList::add);

    entryList.add(
        new TaskListEntry(
            "Header information",
            editPipelineHeaderUrl,
            padPipelineService.isPadPipelineValid(padPipeline, applicationContext.getApplicationType()),
            10)
    );
    entryList.add(
        new TaskListEntry(
            "Idents",
            identTaskUrl,
            padPipelineIdentService.isSectionValid(padPipeline),
            List.of(new TaskInfo("IDENT", padPipelineTaskListHeader.getNumberOfIdents())),
            20
        )
    );


    return entryList;
  }

  private String getEditPipelineHeaderUrl(int applicationId, PwaApplicationType applicationType, int padPipelineId) {
    return ReverseRouter.route(on(PipelinesController.class).renderEditPipeline(
        applicationId,
        applicationType,
        padPipelineId,
        null,
        null,
        null));
  }

  private String getPipelineIdentOverviewUrl(int applicationId, PwaApplicationType applicationType, int padPipelineId) {
    return ReverseRouter.route(on(PipelineIdentsController.class).renderIdentOverview(
        applicationId,
        applicationType,
        padPipelineId,
        null,
        null));
  }


  public boolean canImportConsentedPipelines(PwaApplicationDetail pwaApplicationDetail) {
    PwaApplicationType[] appTypes = ModifyPipelineController.class.getAnnotation(PwaApplicationTypeCheck.class).types();
    return Arrays.asList(appTypes).contains(pwaApplicationDetail.getPwaApplicationType());
  }



  public SummaryScreenValidationResult getValidationResult(PwaApplicationDetail detail) {

    Map<String, String> invalidPipelines = new LinkedHashMap<>();

    // get sorted pipeline overviews so we can use the pipe name in the error messages
    var overviews = padPipelineService.getApplicationPipelineOverviews(detail);
    var padPipelineMap = padPipelineService.getPadPipelineMapForOverviews(detail, overviews);

    overviews.forEach(pipelineOverview -> {

      var padPipeline = padPipelineMap.get(new PadPipelineId(pipelineOverview.getPadPipelineId()));
      var padPipelineId = new PadPipelineId(pipelineOverview.getPadPipelineId());
      boolean pipelineComplete = padPipelineService.isPadPipelineValid(padPipeline, detail.getPwaApplicationType());

      // validate the ident summary, if incomplete, the whole pipeline is incomplete
      if (padPipelineService.isValidationRequiredByStatus(padPipeline.getPipelineStatus())) {
        var summaryValidationResult = padPipelineIdentService.getSummaryScreenValidationResult(padPipeline);
        if (!summaryValidationResult.isSectionComplete()) {
          pipelineComplete = false;
        }
      }

      // if the pipeline has invalid idents (or no idents), it is invalid
      if (!pipelineComplete) {
        invalidPipelines.put(String.valueOf(padPipelineId.asInt()), pipelineOverview.getPipelineName());
      }

    });

    // section is complete if there's at least 1 pipeline, and no invalid pipelines
    boolean sectionComplete = !padPipelineMap.isEmpty() && invalidPipelines.isEmpty();

    String sectionIncompleteError = !sectionComplete
        ? "At least one pipeline must be added with valid header information. Each pipeline must have at least one valid ident." : null;

    return new SummaryScreenValidationResult(invalidPipelines, "pipeline", "must have all sections completed",
        sectionComplete,
        sectionIncompleteError);
  }

  @Override
  public List<MailMergeFieldMnem> getAvailableMailMergeFields(PwaApplicationType pwaApplicationType) {

    if (MailMergeFieldMnem.PL_NUMBER_LIST.appTypeIsSupported(pwaApplicationType)) {
      return List.of(MailMergeFieldMnem.PL_NUMBER_LIST);
    }

    return List.of();

  }

  @Override
  public Map<MailMergeFieldMnem, String> resolveMailMergeFields(PwaApplicationDetail pwaApplicationDetail) {

    var availableMergeFields = getAvailableMailMergeFields(pwaApplicationDetail.getPwaApplicationType());

    EnumMap<MailMergeFieldMnem, String> map = new EnumMap<>(MailMergeFieldMnem.class);

    if (availableMergeFields.contains(MailMergeFieldMnem.PL_NUMBER_LIST)) {

      var plNumbers = padPipelineService.getApplicationPipelineOverviews(pwaApplicationDetail)
          .stream()
          .map(PipelineOverview::getPipelineNumber)
          .sorted(PipelineNumberSortingUtil::compare)
          .collect(Collectors.joining(", "));

      map.put(MailMergeFieldMnem.PL_NUMBER_LIST, plNumbers);

    }

    return map;

  }

}
