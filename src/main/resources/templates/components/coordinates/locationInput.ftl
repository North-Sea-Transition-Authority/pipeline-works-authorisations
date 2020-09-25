<#--PWA Coordinate input-->
<#import '/spring.ftl' as spring>
<#include '../../layout.ftl'>
<#import '../../components/numberInput/numberInput.ftl' as numberInput>

<#macro locationInput
degreesLocationPath
minutesLocationPath
secondsLocationPath
formId
directionList=[]
direction="NS"
directionPath=""
labelText=""
hintText=""
optionalLabel="fromInterceptor"
nestingPath=""
fieldsetHeadingSize="h3"
fieldsetHeadingClass="govuk-fieldset__legend--s"
formGroupClass=""
caption=""
captionClass="govuk-caption-s">
    <@spring.bind degreesLocationPath/>
    <#local hasErrorDegrees=(spring.status.errorMessages?size > 0)>
    <#assign degreesError>
        <@fdsError.inputError inputId="${formId}-degrees"/>
    </#assign>
    <#if optionalLabel=="true">
        <#local optionalFlag=true>
    <#elseif optionalLabel=="false">
        <#local optionalFlag=false>
    <#elseif optionalLabel=="fromInterceptor">
        <#local optionalFlag=(!(validation[spring.status.path].mandatory)!true)>
    </#if>
    <@spring.bind minutesLocationPath/>
    <#local hasErrorMinutes=(spring.status.errorMessages?size > 0)>
    <#assign minutesError>
        <@fdsError.inputError inputId="${formId}-minutes"/>
    </#assign>
    <@spring.bind secondsLocationPath/>
    <#local hasErrorSeconds=(spring.status.errorMessages?size > 0)>
    <#assign secondsError>
        <@fdsError.inputError inputId="${formId}-seconds"/>
    </#assign>
    <#local hasError=hasErrorDegrees || hasErrorMinutes || hasErrorSeconds>
    <#assign errorDisplay>
        <#if hasErrorDegrees>
            ${degreesError}
        </#if>
        <#if hasErrorMinutes>
            ${minutesError}
        </#if>
        <#if hasErrorSeconds>
            ${secondsError}
        </#if>
    </#assign>
  <div class="govuk-form-group ${formGroupClass}<#if hasError>govuk-form-group--error</#if>">
      <@fdsFieldset.fieldset legendHeading="${labelText} in WGS 84" legendHeadingSize=fieldsetHeadingSize legendHeadingClass=fieldsetHeadingClass caption=caption captionClass=captionClass optionalLabel=optionalFlag hintText=hintText>
          <#if hasError>
              ${errorDisplay}
          </#if>
        <div class="govuk-date-input" id="${formId}-number-input">
            <@numberInput.numberInputItem path=degreesLocationPath labelText="Degrees" leftPadAmount=2 leftPadCharacter="0" inputClass="govuk-input--width-3"/>
            <@numberInput.numberInputItem path=minutesLocationPath labelText="Minutes" leftPadAmount=2 leftPadCharacter="0" inputClass="govuk-input--width-3"/>
            <@numberInput.numberInputItem path=secondsLocationPath labelText="Seconds" leftPadAmount=2 leftPadCharacter="0" inputClass="govuk-input--width-3"/>
          <div class="govuk-date-input__item">
            <div class="govuk-form-group">
                <#if direction=="NS">
                  <div class="govuk-date-input__item">
                    <div class="govuk-form-group">
                      <label class="govuk-label govuk-date-input__label" for="${formId}-hemisphere-north">
                        Hemisphere (north / south)
                      </label>
                      <input class="govuk-input <#if hasError>govuk-input--error</#if> govuk-date-input__input govuk-input--width-3 govuk-input--read-only"
                        id="${formId}-hemisphere-north" name="hemisphere-north" type="text" disabled value="North">
                    </div>
                  </div>
                <#elseif direction=="NS_MANUAL">
                    <@fdsSelect.select path=directionPath options=directionList labelText="Hemisphere (north / south)"/>
                <#else>
                    <@fdsSelect.select path=directionPath options=directionList labelText="Hemisphere (east / west)"/>
                </#if>
            </div>
          </div>
        </div>
      </@fdsFieldset.fieldset>
  </div>
<#--Rebind your form when a component is used inside show/hide radio groups-->
    <#if nestingPath?has_content>
        <@spring.bind nestingPath/>
    </#if>
</#macro>