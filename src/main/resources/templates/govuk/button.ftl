<#--GOVUK Button Widget-->
<#--https://design-system.service.gov.uk/components/button/-->
<#macro button buttonText buttonClass="" disabled=false id="">
  <button <#if id?has_content>id="${id}"</#if> type="submit" class="govuk-button <#if disabled>govuk-button--disabled</#if> ${buttonClass}" <#if disabled>disabled="disabled" aria-disabled="true"</#if> value="${buttonText}" name="${buttonText}">
    ${buttonText}
  </button>
</#macro>

<#macro link linkText linkClass="" linkUrl="#" role=false openInNewTab=false>
  <a href="${linkUrl}" class="${linkClass}" <#if openInNewTab>target="_blank"</#if> <#if role>role="button" draggable="false"</#if>>${linkText}</a>
</#macro>