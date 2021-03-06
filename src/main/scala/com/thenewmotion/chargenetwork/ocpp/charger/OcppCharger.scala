package com.thenewmotion.chargenetwork.ocpp.charger

import java.net.URI
import javax.net.ssl.SSLContext

import dispatch.Http
import akka.actor.{ActorRef, Props}
import com.thenewmotion.ocpp.soap.CentralSystemClient
import com.thenewmotion.ocpp.Version
import com.thenewmotion.ocpp.messages.CentralSystem

trait OcppCharger {
  def chargerActor: ActorRef
}

class OcppSoapCharger(chargerId: String,
                  numConnectors: Int,
                  ocppVersion: Version.Value,
                  centralSystemUri: URI,
                  server: ChargerServer,
                  http: Http = new Http) extends OcppCharger {

  val client = CentralSystemClient(chargerId, ocppVersion, centralSystemUri, http, Some(server.url))
  val chargerActor = system.actorOf(Props(new ChargerActor(BosService(chargerId, client), numConnectors)))
  server.actor ! ChargerServer.Register(chargerId, new ChargePointService(chargerId, chargerActor))
}

class OcppJsonCharger(chargerId: String,
                       numConnectors: Int,
                       centralSystemUri: URI,
                       authPassword: Option[String])(
                       implicit sslContext: SSLContext = SSLContext.getDefault)
                       extends OcppCharger {
  val client: CentralSystem = new JsonCentralSystemClient(chargerId, centralSystemUri, authPassword)
  val chargerActor = system.actorOf(Props(new ChargerActor(BosService(chargerId, client), numConnectors)))
}
