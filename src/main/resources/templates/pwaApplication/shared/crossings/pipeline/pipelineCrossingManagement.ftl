<#include '../../../../layout.ftl'>

<#-- @ftlvariable name="pipelineCrossings" type="java.util.List<uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.BlockCrossingView>" -->
<#-- @ftlvariable name="pipelineCrossingFileViews" type="java.util.List<uk.co.ogauthority.pwa.model.form.files.UploadedFileView>" -->
<#-- @ftlvariable name="urlFactory" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.BlockCrossingUrlFactory" -->

<#macro pipelineCrossingManagement urlFactory pipelineCrossings=[] pipelineCrossingFileViews=[] isCompleted=false>
  <h2 class="govuk-heading-l">Pipeline crossings <@completedTag.completedTag isCompleted/></h2>

    <@fdsAction.link linkText="Add pipeline crossing" linkUrl=springUrl(urlFactory.getAddCrossingUrl()) linkClass="govuk-button govuk-button--blue"/>

</#macro>