package net.xorf.queries

import Query._

class JPAQuery[T](q: Query[T]) {
  val max = q.max
  val typeName = q.from.typeName
  val query = "FROM %s WHERE %s".format(q.from.typeName, JPAQuery.emitExpr(new ExprCount, q.where))

  println("building query: %s".format(query))

  val params = JPAQuery.setParameters(Map(), new ExprCount, q.where)
  println("with parameters:")
  for (p <- params) { println("%s -> %s".format(p._1, p._2.toString)) }
}

class ExprCount {
  private var cnt = 0
  def ++ = {
    cnt += 1
    this
  }

  def apply() = "param_%s".format(cnt)
}

object JPAQuery {
  private def formatCond(cnt: ExprCount, name: String, op: String) = "%1$s %2$s :%3$s".format(name, op, cnt.++())

  private def emitExpr(cnt: ExprCount, e: Expr): String = {
    e match {
      case x:EqualsCondition => formatCond(cnt, x.name, "=")
      case x:LikeCondition => formatCond(cnt, x.name, "LIKE")
      case x:GTCondition[_] => formatCond(cnt, x.name, ">")
      case x:LTCondition[_] => formatCond(cnt, x.name, "<")
      case x:AndOp => emitExpr(cnt, x.left) + " AND " + emitExpr(cnt, x.right)
      case x:OrOp => emitExpr(cnt, x.left) + " OR " + emitExpr(cnt, x.right)
    }
  }

  private def formatValue(cond: Condition) =
    cond match {
      case x:LikeLeftCondition => "%%%s".format(cond.value.value)
      case x:LikeRightCondition => "%s%%".format(cond.value.value)
      case _ =>  cond.value.value
    }

  private def setParameters(map: Map[String, Any], cnt: ExprCount, e: WhereExpr): Map[String, Any] = {
    e match {
      case x:Condition => map + (cnt.++() -> formatValue(x))
      case x:BooleanOp => {
        setParameters(map, cnt,  x.bl) ++ setParameters(map, cnt,  x.br)
      }
    }
  }

  implicit def query2JPAQuery[T](q: Query[T]): JPAQuery[T] = new JPAQuery(q)
}
