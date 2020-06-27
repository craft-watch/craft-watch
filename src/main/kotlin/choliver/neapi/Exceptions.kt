package choliver.neapi

abstract class ScraperException : RuntimeException {
  constructor(message: String) : super(message)
  constructor(message: String, cause: Throwable) : super(message, cause)
}

// Intentionally skip because we know we aren't dealing with something
class NotAnItemException : ScraperException {
  constructor(message: String) : super(message)
  constructor(message: String, cause: Throwable) : super(message, cause)
}

// Input data is malformed in some way
class MalformedInputException : ScraperException {
  constructor(message: String) : super(message)
  constructor(message: String, cause: Throwable) : super(message, cause)
}

// Scraped item doesn't pass validation checks
class InvalidItemException : ScraperException {
  constructor(message: String) : super(message)
  constructor(message: String, cause: Throwable) : super(message, cause)
}

// Hard fail
class FatalException : ScraperException {
  constructor(message: String) : super(message)
  constructor(message: String, cause: Throwable) : super(message, cause)
}
