<#import '/spring.ftl' as spring>
<#import 'details.ftl' as furtherGuidance>

<#--GOVUK Select-->
<#--https://design-system.service.gov.uk/components/select/-->
<#macro select path options label="" optionalInputDefault="None" nestingPath="" mandatoryOverride=false>
  <@spring.bind path/>

  <#local id=spring.status.expression?replace('[','')?replace(']','')>
  <#local hasError=(spring.status.errorMessages?size > 0)>
  <#if mandatoryOverride>
    <#local mandatory=true>
  <#else>
    <#local mandatory=((validation[spring.status.path].mandatory)!false)>
  </#if>
  <#local questionText=(questionMapping[spring.status.path].questionText)!label>
  <#local primaryHintText=(questionMapping[spring.status.path].primaryHintText)!"">
  <#local furtherHintTitle=(questionMapping[spring.status.path].furtherGuidanceTitle)!"">
  <#local furtherHintText=(questionMapping[spring.status.path].furtherGuidanceText)!"">

  <div class="govuk-form-group <#if hasError>govuk-form-group--error</#if>">
    <label class="govuk-label" for="${id}">
      ${questionText} <#if !mandatory>(optional)</#if>
    </label>
    <#if primaryHintText?has_content>
      <span id="${id}-hint" class="govuk-hint">
        ${primaryHintText}
      </span>
    </#if>
    <#if hasError>
      <span id="${id}-error" class="govuk-error-message">
        <#list spring.status.errorMessages as errorMessage>
          <#if errorMessage?has_content>
            ${errorMessage}<br/>
          </#if>
        </#list>
      </span>
    </#if>
    <select class="govuk-select <#if hasError>govuk-select--error</#if>" id="${id}" name="${spring.status.expression}" <#if primaryHintText?has_content>aria-describedby="${id}-hint"</#if>>
      <#if spring.stringStatusValue?has_content>
        <#if !mandatory>
          <option value="" selected>${optionalInputDefault}</option>
        </#if>
      <#else>
        <#if mandatory>
          <option value="" selected disabled>Select One...</option>
        <#else>
          <option value="" selected>${optionalInputDefault}</option>
        </#if>
      </#if>
      <#list options?keys as option>
        <#assign isSelected = spring.stringStatusValue == option>
        <option value="${option}" <#if isSelected>selected</#if>>${options[option]}</option>
      </#list>
    </select>
  </div>
  <#if furtherHintTitle?has_content>
    <@furtherGuidance.details summaryTitle=furtherHintTitle summaryText=furtherHintText/>
  </#if>

  <#if nestingPath?has_content>
    <@spring.bind nestingPath/>
  </#if>
</#macro>

<#--The following is used for filtering search results or work area entries -->

<#macro selectFilter path options optionalInputDefault="Filter By">
  <@spring.bind path/>

  <#local id=spring.status.expression?replace('[','')?replace(']','')>

  <div class="govuk-form-group">
    <select class="govuk-select" id="${id}" name="${spring.status.expression}">
      <option value="" selected disabled>${optionalInputDefault}</option>
      <#list options?keys as option>
        <#assign isSelected = spring.stringStatusValue == option>
        <option value="${option}" <#if isSelected>selected</#if>>${options[option]}</option>
      </#list>
    </select>
  </div>
</#macro>