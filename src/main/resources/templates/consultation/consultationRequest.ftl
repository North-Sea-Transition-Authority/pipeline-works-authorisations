<#include '../layout.ftl'>

<@defaultPage htmlTitle="${appRef} request consultations" pageHeading="${appRef} request consultations" topNavigation=true twoThirdsColumn=false>

  <@fdsForm.htmlForm>


    <@fdsFieldset.fieldset legendHeading="What do you want to consult" legendHeadingSize="h3" legendHeadingClass="govuk-fieldset__legend--m">
      <@fdsCheckbox.checkboxGroup path="form.consulteeGroupSelection" hiddenContent=true>

          <#list consulteeGroups as  consulteeGroup>          
              <@fdsCheckbox.checkboxItem path="form.consulteeGroupSelection[${consulteeGroup.id}]" labelText=consulteeGroup.name/>
          </#list>    

          <@fdsCheckbox.checkboxItem path="form.consulteeGroupSelection['OTHER']" labelText="Other">
            <@fdsTextInput.textInput path="form.otherGroupLogin" labelText="What is the consultee's email address or login ID" />
          </@fdsCheckbox.checkboxItem>

      </@fdsCheckbox.checkboxGroup>
    </@fdsFieldset.fieldset>

    <@fdsTextInput.textInput path="form.daysToRespond" labelText="How many calendar days do they have to respond?" inputClass="govuk-input--width-2"/>



    <@fdsAction.submitButtons primaryButtonText="Complete" secondaryButtonText="Save and complete later"/>
  </@fdsForm.htmlForm>
</@defaultPage>