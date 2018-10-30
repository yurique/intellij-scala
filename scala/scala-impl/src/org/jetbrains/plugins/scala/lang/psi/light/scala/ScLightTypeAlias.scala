package org.jetbrains.plugins.scala.lang
package psi
package light.scala

import com.intellij.psi.PsiElement
import org.jetbrains.plugins.scala.lang.psi.api.base.types.ScTypeElement
import org.jetbrains.plugins.scala.lang.psi.api.statements.params.ScTypeParamClause
import org.jetbrains.plugins.scala.lang.psi.api.statements.{ScTypeAlias, ScTypeAliasDeclaration, ScTypeAliasDefinition}
import org.jetbrains.plugins.scala.lang.psi.types.TypeAliasSignature
import org.jetbrains.plugins.scala.lang.psi.types.result.TypeResult

import scala.annotation.tailrec

sealed abstract class ScLightTypeAlias[A <: ScTypeAlias] protected(override protected val delegate: A)
                                                                  (implicit private val signature: TypeAliasSignature)
  extends ScLightModifierOwner(delegate) with ScTypeAlias {

  private val TypeAliasSignature(_, typeParams, lowerType, upperType, _, _) = signature

  override def getNavigationElement: PsiElement = super[ScLightModifierOwner].getNavigationElement

  override def getOriginalElement: PsiElement = delegate.getOriginalElement

  override def lowerBound: TypeResult = Right(lowerType)

  override def upperBound: TypeResult = Right(upperType)

  override final def typeParametersClause: Option[ScTypeParamClause] =
    delegate.typeParametersClause.map {
      new ScLightTypeParamClause(typeParams, _)
    }

  override def physical: ScTypeAlias = delegate
}

object ScLightTypeAlias {

  def apply(signature: TypeAliasSignature): ScLightTypeAlias[_] = apply(signature.ta)(signature)

  @tailrec
  private[this] def apply(typeAlias: ScTypeAlias)
                         (implicit signature: TypeAliasSignature): ScLightTypeAlias[_] = typeAlias match {
    case light: ScLightTypeAlias[_] => apply(light.delegate)
    case declaration: ScTypeAliasDeclaration => new ScLightTypeAliasDeclaration(declaration)
    case definition: ScTypeAliasDefinition => new ScLightTypeAliasDefinition(definition)
  }

  private final class ScLightTypeAliasDeclaration(override protected val delegate: ScTypeAliasDeclaration)
                                                 (implicit signature: TypeAliasSignature)
    extends ScLightTypeAlias(delegate) with ScTypeAliasDeclaration {

    override def lowerBound: TypeResult = super[ScLightTypeAlias].lowerBound

    override def upperBound: TypeResult = super[ScLightTypeAlias].upperBound
  }

  final class ScLightTypeAliasDefinition(override protected val delegate: ScTypeAliasDefinition)
                                        (implicit signature: TypeAliasSignature)
    extends ScLightTypeAlias(delegate) with ScTypeAliasDefinition {

    override def lowerBound: TypeResult = super[ScLightTypeAlias].lowerBound

    override def upperBound: TypeResult = super[ScLightTypeAlias].upperBound

    override def aliasedType: TypeResult = lowerBound

    override def aliasedTypeElement: Option[ScTypeElement] = delegate.aliasedTypeElement
  }

}