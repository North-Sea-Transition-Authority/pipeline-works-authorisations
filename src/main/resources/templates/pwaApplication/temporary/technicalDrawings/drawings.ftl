<#include '../../../layout.ftl'>

<!-- @ftlvariable name="technicalDrawings" type="java.util.Map<uk.co.ogauthority.pwa.temp.model.view.TechnicalDrawingView, String>" -->

<@defaultPage htmlTitle="Technical drawings" pageHeading="Technical drawings" backLink=true backLinkUrl=springUrl(backLinkUrl)>

    <@fdsAction.link linkText="Add a new technical drawing" linkUrl="#" linkClass="govuk-button"/>

    <#list technicalDrawings as drawing, route>
        <@fdsCard.card>
          <@fdsCard.cardHeader cardHeadingText="">
              <h2 class="govuk-visually-hidden">Technical drawing</h2>
              <div>
                <img src="${springUrl(drawing.imageUrl)}" alt="Technical drawing" width="256"/>
              </div>
              <p>&nbsp;&nbsp;&nbsp;</p>
            <div>
                <#if drawing.pipelineViewList?has_content>
                  <ul class="govuk-list govuk-list--bullet">
                      <#list drawing.pipelineViewList as pipelineView>
                        <li>${pipelineView.pipelineNumber}</li>
                      </#list>
                  </ul>
                <#else>
                    <p class="govuk-body">No pipelines are linked to this technical drawing.</p>
                </#if>
              </div>
          </@fdsCard.cardHeader>
            <br/>
            <@fdsAction.link linkUrl=springUrl(route) linkText="Edit linked pipelines" linkClass="govuk-button govuk-button--secondary"/>
        </@fdsCard.card>
    </#list>

</@defaultPage>