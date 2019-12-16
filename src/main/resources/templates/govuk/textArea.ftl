<#import '/spring.ftl' as spring>
<#import 'details.ftl' as furtherGuidance>

<#--GOVUK Textarea-->
<#--https://design-system.service.gov.uk/components/textarea/-->

<!-- the "value" parameter gets displayed in the textarea as spring.stringStatusValue is lost when using forms' fieldsets. See fileUpload.ftl for an application -->
<!-- the "mandatoryOverride" parameter is used to support the check on the mandatoriness of the textarea field. This is because the local variable's value "mandatory" alone is
     not enough as its value is based on the form's question mapping which is not correctly bound when using forms mandatoryOverrides. -->
<#macro textarea path label="" rows=5 nestingPath="" value="" mandatoryOverride=false regExpReplace=true maxCharacterLength="">
  <@spring.bind path/>

  <#if regExpReplace>
    <#local id=spring.status.expression?replace('[','')?replace(']','')>
    <#else>
    <#local id=spring.status.expression>
  </#if>

  <#local hasError=(spring.status.errorMessages?size > 0)>
  <#local mandatory=((validation[spring.status.path].mandatory)!false)>
  <#local questionText=(questionMapping[spring.status.path].questionText)!label>
  <#local primaryHintText=(questionMapping[spring.status.path].primaryHintText)!"">
  <#local furtherHintTitle=(questionMapping[spring.status.path].furtherGuidanceTitle)!"">
  <#local furtherHintText=(questionMapping[spring.status.path].furtherGuidanceText)!"">

  <div class="govuk-form-group <#if hasError>govuk-form-group--error</#if>">
    <label class="govuk-label" for="${id}">
      ${questionText} <#if !mandatory && !mandatoryOverride>(optional)</#if>
    </label>
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
    <textarea class="govuk-textarea <#if hasError>govuk-textarea--error</#if>" id="${id}" name="${spring.status.expression}" rows=${rows} <#if primaryHintText?has_content>aria-describedby="${id}-hint</#if><#if hasError>${id}-error</#if>" <#if maxCharacterLength?has_content>maxlength="${maxCharacterLength}"</#if>><#if value?has_content>${value}<#else>${spring.stringStatusValue}</#if></textarea>
  </div>
  <#if furtherHintTitle?has_content>
    <@furtherGuidance.details summaryTitle=furtherHintTitle summaryText=furtherHintText/>
  </#if>

  <#if nestingPath?has_content>
    <@spring.bind nestingPath/>
  </#if>
</#macro>