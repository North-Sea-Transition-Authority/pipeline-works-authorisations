<#-- @ftlvariable name="errorList" type="java.util.Map<java.lang.String,java.util.List<java.lang.String,java.lang.String>>" -->

<#include '../layout.ftl'>

<@defaultPage htmlTitle="Request application update" pageHeading="${appRef} request update" topNavigation=true twoThirdsColumn=false backLink=true>
  <@fdsError.errorSummary errorItems=errorList />

  <@fdsForm.htmlForm>

    <@fdsTextarea.textarea path="form.requestReason" labelText="What is the reason an update is required?" characterCount=true maxCharacterLength="4000"/>

    <@fdsAction.submitButtons primaryButtonText="Request applicaton update" linkSecondaryAction=true secondaryLinkText="Cancel" linkSecondaryActionUrl=springUrl(cancelUrl)/>
  </@fdsForm.htmlForm>
</@defaultPage>