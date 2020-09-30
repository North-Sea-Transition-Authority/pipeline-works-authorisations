<#include '../../../layout.ftl'>

<#-- @ftlvariable name="caseHistoryItems" type="java.util.List<uk.co.ogauthority.pwa.model.view.appprocessing.casehistory.CaseHistoryItemView>" -->

<#macro tab caseHistoryItems>

  <@fdsTimeline.timeline timelineClass="fds-timeline--left-padding">

      <@fdsTimeline.timelineSection sectionHeading="">

        <#list caseHistoryItems as item>

          <#if item?counter == caseHistoryItems?size>
            <#local stampClass="fds-timeline__time-stamp--no-border" />
            <#else>
              <#local stampClass = "fds-timeline__time-stamp" />
          </#if>

          <@fdsTimeline.timelineTimeStamp timeStampHeading=item.headerText nodeNumber=" " timeStampClass=stampClass >

              <@fdsDataItems.dataItem dataItemListClasses="fds-data-items-list--tight">
                  <@fdsDataItems.dataValues key="Date and time" value=item.dateTimeDisplay />
                  <@fdsDataItems.dataValues key=item.personLabelText value=item.personName />
              </@fdsDataItems.dataItem>

              <@fdsDataItems.dataItem dataItemListClasses="fds-data-items-list--tight">
                  <#list item.dataItems as key, value>
                    <@fdsDataItems.dataValues key=key value=value />
                  </#list>
              </@fdsDataItems.dataItem>

              <#if item.uploadedFileViews?has_content>

                  <@fdsCheckAnswers.checkAnswers summaryListClass="">

                    <#list item.uploadedFileViews as fileView>

                      <div class="govuk-summary-list__row">
                        <dt class="govuk-summary-list__key">
                            <@fdsAction.link linkText=fileView.fileName linkUrl=springUrl(fileView.fileUrl) linkClass="govuk-link" linkScreenReaderText="Download ${fileView.fileName}" role=false start=false openInNewTab=true/>
                        </dt>
                        <dd class="govuk-summary-list__value">
                            <@multiLineText.multiLineText blockClass="govuk-summary-list">${fileView.fileDescription!}</@multiLineText.multiLineText>
                        </dd>
                      </div>

                    </#list>

                  </@fdsCheckAnswers.checkAnswers>

              </#if>

          </@fdsTimeline.timelineTimeStamp>

        </#list>

      </@fdsTimeline.timelineSection>

  </@fdsTimeline.timeline>

</#macro>