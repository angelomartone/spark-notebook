package modules

import com.google.inject.AbstractModule
import org.pac4j.core.client.Clients
import org.pac4j.http.client.direct.{DirectBasicAuthClient, ParameterClient}
import org.pac4j.http.client.indirect.{FormClient, IndirectBasicAuthClient}
import org.pac4j.http.credentials.authenticator.test.SimpleTestUsernamePasswordAuthenticator
import org.pac4j.play.{CallbackController, LogoutController}
import play.api.{Configuration, Environment}
import org.pac4j.play.store.{PlayCacheSessionStore, PlaySessionStore}
import org.pac4j.core.authorization.authorizer.RequireAnyRoleAuthorizer
import org.pac4j.core.client.direct.AnonymousClient
import org.pac4j.core.config.Config
import org.pac4j.play.http.DefaultHttpActionAdapter

/**
  * Guice DI module to be included in application.conf
  */
class SecurityModule(environment: Environment, configuration: Configuration) extends AbstractModule {

  override def configure(): Unit = {
    val baseUrl = configuration.getString("baseUrl").getOrElse("")
    // HTTP
    val formClient = new FormClient(baseUrl + "/loginForm", new SimpleTestUsernamePasswordAuthenticator())
    val indirectBasicAuthClient = new IndirectBasicAuthClient(new SimpleTestUsernamePasswordAuthenticator())

    // basic auth
    val directBasicAuthClient = new DirectBasicAuthClient(new SimpleTestUsernamePasswordAuthenticator)

    val clients = new Clients(
      baseUrl + "/callback",
      formClient,
      indirectBasicAuthClient,
      directBasicAuthClient,
      new AnonymousClient()
    )

    val config = new Config(clients)

    //    config.addAuthorizer("admin", new RequireAnyRoleAuthorizer[Nothing]("ROLE_ADMIN"))
    //    config.addAuthorizer("custom", new CustomAuthorizer)
    config.setHttpActionAdapter(new DefaultHttpActionAdapter())
    println("FormClient callback: " + config.getClients.findClient("FormClient").asInstanceOf[FormClient].getCallbackUrl)
    bind(classOf[Config]).toInstance(config)

    bind(classOf[PlaySessionStore]).to(classOf[PlayCacheSessionStore])

    // callback
    val callbackController = new CallbackController()
    callbackController.setDefaultUrl("/?defaulturlafterlogout")
    callbackController.setMultiProfile(true)
    bind(classOf[CallbackController]).toInstance(callbackController)

    // logout
    val logoutController = new LogoutController()
    logoutController.setDefaultUrl("/")
    bind(classOf[LogoutController]).toInstance(logoutController)
  }
}
