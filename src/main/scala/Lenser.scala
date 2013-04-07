package com.github.hexx.scalaz

import language.experimental.macros
import language.dynamics

import scala.reflect.macros.Context

class Lenser[A] extends Dynamic {
  def selectDynamic(propName: String)  = macro Lenser.selectDynamic[A]
  def applyDynamic(propName: String)() = macro Lenser.applyDynamic[A]
}

object Lenser {
  def lens[A] = new Lenser[A]

  def selectDynamic[A: c.WeakTypeTag](c: Context)(propName: c.Expr[String]) = applyDynamic[A](c)(propName)()

  def applyDynamic[A: c.WeakTypeTag](c: Context)(propName: c.Expr[String])() = {
    import c.universe._

    // (name: tpe) => ...
    def mkParam(name: String, tpe: c.Type) =
      ValDef(Modifiers(Flag.PARAM), newTermName(name), TypeTree(tpe), EmptyTree)

    // (a$: classType, x$: memberType) => a$.copy(memberName = x$)
    def mkSetter(memberName: String, classType: Type, memberType: Type) =
      Function(List(mkParam("a$", classType), mkParam("x$", memberType)),
        Apply(Select(Ident(newTermName("a$")), newTermName("copy")),
          List(AssignOrNamedArg(Ident(newTermName(memberName)), Ident(newTermName("x$"))))))

    // (a$: classType) => a$.memberName
    def mkGetter(memberName: String, classType: Type) =
      Function(List(mkParam("a$", classType)), Select(Ident(newTermName("a$")), newTermName(memberName)))

    // scalaz.Lens.lensg(setter, getter)
    def mkLens(setter: Tree, getter: Tree) =
      Apply(Select(Select(Ident(newTermName("scalaz")), newTermName("Lens")), newTermName("lensu")), List(setter, getter))

    val classType = implicitly[WeakTypeTag[A]].tpe
    val Literal(Constant(memberName: String)) = propName.tree.asInstanceOf[Tree]
    val getterMember = classType.member(newTermName(memberName)) orElse {
      c.abort(c.enclosingPosition, "value " + memberName + " is not a member of " + classType)
    }
    val memberType = getterMember.typeSignatureIn(classType) match {
      case NullaryMethodType(memberType) => memberType
      case _                             => c.abort(c.enclosingPosition, "member %s is not a field".format(memberName))
    }

    c.Expr[Any](mkLens(mkSetter(memberName, classType, memberType), mkGetter(memberName, classType)))
  }
}
