package uk.co.ogauthority.pwa.service.documents.instances;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.exception.documents.DocumentInstanceException;
import uk.co.ogauthority.pwa.model.documents.instances.DocumentInstanceSectionClauseVersionDto;
import uk.co.ogauthority.pwa.model.documents.templates.DocumentTemplateDto;
import uk.co.ogauthority.pwa.model.documents.templates.TemplateSectionClauseVersionDto;
import uk.co.ogauthority.pwa.model.documents.view.DocumentView;
import uk.co.ogauthority.pwa.model.documents.view.SectionClauseVersionView;
import uk.co.ogauthority.pwa.model.documents.view.SectionView;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstance;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstanceSectionClause;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstanceSectionClauseVersion;
import uk.co.ogauthority.pwa.model.entity.documents.templates.DocumentTemplateSection;
import uk.co.ogauthority.pwa.model.entity.enums.documents.DocumentTemplateMnem;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocumentSection;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.enums.documents.PwaDocumentType;
import uk.co.ogauthority.pwa.model.enums.documents.SectionClauseVersionStatus;
import uk.co.ogauthority.pwa.model.form.documents.ClauseForm;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;
import uk.co.ogauthority.pwa.repository.documents.instances.DocumentInstanceRepository;
import uk.co.ogauthority.pwa.repository.documents.instances.DocumentInstanceSectionClauseRepository;
import uk.co.ogauthority.pwa.repository.documents.instances.DocumentInstanceSectionClauseVersionDtoRepository;
import uk.co.ogauthority.pwa.repository.documents.instances.DocumentInstanceSectionClauseVersionRepository;
import uk.co.ogauthority.pwa.service.documents.SectionClauseCreator;

@Service
public class DocumentInstanceService {

  private final DocumentInstanceRepository documentInstanceRepository;
  private final DocumentInstanceSectionClauseRepository instanceSectionClauseRepository;
  private final DocumentInstanceSectionClauseVersionRepository instanceSectionClauseVersionRepository;
  private final SectionClauseCreator sectionClauseCreator;
  private final Clock clock;
  private final DocumentInstanceSectionClauseVersionDtoRepository sectionClauseVersionDtoRepository;

  @Autowired
  public DocumentInstanceService(DocumentInstanceRepository documentInstanceRepository,
                                 DocumentInstanceSectionClauseRepository instanceSectionClauseRepository,
                                 DocumentInstanceSectionClauseVersionRepository instanceSectionClauseVersionRepository,
                                 SectionClauseCreator sectionClauseCreator,
                                 @Qualifier("utcClock") Clock clock,
                                 DocumentInstanceSectionClauseVersionDtoRepository sectionClauseVersionDtoRepository) {
    this.documentInstanceRepository = documentInstanceRepository;
    this.instanceSectionClauseRepository = instanceSectionClauseRepository;
    this.instanceSectionClauseVersionRepository = instanceSectionClauseVersionRepository;
    this.sectionClauseCreator = sectionClauseCreator;
    this.clock = clock;
    this.sectionClauseVersionDtoRepository = sectionClauseVersionDtoRepository;
  }

