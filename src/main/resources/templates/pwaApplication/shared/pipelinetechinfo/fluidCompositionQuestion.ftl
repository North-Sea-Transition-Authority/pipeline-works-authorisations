<#include '../../../layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" --> 


<#macro fluidCompositionQuestion chemical>
    <h3 class="govuk-heading-m"> ${chemical.getDisplayText()} </h3>
</#macro>