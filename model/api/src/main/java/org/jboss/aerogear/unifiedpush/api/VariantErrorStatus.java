package org.jboss.aerogear.unifiedpush.api;

public class VariantErrorStatus extends BaseModel {

  //@Id
  private String pushMessageVariantId; // = "push-job-id" + ":" + "variant-id";
  private String errorReason; // the text we receive for the error

  private String pushJobId;
  private String variantId;

  private FlatPushMessageInformation pushMessageInformation;

  public VariantErrorStatus() {
  }

  public VariantErrorStatus(final String pushJobId, final String variantId, final String errorReason) {
    this.pushMessageVariantId = new StringBuilder(pushJobId).append(':').append(variantId).toString();
    this.variantId = variantId;
    this.pushJobId = pushJobId;
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

  public String getPushJobId() {
    return pushJobId;
  }

  public void setPushJobId(String pushJobId) {
    this.pushJobId = pushJobId;
  }

  public String getVariantId() {
    return variantId;
  }

  public void setVariantId(String variantId) {
    this.variantId = variantId;
  }

  public void setPushMessageInformation(FlatPushMessageInformation pushMessageInformation) {
    this.pushMessageInformation = pushMessageInformation;
  }

  public FlatPushMessageInformation getPushMessageInformation() {
    return pushMessageInformation;
  }
}