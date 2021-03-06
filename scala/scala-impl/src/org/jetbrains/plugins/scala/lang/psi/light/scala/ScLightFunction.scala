package org.jetbrains.plugins.scala.lang
package psi
package light.scala

import com.intellij.psi.{PsiAnnotation, PsiElement}
import org.jetbrains.plugins.scala.lang.psi.api.base.ScModifierList
import org.jetbrains.plugins.scala.lang.psi.api.base.types.ScTypeElement
import org.jetbrains.plugins.scala.lang.psi.api.expr.{ScAnnotation, ScExpression}
import org.jetbrains.plugins.scala.lang.psi.api.statements.params.ScTypeParamClause
import org.jetbrains.plugins.scala.lang.psi.api.statements.{ScFunction, ScFunctionDeclaration, ScFunctionDefinition}
import org.jetbrains.plugins.scala.lang.psi.types.ScType
import org.jetbrains.plugins.scala.lang.psi.types.api.TypeParameter

import scala.annotation.tailrec

sealed abstract class ScLightFunction[F <: ScFunction](override protected val delegate: F,
                                                       override final val paramClauses: ScLightParameters)
                                                      (implicit private val returnType: ScType,
                                                       private val innerTypeParameters: List[TypeParameter])
  extends ScLightElement(delegate) with ScFunction {

  override final def getNavigationElement: PsiElement = super[ScLightElement].getNavigationElement

  override protected final def returnTypeInner = Right(returnType)

  override final def definedReturnType = Right(returnType)

  override final def typeParametersClause: Option[ScTypeParamClause] =
    delegate.typeParametersClause.map {
      new ScLightTypeParamClause(innerTypeParameters, _)
    }

  override final def hasAssign: Boolean = delegate.hasAssign

  override final def hasExplicitType: Boolean = true

  override final def returnTypeElement: Option[ScTypeElement] = delegate.returnTypeElement

  override final def getModifierList: ScModifierList = delegate.getModifierList

  override final def psiAnnotations: Array[PsiAnnotation] = delegate.getAnnotations

  override final def getApplicableAnnotations: Array[PsiAnnotation] = delegate.getApplicableAnnotations

  override final def findAnnotation(qualifiedName: String): PsiAnnotation = delegate.findAnnotation(qualifiedName)

  override final def addAnnotation(qualifiedName: String): PsiAnnotation = delegate.addAnnotation(qualifiedName)

  override final def hasAnnotation(qualifiedName: String): Boolean = delegate.hasAnnotation(qualifiedName)

  override final def annotations: Seq[ScAnnotation] = delegate.annotations
}

object ScLightFunction {

  def apply(function: ScFunction,
            evalTypes: Seq[Seq[() => ScType]],
            typeParameters: Seq[TypeParameter])
           (implicit returnType: ScType): ScLightFunction[_] = this (
    function,
    new ScLightParameters(evalTypes.map(_.map(_.apply())), function)
  )(returnType, typeParameters.toList)

  @tailrec
  private[this] def apply(function: ScFunction, paramClauses: ScLightParameters)
                         (implicit returnType: ScType,
                          typeParameters: List[TypeParameter]): ScLightFunction[_] = function match {
    case light: ScLightFunction[_] => apply(light.delegate, paramClauses)
    case declaration: ScFunctionDeclaration => new ScLightFunctionDeclaration(declaration, paramClauses)
    case definition: ScFunctionDefinition => new ScLightFunctionDefinition(definition, paramClauses)
  }

  private final class ScLightFunctionDeclaration(override protected val delegate: ScFunctionDeclaration,
                                                 paramClauses: ScLightParameters)
                                                (implicit returnType: ScType,
                                                 typeParameters: List[TypeParameter])
    extends ScLightFunction(delegate, paramClauses) with ScFunctionDeclaration {

    override def getParent: PsiElement = delegate.getParent
  }

  private final class ScLightFunctionDefinition(override protected val delegate: ScFunctionDefinition,
                                                paramClauses: ScLightParameters)
                                               (implicit returnType: ScType,
                                                typeParameters: List[TypeParameter])
    extends ScLightFunction(delegate, paramClauses) with ScFunctionDefinition {

    override def assignment: Option[PsiElement] = delegate.assignment

    override def body: Option[ScExpression] = delegate.body

    override def hasParameterClause: Boolean = delegate.hasParameterClause
  }

}