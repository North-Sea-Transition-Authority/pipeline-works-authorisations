<#include '../../../layout.ftl'>

<#-- @ftlvariable name="uooTypes" type="java.util.List<uk.co.ogauthority.pwa.temp.model.contacts.UooType>" -->
<#-- @ftlvariable name="uooAgreements" type="java.util.List<uk.co.ogauthority.pwa.temp.model.contacts.UooAgreement>" -->
<#-- @ftlvariable name="uooRoles" type="java.util.List<uk.co.ogauthority.pwa.temp.model.contacts.UooRole>" -->

<@defaultPage htmlTitle="New UOO contact" pageHeading="New UOO contact" backLink=true>

    <@fdsForm.htmlForm>

        <@fdsRadio.radioGroup path="form.type" labelText="What are you adding to the application?" hiddenContent=true fieldsetHeadingClass="govuk-fieldset__legend--l">
            <#list uooTypes as typeName,typeValue>
                <@fdsRadio.radioItem path="form.type" itemMap={typeName: typeValue}>
                  <#if typeName == "TREATY">
                      <@fdsRadio.radioGroup path="form.uooAgreement" labelText="Which agreement is to be used?" fieldsetHeadingSize="h3">
                        <#list uooAgreements as agreementName, agreementValue>
                            <@fdsRadio.radioItem path="form.uooAgreement" itemMap={agreementName: agreementValue}/>
                        </#list>
                      </@fdsRadio.radioGroup>
                  <#else>
                      <@fdsTextInput.textInput path="form.companyNumber" labelText="Company number" inputClass="govuk-input--width-10"/>
                      <@fdsTextInput.textInput path="form.companyName" labelText="Company name"/>
                      <@fdsTextarea.textarea path="form.companyAddress" labelText="Company address"/>

                      <@fdsFieldset.fieldset legendHeading="Roles" legendHeadingSize="h3" legendHeadingClass="govuk-fieldset__legend--m">
                          <@fdsCheckbox.checkboxes path="form.roles" checkboxes=uooRoles/>
                      </@fdsFieldset.fieldset>
                  </#if>
                </@fdsRadio.radioItem>
            </#list>
        </@fdsRadio.radioGroup>

        <@fdsAction.button buttonText="Add UOO"/>
    </@fdsForm.htmlForm>

</@defaultPage>