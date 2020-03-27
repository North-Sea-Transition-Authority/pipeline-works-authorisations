'use strict';

class FileUpload {
  constructor($fileInput) {
    this.$fileInput = $fileInput;
    this.fileInputId = this.$fileInput.attr('id');
    this.$dropzone = $(`#${this.fileInputId}-dropzone`);

    this._bindEventHandlers();

    $fileInput.fileupload({
      dataType: 'json',
      dropZone: this.$dropzone,
      limitConcurrentUploads: 2,
      add: (e, data) => this._addHandler(data),
      progress: (e, data) => this._progressHandler(data),
      done: (e, data) => this._doneHandler(data),
      fail: (e, data) => this._failHandler(data)
    });
  }


  _bindEventHandlers() {
    this.$dropzone.bind('dragover', () => $(this).addClass('fileupload-dropzone--hover'));
    this.$dropzone.bind('dragleave', () => $(this).removeClass('fileupload-dropzone--hover'));
    this.$dropzone.bind('drop', () => $(this).removeClass('fileupload-dropzone--hover'));
    this.$dropzone.find('.fileupload-dropzone__hidden-input').bind('focus', () => $(this).addClass('fileupload-dropzone__hidden-input--has-focus'));
    this.$dropzone.find('.fileupload-dropzone__hidden-input').bind('blur', () => $(this).removeClass('fileupload-dropzone__hidden-input--has-focus'));

    /* Keyboard accessibility - we make the 'choose a file' link-styled label appear focused when the file input
       actually has focus. In IE, the spacebar makes the file picker appear, but the enter key doesn't. So we intercept
       the keypress and call click() on the label instead.*/
    this.$dropzone.find('.fileupload-dropzone__hidden-input').bind('keydown', (e) => {
      if(e.keyCode===13) {
        $(this).siblings('.fileupload-dropzone__button').click();
        e.preventDefault();
        return false;
      }
    });
  }

  _addHandler(data) {
    const filename = data.files[0].name;
    const size = data.files[0].size;

    const maxSize = parseInt(this.$fileInput.attr('upload-file-max-size'));
    const allowedExtensions = this.$fileInput.attr('accept').split(',');

    const indexCard = $(`.uploaded-file`).last().index() + 1;
    const uploadedFileInfoHtml = `<div class="uploaded-file"><div class="uploaded-file__info">
        <div class="uploaded-file__file-info-wrapper">
          <span class="uploaded-file__filename"> </span> 
          <span class="uploaded-file__extra-info"> </span>
        </div>
        <div class="uploaded-file__error"></div>
      </div></div>`;
    const fileDescriptionHtml = `<div class="govuk-form-group govuk-form-group--file-upload"><label class="govuk-label" for="file-upload-description">File description</label>
                                  <textarea class="govuk-textarea govuk-textarea--file-upload" id="file-upload-description" name="documentListDetails[${indexCard}].uploadedFileDescription" rows="2"></textarea>
                                 </div>`;
    const progressText = `<div class="uploaded-file__progress">- <span class="uploaded-file__progress-value" role="progressbar" aria-valuemin="0" aria-valuemax="100" aria-valuenow="0"></span><span class="uploaded-file__progress-unit">%</span></div>`;

    data.context = $(uploadedFileInfoHtml);

    $('.fileupload').prepend(data.context);

    data.context.find('.uploaded-file__filename').text(filename);
    data.context.find('.uploaded-file__extra-info').html(`- ${this._getReadableFileSizeString(size)}`);

    if(size > maxSize) {
      data.context.addClass('uploaded-file--error');
      this._showError(data.context, `Sorry, this file is too large. The maximum size allowed is ${this._getReadableFileSizeString(maxSize)}`);
    }
    else if(!allowedExtensions.some((extension) => { return this._endsWith(filename, extension.trim()); })) {
      data.context.addClass('uploaded-file--error');
      this._showError(data.context, `Sorry, this type of file is not allowed. File types accepted are ${allowedExtensions.join(', ')}`);
    }
    else {
      data.context.attr('id', indexCard);
      data.context.find('.uploaded-file__info').append(fileDescriptionHtml);
      data.context.find('.uploaded-file__file-info-wrapper').append(progressText);
      data.submitTimestamp = new Date().toISOString();
      data.submit();
    }
  }

  _progressHandler(data) {
    const progress = parseInt(data.loaded / data.total * 100, 10) - 1;
    data.context.find('.uploaded-file__progress-value').attr('aria-valuenow', progress).text(progress);
  };

