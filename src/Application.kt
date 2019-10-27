package com.example

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import freemarker.cache.ClassTemplateLoader
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.AutoHeadResponse
import io.ktor.features.ContentNegotiation
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.freemarker.FreeMarker
import io.ktor.freemarker.FreeMarkerContent
import io.ktor.http.cio.websocket.DefaultWebSocketSession
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.http.content.*
import io.ktor.jackson.jackson
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.sessions.*
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import java.io.*
import java.util.*
import kotlin.collections.ArrayList

class ChatClient(val session: DefaultWebSocketSession, val username: String) {
    val name = username
}

fun main(args: Array<String>) {

    val server = embeddedServer(Netty, 8080) {

        install(ContentNegotiation) {
            jackson {
            }
        }
        install(FreeMarker) {
            templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")
        }

        data class MySession(val username: String)

        val install = install(Sessions) {
            cookie<MySession>("SESSION")
        }

        val mapper = jacksonObjectMapper()
        var AllDataMessages: ArrayList<Map<String, String>> = ArrayList()

        val this_dir = File("").getAbsolutePath().replace("\\", "/")

        val file_messages = this_dir + "/resources/static/all_messages_data.txt"
        if(File(file_messages).exists()) {
            try {
                AllDataMessages = mapper.readValue(File(file_messages).readText())
            } finally {}
        }

        routing {
            static("/static") {
                resources("static")
            }
            static("/sended_images") {
                resources("/sended_images")
            }

            install(Authentication) {
                form("login") {
                    userParamName = "username"
                    passwordParamName = "password"
                    challenge = FormAuthChallenge.Unauthorized
                    validate { credentials -> if (credentials.name == credentials.password) UserIdPrincipal(credentials.name) else null }
                }
            }

            install(WebSockets)

            route("/") {
                install(AutoHeadResponse)

                get {
                    val session = call.sessions.get<MySession>()
                    if (session != null) {
                        call.respond(FreeMarkerContent("index.ftl", null))
                    } else {
                        call.respondRedirect("/login", permanent = false)
                    }
                }
            }

            get("/sended_images/{file_name}") {
                call.respondFile(File(this_dir + "/resources/sended_images", call.parameters["file_name"]))
            }

            route ("/post_file") {
                post {
                    val multipart = call.receiveMultipart()

                    multipart.forEachPart { part ->
                        if (part is PartData.FormItem) {
                            if (part.name == "data") {
                            }
                        } else if (part is PartData.FileItem) {
                            val ext = File(part.originalFileName).extension

                            val url = "upload-${System.currentTimeMillis()}-${part.name}.$ext"

                            val file = File(
                                this_dir + "/resources/sended_images",
                                url
                            )

                            part.streamProvider().use { its -> file.outputStream().buffered().use { its.copyToSuspend(it) } }

                            call.respondText("/sended_images/$url")
                        }

                        part.dispose()
                    }

                }
            }

            val clients = Collections.synchronizedSet(LinkedHashSet<ChatClient>())

            route("/chat") {
                webSocket {
                    val client = ChatClient(this, call.sessions.get<MySession>()?.username ?: "")
                    clients += client
                    client.session.outgoing.send(Frame.Text("""{"you": "${client.name}"}"""))
                    for (other in clients.toList()) {
                        if (other != client) {
                            other.session.outgoing.send(Frame.Text("""{"new": "${client.name}"}"""))
                            client.session.outgoing.send(Frame.Text("""{"new": "${other.name}"}"""))
                        }
                    }

                    try {
                        while (true) {
                            val frame = incoming.receive()
                            when (frame) {
                                is Frame.Text -> {
                                    val text = frame.readText()

                                    val json_data_regist: JsonMessage = mapper.readValue(text)

                                    for (other in clients.toList()) {
                                        if(json_data_regist.to_who == "to_all") {
                                            other.session.outgoing.send(
                                                Frame.Text(
                                                    """{"from": "${client.name}",
                                                    |"to": "to_all",
                                                    |"message": "${json_data_regist.message ?: ""}",
                                                    |"image": "${json_data_regist.image ?: ""}"}""".trimMargin()
                                                )
                                            )
                                        } else if (other.name == json_data_regist.to_who) {
                                            other.session.outgoing.send(
                                                Frame.Text(
                                                    """{"from": "${client.name}",
                                                    |"to": "${other.name}",
                                                    |"message": "${json_data_regist.message ?: ""}",
                                                    |"image": "${json_data_regist.image ?: ""}"}""".trimMargin()
                                                )
                                            )
                                        }
                                    }

                                    AllDataMessages.add(mapOf(
                                        "from" to client.name,
                                        "to" to json_data_regist.to_who,
                                        "message" to (json_data_regist.message ?: ""),
                                        "image" to (json_data_regist.image ?: "")
                                    ))

                                    if(File(file_messages).exists()) {
                                        File(file_messages).writeText(mapper.writeValueAsString(AllDataMessages))
                                    }
                                }
                            }
                        }
                    } finally {
                        clients -= client
                        for (other in clients.toList()) {
                            other.session.outgoing.send(Frame.Text("""{"ofline": "${client.name}"}"""))
                        }
                    }
                }
            }

            route("/login") {
                get {
                    call.respond(FreeMarkerContent("login.ftl", null))
                }
                authenticate("login") {
                    post {
                        val principal = call.principal<UserIdPrincipal>() ?: error("No principal")
                        call.sessions.set(MySession(principal.name))
                        call.respondRedirect("/", permanent = false)
                    }
                }
                post {
                    val post = call.receiveParameters()
                    if (post["username"] != null && post["password"] != null && post["password"] == post["repeat_password"]) {
                        call.sessions.set(post["username"] ?: "")

                        call.respondText("OK")
                    } else {
                        call.respond(FreeMarkerContent("login.ftl", mapOf("error" to "Пароли не совпадают")))
                    }
                }
            }

            post("/get-messages") {
                val post = call.receiveParameters()

                val ans = ArrayList<Map<String, String?>>()

                for (mes in AllDataMessages) {
                    val message = mapOf(
                        "from" to mes.get("from"),
                        "to" to mes.get("to"),
                        "message" to mes.get("message"),
                        "image" to mes.get("image")
                    )

                    if(mes.get("to") != "to_all" && (mes.get("from") == post["user"] || mes.get("to") == post["user"]) ||
                        mes.get("to") == "to_all") {
                        ans.add(message)
                    }
                }

                call.respond(ans)
            }
        }
    }
    server.start(wait = true)
}

suspend fun InputStream.copyToSuspend(
    out: OutputStream,
    bufferSize: Int = DEFAULT_BUFFER_SIZE,
    yieldSize: Int = 4 * 1024 * 1024,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
): Long {
    return withContext(dispatcher) {
        val buffer = ByteArray(bufferSize)
        var bytesCopied = 0L
        var bytesAfterYield = 0L
        while (true) {
            val bytes = read(buffer).takeIf { it >= 0 } ?: break
            out.write(buffer, 0, bytes)
            if (bytesAfterYield >= yieldSize) {
                yield()
                bytesAfterYield %= yieldSize
            }
            bytesCopied += bytes
            bytesAfterYield += bytes
        }
        return@withContext bytesCopied
    }
}