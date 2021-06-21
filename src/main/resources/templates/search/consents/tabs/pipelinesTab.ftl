<#include '../../../layout.ftl'>

<#-- @ftlvariable name="pwaPipelineViews" type="java.util.List<uk.co.ogauthority.pwa.service.search.consents.tabcontentviews.PwaPipelineView>" -->
<#-- @ftlvariable name="urlFactory" type="uk.co.ogauthority.pwa.service.search.consents.pwaviewtab.PwaViewUrlFactory" -->

<#macro tab urlFactory pwaPipelineViews=[]>

  <table class="govuk-table">
    <thead class="govuk-table__head">
      <tr class="govuk-table__row">
        <th class="govuk-table__header" scope="col">Pipeline number</th>
        <th class="govuk-table__header" scope="col">Pipeline status/As-built status</th>
        <th class="govuk-table__header" scope="col">Start location (WGS 84)</th>
        <th class="govuk-table__header" scope="col">End location (WGS 84)</th>
        <th class="govuk-table__header" scope="col">Length (metres)</th>        
      </tr>
    </thead>

    <tbody class="govuk-table__body">
    <#list pwaPipelineViews as pwaPipelineView>
      <tr class="govuk-table__row">
        <td class="govuk-table__cell">
          <@fdsAction.link linkText=pwaPipelineView.pipelineNumber linkUrl=springUrl(urlFactory.getPwaPipelineViewUrl(pwaPipelineView.pipelineId)) 
              linkClass="govuk-link" linkScreenReaderText="Go to pipeline ${pwaPipelineView.pipelineNumber} view screen"/> 
        </td>
        <td class="govuk-table__cell">
          ${pwaPipelineView.status.getDisplayText()}
          <br>
          <#if pwaPipelineView.asBuiltNotificationStatus?hasContent>
            ${pwaPipelineView.asBuiltNotificationStatus.getDisplayName()}
          </#if>
        </td>
        <td class="govuk-table__cell">
          <div>
            ${pwaPipelineView.fromLocation}
            </br>
            <@pwaCoordinate.display coordinatePair=pwaPipelineView.fromCoordinates />
          </div>
        </td>
        <td class="govuk-table__cell">
          <div>
            ${pwaPipelineView.toLocation}
            </br>
            <@pwaCoordinate.display coordinatePair=pwaPipelineView.toCoordinates />
          </div>
        </td>
        <td class="govuk-table__cell">
          ${pwaPipelineView.length}
        </td>
      </tr>
    </#list>    
    </tbody>

  </table>

  

</#macro>