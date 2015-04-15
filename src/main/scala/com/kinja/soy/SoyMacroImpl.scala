package com.kinja.soy

import scala.language.higherKinds
import scala.reflect.macros.Context
import language.experimental.macros

object SoyMacroImpl {

  def writesImpl[A](c: Context)(implicit atag: c.WeakTypeTag[A]): c.Expr[SoyMapWrites[A]] = {

    import c.universe._
    import c.universe.Flag._

    val soyWrites = c.universe.weakTypeTag[SoyWrites[_]]
    val soyMapWrites = c.universe.weakTypeTag[SoyMapWrites[_]]

    val companioned = weakTypeOf[A].typeSymbol
    val companionSymbol = companioned.companionSymbol
    val companionType = companionSymbol.typeSignature

    val soyPkg = Select(Select(Ident(newTermName("com")), newTermName("kinja")), newTermName("soy"))

    val soy = Select(soyPkg, newTermName("Soy"))
    val writesSelect = Select(soyPkg, newTermName("SoyWrites"))
    val lazyHelperSelect = Select(Select(soyPkg, newTermName("util")), newTypeName("Lazy"))

    val candidateUnapply = companionType.declaration(newTermName("unapply"))
    val candidateUnapplySeq = companionType.declaration(newTermName("unapplySeq"))
    val hasVarArgs = candidateUnapplySeq != NoSymbol

    val unapply = (candidateUnapply, candidateUnapplySeq) match {
      case (s, _) if s != NoSymbol => s.asMethod
      case (_, s) if s != NoSymbol => s.asMethod
      case _ => c.abort(c.enclosingPosition, "No unapply or unapplySeq function found")
    }

    val unapplyReturnTypes: Option[List[Type]] = unapply.returnType match {
      case TypeRef(_, _, Nil) => {
        None
      }
      case TypeRef(_, _, args) =>
        args.head match {
          case t @ TypeRef(_, _, Nil) => Some(List(t))
          case t @ TypeRef(_, _, args) => {
            import c.universe.definitions.TupleClass
            if (!TupleClass.seq.exists(tupleSym => t.baseType(tupleSym) ne NoType)) Some(List(t))
            else if (t <:< typeOf[Product]) Some(args)
            else None
          }
          case _ => None
        }
      case _ => None
    }

    val applies =
      companionType.declaration(stringToTermName("apply")) match {
        case NoSymbol => c.abort(c.enclosingPosition, "No apply function found")
        case s => s.asMethod.alternatives
      }

    // Find the apply method that matches the unapply method.
    val apply = applies.collectFirst {
      case (apply: MethodSymbol) if hasVarArgs && {
        val someApplyTypes = apply.paramss.headOption.map(_.map(_.asTerm.typeSignature))
        val someInitApply = someApplyTypes.map(_.init)
        val someApplyLast = someApplyTypes.map(_.last)
        val someInitUnapply = unapplyReturnTypes.map(_.init)
        val someUnapplyLast = unapplyReturnTypes.map(_.last)
        val initsMatch = someInitApply == someInitUnapply
        val lastMatch = (for {
          lastApply <- someApplyLast
          lastUnapply <- someUnapplyLast
        } yield lastApply <:< lastUnapply).getOrElse(false)
        initsMatch && lastMatch
      } => apply
      case (apply: MethodSymbol) if (apply.paramss.headOption.map(_.map(_.asTerm.typeSignature)) == unapplyReturnTypes) => apply
    }

    val params = apply match {
      case Some(apply) => apply.paramss.head //verify there is a single parameter group
      case None if unapplyReturnTypes.isEmpty => List.empty // The case class has no parameters.
      case None => c.abort(c.enclosingPosition, "No apply function found matching unapply parameters")
    }

    final case class Implicit(paramName: Name, paramType: Type, neededImplicit: Tree, isRecursive: Boolean, tpe: Type)

    def implicitSoyWrites(name: Name, typ: c.universe.type#Type) = {
      val isRecursive = typ match {
        case TypeRef(_, t, args) =>
          args.exists(_.typeSymbol == companioned)
        case TypeRef(_, t, _) => false
      }

      // Infer an implicit SoyWrites[typ]
      val soyWritesTyp = c.inferImplicitValue(
        appliedType(soyWrites.tpe.typeConstructor, List(typ)))
      Implicit(name, typ, soyWritesTyp, isRecursive, typ)
    }

    val applyParamImplicits = params.map(param => implicitSoyWrites(param.name, param.typeSignature))
    val inferredImplicits = if (hasVarArgs) {
      val varArgsImplicit = implicitSoyWrites(
        applyParamImplicits.last.paramName,
        unapplyReturnTypes.get.last)
      applyParamImplicits.init :+ varArgsImplicit
    } else
      applyParamImplicits

    // Collect missing implicits then abort with an error message for each missing implicit.
    val missingImplicits = inferredImplicits.collect {
      case Implicit(_, t, impl, rec, _) if (impl == EmptyTree && !rec) => t
    }
    if (missingImplicits.nonEmpty)
      c.abort(c.enclosingPosition, s"No implicit SoyWrites for ${missingImplicits.mkString(", ")} available.")

    val lazyVal = Select(Ident(newTermName("self")), newTermName("lazyVal"))
    // Select a higher-kinded implicit constructor from SoyWrites.
    def hkImpl(methodName: String): Tree =
      Apply(Select(writesSelect, newTermName(methodName)), List(lazyVal))

    var hasRec = false

    // Collect all the class members into `("fieldName", clazz.fieldName)` pairs.
    val pairs = inferredImplicits.map {
      case Implicit(name, t, impl, rec, tpe) =>
        if (!rec)
          q"(${name.decoded}, $soy.toSoy(clazz.${name.toTermName})($impl))"
        else {
          hasRec = true
          val lazyImpl =
            if (t.typeConstructor <:< typeOf[Option[_]].typeConstructor)
              hkImpl("OptionSoy")
            else if (tpe.typeConstructor <:< typeOf[Array[_]].typeConstructor)
              hkImpl("arraySoy")
            else if (tpe.typeConstructor <:< typeOf[Map[_, _]].typeConstructor)
              hkImpl("mapSoy")
            else if (tpe.typeConstructor <:< typeOf[Traversable[_]].typeConstructor)
              hkImpl("traversableSoy")
            else
              lazyVal
          q"(${name.decoded}, $soy.toSoy(clazz.${name.toTermName})($lazyImpl))"
        }
    }

    val soyMapWritesDef =
      q"new com.kinja.soy.SoyMapWrites[$atag] { def toSoy(clazz: $atag) = $soy.map(..$pairs) }"

    // Recursive types must be constructed using Lazy.
    if (!hasRec)
      c.Expr[SoyMapWrites[A]](soyMapWritesDef)
    else {
      val soyMapWritesA = AppliedTypeTree(
        Ident(soyMapWrites.tpe.typeSymbol),
        List(Ident(atag.tpe.typeSymbol)))
      val block = q"""{
        new $lazyHelperSelect[$soyMapWritesA] { self =>
          override lazy val lazyVal : $soyMapWritesA = $soyMapWritesDef
        }
      }.lazyVal"""

      c.Expr[SoyMapWrites[A]](block)
    }
  }

}
