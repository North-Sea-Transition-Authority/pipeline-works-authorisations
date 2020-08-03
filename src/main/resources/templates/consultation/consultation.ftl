<#-- @ftlvariable name="errorList" type="java.util.Map<java.lang.String,java.util.List<java.lang.String,java.lang.String>>" -->
<#-- @ftlvariable name="consulteeGroupRequestsViews" type="java.util.List<uk.co.ogauthority.pwa.model.form.consultation.ConsulteeGroupRequestsView>" -->

<#include '../layout.ftl'>
<#include 'consultationRequestView.ftl'>

<@defaultPage htmlTitle="${appRef} consultations" pageHeading="${appRef} consultations" topNavigation=true twoThirdsColumn=false>

  <#if consulteeGroupRequestsViews?size == 0>
    <@fdsInsetText.insetText>This application has not been consulted on</@fdsInsetText.insetText>
  </#if>

  <#list consulteeGroupRequestsViews as consultationRequestViewData>
    <@consultationRequestView consultationRequestViewData/>
  </#list>


  <@fdsAction.link linkText="Request consultations" linkUrl=springUrl(requestConsultationsUrl) linkClass="govuk-button"/>
</@defaultPage>