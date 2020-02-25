<#import '/spring.ftl' as spring>
<#include '../../../layout.ftl'>

<#macro locationInput degreesLocationPath minutesLocationPath secondsLocationPath label="" direction="NS" directionList=[] fieldsetHeadingSize="h3" fieldsetHeadingClass="govuk-fieldset__legend--m" caption="" captionClass="govuk-caption-xl">
    <@spring.bind degreesLocationPath/>

    <#local id=spring.status.expression?replace('[','')?replace(']','')>
    <#local hasError=(spring.status.errorMessages?size > 0)>

  <div class="govuk-form-group <#if hasError>govuk-form-group--error</#if>">
      <@fdsNumberInput.numberFieldset.fieldset legendHeading=label legendHeadingSize=fieldsetHeadingSize legendHeadingClass=fieldsetHeadingClass caption=caption captionClass=captionClass>
          <#if hasError>
            <span id="${id}-error" class="govuk-error-message">
          <#list spring.status.errorMessages as errorMessage>
              <#if errorMessage?has_content>
                  ${errorMessage}<br/>
              </#if>
          </#list>
        </span>
          </#if>
        <div class="govuk-date-input" id="${id}-location-input">
          <div class="govuk-date-input__item">
            <div class="govuk-form-group">
              <label class="govuk-label govuk-date-input__label" for="${id}">
                Degrees
              </label>
              <input class="govuk-input <#if hasError>govuk-input--error</#if> govuk-date-input__input govuk-input--width-2" id="${id}" name="${spring.status.expression}" type="text" value="${spring.stringStatusValue}">
            </div>
          </div>
            <@spring.bind minutesLocationPath/>
            <#local id=spring.status.expression?replace('[','')?replace(']','')>
          <div class="govuk-date-input__item">
            <div class="govuk-form-group">
              <label class="govuk-label govuk-date-input__label" for="${id}">
                Minutes
              </label>
              <input class="govuk-input <#if hasError>govuk-input--error</#if> govuk-date-input__input govuk-input--width-2" id="${id}" name="${spring.status.expression}" type="text" value="${spring.stringStatusValue}">
            </div>
          </div>
            <@spring.bind secondsLocationPath/>
            <#local id=spring.status.expression?replace('[','')?replace(']','')>
          <div class="govuk-date-input__item">
            <div class="govuk-form-group">
              <label class="govuk-label govuk-date-input__label" for="${id}">
                Seconds
              </label>
              <input class="govuk-input <#if hasError>govuk-input--error</#if> govuk-date-input__input govuk-input--width-2" id="${id}" name="${spring.status.expression}" type="text" value="${spring.stringStatusValue}">
            </div>
          </div>
          <div class="govuk-date-input__item">
            <div class="govuk-form-group">
                <#if direction=="NS">
                  <div class="govuk-date-input__item">
                    <div class="govuk-form-group">
                      <label class="govuk-label govuk-date-input__label" for="hemisphere-north">
                        Hemisphere (north / south)
                      </label>
                      <input class="govuk-input <#if hasError>govuk-input--error</#if> govuk-date-input__input govuk-input--width-3 govuk-input--read-only" id="hemisphere-north" name="hemisphere-north" type="text" disabled value="North">
                    </div>
                  </div>
                <#elseif direction=="NS_MANUAL">
                    <@fdsSelect.select path="form.latitudeDirection" options=directionList labelText="Hemisphere (north / south)"></@fdsSelect.select>
                <#elseif direction=="EW">
                  <div class="govuk-date-input__item">
                    <div class="govuk-form-group">
                      <label class="govuk-label govuk-date-input__label" for="hemisphere-east">
                        Hemisphere (east / west)
                      </label>
                      <input class="govuk-input <#if hasError>govuk-input--error</#if> govuk-date-input__input govuk-input--width-3 govuk-input--read-only" id="hemisphere-east" name="hemisphere-east" type="text" disabled value="East">
                    </div>
                  </div>
                <#else>
                    <@fdsSelect.select path="form.longitudeDirection" options=directionList labelText="Hemisphere (east / west)"></@fdsSelect.select>
                </#if>
            </div>
          </div>
        </div>
      </@fdsNumberInput.numberFieldset.fieldset>
  </div>
</#macro>