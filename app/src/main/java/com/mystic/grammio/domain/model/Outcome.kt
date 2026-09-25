package com.mystic.grammio.domain.model

/**
 * Success-or-typed-failure result. Unlike [kotlin.Result], the error type is part of the signature,
 * so callers handle a closed set of failures instead of guessing from a Throwable.
 */
sealed interface Outcome<out T, out E> {
    data class Success<out T>(val value: T) : Outcome<T, Nothing>
    data class Failure<out E>(val error: E) : Outcome<Nothing, E>
}

inline fun <T, E, R> Outcome<T, E>.map(transform: (T) -> R): Outcome<R, E> = when (this) {
    is Outcome.Success -> Outcome.Success(transform(value))
    is Outcome.Failure -> this
}
