<#-- @ftlvariable name="errorList" type="java.util.Map<java.lang.String,java.util.List<java.lang.String,java.lang.String>>" -->
<#-- @ftlvariable name="caseOfficerCandidates" type="java.util.Map<java.lang.Integer, java.lang.String>" -->

<#include '../layout.ftl'>

<@defaultPage htmlTitle="Assign case officer" pageHeading="${appRef} assign case officer" topNavigation=true twoThirdsColumn=false>
  <@fdsError.errorSummary errorItems=errorList />

  <@fdsForm.htmlForm>


    <@fdsSearchSelector.searchSelectorEnhanced path="form.caseOfficerPersonId" options=caseOfficerCandidates labelText="Select a case officer" />



    <@fdsAction.submitButtons primaryButtonText="Assign case officer" linkSecondaryAction=true secondaryLinkText="Cancel" linkSecondaryActionUrl=springUrl(cancelUrl)/>
  </@fdsForm.htmlForm>
</@defaultPage>