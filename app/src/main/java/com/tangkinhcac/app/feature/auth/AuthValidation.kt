package com.tangkinhcac.app.feature.auth

internal const val MIN_PASSWORD_LENGTH = 8
internal const val MAX_PASSWORD_LENGTH = 128
internal const val MAX_EMAIL_LENGTH = 255
internal const val BIRTH_DATE_PATTERN = "dd/MM/uuuu"
internal val EMAIL_PATTERN = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
