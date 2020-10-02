package models.requests;

case class IdentifierRequest[A] (request: Request[A], identifier: String) extends WrappedRequest[A](request)
