<#import '/spring.ftl' as spring>
<#import 'details.ftl' as furtherGuidance>

<#--GOVUK Input-->
<#--https://design-system.service.gov.uk/components/text-input/-->
<#macro textInput path label="" hint="" inputWidth="govuk-input--width-100" suffix="" suffixScreenReaderPrompt="" nestingPath="" mandatoryOverride=false maxCharacterLength="4000" pageHeading=false labelHeadingClass="govuk-label--l">
  <@spring.bind path/>

  <#local id=spring.status.expression?replace('[','')?replace(']','')>
  <#local hasError=(spring.status.errorMessages?size > 0)>
  <#if mandatoryOverride>
    <#local mandatory=true>
  <#else>
    <#local mandatory=((validation[spring.status.path].mandatory)!false)>
  </#if>
  <#local questionText=(questionMapping[spring.status.path].questionText)!label>
  <#local primaryHintText=(questionMapping[spring.status.path].primaryHintText)!hint>
  <#local furtherHintTitle=(questionMapping[spring.status.path].furtherGuidanceTitle)!"">
  <#local furtherHintText=(questionMapping[spring.status.path].furtherGuidanceText)!"">

  <div class="govuk-form-group <#if hasError>govuk-form-group--error</#if>">
    <#if pageHeading>
      <h1 class="govuk-label-wrapper">
    </#if>
      <label class="govuk-label <#if pageHeading>${labelHeadingClass}</#if>" for="${id}">
        ${questionText}
        <#if suffixScreenReaderPrompt?has_content>
          <span class="govuk-visually-hidden">${suffixScreenReaderPrompt}</span>
        </#if>
        <#if !mandatory>(optional)</#if>
      </label>
    <#if pageHeading>
      </h1>
    </#if>
    <#if primaryHintText?has_content>
      <span id="${id}-hint" class="govuk-hint">${primaryHintText}</span>
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
    <#--inputWidth e.g. Fixed Width = govuk-input--width-5-->
    <#--Fluid Width = govuk-!-width-two-thirds-->
    <input class="govuk-input <#if hasError>govuk-input--error </#if> ${inputWidth}" id="${id}" name="${spring.status.expression}" type="text" value="${spring.stringStatusValue}" maxlength="${maxCharacterLength}" <#if primaryHintText?has_content>aria-describedby="${id}-hint"</#if>>
    <#if suffix?has_content>
      <span id="${id}-suffix" class="input__suffix">${suffix}</span>
    </#if>
  </div>
  <#if furtherHintTitle?has_content>
    <@furtherGuidance.details summaryTitle=furtherHintTitle summaryText=furtherHintText/>
  </#if>

  <#if nestingPath?has_content>
    <@spring.bind nestingPath/>
  </#if>
</#macro>