  /**
   * Create a document instance based on a document template.
   * @param pwaApplication document is being created for
   * @param documentTemplateDto object containing document template information
   * @param creatingPerson person creating document
   */
  public void createFromDocumentDto(PwaApplication pwaApplication,
                                    DocumentTemplateDto documentTemplateDto,
                                    Person creatingPerson) {

    // find instance if present already, otherwise create it
    var instance = getDocumentInstance(pwaApplication, documentTemplateDto.getDocumentTemplate().getMnem())
        .orElseGet(() -> new DocumentInstance(documentTemplateDto.getDocumentTemplate(), pwaApplication, clock.instant()));

    var templateClauseVersionToInstanceClauseVersionMap =
        new HashMap<TemplateSectionClauseVersionDto, DocumentInstanceSectionClauseVersion>();

    var templateClauseIdToNewInstanceClauseMap = new HashMap<Integer, DocumentInstanceSectionClause>();

    documentTemplateDto.getSections().forEach(section -> {

      // for each current clause version within a section
      section.getClauses().forEach(templateClauseVersion -> {

        var createdFromTemplateClause = templateClauseVersion.getTemplateClauseRecord();

        // create a corresponding instance clause linked to the template clause it was created from
        var clause = new DocumentInstanceSectionClause();
        clause.setDocumentInstance(instance);
        clause.setDocumentTemplateSectionClause(createdFromTemplateClause);

        // store the new instance clause against the id of its associated template clause
        templateClauseIdToNewInstanceClauseMap.put(createdFromTemplateClause.getId(), clause);

        // create a new instance clause version from the template clause version
        DocumentInstanceSectionClauseVersion newClauseVersion = sectionClauseCreator
            .createInstanceClauseVersionFromTemplate(templateClauseVersion, creatingPerson);

        // link the newly created instance clause to its version
        newClauseVersion.setDocumentInstanceSectionClause(clause);

        // store the creating template clause version and the new instance clause based on it for later lookup
        templateClauseVersionToInstanceClauseVersionMap.put(templateClauseVersion, newClauseVersion);

      });

    });

    // set parent clauses as required, returning to list before saving
    List<DocumentInstanceSectionClauseVersion> modifiedInstanceClauseVersions = templateClauseVersionToInstanceClauseVersionMap
        .entrySet()
        .stream()
        .peek(entry -> setParentIfNeeded(entry.getKey(), entry.getValue(), templateClauseIdToNewInstanceClauseMap))
        .map(Map.Entry::getValue)
        .collect(Collectors.toList());

    documentInstanceRepository.save(instance);
    instanceSectionClauseRepository.saveAll(templateClauseIdToNewInstanceClauseMap.values());
    instanceSectionClauseVersionRepository.saveAll(modifiedInstanceClauseVersions);

  }

  /**
   * If the template clause has a parent, set the parent on the newly created instance clause appropriately by looking
   * up the instance parent using the template parent.
   * @param templateClauseVersion that created the instance clause
   * @param newInstanceClauseVersion instance clause created from template clause
   * @param templateClauseIdToNewInstanceClauseMap map of template clause ids to the instance clauses created from them
   */
  private void setParentIfNeeded(TemplateSectionClauseVersionDto templateClauseVersion,
                                 DocumentInstanceSectionClauseVersion newInstanceClauseVersion,
                                 Map<Integer, DocumentInstanceSectionClause> templateClauseIdToNewInstanceClauseMap) {

    if (templateClauseVersion.getParentTemplateClause() != null) {

      // lookup the parent template clause and get the instance clause that was created from that
      var parentTemplateClause = templateClauseVersion.getParentTemplateClause();

      var associatedNewInstanceClause = templateClauseIdToNewInstanceClauseMap
          .get(parentTemplateClause.getId());

      // set the correct instance clause as the parent of the instance clause being processed
      newInstanceClauseVersion.setParentDocumentInstanceSectionClause(associatedNewInstanceClause);

    }

  }

  /**
   * Remove all clauses linked to a document instance for an application, to allow for reloading.
   * @param pwaApplication that document is being cleared for
   * @param templateMnem to identify which document to clear
   */
  public void clearClauses(PwaApplication pwaApplication,
                           DocumentTemplateMnem templateMnem) {

    var instance = getDocumentInstance(pwaApplication, templateMnem)
        .orElseThrow(() -> new EntityNotFoundException(String.format(
            "Couldn't find doc instance for pwa app with id [%s] and doc template mnem [%s]",
            pwaApplication.getId(),
            templateMnem.name())));

    var clauses = instanceSectionClauseRepository.findAllByDocumentInstance(instance);

    var instanceClauseVersions = instanceSectionClauseVersionRepository
        .findAllByDocumentInstanceSectionClauseIn(clauses);

    instanceSectionClauseVersionRepository.deleteAll(instanceClauseVersions);
    instanceSectionClauseRepository.deleteAll(clauses);

  }

  public Optional<DocumentInstance> getDocumentInstance(PwaApplication application,
                                                        DocumentTemplateMnem templateMnem) {
    return documentInstanceRepository.findByPwaApplicationAndDocumentTemplate_Mnem(application, templateMnem);
  }

  public DocumentView getDocumentView(DocumentInstance instance) {

    var clauseVersionDtos = sectionClauseVersionDtoRepository.findAllByDiId(instance.getId());

    return buildDocumentViewFromClauseVersionDtos(instance, clauseVersionDtos);

  }

