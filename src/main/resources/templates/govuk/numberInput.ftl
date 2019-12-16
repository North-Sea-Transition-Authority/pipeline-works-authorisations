<#import '/spring.ftl' as spring>
<#import 'fieldset.ftl' as numberFieldset>
<#import 'select.ftl' as govukSelect>
<#import 'details.ftl' as furtherGuidance>

<#macro timeInput hoursPath minutesPath label="" fieldsetHeadingSize="h3" fieldsetHeadingClass="govuk-fieldset__legend--m">
  <@spring.bind hoursPath/>

  <#local id=spring.status.expression?replace('[','')?replace(']','')>
  <#local hasError=(spring.status.errorMessages?size > 0)>
  <#local mandatory=((validation[spring.status.path].mandatory)!false)>
  <#local questionText=(questionMapping[spring.status.path].questionText)!label>
  <#local primaryHintText=(questionMapping[spring.status.path].primaryHintText)!"">
  <#local furtherHintTitle=(questionMapping[spring.status.path].furtherGuidanceTitle)!"">
  <#local furtherHintText=(questionMapping[spring.status.path].furtherGuidanceText)!"">

  <div class="govuk-form-group <#if hasError>govuk-form-group--error</#if>">
    <@numberFieldset.fieldset legendHeading=questionText legendSize=fieldsetHeadingSize legendHeadingClass=fieldsetHeadingClass>
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
      <div class="govuk-date-input" id="${id}-number-input">
        <div class="govuk-date-input__item">
          <div class="govuk-form-group">
            <label class="govuk-label govuk-date-input__label" for="${id}">
              Hours
            </label>
            <input class="govuk-input <#if hasError>govuk-input--error</#if> govuk-date-input__input govuk-input--width-2" id="${id}" name="${spring.status.expression}" type="text" value="${spring.stringStatusValue}">
          </div>
        </div>
        <@spring.bind minutesPath/>
        <#local id=spring.status.expression?replace('[','')?replace(']','')>
        <div class="govuk-date-input__item">
          <div class="govuk-form-group">
            <label class="govuk-label govuk-date-input__label" for="${id}">
              Minutes
            </label>
            <input class="govuk-input <#if hasError>govuk-input--error</#if> govuk-date-input__input govuk-input--width-2" id="${id}" name="${spring.status.expression}" type="text" value="${spring.stringStatusValue}">
          </div>
        </div>
      </div>
    </@numberFieldset.fieldset>
  </div>
  <#if furtherHintTitle?has_content>
    <@furtherGuidance.details summaryTitle=furtherHintTitle summaryText=furtherHintText/>
  </#if>
</#macro>

<#macro locationInput degreesLocationpathPath minutesLocationPath secondsLocationPath label="" direction="NS" directionList=[] fieldsetHeadingSize="h3" fieldsetHeadingClass="govuk-fieldset__legend--m">
  <@spring.bind degreesLocationpathPath/>

  <#local id=spring.status.expression?replace('[','')?replace(']','')>
  <#local hasError=(spring.status.errorMessages?size > 0)>
  <#local mandatory=((validation[spring.status.path].mandatory)!false)>
  <#local questionText=(questionMapping[spring.status.path].questionText)!label>
  <#local primaryHintText=(questionMapping[spring.status.path].primaryHintText)!"">
  <#local furtherHintTitle=(questionMapping[spring.status.path].furtherGuidanceTitle)!"">
  <#local furtherHintText=(questionMapping[spring.status.path].furtherGuidanceText)!"">

  <div class="govuk-form-group <#if hasError>govuk-form-group--error</#if>">
    <@numberFieldset.fieldset legendHeading=questionText legendSize=fieldsetHeadingSize legendHeadingClass=fieldsetHeadingClass primaryHintText=primaryHintText>
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
            <#else>
              <@govukSelect.select path="form.longitudeDirection" options=directionList label="Hemisphere (east / west)"></@govukSelect.select>
            </#if>
          </div>
        </div>
      </div>
    </@numberFieldset.fieldset>
  </div>
  <#if furtherHintTitle?has_content>
    <@furtherGuidance.details summaryTitle=furtherHintTitle summaryText=furtherHintText/>
  </#if>
</#macro>

