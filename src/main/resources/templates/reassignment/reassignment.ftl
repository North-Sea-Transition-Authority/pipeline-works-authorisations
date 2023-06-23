<#include '../layout.ftl'>

<#-- @ftlvariable name="assignableCases" type="java.util.List<uk.co.ogauthority.pwa.model.entity.pwaapplications.search>" -->


<@defaultPage htmlTitle="case reassignment" pageHeading="Reassign case officers" topNavigation=true fullWidthColumn=true wrapperWidth=true>
  <@fdsSearch.searchPage>
    <@fdsSearch.searchFilter formActionUrl="${springUrl(filterURL)}">
      <@fdsSearch.searchFilterList filterButtonItemText="" clearFilterText="Clear filters" clearFilterUrl=springUrl(clearURL)>
        <@fdsSearch.searchFilterItem itemName="Case officer" expanded=true>
          <@fdsSearchSelector.searchSelectorEnhanced path="filterForm.caseOfficerPersonId" options=caseOfficerCandidates labelText="Select a case officer" optionalInputDefault="Any" labelClass="govuk-visually-hidden" />
        </@fdsSearch.searchFilterItem>
      </@fdsSearch.searchFilterList>
    </@fdsSearch.searchFilter>
    <@fdsSearch.searchPageContent>
      <@fdsForm.htmlForm>
        <@fdsAction.button buttonText="Reassign Cases"/>
        <@spring.bind "form.selectedCases"/>
        <table id="selectCaseTable" class="govuk-table">
          <thead class="govuk-table__head">
            <tr id=class="govuk-table__row">
              <th class="govuk-table__header" scope="col">
                <@pwaTableSelectionToggler.linksToggler tableId="selectCaseTable"
                prefixText=""
                selectAllLinkText="All" selectAllScreenReaderText="Select all available pipelines"
                selectNoneLinkText="None" selectNoneScreenReaderText="Select none of the available pipelines" />
              </th>
              <th class="govuk-table__header govuk-!-width-one-third" scope="col" >PWA application</th>
              <th class="govuk-table__header" scope="col">Currently assigned Case officer</th>
              <th class="govuk-table__header govuk-!-width-one-quarter" scope="col">In review since</th>
            </tr>
          </thead>
          <tbody class="govuk-table__body">
            <#list assignableCases as case>
              <tr class="govuk-table__row">
                <td class="govuk-table__cell">
                  <div class="govuk-checkboxes govuk-checkboxes--small">
                    <div class="govuk-checkboxes__item">
                        <#assign checkboxId>
                          reassignable-cases-${case.getPwaApplicationDetailId()}
                        </#assign>
                      <input class="govuk-checkboxes__input" id=${checkboxId} name="${spring.status.expression}" type="checkbox" value="${case.getPwaApplicationDetailId()}">
                      <label class="govuk-label govuk-checkboxes__label" for="pipeline-checkbox-${checkboxId}">
                        <span class="govuk-visually-hidden">Select or de-select ${case.getPwaApplicationDetailId()}</span>
                      </label>
                    </div>
                  </div>
                </td>
                <td class="govuk-table__cell">
                  ${case.getPwaReference()}
                </td>
                <td class="govuk-table__cell">
                  ${case.getCaseOfficerName()}
                </td>
                <td class="govuk-table__cell">
                  ${case.getPadStatusTimestamp()?datetime.iso}
                </td>
              </tr>
            </#list>
          </tbody>
        </table>
      </@fdsForm.htmlForm>
    </@fdsSearch.searchPageContent>
  </@fdsSearch.searchPage>
</@defaultPage>

