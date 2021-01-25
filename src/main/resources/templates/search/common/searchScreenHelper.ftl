<#include '../../layout.ftl'>

<#macro noResultsFound>
  <h2 class="govuk-heading-s">There are no matching results</h2>
  <p class="govuk-body">Improve your results by:</p>
  <ul class="govuk-list govuk-list--bullet">
    <li>removing filters</li>
    <li>double-checking your spelling</li>
    <li>using fewer keywords</li>
    <li>searching for something less specific</li>
  </ul>
</#macro>

<#macro resultsCountText searchScreenView searchType>
  <h2 class="govuk-heading-s" role="alert">
    <#if searchScreenView.resultsHaveBeenLimited()>
      ${searchScreenView.fullResultCount?c} results have been found but only ${searchScreenView.searchResults?size?c} are shown, you may need to refine your ${searchType} criteria.
    <#else>
      <@stringUtils.pluralise count=searchScreenView.searchResults?size word="result"/>
    </#if>
  </h2>
</#macro>