<#macro quadrantBlockInput quadrantPath blockPath label="" fieldsetHeadingSize="h3" fieldsetHeadingClass="govuk-fieldset__legend--m">
  <@spring.bind quadrantPath/>

  <#local id=spring.status.expression?replace('[','')?replace(']','')>
  <#local hasError=(spring.status.errorMessages?size > 0)>
  <#local mandatory=((validation[spring.status.path].mandatory)!false)>
  <#local questionText=(questionMapping[spring.status.path].questionText)!label>
  <#if !mandatory>
    <#local questionText += " (optional)">
  </#if>
  <#local primaryHintText=(questionMapping[spring.status.path].primaryHintText)!"">
  <#local furtherHintTitle=(questionMapping[spring.status.path].furtherGuidanceTitle)!"">
  <#local furtherHintText=(questionMapping[spring.status.path].furtherGuidanceText)!"">

  <div class="govuk-form-group <#if hasError>govuk-form-group--error</#if>">
    <@numberFieldset.fieldset legendHeading=questionText legendSize=fieldsetHeadingSize legendHeadingClass=fieldsetHeadingClass primaryHintText=primaryHintText>
      <#if hasError>
        <span id="${id}-error" class="govuk-error-message">
          <#list spring.status.errorMessages as errorMessage>
            <#if errorMessage?has_content>
              ${errorMessage}<br/>
            </#if>
          </#list>
        </span>
      </#if>
      <div class="govuk-date-input" id="${id}-quadrant-block-input">
        <div class="govuk-date-input__item">
          <div class="govuk-form-group">
            <label class="govuk-label govuk-date-input__label" for="${id}">
              Quadrant
            </label>
            <input class="govuk-input <#if hasError>govuk-input--error</#if> govuk-date-input__input govuk-input--width-2" id="${id}" name="${spring.status.expression}" type="text" value="${spring.stringStatusValue}">
          </div>
        </div>
        <@spring.bind blockPath/>
        <#local id=spring.status.expression?replace('[','')?replace(']','')>
        <div class="govuk-date-input__item">
          <div class="govuk-form-group">
            <label class="govuk-label govuk-date-input__label" for="${id}">
              Block
            </label>
            <input class="govuk-input <#if hasError>govuk-input--error</#if> govuk-date-input__input govuk-input--width-2" id="${id}" name="${spring.status.expression}" type="text" value="${spring.stringStatusValue}">
          </div>
        </div>
      </div>
    </@numberFieldset.fieldset>
  </div>
  <#if furtherHintTitle?has_content>
    <@furtherGuidance.details summaryTitle=furtherHintTitle summaryText=furtherHintText/>
  </#if>
</#macro>

<#macro releaseAreaInput lengthPath widthPath releaseAreaUnits label="" fieldsetHeadingSize="h3" fieldsetHeadingClass="govuk-fieldset__legend--m" nestingPath="">

  <@spring.bind lengthPath/>
  <#local id=spring.status.expression?replace('[','')?replace(']','')>

  <#local hasError=(spring.status.errorMessages?size > 0)>
  <#local mandatory=((validation[spring.status.path].mandatory)!false)>
  <#local questionText=(questionMapping[spring.status.path].questionText)!label>
  <#local primaryHintText=(questionMapping[spring.status.path].primaryHintText)!"">
  <#local furtherHintTitle=(questionMapping[spring.status.path].furtherGuidanceTitle)!"">
  <#local furtherHintText=(questionMapping[spring.status.path].furtherGuidanceText)!"">

  <div class="govuk-form-group <#if hasError>govuk-form-group--error</#if>">
    <@numberFieldset.fieldset legendHeading=questionText legendSize=fieldsetHeadingSize legendHeadingClass=fieldsetHeadingClass primaryHintText=primaryHintText>
      <#if hasError>
        <span id="${id}-error" class="govuk-error-message">
          <#list spring.status.errorMessages as errorMessage>
            <#if errorMessage?has_content>
              ${errorMessage}<br/>
            </#if>
          </#list>
        </span>
      </#if>
      <div class="govuk-date-input" id="release-area-input">
        <div class="govuk-date-input__item">
          <div class="govuk-form-group">
            <label class="govuk-label govuk-date-input__label" for="${id}">
              Length
            </label>
            <input class="govuk-input <#if hasError>govuk-input--error</#if> govuk-date-input__input govuk-input--width-6" id="${id}" name="${spring.status.expression}" value="${spring.stringStatusValue}" type="text">
          </div>
        </div>
        <@spring.bind widthPath/>
        <#local id=spring.status.expression?replace('[','')?replace(']','')>
        <div class="govuk-date-input__item">
          <div class="govuk-form-group">
            <label class="govuk-label govuk-date-input__label" for="${id}">
              Width
            </label>
            <input class="govuk-input <#if hasError>govuk-input--error</#if> govuk-date-input__input govuk-input--width-6" id="${id}" name="${spring.status.expression}" value="${spring.stringStatusValue}" type="text">
          </div>
        </div>
        <div class="govuk-date-input__item">
          <@govukSelect.select path="form.releaseAreaUnits" options=releaseAreaUnits label="Unit of measure"/>
        </div>
      </div>
    </@numberFieldset.fieldset>
  </div>
  <#if furtherHintTitle?has_content>
    <@furtherGuidance.details summaryTitle=furtherHintTitle summaryText=furtherHintText/>
  </#if>

  <#if nestingPath?has_content>
    <@spring.bind nestingPath/>
  </#if>
</#macro>