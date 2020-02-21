<#include '../../../layout.ftl'>

<@defaultPage htmlTitle="Add PWA contact" pageHeading="Add a new PWA contact" backLink=true>

    <@fdsForm.htmlForm>
        <@fdsFieldset.fieldset legendHeading="Contact details">
            <@fdsTextInput.textInput path="form.name" labelText="Name"/>
            <@fdsTextInput.textInput path="form.emailAddress" labelText="Email address"/>
            <@fdsTextInput.textInput path="form.telephoneNo" labelText="Telephone number"/>
        </@fdsFieldset.fieldset>

        <@fdsRadio.radioGroup path="form.role" labelText="Contact role">
          <#list roles as name, value>
            <@fdsRadio.radioItem path="form.role" itemMap={name: value}/>
          </#list>
        </@fdsRadio.radioGroup>

        <@fdsAction.button buttonText="Add contact"/>

    </@fdsForm.htmlForm>

</@defaultPage>