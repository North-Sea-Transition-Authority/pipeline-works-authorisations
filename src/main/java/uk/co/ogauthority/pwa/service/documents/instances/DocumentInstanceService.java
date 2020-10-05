package uk.co.ogauthority.pwa.service.documents.instances;

import java.time.Clock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.documents.instances.DocumentInstanceSectionClauseVersionDto;
import uk.co.ogauthority.pwa.model.documents.templates.DocumentTemplateDto;
import uk.co.ogauthority.pwa.model.documents.templates.TemplateSectionClauseVersionDto;
import uk.co.ogauthority.pwa.model.documents.view.DocumentView;
import uk.co.ogauthority.pwa.model.documents.view.SectionClauseVersionView;
import uk.co.ogauthority.pwa.model.documents.view.SectionView;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstance;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstanceSectionClause;
import uk.co.ogauthority.pwa.model.entity.documents.instances.DocumentInstanceSectionClauseVersion;
import uk.co.ogauthority.pwa.model.entity.enums.documents.DocumentTemplateMnem;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
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
        .findAllByDocumentInstanceSectionClauseInAndTipFlagIsTrue(clauses);

    instanceSectionClauseVersionRepository.deleteAll(instanceClauseVersions);
    instanceSectionClauseRepository.deleteAll(clauses);

  }

  public Optional<DocumentInstance> getDocumentInstance(PwaApplication application,
                                                        DocumentTemplateMnem templateMnem) {
    return documentInstanceRepository.findByPwaApplicationAndDocumentTemplate_Mnem(application, templateMnem);
  }

  public DocumentView getDocumentView(PwaApplication application,
                                      DocumentTemplateMnem templateMnem) {

    var instance = documentInstanceRepository.findByPwaApplicationAndDocumentTemplate_Mnem(application, templateMnem)
        .orElseThrow(() -> new PwaEntityNotFoundException(
            String.format("Couldn't find doc instance for app with id [%s] and template mnem [%s]",
                application.getId(),
                templateMnem.name())));

    var clauseVersionDtos = sectionClauseVersionDtoRepository.findAllByDiId(instance.getId());

    var sectionToClauseVersionMap = clauseVersionDtos.stream()
        .collect(Collectors.groupingBy(DocumentInstanceSectionClauseVersionDto::getSectionName));

    var docView = new DocumentView();
    docView.setDocumentTemplate(instance.getDocumentTemplate());

    var sections = sectionToClauseVersionMap.entrySet().stream()
        .map(entry -> createSectionView(entry.getKey(), entry.getValue()))
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

      var link = SidebarSectionLink.createAnchorLink(clause.getName(), "#clauseId-" + clause.getId());

      sidebarLinks.add(link);

      clause.getChildClauses().forEach(child -> {

        var l = SidebarSectionLink.createAnchorLink(child.getName(), "#clauseId-" + child.getId());

        sidebarLinks.add(l);

      });

    });

    sectionView.setSidebarSectionLinks(sidebarLinks);

  }

}
