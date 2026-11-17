package uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.overview;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitId;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooType;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.OrganisationRoleOwnerDto;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.TreatyAgreement;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineIdentifier;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.modifyhuoo.PickableHuooPipelineType;

/**
 * Captures a set of organisationRoleOwners where they have an instance of that role associated with a pipeline and allows
 * easy display of the owner names and pipeline numbers.
 */
public final class PipelinesAndOrgRoleGroupView {

  private final Set<PipelineIdentifier> pipelineIdentifierSet;
  private final Set<OrganisationRoleOwnerDto> organisationRoleOwnerSet;
  private final List<String> pipelineNumbers;
  private final List<String> organisationNames;
  private final String sortKey;
  private final String groupKey;

  PipelinesAndOrgRoleGroupView(
      Set<PipelineIdentifier> pipelineIdentifierSet,
      Set<OrganisationRoleOwnerDto> organisationRoleOwnerSet,
      List<String> pipelineNumbers,
      List<String> organisationNames) {
    this.pipelineIdentifierSet = pipelineIdentifierSet;
    this.organisationRoleOwnerSet = organisationRoleOwnerSet;
    this.pipelineNumbers = pipelineNumbers.stream()
        .sorted(Comparator.comparing(String::toLowerCase))
        .collect(Collectors.toList());
    this.organisationNames = organisationNames.stream()
        .sorted(Comparator.comparing(String::toLowerCase))
        .collect(Collectors.toList());
    this.sortKey = String.join(",", this.pipelineNumbers);
    this.groupKey = createGroupKey(pipelineIdentifierSet);
  }

  /**
   * A pipeline or pipeline section belongs to at most one group per role, so a hash of the group's pipelines identifies
   * the group compactly. Used in URLs instead of listing every pipeline, which can exceed WAF query string limits.
   */
  private static String createGroupKey(Set<PipelineIdentifier> pipelineIdentifiers) {
    var joinedPickableStrings = pipelineIdentifiers.stream()
        .map(PickableHuooPipelineType::createPickableString)
        .sorted()
        .collect(Collectors.joining("\n"));
    try {
      var digest = MessageDigest.getInstance("SHA-256").digest(joinedPickableStrings.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(digest);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 algorithm not available", e);
    }
  }

  public Set<PipelineIdentifier> getPipelineIdentifierSet() {
    return pipelineIdentifierSet;
  }

  public Set<OrganisationRoleOwnerDto> getOrganisationRoleOwnerSet() {
    return organisationRoleOwnerSet;
  }


  public Set<OrganisationUnitId> getOrganisationIdsOfRoleOwners() {
    return this.organisationRoleOwnerSet.stream()
        .filter(o -> HuooType.PORTAL_ORG.equals(o.getHuooType()))
        .map(OrganisationRoleOwnerDto::getOrganisationUnitId)
        .collect(Collectors.toSet());
  }

  public Set<TreatyAgreement> getTreatyAgreementsOfRoleOwners() {
    return this.organisationRoleOwnerSet.stream()
        .filter(o -> HuooType.TREATY_AGREEMENT.equals(o.getHuooType()))
        .map(OrganisationRoleOwnerDto::getTreatyAgreement)
        .collect(Collectors.toSet());
  }

  public List<String> getPipelineNumbers() {
    return pipelineNumbers;
  }

  public List<String> getOrganisationNames() {
    return organisationNames;
  }

  public String getSortKey() {
    return sortKey;
  }

  public String getGroupKey() {
    return groupKey;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PipelinesAndOrgRoleGroupView that = (PipelinesAndOrgRoleGroupView) o;
    return Objects.equals(pipelineIdentifierSet, that.pipelineIdentifierSet)
        && Objects.equals(organisationRoleOwnerSet, that.organisationRoleOwnerSet)
        && Objects.equals(pipelineNumbers, that.pipelineNumbers)
        && Objects.equals(organisationNames, that.organisationNames)
        && Objects.equals(sortKey, that.sortKey)
        && Objects.equals(groupKey, that.groupKey);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        pipelineIdentifierSet,
        organisationRoleOwnerSet,
        pipelineNumbers,
        organisationNames,
        sortKey,
        groupKey
    );
  }

  @Override
  public String toString() {
    return "PipelinesAndOrgRoleGroupView{" +
        "pipelineIdSet=" + pipelineIdentifierSet +
        ", organisationRoleOwnerSet=" + organisationRoleOwnerSet +
        ", pipelineNumbers=" + pipelineNumbers +
        ", organisationNames=" + organisationNames +
        ", sortKey='" + sortKey + '\'' +
        ", groupKey='" + groupKey + '\'' +
        '}';
  }
}