  public DocumentView getDocumentView(DocumentInstance instance,
                                      DocumentSection documentSection) {

    var clauseVersionDtos = sectionClauseVersionDtoRepository.findAllByDiId_AndSectionNameEquals(instance.getId(), documentSection.name());

    return buildDocumentViewFromClauseVersionDtos(instance, clauseVersionDtos);

  }

  private DocumentView buildDocumentViewFromClauseVersionDtos(DocumentInstance documentInstance,
                                                              Collection<DocumentInstanceSectionClauseVersionDto> clauseVersionDtos) {

    var sectionToClauseVersionMap = clauseVersionDtos.stream()
        .filter(dto -> !SectionClauseVersionStatus.DELETED.equals(SectionClauseVersionStatus.valueOf(dto.getStatus())))
        .collect(Collectors.groupingBy(DocumentInstanceSectionClauseVersionDto::getSectionName));

    var docView = new DocumentView(PwaDocumentType.INSTANCE, documentInstance.getDocumentTemplate().getMnem());

    var sections = sectionToClauseVersionMap.entrySet().stream()
        .map(entry -> createSectionView(DocumentSection.valueOf(entry.getKey()).getDisplayName(), entry.getValue()))
        .collect(Collectors.toList());

    docView.setSections(sections);

    return docView;

  }

  private SectionView createSectionView(String sectionName, List<DocumentInstanceSectionClauseVersionDto> clauseVersionDtos) {

    var sectionView = new SectionView();
    sectionView.setName(sectionName);

    // group clauses according to their level in the hierarchy, i.e. 1 = top-level, 3 = lowest level, 2 is a child of 1 etc
    var clauseViewLevelToViewMap = clauseVersionDtos.stream()
        .map(SectionClauseVersionView::from)
        .collect(Collectors.groupingBy(SectionClauseVersionView::getLevelNumber));

    // set lowest level children on their parents
    updateParentsWithChildren(clauseViewLevelToViewMap, 3);

    // set mid level children on their parents
    updateParentsWithChildren(clauseViewLevelToViewMap, 2);

    // now we've built the hierarchy, set the top-level clauses (now containing their children) onto the section view
    sectionView.setClauses(clauseViewLevelToViewMap.get(1));

    buildSidebarLinks(sectionView);

    return sectionView;

  }

  private void updateParentsWithChildren(Map<Integer, List<SectionClauseVersionView>> levelNumberToClauseVersionMap,
                                         Integer childLevel) {

    int parentLevel = childLevel - 1;

    levelNumberToClauseVersionMap.getOrDefault(childLevel, List.of()).forEach(child -> {

      levelNumberToClauseVersionMap.get(parentLevel).stream()
          .filter(parent -> Objects.equals(parent.getClauseId(), child.getParentClauseId()))
          .findFirst()
          .ifPresent(parent -> parent.getChildClauses().add(child));

    });

  }

  private void buildSidebarLinks(SectionView sectionView) {

    var sidebarLinks = new ArrayList<SidebarSectionLink>();

    sectionView.getClauses().forEach(clause -> {

      var link = SidebarSectionLink.createAnchorLink(clause.getName(), "#clauseId-" + clause.getClauseId());

      sidebarLinks.add(link);

      clause.getChildClauses().forEach(child -> {

        var l = SidebarSectionLink.createAnchorLink(child.getName(), "#clauseId-" + child.getClauseId());

        sidebarLinks.add(l);

      });

    });

    sectionView.setSidebarSectionLinks(sidebarLinks);

  }

  public SectionClauseVersionView getSectionClauseView(Integer clauseId) {

    var docInstanceSectionClauseVersion = getInstanceClauseVersionByClauseIdOrThrow(clauseId);
    var docInstance = docInstanceSectionClauseVersion.getDocumentInstanceSectionClause().getDocumentInstance();
    var docView = getDocumentView(docInstance);

    Map<Integer, SectionClauseVersionView> allLevelsSectionClauseVersionViewsMap = new HashMap<>();
    docView.getSections().stream()
        .flatMap(sectionView -> sectionView.getClauses().stream())
        .forEach(sectionClauseVersionView -> {
          allLevelsSectionClauseVersionViewsMap.put(sectionClauseVersionView.getClauseId(), sectionClauseVersionView);
          sectionClauseVersionView.getChildClauses().forEach(childClause -> {
            allLevelsSectionClauseVersionViewsMap.put(childClause.getClauseId(), childClause);
            childClause.getChildClauses().forEach(
                subChildClause -> allLevelsSectionClauseVersionViewsMap.put(subChildClause.getClauseId(), subChildClause));
          });
        });

    var sectionClauseView = allLevelsSectionClauseVersionViewsMap.get(clauseId);
    if (sectionClauseView != null) {
      return sectionClauseView;
    }
    throw(new PwaEntityNotFoundException("Could not find SectionClauseVersionView with clause id " + clauseId));
  }

