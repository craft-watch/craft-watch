package watch.craft

abstract class ScraperException : RuntimeException {
  constructor(message: String) : super(message)
  constructor(message: String, cause: Throwable) : super(message, cause)
}

abstract class NonFatalScraperException : ScraperException {
  constructor(message: String) : super(message)
  constructor(message: String, cause: Throwable) : super(message, cause)
}

/** Intentionally skip because we know we aren't dealing with something. */
class SkipItemException : NonFatalScraperException {
  constructor(message: String) : super(message)
  constructor(message: String, cause: Throwable) : super(message, cause)
}

/** Input data is malformed in some way. */
class MalformedInputException : NonFatalScraperException {
  constructor(message: String) : super(message)
  constructor(message: String, cause: Throwable) : super(message, cause)
}

/** Scraped item doesn't pass validation checks. */
class InvalidItemException : NonFatalScraperException {
  constructor(message: String) : super(message)
  constructor(message: String, cause: Throwable) : super(message, cause)
}

/** Error retrieving data over network. */
class UnretrievableException : NonFatalScraperException {
  constructor(message: String) : super(message)
  constructor(message: String, cause: Throwable) : super(message, cause)
}

/** Hard fail. */
class FatalScraperException : ScraperException {
  constructor(message: String) : super(message)
  constructor(message: String, cause: Throwable) : super(message, cause)
}
