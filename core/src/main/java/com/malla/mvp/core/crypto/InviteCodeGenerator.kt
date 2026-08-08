package com.malla.mvp.core.crypto

import kotlin.random.Random

object InviteCodeGenerator {

    private const val BASE_CODE_LENGTH = 8
    private const val VALIDITY_MS = 24 * 60 * 60 * 1000L

    private val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

    data class InviteCode(
        val code: String,
        val extra: String? = null,
        val expiresAt: Long
    ) {
        val fullCode: String get() = if (extra != null) code + extra else code
    }

    fun generate(extra: String? = null): InviteCode {
        val base = (1..BASE_CODE_LENGTH).map { chars[Random.nextInt(chars.length)] }.joinToString("")
        return InviteCode(
            code = base,
            extra = extra,
            expiresAt = System.currentTimeMillis() + VALIDITY_MS
        )
    }

    fun isValid(invite: InviteCode?): Boolean {
        if (invite == null) return false
        if (System.currentTimeMillis() > invite.expiresAt) return false
        return invite.code.length == BASE_CODE_LENGTH && invite.code.all { it in chars }
    }
}
