<#--GOVUK Fieldset-->
<#--https://design-system.service.gov.uk/components/fieldset/-->
<#global fieldSetCounter></#global>
<#assign fieldSetCounter = 0/>

<#macro fieldset legendHeadingClass="govuk-fieldset__legend--l" legendHeading="" legendSize="h1" primaryHintText="" caption="" mandatory=true>
  <#assign fieldSetCounter++/>
  <fieldset class="govuk-fieldset" <#if primaryHintText?has_content>aria-describedby="fieldset-hint-${fieldSetCounter}"</#if>>
    <legend class="govuk-fieldset__legend ${legendHeadingClass}">
      <#if legendHeading?has_content>
        <#if legendSize="h1">
          <#if caption?has_content>
            <span class="govuk-caption-xl">${caption}</span>
          </#if>
          <h1 class="govuk-fieldset__heading">
            ${legendHeading} <#if !mandatory>(optional)</#if>
          </h1>
          <#elseif legendSize="h2">
            <#if caption?has_content>
              <span class="govuk-caption-l">${caption}</span>
            </#if>
            <h2 class="govuk-fieldset__heading">
            ${legendHeading} <#if !mandatory>(optional)</#if>
          </h2>
          <#elseif legendSize="h3">
          <#if caption?has_content>
            <span class="govuk-caption-m">${caption}</span>
          </#if>
          <h3 class="govuk-fieldset__heading">
            ${legendHeading} <#if !mandatory>(optional)</#if>
          </h3>
        </#if>
      </#if>
    </legend>
    <#if primaryHintText?has_content>
      <span class="govuk-hint" id="fieldset-hint-${fieldSetCounter}">
        ${primaryHintText}
      </span>
    </#if>

    <#nested>

  </fieldset>
</#macro>