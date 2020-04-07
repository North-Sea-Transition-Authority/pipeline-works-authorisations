<#include '../../../layout.ftl'>

<#-- @ftlvariable name="categoryOptions" type="java.util.Map<String, String>" -->

<@defaultPage htmlTitle="Which organisations would you like to update?" pageHeading="Which organisations would you like to update?" breadcrumbs=true>

    <@fdsCheckbox.checkboxes path="form.categories" checkboxes=categoryOptions />

    <@fdsForm.htmlForm>
      <@fdsAction.button buttonText="Continue" />
    </@fdsForm.htmlForm>

</@defaultPage>