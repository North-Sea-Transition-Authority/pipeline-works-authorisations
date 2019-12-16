<#import '/spring.ftl' as spring>
<#import 'fieldset.ftl' as radioFieldset>
<#import 'details.ftl' as furtherGuidance>

<#--GOVUK Radio-->
<#--https://design-system.service.gov.uk/components/radios/-->


<#macro answerRadios path answers label="" hintText="" caption="" inline=false nestingPath="" fieldsetHeadingSize="h1" fieldsetHeadingClass="govuk-fieldset__legend--xl">
  <@spring.bind path/>

  <#local id=spring.status.expression?replace('[','')?replace(']','')>
  <#local hasError=(spring.status.errorMessages?size > 0)>
  <#local fieldName=spring.status.expression>

  <div class="govuk-form-group <#if hasError>govuk-form-group--error</#if>">
    <@radioFieldset.fieldset legendHeading=label legendHeadingClass=fieldsetHeadingClass legendSize=fieldsetHeadingSize mandatory=true primaryHintText=hintText caption=caption>
      <#if hasError>
        <span id="${id}-error" class="govuk-error-message">
          <#list spring.status.errorMessages as errorMessage>
            <#if errorMessage?has_content>
              ${errorMessage}<br/>
            </#if>
          </#list>
        </span>
      </#if>
      <div class="govuk-radios <#if inline>govuk-radios--inline</#if>">
        <#list answers as answer>
          <#assign isSelected = spring.stringStatusValue == answer.id>
          <div class="govuk-radios__item">
            <input class="govuk-radios__input" id="${id}-${answer.id}" name="${fieldName}" type="radio" value="${answer.id}" <#if isSelected>checked="checked"</#if>>
            <label class="govuk-label govuk-radios__label" for="${id}-${answer.id}">
              ${answer.text}
            </label>
            <#if answer.hint?has_content>
              <span id="${id}-${answer.id}-item-hint" class="govuk-hint govuk-radios__hint">
                ${answer.hint?no_esc}
              </span>
            </#if>
          </div>
        </#list>
      </div>
    </@radioFieldset.fieldset>
  </div>
</#macro>




<#macro radio path radioItems label="" inline=false nestingPath="" mandatoryOverride=false fieldsetHeadingSize="h3" fieldsetHeadingClass="govuk-fieldset__legend--m">
  <@spring.bind path/>

  <#local id=spring.status.expression?replace('[','')?replace(']','')>
  <#local hasError=(spring.status.errorMessages?size > 0)>
  <#local errorList=spring.status.errorMessages>
  <#local fieldName=spring.status.expression>
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
    <@radioFieldset.fieldset legendHeading=questionText legendHeadingClass=fieldsetHeadingClass legendSize=fieldsetHeadingSize mandatory=mandatory primaryHintText=primaryHintText>
      <#if hasError>
        <span id="${id}-error" class="govuk-error-message">
          <#list spring.status.errorMessages as errorMessage>
            <#if errorMessage?has_content>
              ${errorMessage}<br/>
            </#if>
          </#list>
        </span>
      </#if>
      <div class="govuk-radios <#if inline>govuk-radios--inline</#if>">
        <#list radioItems?keys as item>
          <#assign isSelected = spring.stringStatusValue == item>
          <div class="govuk-radios__item">
            <input class="govuk-radios__input" id="${id}<#if item?counter != 1>-${item}</#if>" name="${fieldName}" type="radio" value="${item}" <#if isSelected>checked="checked"</#if>>
            <label class="govuk-label govuk-radios__label" for="${id}<#if item?counter != 1>-${item}</#if>">
              ${radioItems[item]}
            </label>
          </div>
        </#list>
      </div>
    </@radioFieldset.fieldset>
  </div>
  <#if furtherHintTitle?has_content>
    <@furtherGuidance.details summaryTitle=furtherHintTitle summaryText=furtherHintText/>
  </#if>
  <#if nestingPath?has_content>
    <@spring.bind nestingPath/>
  </#if>
</#macro>

