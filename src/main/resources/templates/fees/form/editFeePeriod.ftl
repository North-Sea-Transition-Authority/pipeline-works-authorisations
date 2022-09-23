<#include '../../layout.ftl'>

<#-- @ftlvariable name="applicationTypes" type="java.util.List<PwaApplicationType>" -->
<#-- @ftlvariable name="applicationFeeTypes" type="java.util.List<PwaApplicationFeeType>" -->
<#-- @ftlvariable name="errorList" type="java.util.Map<java.lang.String,java.util.List<java.lang.String,java.lang.String>>" -->
<#-- @ftlvariable name="feePeriod" type="uk.co.ogauthority.pwa.features.feemanagement.display.internal.DisplayableFeePeriodDetail" -->

<@defaultPage htmlTitle="Edit pending fee period"
pageHeading="Edit pending fee period"
topNavigation=false
errorItems=errorList
errorCheck=errorList?has_content>
    <@fdsForm.htmlForm>
      <#include 'feePeriodBaseForm.ftl'>
      <@fdsAction.button buttonText="Save"/>
    </@fdsForm.htmlForm>
</@defaultPage>