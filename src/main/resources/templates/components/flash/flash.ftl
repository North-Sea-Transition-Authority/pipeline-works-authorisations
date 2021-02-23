<#include '../../layout.ftl'>

<#macro flashContent flashTitle="" flashMessage="" flashClass="">
  <#if flashTitle?has_content>
      <@fdsFlash.flash flashTitle=flashTitle flashClass=flashClass!"">
          <#if flashMessage?has_content>
            <p class="govuk-body">${flashMessage}</p>
          </#if>
      </@fdsFlash.flash>
  </#if>
</#macro>