<#macro radioGroup path nestingPath="" label="" hint="" inline=false hiddenContentId="" mandatoryOverride=false fieldsetHeadingSize="h3" fieldsetHeadingClass="govuk-fieldset__legend--m">
  <@spring.bind path/>

  <#local id=spring.status.expression?replace('[','')?replace(']','')>
  <#local hasError=(spring.status.errorMessages?size > 0)>
  <#local errorList=spring.status.errorMessages>
  <#local fieldName=spring.status.expression>
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
    <@radioFieldset.fieldset legendHeading=questionText legendHeadingClass=fieldsetHeadingClass legendSize=fieldsetHeadingSize mandatory=mandatory primaryHintText=primaryHintText>
      <#if hasError>
        <span id="${id}-error" class="govuk-error-message">
          <#list spring.status.errorMessages as errorMessage>
            <#if errorMessage?has_content>
              ${errorMessage}<br/>
            </#if>
          </#list>
        </span>
      </#if>
      <#if inline && hiddenContentId?has_content>
        <#assign classModifier = "govuk-radios--inline govuk-radios--conditional">
      <#elseif inline && !hiddenContentId?has_content>
        <#assign classModifier = "govuk-radios--inline">
      <#elseif !inline && hiddenContentId?has_content>
        <#assign classModifier = "govuk-radios--conditional">
      </#if>
      <div class="govuk-radios <#if classModifier?has_content>${classModifier}</#if>" <#if hiddenContentId?has_content> data-module="radios"</#if>>
        <#nested/>
        <#if nestingPath?has_content>
          <@spring.bind nestingPath/>
        </#if>
      </div>
    </@radioFieldset.fieldset>
  </div>

  <#if furtherHintTitle?has_content>
    <@furtherGuidance.details summaryTitle=furtherHintTitle summaryText=furtherHintText/>
  </#if>
</#macro>

<!--
  isFirstItem is required to be true when you have more than one radioItem macro linked to the same form field or if you
  only have one radioItem linked to a form field. The error summary anchor tag will navigate to the radioItem that
  has the isFirstItem flag set to true.

  If none of the radioItem linked to a form field have isFirstItem set to true the anchor tag in the error summary will
  not navigate the user to an radio input when clicked.
-->
<#macro radioItem path itemMap listName="" isFirstItem=false>
  <@spring.bind path/>

  <#local id=spring.status.expression?replace('[','')?replace(']','')?replace('.','-')>
  <#if listName?has_content>
    <#local fieldName=listName>
  <#else>
    <#local fieldName=id>
  </#if>
  <#local nested><#nested/></#local>

  <#list itemMap?keys as item>
    <#assign isSelected = spring.stringStatusValue == item>
    <div class="govuk-radios__item">
      <input class="govuk-radios__input" id="${id}<#if !isFirstItem>-${item?replace(' ', '')}</#if>" name="${fieldName}" type="radio" value="${item}" <#if isSelected>checked="checked"</#if> <#if nested?has_content>data-aria-controls="${id}<#if !isFirstItem>-${item?replace(' ', '')}</#if>-hidden"</#if>>
      <label class="govuk-label govuk-radios__label" for="${id}<#if !isFirstItem>-${item?replace(' ', '')}</#if>">
        ${itemMap[item]}
      </label>
    </div>
    <#if nested?has_content>
      <div class="govuk-radios__conditional govuk-radios__conditional--hidden" id="${id}<#if !isFirstItem>-${item?replace(' ', '')}</#if>-hidden">
        ${nested}
      </div>
    </#if>
  </#list>
</#macro>

<#macro radioYes path>
  <@spring.bind path/>

  <#local id=spring.status.expression?replace('[','')?replace(']','')>
  <#local fieldName=spring.status.expression>
  <#local nested><#nested/></#local>

  <#assign isSelected = spring.stringStatusValue == "true">
  <div class="govuk-radios__item">
    <input class="govuk-radios__input" id="${id}" name="${fieldName}" type="radio" value="true" <#if isSelected>checked="checked"</#if> <#if nested?has_content>data-aria-controls="${id}-hidden"</#if>>
    <label class="govuk-label govuk-radios__label" for="${id}">
      Yes
    </label>
  </div>
  <#if nested?has_content>
    <div class="govuk-radios__conditional govuk-radios__conditional--hidden" id="${id}-hidden">
      ${nested}
    </div>
  </#if>
</#macro>

<#macro radioNo path>
  <@spring.bind path/>

  <#local id=spring.status.expression?replace('[','')?replace(']','')>
  <#local fieldName=spring.status.expression>
  <#local nested><#nested/></#local>

  <#assign isSelected = spring.stringStatusValue == "false">
  <div class="govuk-radios__item">
    <input class="govuk-radios__input" id="${id}-NO" name="${fieldName}" type="radio" value="false" <#if isSelected>checked="checked"</#if> <#if nested?has_content>data-aria-controls="${id}-NO-hidden"</#if>>
    <label class="govuk-label govuk-radios__label" for="${id}-NO">
      No
    </label>
  </div>
  <#if nested?has_content>
    <div class="govuk-radios__conditional govuk-radios__conditional--hidden" id="${id}-NO-hidden">
      ${nested}
    </div>
  </#if>
</#macro>