<#include '../../layout.ftl'>
<#import '/spring.ftl' as spring>

<#-- @ftlvariable name="pickableOrgDetailOptions" type="java.util.List<uk.co.ogauthority.pwa.model.form.pwaapplications.views.PickableOrganisationUnitDetail>" -->

<#macro pickableOrgDetailsTableSelection path pickableOrgDetailOptions readOnlySelected=false caption="" captionClass="">
    <@spring.bind path/>
    <#local id=spring.status.expression?replace('[','')?replace(']','')>
    <#local hasError=(spring.status.errorMessages?size > 0)>

  <div class="govuk-form-group <#if hasError> govuk-form-group--error</#if>">
      <#if hasError>
          <@fdsError.inputError inputId="${id}"/>
      </#if>
    <table id="${id}" class="govuk-table">
        <#if caption?hasContent>
          <caption class="govuk-table__caption ${captionClass!""}">${caption}</caption>
        </#if>
      <thead class="govuk-table__head">
      <tr class="govuk-table__row">

        <th class="govuk-table__header ${readOnlySelected?then("govuk-visually-hidden", "")}" scope="col">
            <@pwaTableSelectionToggler.linksToggler tableId=id
            selectAllText="Select all" selectAllScreenReaderText=" available organisations"
            selectNoneText="Select none" selectNoneScreenReaderText=" of the available organisations" />
        </th>
        <th class="govuk-table__header govuk-!-width-one-third" scope="col">Company name</th>
        <th class="govuk-table__header govuk-!-width-one-quarter" scope="col">Registered number</th>
        <th class="govuk-table__header govuk-!-width-one-third" scope="col">Address</th>
      </tr>
      </thead>
      <tbody class="govuk-table__body">
      <#assign selectedItems = [] />
      <#list spring.stringStatusValue?split(",") as item>
          <#assign selectedItems = selectedItems + [item] />
      </#list>
      <#list pickableOrgDetailOptions as orgDetail>
          <#if orgDetail?counter != 1>
              <#assign checkboxId = id + '-' + orgDetail?counter/>
          <#else>
              <#assign checkboxId = id/>
          </#if>
          <#assign isSelected = selectedItems?seqContains("${orgDetail.getOrgUnitId()?c}") />
          <#if ((readOnlySelected && isSelected) || !readOnlySelected)>
            <tr class="govuk-table__row">

              <td class="govuk-table__cell ${readOnlySelected?then("govuk-visually-hidden", "")}">
                <div class="govuk-checkboxes govuk-checkboxes--small">
                  <div class="govuk-checkboxes__item">
                    <input class="govuk-checkboxes__input" id="org-detail-checkbox-${checkboxId}"
                           name="${spring.status.expression}" type="checkbox" value="${orgDetail.getOrgUnitId()?c}"
                           <#if isSelected>checked</#if>>
                    <label class="govuk-label govuk-checkboxes__label" for="org-detail-checkbox-${checkboxId}"><span
                        class="govuk-visually-hidden">Select or de-select ${orgDetail.getCompanyName()}</span>&nbsp;</label>
                  </div>
                </div>
              </td>

              <td class="govuk-table__cell">${orgDetail.getCompanyName()}</td>
              <td class="govuk-table__cell">${orgDetail.getRegisteredNumber()!""}</td>
              <td class="govuk-table__cell">
                  <@multiLineText.multiLineText blockClass="govuk-table">${orgDetail.getCompanyAddress()!""}</@multiLineText.multiLineText>
              </td>
            </tr>
          </#if>
      </#list>
      </tbody>
    </table>
  </div>
</#macro>