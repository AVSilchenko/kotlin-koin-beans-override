package bug.reproduce

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.*
import org.junit.Assert.assertEquals
import org.junit.Test

class ApplicationTest {
    // CASE WITHOUT OVERRIDE:
    @Test
    fun `prod printer works correct`() {
        withTestApplication({ mainModule(testing = false) }) {
            handleRequest(HttpMethod.Get, "/hello").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("ITS PRODUCTION", response.content)
            }
        }
    }
    @Test
    fun `invocation counts in trivial case`() {
        withTestApplication({ mainModule(testing = false) }) {
            handleRequest(HttpMethod.Get, "/hello_invocations").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("ITS PRODUCTION", response.content)
            }
        }
    }

    // CASE WITH OVERRIDE:
    @Test
    fun `printer overridden correctly in test`() {
        withTestApplication({ mainModule(testing = true) }) {
            handleRequest(HttpMethod.Get, "/hello").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("ITS TESTING", response.content)
            }
        }
    }

    @Test
    fun `invocation counts in case with override`() {
        withTestApplication({ mainModule(testing = true) }) {
            handleRequest(HttpMethod.Get, "/hello_invocations").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("ITS TESTING", response.content) // fails here, got "ITS PRODUCTION, ITS TESTING"
            }
        }
    }
}
