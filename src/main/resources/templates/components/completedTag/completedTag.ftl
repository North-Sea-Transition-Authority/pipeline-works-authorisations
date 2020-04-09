<#macro completedTag isCompleted>
  <#if isCompleted>
    <strong class="govuk-tag">COMPLETED</strong>
  <#else>
    <strong class="govuk-tag govuk-tag--grey">NOT COMPLETED</strong>
  </#if>
</#macro>

