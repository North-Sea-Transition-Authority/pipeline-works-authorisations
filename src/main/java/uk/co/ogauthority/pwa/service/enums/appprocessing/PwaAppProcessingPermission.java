package uk.co.ogauthority.pwa.service.enums.appprocessing;

import java.util.stream.Stream;

/**
 * Enumeration of all of the permissions that are linked to case processing actions.
 */
public enum PwaAppProcessingPermission {

  // OGA
  ACCEPT_INITIAL_REVIEW(ProcessingPermissionType.GENERIC),
  CASE_OFFICER_REVIEW,
  ACCEPT_APPLICATION,
  VIEW_ALL_CONSULTATIONS(ProcessingPermissionType.GENERIC),
  EDIT_CONSULTATIONS,
  WITHDRAW_CONSULTATION,
  ASSIGN_CASE_OFFICER(ProcessingPermissionType.GENERIC),
  APPROVE_OPTIONS,
  APPROVE_OPTIONS_VIEW,
  REQUEST_APPLICATION_UPDATE,
  CASE_MANAGEMENT_OGA,
  ADD_CASE_NOTE(ProcessingPermissionType.GENERIC),
  PUBLIC_NOTICE,
  EDIT_CONSENT_DOCUMENT,
  WITHDRAW_APPLICATION,

  // CONSULTEES
  CASE_MANAGEMENT_CONSULTEE,
  ASSIGN_RESPONDER,
  CONSULTATION_RESPONDER,

  // INDUSTRY
  CASE_MANAGEMENT_INDUSTRY,
  UPDATE_APPLICATION,

  // ALL
  VIEW_APPLICATION_SUMMARY;

  private final ProcessingPermissionType processingPermissionType;

  PwaAppProcessingPermission() {
    this.processingPermissionType = ProcessingPermissionType.APP_SPECIFIC;
  }

  PwaAppProcessingPermission(ProcessingPermissionType processingPermissionType) {
    this.processingPermissionType = processingPermissionType;
  }

  public ProcessingPermissionType getProcessingPermissionType() {
    return processingPermissionType;
  }

  public static Stream<PwaAppProcessingPermission> streamGenericPermissions() {
    return Stream.of(PwaAppProcessingPermission.values())
        .filter(val -> val.getProcessingPermissionType().equals(ProcessingPermissionType.GENERIC));
  }

  public static Stream<PwaAppProcessingPermission> streamAppPermissions() {
    return Stream.of(PwaAppProcessingPermission.values())
        .filter(val -> val.getProcessingPermissionType().equals(ProcessingPermissionType.APP_SPECIFIC));
  }

}
