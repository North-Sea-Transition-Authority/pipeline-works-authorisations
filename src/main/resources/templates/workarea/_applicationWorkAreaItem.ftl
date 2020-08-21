<#include '../layout.ftl'>

<#-- @ftlvariable name="item" type="uk.co.ogauthority.pwa.service.workarea.ApplicationWorkAreaItem" -->
<#-- @ftlvariable name="columnItem" type="uk.co.ogauthority.pwa.service.workarea.WorkAreaColumnItemView" -->

<#macro workAreaColumnItemWrapper>
  <ul class="govuk-list">
      <#nested/>
  </ul>
</#macro>


<#macro workAreaItemContentRow item>
  <tr class="govuk-table__row">
    <td class="govuk-table__cell">
        <@workAreaColumnItemWrapper >
            <#list item.getApplicationColumn() as columnItem>
                <@workAreaColumnItem columnItem/>
            </#list>
        </@workAreaColumnItemWrapper>
    </td>
    <td class="govuk-table__cell">
        <@workAreaColumnItemWrapper >
            <#list item.getHolderColumn() as columnItem>
                <@workAreaColumnItem columnItem/>
            </#list>
        </@workAreaColumnItemWrapper>
    </td>
    <td class="govuk-table__cell">
        <@workAreaColumnItemWrapper >
            <#list item.getSummaryColumn() as columnItem>
                <@workAreaColumnItem columnItem/>
            </#list>
        </@workAreaColumnItemWrapper>
    </td>
    <td class="govuk-table__cell">
        <@workAreaColumnItemWrapper >
            <#list item.getApplicationStatusColumn() as columnItem>
                <@workAreaColumnItem columnItem/>
            </#list>
        </@workAreaColumnItemWrapper>
    </td>
  </tr>
</#macro>

<#macro workAreaColumnItem columnItem>
    <#if columnItem.labelType == "DEFAULT">
        <li>${(columnItem.label!"") + ":"}<@_workAreaColumnItemValue columnItem/></li>
    </#if>
    <#if columnItem.labelType == "NONE">
        <li><@_workAreaColumnItemValue columnItem/></li>
    </#if>
    <#if columnItem.labelType == "LINK">
        <li>
          <@fdsAction.link linkText=columnItem.label linkUrl=springUrl(columnItem.value) linkClass="govuk-link govuk-link--no-visited-state" />
        </li>
    </#if>
</#macro>

<#macro _workAreaColumnItemValue columnItem>
  <#if columnItem.valueTagType == "NONE">
      ${columnItem.value!""}
  <#else>
      <#local tagClass>
          <#if columnItem.valueTagType == "DEFAULT"></#if>
          <#if columnItem.valueTagType == "INFO">govuk-tag--blue</#if>
          <#if columnItem.valueTagType == "SUCCESS">govuk-tag--green</#if>
          <#if columnItem.valueTagType == "DANGER">govuk-tag--red</#if>
      </#local>
      <span class="govuk-tag ${tagClass!""}">${columnItem.value!""}</span>
  </#if>
</#macro>