  _doneHandler(data) {
    if(data.result.valid) {
      this.uploadedFileId = data.result.fileId;

      const downloadUrl = this.$fileInput.attr('data-download-url');
      const deleteUrl = this.$fileInput.attr('data-delete-url');
      const removeLink = ` <a href=# class="govuk-link uploaded-file__delete-link">Remove file <span class="govuk-visually-hidden">${data.files[0].name}</span></a>`;

      const filenameDownloadLink = `<a href="${downloadUrl + data.result.fileId}" class="govuk-link">${data.files[0].name} </a>`;
      const indexCard = data.context.attr("id");

      data.context.attr('data-fileId', data.result.fileId);
      data.context.attr('data-deleteUrl', `${deleteUrl}${data.result.fileId}`);
      data.context.find('.uploaded-file__filename').html(filenameDownloadLink);
      data.context.find('.uploaded-file__file-info-wrapper').append(removeLink);
      data.context.find('.uploaded-file__delete-link').on('click', (e) => {
        e.preventDefault();
        IRS.Modal.displayModal(FileUploadUtils.generateModal(data.files[0].name), `Remove uploaded file ${data.files[0].name}`);
        FileUploadUtils.beforeRemove(data.result.fileId);
      });

      data.context.find('input[name="uploadedFileId"]').attr('value', data.result.fileId);
      data.context.find('.uploaded-file__info').append($(`<input type="hidden" name="documentListDetails[${indexCard}].uploadedFileId" value="${this.uploadedFileId}"/>`));
      data.context.find('.uploaded-file__info').append($(`<input type="hidden" name="documentListDetails[${indexCard}].uploadedFileInstant" value="${data.submitTimestamp}"/>`));
    }
    else if (data.result.errorType === "VIRUS_FOUND_IN_FILE") {
      data.context.addClass('uploaded-file--error');
      data.context.find('.uploaded-file__extra-info').remove();
      data.context.find('.govuk-form-group--file-upload').remove();
      this._showError(data.context, `The file provided appears to contain a virus and it will not be uploaded.`);
    } else {
      this._showError(data.context, `Sorry, there was a problem uploading the file. ${data.result.errorMessage}`);
    }

    data.context.find('.uploaded-file__progress').remove();
    data.context.focus();
  }

  _failHandler(data) {
    this._showError(data.context,'Sorry, there was a problem uploading the file.');
  }

  _showError(context, msg) {
    context.find('.uploaded-file__error').text(msg);
  }
  _endsWith(filename, extension) {
    return filename.toLowerCase().lastIndexOf(extension.toLowerCase()) === filename.length - extension.length;
  }

  _getReadableFileSizeString(fileSizeInBytes) {
    let i = -1;
    const byteUnits = [' kB', ' MB', ' GB', ' TB', ' PB', ' EB', ' ZB', ' YB'];
    do {
      fileSizeInBytes = fileSizeInBytes / 1024;
      i++;
    } while (fileSizeInBytes > 1024);

    return Math.max(fileSizeInBytes, 1).toFixed() + byteUnits[i];
  }

}

// Scope the File Upload utility functions
const FileUploadUtils = {
  // return the modal content
  generateModal(filename) {
    return $(`<div class="modal" id="uploaded-file__display-modal">
          <p class="govuk-body modal__filename">Are you sure you want to remove ${filename}?</p>
          <div class="modal__actions">
            <button data-module="govuk-button" class="modal__confirm-button govuk-button">Remove</button>
            <button data-module="govuk-button" class="modal__cancel-button close-modal govuk-button govuk-button--link-exit">Cancel</button>
          </div>
        </div>`);
  },

  // add a click handler to the modal remove action and do an ajax post to remove the file
  beforeRemove(fileId) {
    $('#modal-overlay .modal__confirm-button').on('click', () => {
      $('span.govuk-error-message').remove();

      const csrf = $('input[name="_csrf"]').attr('value');
      const fileIdElement = $(`.uploaded-file[data-fileId="${fileId}"]`);
      const url = fileIdElement.attr('data-deleteUrl');
      const removeErrorMessage = $(`<span class="govuk-error-message">Sorry, there was a problem removing this file.</span>`);

      $.post(url, {_csrf: csrf})
        .done(() => {
          fileIdElement.remove()
          IRS.Modal.closeModal();
        })
        .fail(() => $('.modal__filename').append(removeErrorMessage));
    });
  }
};


$(document).ready(() => {
  $('.fileupload-dropzone__hidden-input').each((index, element) => new FileUpload($(element)));

  $('.uploaded-file').each((index, element) => $(element).find('.uploaded-file__delete-link').click((event) => {
    event.preventDefault();
    const fileId = $(element).attr('data-fileId');
    const fileName = $(element).attr('data-fileName');

    IRS.Modal.displayModal(FileUploadUtils.generateModal(fileName), `Remove uploaded file ${fileName}`);
    FileUploadUtils.beforeRemove(fileId);
  }));
});