  public DocumentInstanceSectionClauseVersion getInstanceClauseVersionByClauseIdOrThrow(Integer clauseId) {
    return instanceSectionClauseVersionRepository
        .findByDocumentInstanceSectionClause_IdAndTipFlagIsTrue(clauseId)
        .orElseThrow(() -> new DocumentInstanceException(String.format("Couldn't find instance clause with ID: [%s]", clauseId)));
  }

  /**
   * Add a new instance clause to a doc instance.
   * @param documentInstance clause is being added to
   * @param section clause is being added to
   * @param newParent clause that should be set as the parent on the new clause
   * @param form containing clause info to set
   * @param creatingPerson person creating clause
   * @param levelOrder to set for the new clause
   * @return the newly created version
   */
  private DocumentInstanceSectionClauseVersion addClause(DocumentInstance documentInstance,
                                                         DocumentTemplateSection section,
                                                         @Nullable DocumentInstanceSectionClause newParent,
                                                         Integer levelOrder,
                                                         ClauseForm form,
                                                         Person creatingPerson) {

    var newClause = new DocumentInstanceSectionClause();
    var newClauseVersion = new DocumentInstanceSectionClauseVersion();

    newClause.setDocumentInstance(documentInstance);
    newClause.setDocumentTemplateSection(section);

    newClauseVersion.setParentDocumentInstanceSectionClause(newParent);

    newClauseVersion.setDocumentInstanceSectionClause(newClause);

    sectionClauseCreator.setCommonData(
        newClauseVersion,
        form.getName(),
        form.getText(),
        levelOrder,
        SectionClauseVersionStatus.ACTIVE,
        1,
        creatingPerson);

    instanceSectionClauseRepository.save(newClause);
    instanceSectionClauseVersionRepository.save(newClauseVersion);

    return newClauseVersion;

  }

  /**
   * Add a new clause on the same level as but one position after the passed-in clause.
   */
  @Transactional
  public void addClauseAfter(DocumentInstanceSectionClauseVersion versionToAddAfter, ClauseForm form, Person creatingPerson) {
    addClause(
        versionToAddAfter.getDocumentInstanceSectionClause().getDocumentInstance(),
        versionToAddAfter.getDocumentInstanceSectionClause().getSection(),
        versionToAddAfter.getParentDocumentInstanceSectionClause(),
        versionToAddAfter.getLevelOrder() + 1,
        form,
        creatingPerson);
  }

  /**
   * Add a new clause on the same level as but one position before the passed-in clause.
   * The passed-in clause and any clauses following that (on the same level) are re-ordered.
   */
  @Transactional
  public void addClauseBefore(DocumentInstanceSectionClauseVersion versionToAddBefore, ClauseForm form, Person creatingPerson) {

    // add a new clause one position higher than the one we are adding before
    var newClauseVersion = addClause(
        versionToAddBefore.getDocumentInstanceSectionClause().getDocumentInstance(),
        versionToAddBefore.getDocumentInstanceSectionClause().getSection(),
        versionToAddBefore.getParentDocumentInstanceSectionClause(),
        versionToAddBefore.getLevelOrder() - 1,
        form,
        creatingPerson);

    var docInstance = newClauseVersion.getDocumentInstanceSectionClause().getDocumentInstance();
    var parent = newClauseVersion.getParentDocumentInstanceSectionClause();

    // increment the position of everything ahead of our new clause within its level, which will leave a gap
    var updatedClauseVersions = instanceSectionClauseVersionRepository
        .findByDocumentInstanceSectionClause_DocumentInstanceAndParentDocumentInstanceSectionClause(docInstance, parent).stream()
        .filter(clauseVersion -> clauseVersion.getLevelOrder() > newClauseVersion.getLevelOrder())
        .peek(clauseVersion -> clauseVersion.setLevelOrder(clauseVersion.getLevelOrder() + 1))
        .collect(Collectors.toList());

    // fill the gap left by the reordering by bumping the order on our new clause
    newClauseVersion.setLevelOrder(newClauseVersion.getLevelOrder() + 1);
    updatedClauseVersions.add(newClauseVersion);

    instanceSectionClauseVersionRepository.saveAll(updatedClauseVersions);

  }

