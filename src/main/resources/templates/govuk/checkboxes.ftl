<#import '/spring.ftl' as spring>
<#import 'fieldset.ftl' as checkboxFieldset>

<#--GOVUK Checkboxes-->
<#--https://design-system.service.gov.uk/components/checkboxes/-->
<#-- This macro is designed to take in a list of objects that implement the Checkable interface defined in this project -->
<#macro checkboxes path checkboxes label hintText="" caption="" fieldsetHeadingSize="h3" fieldsetHeadingClass="govuk-fieldset__legend--m" useSmallCheckboxes=false>
  <@spring.bind path/>

  <#local id=spring.status.expression?replace('[','')?replace(']','')>
  <#local hasError=(spring.status.errorMessages?size > 0)>

  <div class="govuk-form-group <#if hasError>govuk-form-group--error</#if>">
    <@checkboxFieldset.fieldset legendHeading=label legendSize=fieldsetHeadingSize legendHeadingClass=fieldsetHeadingClass primaryHintText=hintText caption=caption>
      <#if hasError>
        <span id="${id}-error" class="govuk-error-message">
          <#list spring.status.errorMessages as errorMessage>
            <#if errorMessage?has_content>
              ${errorMessage}<br/>
            </#if>
          </#list>
        </span>
      </#if>
      <div class="govuk-checkboxes <#if useSmallCheckboxes>govuk-checkboxes--small</#if>">
        <#assign selectedItems = [] />
        <#list spring.stringStatusValue?split(",") as item>
          <#assign selectedItems = selectedItems + [item] />
        </#list>
        <#list checkboxes as checkbox>
          <#assign isSelected = selectedItems?seq_contains(checkbox.getIdentifier())>
          <div class="govuk-checkboxes__item">
            <input class="govuk-checkboxes__input" id="${id}-${checkbox.getIdentifier()}" name="${spring.status.expression}" type="checkbox" value="${checkbox.getIdentifier()}" <#if isSelected>checked</#if>>
            <label class="govuk-label govuk-checkboxes__label" for="${id}-${checkbox.getIdentifier()}">
              ${checkbox.getDisplayName()}
            </label>
            <#if checkbox.getHintText()?has_content>
              <span id="${id}-${checkbox.getIdentifier()}-item-hint" class="govuk-hint govuk-radios__hint">
              ${checkbox.getHintText()?no_esc}
            </span>
            </#if>
          </div>
        </#list>
      </div>
    </@checkboxFieldset.fieldset>
  </div>
</#macro>