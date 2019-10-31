/*
 * Copyright 2009 Twitter, Inc.
 * Copyright 2009 Robey Pointer <robeypointer@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.lag.kestrel
package config

import com.twitter.conversions.storage._
import com.twitter.conversions.time._
import com.twitter.logging.Logger
import com.twitter.logging.config._
import com.twitter.ostrich.admin.{RuntimeEnvironment, ServiceTracker}
import com.twitter.ostrich.admin.config._
import com.twitter.util.Duration
import scala.collection.JavaConversions

/**
 * KestrelConfig is the main point of configuration for Kestrel.
 */
trait KestrelConfig extends ServerConfig[Kestrel] {
  /**
   * Default queue settings. Starting with Kestrel 2.3.4, queue settings are
   * inherited. See QueueBuilder for more information.
   */
  val default: QueueBuilder = new QueueBuilder

  /**
   * Specific per-queue config. Starting with Kestrel 2.3.4, queue settings are
   * inherited. See QueueBuilder for more information.
   */
  var queues: List[QueueBuilder] = Nil

  /*
   * Alias configurations.
   */
  var aliases: List[AliasBuilder] = Nil

  /**
   * Address to listen for client connections. By default, accept from any interface.
   */
  var listenAddress: String = "0.0.0.0"

  /**
   * Port for accepting memcache protocol connections. 22133 is the standard port.
   */
  var memcacheListenPort: Option[Int] = Some(22133)

  /**
   * Port for accepting text protocol connections.
   */
  var textListenPort: Option[Int] = Some(2222)

  /**
   * Port for accepting thrift protocol connections.
   */
  var thriftListenPort: Option[Int] = Some(2229)

  /**
   * Where queue journals should be stored. Each queue will have its own files in this folder.
   */
  var queuePath: String = "/tmp"

  /**
   * If you would like a timer to periodically sweep through queues and clean up expired items
   * (when they are at the head of a queue), set the timer's frequency here. This is only useful
   * for queues that are rarely (or never) polled, but may contain short-lived items.
   */
  var expirationTimerFrequency: Option[Duration] = None

  /**
   * An optional timeout for idle client connections. A client that hasn't sent a request in this
   * period of time will be disconnected.
   */
  var clientTimeout: Option[Duration] = None

  /**
   * Maximum # of transactions (incomplete GETs) each client can have open at one time.
   */
  var maxOpenTransactions: Int = 1

  /**
   * An optional size for the backlog of connecting clients. This setting is applied to each listening port.
   */
  var connectionBacklog: Option[Int] = None

  /**
   * Path to a file where Kestrel can store information about its current status.
   * When restarted, the server will come up with the same status that it had at shutdown,
   * provided data in this file can be accessed.
   *
   * Kestrel will attempt to create the parent directories of this file if they do not already
   * exist.
   */
  var statusFile: String = "/tmp/.kestrel-status"

  /**
   * In the absence of a readable status file, Kestrel will default to this status.
   */
  var defaultStatus: Status = Up

  /**
   * When changing to a more restricted status (e.g., from Up to ReadOnly), Kestrel will wait
   * until this duration expires before beginning to reject operations. Non-zero settings
   * allow clients to gracefully cease operations without incurring errors.
   */
  var statusChangeGracePeriod = 0.seconds

   /**
   * When true, enables tracing of session lifetime in the kestrel log
   */
  var enableSessionTrace: Boolean = false

  // When true, enabled Elastic APM tracing for the instance
  var enableElasticAPM: Boolean = false
  var elasticAPMConfig: Map[String, String] = Map(
    "service_name" -> "Kestrel",
    "application_packages" -> "net.lag.kestrel"
  )

  def apply(runtime: RuntimeEnvironment) = {
    new Kestrel(
      default(), queues, aliases, listenAddress, memcacheListenPort, textListenPort, thriftListenPort,
      queuePath, expirationTimerFrequency, clientTimeout, maxOpenTransactions, connectionBacklog,
      statusFile, defaultStatus, statusChangeGracePeriod, enableSessionTrace,
      enableElasticAPM = enableElasticAPM,
      elasticAPMConfig = elasticAPMConfig
    )
  }

  def reload(kestrel: Kestrel) {
    Logger.configure(loggers)
    // only the queue and alias configs can be changed.
    kestrel.reload(default(), queues, aliases)
  }
}
