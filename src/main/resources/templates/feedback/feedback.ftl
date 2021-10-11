<#include '../layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pathfinder.model.form.fds.ErrorItem>" -->
<#-- @ftlvariable name="service" type="uk.co.ogauthority.pwa.config.ServiceProperties" -->
<#-- @ftlvariable name="serviceRatings" type="java.util.Map<String, String>" -->
<#-- @ftlvariable name="feedbackCharacterLimit" type="String" -->
<#-- @ftlvariable name="cancelUrl" type="String" -->

<#assign pageTitle = "Give feedback on ${service.serviceName}"/>

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  topNavigation=false
  backLink=false
  errorItems=errorList
  phaseBanner=false
>

  <@fdsForm.htmlForm>
    <@fdsRadio.radio path="form.serviceRating" labelText="Overall, how did you feel about using this service?" radioItems=serviceRatings />
    
    <@fdsTextarea.textarea
      path="form.feedback"
      labelText="How could we improve this service?"
      hintText="Do not include any personal or financial information, for example your National Insurance or credit card numbers"
      optionalLabel=true
      maxCharacterLength=feedbackCharacterLimit
      characterCount=true
      rows="10"
    />
    
    <@fdsAction.submitButtons
      primaryButtonText="Send feedback"
      linkSecondaryAction=true
      secondaryLinkText="Cancel"
      linkSecondaryActionUrl=springUrl(cancelUrl)
    />


  </@fdsForm.htmlForm>
</@defaultPage>