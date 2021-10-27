package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import com.google.common.annotations.VisibleForTesting;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineOverview;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.dto.pipelines.PadPipelineId;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineIdent;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineIdentData;
import uk.co.ogauthority.pwa.model.form.location.CoordinateForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.PipelineIdentDataForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.PipelineIdentForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelines.PadPipelineIdentRepository;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.pipelinedatautils.PipelineIdentViewCollectorService;
import uk.co.ogauthority.pwa.service.validation.SummaryScreenValidationResult;
import uk.co.ogauthority.pwa.util.CoordinateUtils;
import uk.co.ogauthority.pwa.util.StringDisplayUtils;
import uk.co.ogauthority.pwa.util.forminputs.decimal.DecimalInput;

@Service
public class PadPipelineIdentService {

  private final PadPipelineIdentRepository padPipelineIdentRepository;
  private final PadPipelineIdentDataService identDataService;
  private final PadPipelinePersisterService padPipelinePersisterService;
  private final PipelineIdentFormValidator pipelineIdentFormValidator;
  private final PipelineIdentViewCollectorService pipelineIdentViewCollectorService;

  @Autowired
  public PadPipelineIdentService(PadPipelineIdentRepository padPipelineIdentRepository,
                                 PadPipelineIdentDataService identDataService,
                                 PadPipelinePersisterService padPipelinePersisterService,
                                 PipelineIdentFormValidator pipelineIdentFormValidator,
                                 PipelineIdentViewCollectorService pipelineIdentViewCollectorService) {
    this.padPipelineIdentRepository = padPipelineIdentRepository;
    this.identDataService = identDataService;
    this.padPipelinePersisterService = padPipelinePersisterService;
    this.pipelineIdentFormValidator = pipelineIdentFormValidator;
    this.pipelineIdentViewCollectorService = pipelineIdentViewCollectorService;
  }

  public PadPipelineIdent getIdent(PadPipeline pipeline, Integer identId) {
    return padPipelineIdentRepository.getPadPipelineIdentByPadPipelineAndId(pipeline, identId)
        .orElseThrow(() -> new PwaEntityNotFoundException(
            String.format("Couldn't find ident with id '%d' linked to pipeline with id '%d'", identId,
                pipeline.getId())));
  }

  public Map<PipelineId, List<IdentView>> getApplicationIdentViewsForPipelines(PwaApplicationDetail pwaApplicationDetail,
                                                                               Collection<PipelineId> pipelineIds) {
    return pipelineIdentViewCollectorService.getPipelineIdToIdentVewsMap(
        PadPipelineIdent.class,
        PadPipelineIdentData.class,
        () -> padPipelineIdentRepository.getAllByPadPipeline_Pipeline_IdInAndPadPipeline_PwaApplicationDetail(
            pipelineIds.stream()
                .map(PipelineId::asInt)
            .collect(Collectors.toSet()),
            pwaApplicationDetail
        ),
        identDataService::getAllPadPipelineIdentDataForIdents
    );
  }

  public IdentView getIdentView(PadPipeline pipeline, Integer identId) {
    var padIdent = getIdent(pipeline, identId);
    var identData = identDataService.getIdentData(padIdent);
    return new IdentView(identData);
  }

  public List<PadPipelineIdent> getAllIdents(PadPipeline padPipeline) {
    return padPipelineIdentRepository.getAllByPadPipeline(padPipeline);
  }

