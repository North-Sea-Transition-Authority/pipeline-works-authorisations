<#include '../../layout.ftl'>

<#macro fileUpload path id uploadUrl deleteUrl downloadUrl maxAllowedSize allowedExtensions=[] dropzoneText="Drag and drop a file here" multiFile=true existingFiles=[] validationErrors=[] >
  <#local inputName = path?remove_beginning("form.") />
  <div id="${id}-dropzone" class="fileupload-dropzone">
    <#local allowedExtensionsString=allowedExtensions?join(",")/>
    <div class="fileupload-dropzone__text">
      ${dropzoneText}, or
      <input id="${id}"
        class="fileupload-dropzone__hidden-input"
        type="file"
        name="file"
        tabindex="-1"
        data-form-data='{"${_csrf.parameterName}": "${_csrf.token}"}'
        data-fileInputName="${inputName}"
        data-url="<@spring.url uploadUrl/>"
        data-delete-url="<@spring.url deleteUrl/>"
        data-download-url="<@spring.url downloadUrl/>"
        upload-file-allowed-extensions="${allowedExtensionsString}"
        upload-file-max-size="${maxAllowedSize}"
        accept="${allowedExtensionsString}"
        <#if multiFile>multiple</#if>
      >
      <label for="${id}" class="fileupload-dropzone__link" tabindex="0">choose a file</label>
    </div>
  </div>

  <div class="fileupload">
    <#list existingFiles as file>
      <@uploadedFile
      index=file?index
      path=path
      htmlId=file.getFileId()
      fileName=file.getFileName()
      fileSize=file.getFileSize()
      fileDescription=file.getFileDescription()
      fileId=file.getFileId()
      fileUploadedInstant=file.getFileUploadedTime()
      url=downloadUrl+file.getFileId()
      deleteUrl=deleteUrl+file.getFileId()
      sourceUploadId=id/>
    </#list>
  </div>

  <#list validationErrors as validationError>
    <div class="error-message">${validationError}</div> 
  </#list>
  <script src="<@spring.url '/assets/static/js/pwa/modal.js'/>"></script>
  <script src="<@spring.url '/assets/static/js/pwa/fileUpload.js'/>"></script>
</#macro>

<#--
  fileDescription is an optional parameter to avoid a freemarker null or missing value crash if the user removes the file
  description from an existing file and then attempts to save the page.
 -->
<#macro uploadedFile index path htmlId fileName fileSize fileId fileUploadedInstant url deleteUrl sourceUploadId fileDescription="">
  <#local inputName = path?remove_beginning("form.") />
  <div id="${htmlId}-uploaded-file" data-fileId="${fileId}" data-fileName="${fileName}" data-deleteUrl="<@spring.url deleteUrl/>" class="uploaded-file" data-source-upload-id="${sourceUploadId}">
    <div class="uploaded-file__info">
      <div class="uploaded-file__file-info-wrapper uploaded-file__file-info-wrapper--saved">
        <span class="uploaded-file__filename">
          <#if url?has_content>
            <a href="<@spring.url url/>" class="govuk-link">${fileName}</a>
          </#if>
        </span>
        <span class="uploaded-file__extra-info"> -
          <#if fileSize?has_content>
            ${fileSize}
          </#if>
        </span>
        <a href="#" class="govuk-link uploaded-file__delete-link uploaded-file__delete-link--saved">Remove file <span class="govuk-visually-hidden">${fileName}</span></a>
      </div>
      <@fdsTextarea.textarea
        path=path+"["+index+"].uploadedFileDescription"
        labelText="File description"
        textareaValue=fileDescription
        rows="2"
        inputClass="govuk-textarea--file-upload"
        formGroupClass="govuk-form-group--file-upload"/>
    </div>
    <input type="hidden" name="${inputName}[${index}].uploadedFileId" value="${fileId}">
    <input type="hidden" name="${inputName}[${index}].uploadedFileInstant" value="${fileUploadedInstant}">
  </div>
</#macro>

