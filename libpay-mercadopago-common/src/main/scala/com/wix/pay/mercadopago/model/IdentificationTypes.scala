package com.wix.pay.mercadopago.model

object IdentificationTypes {
  /**
   * Argentinian Documento Nacional de Identidad.
   * @see <a href="http://en.wikipedia.org/wiki/Documento_Nacional_de_Identidad_(Argentina)">Documento Nacional de Identidad (Argentina)</a>
   */
  val dni = "DNI"

  /**
   * Brazilian Cadastro de Pessoas Físicas.
   * @see <a href="http://en.wikipedia.org/wiki/Cadastro_de_Pessoas_F%C3%ADsicas">Cadastro de Pessoas Físicas</a>
   */
  val cpf = "CPF"

  /**
   * Colombian Cedula de Ciudadania.
   * @see <a href="https://en.wikipedia.org/wiki/Identity_document#Colombia">Identity document in Colombia</a>
   */
  val cc = "CC"

  /**
   * Mexican Registro Federal de Contribuyentes.
   * @see <a href="https://es.wikipedia.org/wiki/Registro_Federal_de_Contribuyentes_(M%C3%A9xico)">Registro Federal de Contribuyentes (México)</a>
   */
  val rfc = "RFC"

  /**
   * Venezuelan Cédula de identidad..
   * @see <a href="https://en.wikipedia.org/wiki/Identity_document#Venezuela">Identity document in Venezuela</a>
   */
  val ci = "CI"
}