  public List<PadPipelineIdent> getAllByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail) {
    return padPipelineIdentRepository.getAllByPadPipeline_PwaApplicationDetail(pwaApplicationDetail);
  }

  private BigDecimal getTotalIdentLength(List<PadPipelineIdent> idents) {
    return idents.stream()
        .filter(ident -> ident.getLength() != null)
        .map(PadPipelineIdent::getLength)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }


  private PadPipelineIdentLocationValidationResult validateIdentsMatchHeaderLocation(PadPipeline padPipeline,
                                                                                     List<PadPipelineIdent> idents) {

    PadPipelineIdent firstIdent;
    PadPipelineIdent lastIdent;

    if (!idents.isEmpty()) {
      firstIdent = idents.get(0);
      lastIdent = idents.get(idents.size() - 1);
    } else {
      return PadPipelineIdentLocationValidationResult.createUnmatched(padPipeline);
    }

    //first check if ident from and to location match header
    var firstIdentMatches = firstIdent.getFromLocation().equals(padPipeline.getFromLocation());
    var lastIdentMatches = lastIdent.getToLocation().equals(padPipeline.getToLocation());

    //now check if coordinates match header only if they exist
    if (firstIdentMatches) {
      firstIdentMatches = !firstIdent.getFromCoordinates().hasValue() || firstIdent.getFromCoordinates().equals(
          padPipeline.getFromCoordinates());
    }
    if (lastIdentMatches) {
      lastIdentMatches = !lastIdent.getToCoordinates().hasValue() || lastIdent.getToCoordinates().equals(
          padPipeline.getToCoordinates());
    }

    return new PadPipelineIdentLocationValidationResult(padPipeline, firstIdent, lastIdent, firstIdentMatches, lastIdentMatches);
  }

  public SummaryScreenValidationResult getSummaryScreenValidationResult(PadPipeline padPipeline) {
    List<PadPipelineIdent> idents = getAllIdents(padPipeline)
        .stream()
        .sorted(Comparator.comparing(PadPipelineIdent::getIdentNo))
        .collect(Collectors.toUnmodifiableList());

    Map<String, String> invalidIdentIdAndDescriptorMap = new LinkedHashMap<>();
    Map<String, List<String>> objectIdToItemErrorSuffixesMap = new HashMap<>();
    idents.forEach(ident -> {
      if (!isIdentValid(padPipeline, ident)) {
        objectIdToItemErrorSuffixesMap.put(String.valueOf(ident.getPipelineIdentId()), List.of("must have all sections completed"));
        invalidIdentIdAndDescriptorMap.put(String.valueOf(ident.getPipelineIdentId()), "Ident " + ident.getIdentNo());
      }
    });


    boolean allIdentsFormsValid = invalidIdentIdAndDescriptorMap.isEmpty() && !idents.isEmpty();

    String errorMessage = null;
    var lengthValidationValid = true;
    if (!allIdentsFormsValid) {
      lengthValidationValid = false;
      errorMessage = "Add at least one ident";

    } else if (getTotalIdentLength(idents).compareTo(padPipeline.getLength()) != 0) {
      lengthValidationValid = false;
      errorMessage = "The total length of all idents must equal the total pipeline length of: " +
          StringDisplayUtils.formatDecimal2DpSeparatedSuffixedOrNull(padPipeline.getLength(), "m");
    }


    //ident forms are valid & ident lengths match header, now validate that ident locations match header
    var identHeaderLocationValidationResult = validateIdentsMatchHeaderLocation(padPipeline, idents);
    if (lengthValidationValid && !identHeaderLocationValidationResult.identsMatchHeaderLocation()) {

      objectIdToItemErrorSuffixesMap.put(
          identHeaderLocationValidationResult.getFirstIdentIdAsString(), new ArrayList<>());
      objectIdToItemErrorSuffixesMap.put(
          identHeaderLocationValidationResult.getLastIdentIdAsString(), new ArrayList<>());

      if (!identHeaderLocationValidationResult.firstIdentFromMatchesHeader()) {
        invalidIdentIdAndDescriptorMap.put(identHeaderLocationValidationResult.getFirstIdentIdAsString(), "");
        objectIdToItemErrorSuffixesMap.get(identHeaderLocationValidationResult.getFirstIdentIdAsString()).add(
                "The from structure and coordinates of the first ident must match the from structure " +
                    "and coordinates in the pipeline header");
      }

      if (!identHeaderLocationValidationResult.lastIdentToMatchesHeader()) {
        invalidIdentIdAndDescriptorMap.put(identHeaderLocationValidationResult.getLastIdentIdAsString(), "");
        objectIdToItemErrorSuffixesMap.get(identHeaderLocationValidationResult.getLastIdentIdAsString()).add(
                "The to structure and coordinates of the last ident must match the to structure and coordinates in the pipeline header");
      }
    }

    return new SummaryScreenValidationResult(
        invalidIdentIdAndDescriptorMap,
        "ident",
        objectIdToItemErrorSuffixesMap,
        lengthValidationValid && identHeaderLocationValidationResult.identsMatchHeaderLocation(),
        errorMessage
    );
  }

  private List<IdentView> createIdentViewsFromPipelineIdents(List<PadPipelineIdent> padPipelineIdents) {
    var identData = identDataService.getDataFromIdentList(padPipelineIdents);

    return identData.keySet()
        .stream()
        .sorted(Comparator.comparing(PadPipelineIdent::getIdentNo))
        .map(ident -> new IdentView(identData.get(ident)))
        .collect(Collectors.toUnmodifiableList());

  }

  public List<IdentView> getIdentViewsFromOverview(PipelineOverview pipeline) {
    var padPipelineId = Optional.ofNullable(pipeline.getPadPipelineId())
        .orElseThrow(() -> new PwaEntityNotFoundException(
            "No padPipelineId available from pipelineOverview. pipelineId:" + pipeline.getPipelineId())
        );
    var idents = padPipelineIdentRepository.getAllByPadPipeline_IdIn(List.of(padPipelineId));
    return createIdentViewsFromPipelineIdents(idents);

  }

  public List<IdentView> getIdentViews(PadPipeline pipeline) {
    var idents = padPipelineIdentRepository.getAllByPadPipeline(pipeline);
    return createIdentViewsFromPipelineIdents(idents);
  }

  public ConnectedPipelineIdentSummaryView getConnectedPipelineIdentSummaryView(PadPipeline pipeline) {
    List<IdentView> identViews = getIdentViews(pipeline);
    var list = new ArrayList<List<IdentView>>();
    var groupList = new ArrayList<IdentView>();
    list.add(groupList);

    for (int i = 0; i < identViews.size(); i++) {
      if (i == 0) {
        // If first ident, there's nothing to compare to.
        groupList.add(identViews.get(i));
        continue;
      }
      var previousView = identViews.get(i - 1);
      var currentView = identViews.get(i);
      // Compare "fromLocation" to the previous ident's "toLocation".
      // If locations are different, add to a new group. If locations are the same, add to the existing group.
      if (!previousView.getToLocation().equalsIgnoreCase(currentView.getFromLocation())) {
        groupList = new ArrayList<>();
        list.add(groupList);
      }
      groupList.add(identViews.get(i));
    }

    List<ConnectedPipelineIdentsView> connectedIdents = list.stream()
        .filter(viewList -> !viewList.isEmpty())
        .map(ConnectedPipelineIdentsView::new)
        .collect(Collectors.toUnmodifiableList());

    var totalIdentLength = identViews.stream()
        .filter(identView -> identView.getLength() != null)
        .map(IdentView::getLength)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    var totalIdentLengthDisplay = !totalIdentLength.equals(BigDecimal.ZERO)
        ? StringDisplayUtils.formatDecimal2DpSeparatedSuffixedOrNull(totalIdentLength, "m") : "0m";

    return new ConnectedPipelineIdentSummaryView(connectedIdents, totalIdentLengthDisplay);
  }

  public Optional<PadPipelineIdent> getMaxIdent(PadPipeline pipeline) {
    return padPipelineIdentRepository.findTopByPadPipelineOrderByIdentNoDesc(pipeline);
  }

  @Transactional
  public PadPipelineIdent addIdent(PadPipeline pipeline, PipelineIdentForm form) {

    var numberOfIdents = padPipelineIdentRepository.countAllByPadPipeline(pipeline);
    var ident = new PadPipelineIdent(pipeline, numberOfIdents.intValue() + 1);

    saveEntityUsingForm(ident, form);
    padPipelinePersisterService.savePadPipelineAndMaterialiseIdentData(ident.getPadPipeline());
    return ident;
  }

  @Transactional
  public void addIdentAtPosition(PadPipeline pipeline, PipelineIdentForm form, Integer position) {

    var ident = new PadPipelineIdent(pipeline, position);

    var idents = padPipelineIdentRepository.getAllByPadPipeline(pipeline);
    idents.stream()
        .filter(existingIdent -> existingIdent.getIdentNo() >= position)
        .forEachOrdered(existingIdent -> existingIdent.setIdentNo(existingIdent.getIdentNo() + 1));

    padPipelineIdentRepository.saveAll(idents);

    saveEntityUsingForm(ident, form);
    padPipelinePersisterService.savePadPipelineAndMaterialiseIdentData(ident.getPadPipeline());
  }

  @Transactional
  public void updateIdent(PadPipelineIdent ident, PipelineIdentForm form) {
    saveEntityUsingForm(ident, form);
    padPipelinePersisterService.savePadPipelineAndMaterialiseIdentData(ident.getPadPipeline());
  }

  @Transactional
  public Optional<PadPipelineIdent> getIdentByIdentNumber(PadPipeline pipeline, Integer identNumber) {
    return padPipelineIdentRepository.getByPadPipelineAndAndIdentNo(pipeline, identNumber);
  }

  public void saveEntityUsingForm(PadPipelineIdent ident, PipelineIdentForm form) {

    ident.setFromLocation(form.getFromLocation());
    ident.setFromCoordinates(CoordinateUtils.coordinatePairFromForm(form.getFromCoordinateForm()));
    ident.setToLocation(form.getToLocation());
    ident.setToCoordinates(CoordinateUtils.coordinatePairFromForm(form.getToCoordinateForm()));
    ident.setLength(form.getDefiningStructure()
        ? form.getLengthOptional().createBigDecimalOrNull() : form.getLength().createBigDecimalOrNull());
    ident.setDefiningStructure(form.getDefiningStructure());

    padPipelineIdentRepository.save(ident);

    var dataForm = form.getDataForm();
    if (form.getDefiningStructure()) {
      setNotDefiningStructureFieldsToBlank(dataForm);
    }

    identDataService.getOptionalOfIdentData(ident)
        .ifPresentOrElse(
            (padPipelineIdentData) -> identDataService.updateIdentData(ident, form.getDefiningStructure(), dataForm),
            () -> identDataService.addIdentData(ident, form.getDefiningStructure(), form.getDataForm()));
  }

  private void setNotDefiningStructureFieldsToBlank(PipelineIdentDataForm dataForm) {
    dataForm.setExternalDiameter(null);
    dataForm.setExternalDiameterMultiCore(null);
    dataForm.setInternalDiameter(null);
    dataForm.setInternalDiameterMultiCore(null);
    dataForm.setInsulationCoatingType(null);
    dataForm.setInsulationCoatingTypeMultiCore(null);
    dataForm.setWallThickness(null);
    dataForm.setWallThicknessMultiCore(null);
    dataForm.setMaop(null);
    dataForm.setMaopMultiCore(null);
  }

  public void mapEntityToForm(PadPipelineIdent ident, PipelineIdentForm form) {
    var fromForm = new CoordinateForm();
    var toForm = new CoordinateForm();
    CoordinateUtils.mapCoordinatePairToForm(ident.getFromCoordinates(), fromForm);
    CoordinateUtils.mapCoordinatePairToForm(ident.getToCoordinates(), toForm);

    form.setFromCoordinateForm(fromForm);
    form.setToCoordinateForm(toForm);
    form.setFromLocation(ident.getFromLocation());
    form.setToLocation(ident.getToLocation());
    form.setDefiningStructure(ident.getIsDefiningStructure());
    if (BooleanUtils.isTrue(ident.getIsDefiningStructure())) {
      form.setLengthOptional(new DecimalInput(ident.getLength()));
    } else {
      form.setLength(new DecimalInput(ident.getLength()));
    }
    var dataForm = identDataService.getDataFormOfIdent(ident);
    form.setDataForm(dataForm);
  }

  @Transactional
  public void removeIdent(PadPipelineIdent pipelineIdent) {
    identDataService.removeIdentData(pipelineIdent);
    padPipelineIdentRepository.delete(pipelineIdent);
    var remainingIdents = padPipelineIdentRepository.getAllByPadPipeline(pipelineIdent.getPadPipeline());

    remainingIdents.stream()
        .filter(ident -> ident.getIdentNo() > pipelineIdent.getIdentNo())
        .forEachOrdered(ident -> ident.setIdentNo(ident.getIdentNo() - 1));

    padPipelineIdentRepository.saveAll(remainingIdents);
    padPipelinePersisterService.savePadPipelineAndMaterialiseIdentData(pipelineIdent.getPadPipeline());
  }

  @Transactional
  public void removeAllIdents(PadPipeline padPipeline) {
    identDataService.removeIdentDataForPipeline(padPipeline);
    var idents = padPipelineIdentRepository.getAllByPadPipeline(padPipeline);
    padPipelineIdentRepository.deleteAll(idents);
  }

  public boolean isSectionValid(PadPipeline pipeline) {
    return getSummaryScreenValidationResult(pipeline).isSectionComplete();
  }

  public List<PadPipelineIdent> getAllIdentsByPadPipelineIds(List<PadPipelineId> padPipelineIds) {

    List<Integer> ids = padPipelineIds.stream()
        .map(PadPipelineId::asInt)
        .collect(Collectors.toList());

    return padPipelineIdentRepository.getAllByPadPipeline_IdIn(ids);

  }

  public void saveAll(Collection<PadPipelineIdent> padPipelineIdents) {
    padPipelineIdentRepository.saveAll(padPipelineIdents);
  }

  @VisibleForTesting
  boolean isIdentValid(PadPipeline padPipeline, PadPipelineIdent ident) {
    var form = new PipelineIdentForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    mapEntityToForm(ident, form);
    pipelineIdentFormValidator.validate(form, bindingResult, null, padPipeline.getCoreType());
    return !bindingResult.hasErrors();
  }

}
