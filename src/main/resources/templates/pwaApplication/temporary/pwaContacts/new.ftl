<#include '../../../layout.ftl'>

<@defaultPage htmlTitle="Add PWA contact" pageHeading="Add a new PWA contact" backLink=true>

    <@fdsForm.htmlForm>
        <h2 class="govuk-heading-l">Information</h2>
        <@fdsTextInput.textInput path="form.userIdentifier" labelText="Contact" hintText="Enter person's email address or login ID"/>

        <@fdsRadio.radioGroup path="form.role" labelText="Role" fieldsetHeadingClass="govuk-fieldset__legend--l">
          <#list roles as name, value>
            <@fdsRadio.radioItem path="form.role" itemMap={name: value}/>
          </#list>
        </@fdsRadio.radioGroup>

        <@fdsAction.button buttonText="Add contact"/>

    </@fdsForm.htmlForm>

</@defaultPage>