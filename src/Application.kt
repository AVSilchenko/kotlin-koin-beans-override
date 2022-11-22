package bug.reproduce

import io.ktor.server.application.*
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.core.context.GlobalContext
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

abstract class BaseHelloWorld(listOfInvocation: MutableList<String>) {
    init {
        listOfInvocation.add(this.hello())
    }
    abstract fun hello(): String
}


class ProductionHelloWorld(listOfInvocation: MutableList<String>) : BaseHelloWorld(listOfInvocation) {
    override fun hello(): String {
        return "ITS PRODUCTION"
    }

}

class TestingHelloWorld(listOfInvocation: MutableList<String>) : BaseHelloWorld(listOfInvocation) {
    override fun hello(): String {
        return "ITS TESTING"
    }
}

@Suppress("unused") // Referenced in application.conf
@JvmOverloads
fun Application.mainModule(testing: Boolean = true) {
    val listOfInvocation = mutableListOf<String>()

    val prodModule: Module = module {
        single(createdAtStart = true) {
            ProductionHelloWorld(listOfInvocation)
        } bind BaseHelloWorld::class
    }

    // should override prodModule:
    val testModule: Module = module {
        single(createdAtStart = true) { // in previous koin module there was override=true here
            TestingHelloWorld(listOfInvocation)
        } bind BaseHelloWorld::class
    }

    install(Koin) {
        if (testing) modules(prodModule + testModule) else modules(prodModule)
    }

    routing {
        get("/hello") {
            val koin = GlobalContext.get()
            val helloWorldPrinter = koin.get<BaseHelloWorld>()
            call.respondText(helloWorldPrinter.hello(), contentType = ContentType.Text.Plain)
        }
        get("/hello_invocations") {
            call.respondText(listOfInvocation.joinToString(), contentType = ContentType.Text.Plain)
        }
    }
}
