<#function springUrl url>
  <#local springUrl>
    <@spring.url url/>
  </#local>
  <#return springUrl>
</#function>
<#--GOVUK Breadcrumbs-->
<#--https://design-system.service.gov.uk/components/breadcrumbs/-->
<#macro breadcrumbs crumbsList currentPage>
  <div class="govuk-breadcrumbs">
    <ol class="govuk-breadcrumbs__list">
      <#list crumbsList as key, value>
        <li class="govuk-breadcrumbs__list-item" aria-current="false">
          <#assign url=springUrl(key)/>
          <a class="govuk-breadcrumbs__link" href="${url}">${value}</a>
        </li>
      </#list>
      <li class="govuk-breadcrumbs__list-item" aria-current="true">${currentPage}</li>
    </ol>
  </div>
</#macro>