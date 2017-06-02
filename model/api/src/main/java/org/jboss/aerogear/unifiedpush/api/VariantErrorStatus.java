package org.jboss.aerogear.unifiedpush.api;

public class VariantErrorStatus {

  //@Id
  private String pushMessageVariantId; // = "push-job-id" + ":" + "variant-id";
  private String errorReason; // the text we receive for the error

  private String pushObjectId;
  private String variantId;

  public VariantErrorStatus() {
  }

  public VariantErrorStatus(final String pushObjectId, final String variantId, final String errorReason) {
    this.pushMessageVariantId = new StringBuilder(pushObjectId).append(':').append(variantId).toString();
    this.variantId = variantId;
    this.pushObjectId = pushObjectId;
    this.errorReason = errorReason;
  }

  public String getErrorReason() {
    return errorReason;
  }

  public void setErrorReason(String errorReason) {
    this.errorReason = errorReason;
  }

  public String getPushMessageVariantId() {
    return pushMessageVariantId;
  }

  public void setPushMessageVariantId(String pushMessageVariantId) {
    this.pushMessageVariantId = pushMessageVariantId;
  }

  public String getPushObjectId() {
    return pushObjectId;
  }

  public void setPushObjectId(String pushObjectId) {
    this.pushObjectId = pushObjectId;
  }

  public String getVariantId() {
    return variantId;
  }

  public void setVariantId(String variantId) {
    this.variantId = variantId;
  }
}