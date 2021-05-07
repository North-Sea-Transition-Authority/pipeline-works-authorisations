<#include '../../layout.ftl'/>
<#import '/spring.ftl' as spring>
<#import '../utils/numberPad.ftl' as numberPad>
<#import '../../fds/utilities/utilities.ftl' as fdsUtil>

<#macro numberInputItem path labelText leftPadAmount=0 leftPadCharacter="0" inputClass="govuk-input--width-2">
    <@spring.bind path/>

    <#local id=fdsUtil.sanitiseId(spring.status.expression)>
    <#local name=spring.status.expression>
    <#local value=spring.stringStatusValue>
    <#local hasError=(spring.status.errorMessages?size > 0)>

    <#assign inputValue>
        <#if value?has_content>
            <@numberPad.leftPad value leftPadAmount leftPadCharacter/>
        </#if>
    </#assign>

  <div class="govuk-date-input__item">
    <div class="govuk-form-group">
      <label class="govuk-label govuk-date-input__label" for="${id}">${labelText}</label>
      <input class="govuk-input <#if hasError>govuk-input--error</#if> govuk-date-input__input ${inputClass}" id="${id}"
             name="${name}" type="text" value="${inputValue?esc?markup_string?trim}">
    </div>
  </div>
</#macro>