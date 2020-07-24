<#include '../layout.ftl'>

<@defaultPage htmlTitle="${appRef} request consultations" pageHeading="${appRef} request consultations" topNavigation=true twoThirdsColumn=false>

  <@fdsForm.htmlForm>


    <@fdsFieldset.fieldset legendHeading="What do you want to consult?" legendHeadingSize="h3" legendHeadingClass="govuk-fieldset__legend--m">
      <@fdsCheckbox.checkboxGroup path="form.consulteeGroupSelection" hiddenContent=true>

          <#list consulteeGroups as  consulteeGroup>          
              <#if (consulteeGroup.abbreviation)?has_content>
                <#assign abbreviation = "(${(consulteeGroup.abbreviation)!})" />
              <#else>
                <#assign abbreviation = "" />
              </#if>              
              <@fdsCheckbox.checkboxItem path="form.consulteeGroupSelection[${consulteeGroup.id}]" labelText="${consulteeGroup.name} ${abbreviation}"/>
          </#list>    
          
          <@fdsCheckbox.checkboxItem path="form.otherGroupSelected" labelText="Other">
            <@fdsTextInput.textInput path="form.otherGroupLogin" labelText="What is the consultee's email address or login ID?" nestingPath="form.otherGroupSelected"
              hintText="The consultee must already have a portal user account" inputClass="govuk-input--width-8"/>
          </@fdsCheckbox.checkboxItem>

      </@fdsCheckbox.checkboxGroup>
    </@fdsFieldset.fieldset>

    <@fdsTextInput.textInput path="form.daysToRespond" labelText="How many calendar days do they have to respond?" inputClass="govuk-input--width-4"/>



    <@fdsAction.submitButtons primaryButtonText="Send consultations" linkSecondaryAction=true secondaryLinkText="Cancel" linkSecondaryActionUrl=springUrl(cancelUrl)/>
  </@fdsForm.htmlForm>
</@defaultPage>