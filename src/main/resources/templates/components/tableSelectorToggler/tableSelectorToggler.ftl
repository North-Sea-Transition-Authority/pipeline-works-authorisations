<#include '../../layout.ftl'/>

<#macro linksToggler tableId
  prefixText=""
  selectAllLinkText="Select all"
  selectNoneLinkText="Select none"
  selectAllScreenReaderText=""
  selectNoneScreenReaderText="">
    <#local defaultLinkClass="govuk-link govuk-link--no-visited-state" />

    <div class="table-selection-toggler" data-table-id="${tableId}">
      <#if prefixText?has_content>
        <span aria-hidden="true">${prefixText}</span>
      </#if>
      <@link linkText=selectAllLinkText linkUrl="#" linkClass="${defaultLinkClass} table-selection-toggler__select-all-link" ariaLabel=selectAllScreenReaderText/>
      <@link linkText=selectNoneLinkText linkUrl="#" linkClass="${defaultLinkClass} table-selection-toggler__select-none-link" ariaLabel=selectNoneScreenReaderText/>
    </div>
</#macro>



<#macro link linkText linkUrl linkClass="govuk-link" linkScreenReaderText="" role=false start=false openInNewTab=false ariaDescribeBy="" ariaLabel="">
    <#if start>
        <#local role = true>
    </#if>
  <a href="${linkUrl}" class="<#if start>govuk-button govuk-button--start </#if>${linkClass}" <#if role>role="button" draggable="false" data-module="govuk-button"</#if><#if openInNewTab> target="_blank"</#if><#if ariaDescribeBy?has_content> aria-describedby="${ariaDescribeBy}"</#if> <#if ariaLabel?has_content> aria-label="${ariaLabel}"</#if>>
      ${linkText} <#if linkScreenReaderText?has_content><span class="govuk-visually-hidden">${linkScreenReaderText}</span></#if>
      <#if start>
          <@startButtonIcon/>
      </#if>
  </a>
</#macro>



