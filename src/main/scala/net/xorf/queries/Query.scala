package net.xorf.queries

abstract class Expr
abstract class BooleanExpr(val left: Expr, val right: Expr) extends Expr

class Query[T](val from: FromExpr[T], val where: WhereExpr) extends BooleanExpr(from, where) {
  private var maxResults = 100

  def max = maxResults

  def max(m: Int) = {
    this.maxResults = m
    this
  }
}

class FromExpr[T](typeManifest: Manifest[T]) extends Expr {
  lazy val typeName = typeManifest.erasure.getSimpleName
  def where(e: WhereExpr) = new Query(this, e)
}

abstract class WhereExpr(val l: Expr, val r: Expr) extends BooleanExpr(l, r) {
  def and(right: WhereExpr) = AndOp(this, right)
  def or(right: WhereExpr) = OrOp(this, right)
}

abstract class Condition(val name: FieldName, val value: Value[_]) extends WhereExpr(name, value)

abstract class BooleanOp(val bl: WhereExpr, val br: WhereExpr) extends WhereExpr(bl, br)

case class AndOp(andl: WhereExpr, andr: WhereExpr) extends BooleanOp(andl, andr)
case class OrOp(orl: WhereExpr, orr: WhereExpr) extends BooleanOp(orl, orr)

case class FieldName(name: String) extends Expr {
  def ===(value: Any) = EqualsCondition(this, Value(value))
  def #==(value: String) = LikeLeftCondition(this, Value(value))
  def ==~(value: String) = LikeRightCondition(this, Value(value))
  def >==[T <: Comparable[T]](value: T) = GTCondition[T](this, Value(value))
  def <==[T <: Comparable[T]](value: T) = LTCondition[T](this, Value(value))
}

case class Value[T](value: T) extends Expr

abstract class LikeCondition(n: FieldName, v: Value[String]) extends Condition(n, v)
case class EqualsCondition(n: FieldName, v: Value[Any]) extends Condition(n, v)
case class LikeLeftCondition(n: FieldName, v: Value[String]) extends LikeCondition(n, v)
case class LikeRightCondition(n: FieldName, v: Value[String]) extends LikeCondition(n, v)
case class GTCondition[T](n: FieldName, v: Value[Comparable[T]]) extends Condition(n, v)
case class LTCondition[T](n: FieldName, v: Value[Comparable[T]]) extends Condition(n, v)

object Query {
  def from[T](implicit manifest: Manifest[T]) = new FromExpr[T](manifest)

  implicit def string2fieldName(name: String): FieldName = FieldName(name)
  implicit def fieldName2String(fn: FieldName): String = fn.name
}
