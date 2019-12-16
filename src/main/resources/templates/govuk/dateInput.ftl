<#import '/spring.ftl' as spring>
<#import 'fieldset.ftl' as dateFieldset>
<#import 'details.ftl' as furtherGuidance>

<#--GOVUK Date Input-->
<#--https://design-system.service.gov.uk/components/date-input/-->
<#macro dateInput dayPath monthPath yearPath label="" fieldsetHeadingSize="h3" fieldsetHeadingClass="govuk-fieldset__legend--m">
  <@spring.bind dayPath/>

  <#local id=spring.status.expression?replace('[','')?replace(']','')>
  <#local hasError=(spring.status.errorMessages?size > 0)>
  <#local mandatory=((validation[spring.status.path].mandatory)!false)>
  <#local questionText=(questionMapping[spring.status.path].questionText)!label>
  <#local primaryHintText=(questionMapping[spring.status.path].primaryHintText)!"">
  <#local furtherHintTitle=(questionMapping[spring.status.path].furtherGuidanceTitle)!"">
  <#local furtherHintText=(questionMapping[spring.status.path].furtherGuidanceText)!"">
  <div class="govuk-form-group <#if hasError>govuk-form-group--error</#if>">
    <@dateFieldset.fieldset legendSize=fieldsetHeadingSize legendHeadingClass=fieldsetHeadingClass legendHeading=questionText primaryHintText=primaryHintText>
      <#if hasError>
        <span id="${id}-error" class="govuk-error-message">
          <#list spring.status.errorMessages as errorMessage>
            <#if errorMessage?has_content>
              ${errorMessage}<br/>
            </#if>
          </#list>
        </span>
      </#if>
      <div class="govuk-date-input" id="dob">
        <div class="govuk-date-input__item">
          <div class="govuk-form-group">
            <label class="govuk-label govuk-date-input__label" for="${id}">
              Day
            </label>
            <input class="govuk-input <#if hasError>govuk-input--error</#if> govuk-date-input__input govuk-input--width-2" id="${id}" name="${spring.status.expression}" type="text" value="${spring.stringStatusValue}">
          </div>
        </div>
        <@spring.bind monthPath/>
        <#local id=spring.status.expression?replace('[','')?replace(']','')>
        <#local hasError=(spring.status.errorMessages?size > 0)>
        <div class="govuk-date-input__item">
          <div class="govuk-form-group">
            <label class="govuk-label govuk-date-input__label" for="${id}">
              Month
            </label>
            <input class="govuk-input <#if hasError>govuk-input--error</#if> govuk-date-input__input govuk-input--width-2" id="${id}" name="${spring.status.expression}" type="text" value="${spring.stringStatusValue}">
          </div>
        </div>
        <@spring.bind yearPath/>
        <#local id=spring.status.expression?replace('[','')?replace(']','')>
        <#local hasError=(spring.status.errorMessages?size > 0)>
        <div class="govuk-date-input__item">
          <div class="govuk-form-group">
            <label class="govuk-label govuk-date-input__label" for="${id}">
              Year
            </label>
            <input class="govuk-input <#if hasError>govuk-input--error</#if> govuk-date-input__input govuk-input--width-4" id="${id}" name="${spring.status.expression}" type="text" value="${spring.stringStatusValue}">
          </div>
        </div>
      </div>
    </@dateFieldset.fieldset>
  </div>
  <#if furtherHintTitle?has_content>
    <@furtherGuidance.details summaryTitle=furtherHintTitle summaryText=furtherHintText/>
  </#if>
</#macro>