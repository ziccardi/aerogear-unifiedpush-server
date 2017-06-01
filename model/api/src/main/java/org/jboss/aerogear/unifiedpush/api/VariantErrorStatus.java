package org.jboss.aerogear.unifiedpush.api;

public class VariantErrorStatus {

  //@Id
  private String pushMessageVariantId = "push-job-id" + ":" + "variant-id";
  private String errorReason; // the text we receive for the error

  public VariantErrorStatus() {

  }

  public void setPushMessageVariantId(final String bogusID) {
    pushMessageVariantId = bogusID;
  }


  public void applyAllTheThings(final String PMI_ID, final String varID ) {
    // update me
  }

}