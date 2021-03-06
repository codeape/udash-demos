package io.udash.todo.jetty

import io.udash.todo.services.InMemoryTodoStorage
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.gzip.GzipHandler
import org.eclipse.jetty.server.session.SessionHandler
import org.eclipse.jetty.servlet.{DefaultServlet, ServletContextHandler, ServletHolder}

class ApplicationServer(val port: Int, resourceBase: String) {
  private val server = new Server(port)
  private val contextHandler = new ServletContextHandler

  contextHandler.setSessionHandler(new SessionHandler)
  contextHandler.setGzipHandler(new GzipHandler)
  server.setHandler(contextHandler)

  def start() = server.start()

  def stop() = server.stop()

  private val appHolder = {
    val appHolder = new ServletHolder(new DefaultServlet)
    appHolder.setAsyncSupported(true)
    appHolder.setInitParameter("resourceBase", resourceBase)
    appHolder
  }
  contextHandler.addServlet(appHolder, "/*")

  private val atmosphereHolder = {
    import io.udash.rpc._
    import io.udash.todo.rpc._
    import scala.concurrent.ExecutionContext.Implicits.global

    val config = new DefaultAtmosphereServiceConfig[MainServerRPC]((clientId) =>
      new DefaultExposesServerRPC[MainServerRPC](new ExposedRpcInterfaces(InMemoryTodoStorage)(clientId))
    )
    val framework = new DefaultAtmosphereFramework(config)

    framework.init()

    val atmosphereHolder = new ServletHolder(new RpcServlet(framework))
    atmosphereHolder.setAsyncSupported(true)
    atmosphereHolder
  }
  contextHandler.addServlet(atmosphereHolder, "/atm/*")
       
}

       