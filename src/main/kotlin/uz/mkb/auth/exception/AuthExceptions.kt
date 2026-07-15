package uz.mkb.auth.exception


class UsernameAlreadyExistsException(message: String) : RuntimeException(message)
class InvalidCredentialsException(message: String) : RuntimeException(message)
class InvalidTokenException(message: String) : RuntimeException(message)