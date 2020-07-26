package watch.craft.utils

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verifyBlocking
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CoroutineUtilsTest {
  private interface Foo {
    suspend fun get(): Int
  }

  @Test
  fun `memoizes suspending function`() {
    val foo = mock<Foo> {
      onBlocking { get() } doReturn 3
    }

    val memoized = memoize(foo::get)

    runBlocking {
      assertEquals(3, memoized())
      assertEquals(3, memoized())
    }

    verifyBlocking(foo, times(1)) { get() }
  }
}
