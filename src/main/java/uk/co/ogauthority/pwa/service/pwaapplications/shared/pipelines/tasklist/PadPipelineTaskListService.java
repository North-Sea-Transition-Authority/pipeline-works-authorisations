package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.tasklist;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelines.ModifyPipelineController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelines.PipelineIdentsController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelines.PipelinesController;
import uk.co.ogauthority.pwa.model.dto.pipelines.PadPipelineId;
import uk.co.ogauthority.pwa.model.dto.pipelines.PadPipelineSummaryDto;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineMaterial;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.PipelineIdentForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PadPipelineTaskListItem;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview;
import uk.co.ogauthority.pwa.model.tasklist.TaskListEntry;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelines.PadPipelineRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.TaskInfo;
import uk.co.ogauthority.pwa.service.pwaapplications.options.PadOptionConfirmedService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineIdentService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PipelineIdentFormValidator;
import uk.co.ogauthority.pwa.service.validation.SummaryScreenValidationResult;

@Service
public class PadPipelineTaskListService implements ApplicationFormSectionService {

  private static final Logger LOGGER = LoggerFactory.getLogger(PadPipelineTaskListService.class);

  private final PadPipelineService padPipelineService;
  private final PadPipelineIdentService padPipelineIdentService;
  private final PadOptionConfirmedService padOptionConfirmedService;
  private final PadPipelineRepository padPipelineRepository;
  private final PipelineIdentFormValidator pipelineIdentFormValidator;
  private final RegulatorPipelineReferenceTaskService regulatorPipelineReferenceTaskService;
  private final PadPipelineDataCopierService padPipelineDataCopierService;

  @Autowired
  public PadPipelineTaskListService(PadPipelineService padPipelineService,
                                    PadPipelineIdentService padPipelineIdentService,
                                    PadOptionConfirmedService padOptionConfirmedService,
                                    PadPipelineRepository padPipelineRepository,
                                    PipelineIdentFormValidator pipelineIdentFormValidator,
                                    RegulatorPipelineReferenceTaskService regulatorPipelineReferenceTaskService,
                                    PadPipelineDataCopierService padPipelineDataCopierService) {
    this.padPipelineService = padPipelineService;
    this.padPipelineIdentService = padPipelineIdentService;
    this.padOptionConfirmedService = padOptionConfirmedService;
    this.padPipelineRepository = padPipelineRepository;
    this.pipelineIdentFormValidator = pipelineIdentFormValidator;
    this.regulatorPipelineReferenceTaskService = regulatorPipelineReferenceTaskService;
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

  @VisibleForTesting
  boolean doesPipelineHaveTasks(PadPipelineSummaryDto padPipelineSummaryDto) {
    return padPipelineService.isValidationRequiredByStatus(padPipelineSummaryDto.getPipelineStatus());
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

    var entryList = new ArrayList<TaskListEntry>();

    regulatorPipelineReferenceTaskService.getTaskListEntry(applicationContext, padPipelineTaskListHeader)
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

    // get all of the idents for the pipelines on the app, grouped by pad pipeline id
    var padPipelineIdToIdentListMap = padPipelineIdentService.getAllIdentsByPadPipelineIds(
        List.copyOf(padPipelineMap.keySet()))
        .stream()
        .collect(Collectors.groupingBy(ident -> new PadPipelineId(ident.getPadPipeline().getId())));

    overviews.forEach(pipelineOverview -> {

      var padPipeline = padPipelineMap.get(new PadPipelineId(pipelineOverview.getPadPipelineId()));
      var padPipelineId = new PadPipelineId(pipelineOverview.getPadPipelineId());
      boolean pipelineComplete = padPipelineService.isPadPipelineValid(padPipeline, detail.getPwaApplicationType());

      var idents = padPipelineIdToIdentListMap.getOrDefault(padPipelineId, List.of());

      // validate each ident on the pipeline, if one is invalid, the whole pipeline is incomplete
      if (padPipelineService.isValidationRequiredByStatus(padPipeline.getPipelineStatus())) {
        for (var ident : idents) {
          var identForm = new PipelineIdentForm();
          padPipelineIdentService.mapEntityToForm(ident, identForm);
          BindingResult bindingResult = new BeanPropertyBindingResult(identForm, "form");
          pipelineIdentFormValidator.validate(identForm, bindingResult, detail, pipelineOverview.getCoreType());
          if (bindingResult.hasErrors()) {
            pipelineComplete = false;
          }
        }
      }

      // if the pipeline has invalid idents (or no idents), it is invalid
      if (!pipelineComplete || idents.isEmpty()) {
        invalidPipelines.put(String.valueOf(padPipelineId.asInt()), pipelineOverview.getPipelineName());
      }

    });

    // section is complete if there's at least 1 pipeline, and no invalid pipelines
    boolean sectionComplete = !padPipelineIdToIdentListMap.isEmpty() && invalidPipelines.isEmpty();

    String sectionIncompleteError = !sectionComplete
        ? "At least one pipeline must be added with valid header information. Each pipeline must have at least one valid ident." : null;

    return new SummaryScreenValidationResult(invalidPipelines, "pipeline", "must have all sections completed",
        sectionComplete,
        sectionIncompleteError);
  }


}