  /**
   * Add a child clause for the passed-in clause.
   */
  @Transactional
  public void addSubClause(DocumentInstanceSectionClauseVersion versionToAddSubFor, ClauseForm form, Person creatingPerson) {

    addClause(
        versionToAddSubFor.getDocumentInstanceSectionClause().getDocumentInstance(),
        versionToAddSubFor.getDocumentInstanceSectionClause().getSection(),
        versionToAddSubFor.getDocumentInstanceSectionClause(),
        1,
        form,
        creatingPerson);

  }

  @Transactional
  public void editClause(DocumentInstanceSectionClauseVersion clauseBeingEdited, ClauseForm form, Person editingPerson) {

    var newClauseVersion = new DocumentInstanceSectionClauseVersion();

    newClauseVersion.setDocumentInstanceSectionClause(clauseBeingEdited.getDocumentInstanceSectionClause());
    newClauseVersion.setParentDocumentInstanceSectionClause(clauseBeingEdited.getParentDocumentInstanceSectionClause());

    sectionClauseCreator.setCommonData(
        newClauseVersion,
        form.getName(),
        form.getText(),
        clauseBeingEdited.getLevelOrder(),
        SectionClauseVersionStatus.ACTIVE,
        clauseBeingEdited.getVersionNo() + 1,
        editingPerson);

    clauseBeingEdited.setTipFlag(false);
    clauseBeingEdited.setEndedTimestamp(clock.instant());
    clauseBeingEdited.setEndedByPersonId(editingPerson.getId());
    clauseBeingEdited.setStatus(SectionClauseVersionStatus.DELETED);

    instanceSectionClauseVersionRepository.saveAll(List.of(clauseBeingEdited, newClauseVersion));

  }

  @Transactional
  public void removeClause(Integer clauseId, Person removingPerson) {

    var parentClauses = getInstanceClauseVersionByClauseIdOrThrow(clauseId);
    var endTime = clock.instant();

    setClauseAsDeleted(parentClauses, removingPerson, endTime);

    var childClauses =
        instanceSectionClauseVersionRepository.findByDocumentInstanceSectionClause_DocumentInstanceAndParentDocumentInstanceSectionClause(
        parentClauses.getDocumentInstanceSectionClause().getDocumentInstance(),
        parentClauses.getDocumentInstanceSectionClause()
    );

    var subChildClauses = new ArrayList<DocumentInstanceSectionClauseVersion>();
    childClauses.forEach(childClause -> {
      setClauseAsDeleted(childClause, removingPerson, endTime);
      var activeSubChildClauses =
          instanceSectionClauseVersionRepository.findByDocumentInstanceSectionClause_DocumentInstanceAndParentDocumentInstanceSectionClause(
              childClause.getDocumentInstanceSectionClause().getDocumentInstance(),
              childClause.getDocumentInstanceSectionClause()
          );
      activeSubChildClauses.forEach(subChildClause -> {
        setClauseAsDeleted(subChildClause, removingPerson, endTime);
        subChildClauses.add(subChildClause);
      });
    });

    instanceSectionClauseVersionRepository.save(parentClauses);
    instanceSectionClauseVersionRepository.saveAll(childClauses);
    instanceSectionClauseVersionRepository.saveAll(subChildClauses);
  }

  private void setClauseAsDeleted(DocumentInstanceSectionClauseVersion sectionClauseVersion, Person removingPerson, Instant endTime) {
    sectionClauseVersion.setStatus(SectionClauseVersionStatus.DELETED);
    sectionClauseVersion.setEndedByPersonId(removingPerson.getId());
    sectionClauseVersion.setEndedTimestamp(endTime);
  }

}
