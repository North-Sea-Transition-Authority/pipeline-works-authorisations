<#include '../../layout.ftl'>
<#--@ftlvariable name="responders" type="java.util.Map<String, String>" -->
<#--@ftlvariable name="cancelUrl" type="String" -->

<@defaultPage htmlTitle="Assign responder" pageHeading="Assign responder" topNavigation=true>

<@fdsForm.htmlForm>
    <@fdsSearchSelector.searchSelectorEnhanced path="form.responderPersonId" options=responders labelText="Select a responder for the consultation request"/>



    <@fdsAction.submitButtons primaryButtonText="Assign" linkSecondaryAction=true secondaryLinkText="Cancel" linkSecondaryActionUrl=springUrl(cancelUrl)/>
  </@fdsForm.htmlForm>
  
</@defaultPage>