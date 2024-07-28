package com.mpc.scalats.configuration

import com.mpc.scalats.CLIOpts.IndentSize

/**
 * Created by Milosz on 09.12.2016.
 */
case class Config(
  stringWithSingleQuotes: Boolean = false,
  indentSize: Int = IndentSize.Default,
  emitSemicolons: Boolean = false,
  optionToNullable: Boolean = true,
  optionToUndefined: Boolean = false,
  prependIPrefix: Boolean = true,
  emitInterfacesAsTypes: Boolean = false
)
