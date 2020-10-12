package models

sealed trait DutyType

object DutyType extends Enumerable.Implicits {
  case object Customs extends WithName("01") with DutyType
  case object Vat extends WithName("02") with DutyType
  case object Other extends WithName("02") with DutyType

  val values: Seq[DutyType] = Seq(
    Customs,
    Vat,
    Other
  )

  implicit val enumerable: Enumerable[DutyType] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
