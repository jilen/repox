package com.gtan.repox.config

import akka.actor.{ActorRef, ActorLogging}
import akka.persistence.{PersistentActor, RecoveryCompleted}
import com.gtan.repox.admin.RepoVO
import com.gtan.repox.{Repox, RequestQueueMaster}
import com.ning.http.client.{ProxyServer => JProxyServer}
import io.undertow.util.StatusCodes

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

trait Cmd {
  def transform(old: Config): Config
}

trait Evt

case class ConfigChanged(config: Config, cmd: Cmd) extends Evt

case object UseDefault extends Evt

case object ConfigPersister extends RepoPersister with ParameterPersister
with ConnectorPersister
with ProxyPersister
with Immediate404RulePersister
with ExpireRulePersister

class ConfigPersister extends PersistentActor with ActorLogging {

  import ConfigPersister._

  override def persistenceId = "Config"

  var config: Config = _

  def onConfigSaved(sender: ActorRef, c: ConfigChanged) = {
    log.debug(s"event: $c")
    config = c.config
    Config.set(config)
    sender ! StatusCodes.OK
  }

  val receiveCommand: Receive = {
    case cmd: Cmd =>
      val newConfig = cmd.transform(config)
      if (newConfig == config) {
        // no change
        sender ! StatusCodes.OK
      } else {
        persist(ConfigChanged(newConfig, cmd))(onConfigSaved(sender(), _))
      }
    case UseDefault =>
      persist(UseDefault) { _ =>
        config = Config.default
        Config.set(config).foreach { _ =>
          Repox.requestQueueMaster ! RequestQueueMaster.ConfigLoaded
        }
      }
  }

  val receiveRecover: Receive = {
    case ConfigChanged(data, cmd) =>
      config = data

    case UseDefault =>
      config = Config.default

    case RecoveryCompleted =>
      if (config == null) {
        // no config history, save default data as snapshot
        self ! UseDefault
      } else {
        Config.set(config).foreach { _ =>
          Repox.requestQueueMaster ! RequestQueueMaster.ConfigLoaded
        }
      }
  }
}
