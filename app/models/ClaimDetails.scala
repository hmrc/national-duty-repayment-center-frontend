package models.requests

final case class ClaimDetails(formType:String,
                              customRegulationType:String,
                              claimedUnderArticle:String,
                             claimant:String,
                              claimType:String,
                              noOfEntries:String,
                              epu:String,
                              entryNumber:String,
                              entryDate:String,
                              claimReason:String,
                              claimDescription:String,
                              dateReceived:String,
                              claimDate:String,payeeIndicator:String,
                              paymentMethod:String
                             )
