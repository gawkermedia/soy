package com.kinja.soy

import scala.language.higherKinds
import scala.reflect.macros.blackbox
import language.experimental.macros

object SoyMacroImpl {

  def writesImplLogic[A](c: blackbox.Context)(typed: Boolean)(implicit atag: c.WeakTypeTag[A]): c.Expr[SoyMapWrites[A]] = {

    import c.universe._
    import c.universe.Flag._

    val soyWrites = c.universe.weakTypeTag[SoyWrites[_]]
    val soyMapWrites = c.universe.weakTypeTag[SoyMapWrites[_]]

    val companioned = weakTypeOf[A].typeSymbol
    val companionObject = companioned.companion
    val companionType = companionObject.typeSignature

    val soyPkg = Select(Select(Ident(TermName("com")), TermName("kinja")), TermName("soy"))

    val soy = Select(soyPkg, TermName("Soy"))
    val writesSelect = Select(soyPkg, TermName("SoyWrites"))
    val lazyHelperSelect = Select(Select(soyPkg, TermName("util")), TypeName("Lazy"))

    val candidateUnapply = companionType.decl(TermName("unapply"))
    val candidateUnapplySeq = companionType.decl(TermName("unapplySeq"))
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
      companionType.decl(TermName("apply")) match {
        case NoSymbol => c.abort(c.enclosingPosition, "No apply function found")
        case s => s.asTerm.alternatives
      }

    // When presented with a case class parameterized over A, the A taken by
    // `apply` and the A returned by `unapply` do not compare equal because they
    // have different owners. In this case we compare the types of the symbols
    // which are both className.A and compare equal.
    def compareTypeLists(a: List[Type], b: List[Type]): Boolean =
      (a.length == b.length) && (a zip b).forall {
        case (a @ TypeRef(aPrefix, aName, aParams), b @ TypeRef(bPrefix, bName, bParams)) =>
          aName.typeSignature =:= bName.typeSignature &&
            aPrefix == bPrefix &&
            compareTypeLists(aParams, bParams)
        case (_a, _b) => _a =:= _b
      }

    // Find the apply method that matches the unapply method.
    val apply = applies.collectFirst {
      case (apply: MethodSymbol) if hasVarArgs && {
        val someApplyTypes = apply.paramLists.headOption.map(_.map(_.asTerm.typeSignature))
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
      case (apply: MethodSymbol) if apply.paramLists.headOption.map(_.map(_.asTerm.typeSignature)).exists(a =>
        unapplyReturnTypes.exists(u => compareTypeLists(a, u))) => apply
    }

    val params = apply match {
      case Some(apply) => apply.paramLists.head // Verify there is a single parameter group
      case None if unapplyReturnTypes.isEmpty => List.empty // The case class has no parameters.
      case None => c.abort(c.enclosingPosition, "No apply function found matching unapply parameters")
    }

    final case class Implicit(paramName: Name, paramType: Type, neededImplicit: Tree, isRecursive: Boolean, tpe: Type)

    def implicitSoyWrites(name: Name, typ: Type) = {
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

    val lazyVal = Select(Ident(TermName("self")), TermName("lazyVal"))
    // Select a higher-kinded implicit constructor from SoyWrites.
    def hkImpl(methodName: String): Tree =
      Apply(Select(writesSelect, TermName(methodName)), List(lazyVal))

    var hasRec = false

    // Collect all the class members into `("fieldName", clazz.fieldName)` pairs.
    val pairs = inferredImplicits.map {
      case Implicit(name, t, impl, rec, tpe) =>
        if (impl == EmptyTree && !rec) // We couldn't supply the implicit, usually because this argument is generic.
          q"(${name.decodedName.toString}, $soy.toSoy(clazz.${name.toTermName}))"
        else if (!rec)
          q"(${name.decodedName.toString}, $soy.toSoy(clazz.${name.toTermName})($impl))"
        else {
          hasRec = true
          val lazyImpl =
            if (t.typeConstructor <:< typeOf[Option[_]].typeConstructor)
              Some(hkImpl("OptionSoy"))
            else if (tpe.typeConstructor <:< typeOf[Array[_]].typeConstructor)
              Some(hkImpl("arraySoy"))
            else if (tpe.typeConstructor <:< typeOf[Map[_, _]].typeConstructor)
              Some(hkImpl("mapSoy"))
            else if (tpe.typeConstructor <:< typeOf[Iterable[_]].typeConstructor)
              Some(hkImpl("iterableSoy"))
            else
              None
          lazyImpl.fold(q"(${name.decodedName.toString}, $soy.toSoy(clazz.${name.toTermName}))")(impl =>
            q"(${name.decodedName.toString}, $soy.toSoy(clazz.${name.toTermName})($impl))")
        }
    }

    val typeName = "type"
    val typeValue = companioned.name.toString

    val pairsWithType = if (typed)
      pairs :+ q"($typeName, $typeValue)"
    else
      pairs

    val soyMapWritesDef =
      q"new com.kinja.soy.SoyMapWrites[$atag] { def toSoy(clazz: $atag) = $soy.map(..$pairsWithType) }"

    // Recursive types must be constructed using Lazy.
    if (!hasRec) {
      c.Expr[SoyMapWrites[A]](soyMapWritesDef)
    } else {
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

  def writesImpl[A](c: blackbox.Context)(implicit atag: c.WeakTypeTag[A]): c.Expr[SoyMapWrites[A]] =
    writesImplLogic(c)(typed = false)(atag)

  def typedWritesImpl[A](c: blackbox.Context)(implicit atag: c.WeakTypeTag[A]): c.Expr[SoyMapWrites[A]] =
    writesImplLogic(c)(typed = true)(atag)
}
