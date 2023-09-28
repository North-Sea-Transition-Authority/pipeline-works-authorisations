<#include '../layout.ftl'>

<#-- @ftlvariable name="assignableCases" type="java.util.Set<uk.co.ogauthority.pwa.features.reassignment.CaseReassignmentView>" -->


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
        <@fdsAction.button buttonText="Reassign cases"/>
        <@spring.bind "form.selectedApplicationIds"/>

        <#assign id=spring.status.expression?replace('[','')?replace(']','')>
        <#assign hasError=(spring.status.errorMessages?size > 0)>

        <div class="govuk-form-group <#if hasError> govuk-form-group--error</#if>">
          <#if hasError>
            <@fdsError.inputError inputId="${id}"/>
          </#if>

          <table id="selectCaseTable" class="govuk-table">
            <thead class="govuk-table__head">
              <tr id=class="govuk-table__row">
                <th class="govuk-table__header" scope="col">
                  <@pwaTableSelectionToggler.linksToggler tableId="selectCaseTable"
                  prefixText=""
                  selectAllLinkText="All" selectAllScreenReaderText="Sellect all reassignable cases"
                  selectNoneLinkText="None" selectNoneScreenReaderText="Select none of the reassignable cases" />
                </th>
                <th class="govuk-table__header" scope="col" >PWA application</th>
                <th class="govuk-table__header govuk-!-width-one-quarter" scope="col" >Project name</th>
                <th class="govuk-table__header" scope="col">Currently assigned case officer</th>
                <th class="govuk-table__header" scope="col">Case officer review requested</th>
              </tr>
            </thead>
            <tbody class="govuk-table__body">
              <#list assignableCases as case>
                <tr class="govuk-table__row">
                  <td class="govuk-table__cell">
                    <div class="govuk-checkboxes govuk-checkboxes--small">
                      <div class="govuk-checkboxes__item">
                          <#assign checkboxId>
                            reassignable-cases-${case.getApplicationId()?c}
                          </#assign>
                        <input class="govuk-checkboxes__input" id=${checkboxId} name="${spring.status.expression}" type="checkbox" value="${case.getApplicationId()?c}">
                        <label class="govuk-label govuk-checkboxes__label" for="case-checkbox-${checkboxId}">
                          <span class="govuk-visually-hidden">Select or de-select ${case.getPadReference()}</span>
                        </label>
                      </div>
                    </div>
                  </td>
                  <td class="govuk-table__cell">
                    ${case.getPadReference()}
                  </td>
                  <td class="govuk-table__cell">
                      ${case.getPadName()}
                  </td>
                  <td class="govuk-table__cell">
                    ${case.getAssignedCaseOfficer()}
                  </td>
                  <td class="govuk-table__cell">
                    ${case.getInCaseOfficerReviewSinceFormatted()}
                  </td>
                </tr>
              </#list>
            </tbody>
          </table>
        </div>
      </@fdsForm.htmlForm>
    </@fdsSearch.searchPageContent>
  </@fdsSearch.searchPage>
</@defaultPage>

