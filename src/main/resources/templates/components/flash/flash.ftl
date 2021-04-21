<#include '../../layout.ftl'>

<#macro flashContent flashTitle="" flashMessage="" flashClass="" flashBulletList=[]>
  <#if flashTitle?has_content>
      <@fdsFlash.flash flashTitle=flashTitle flashClass=flashClass!"">
          <#if flashMessage?has_content>
              <p class="govuk-body">${flashMessage}</p>
          </#if>
          <#if flashBulletList?has_content>
              <ul class="govuk-list govuk-list--bullet">
                  <#list flashBulletList as bulletContent>
                      <li>${bulletContent!"default"}</li>
                  </#list>
              </ul>
          </#if>
      </@fdsFlash.flash>
  </#if>
</#macro>