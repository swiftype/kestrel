import com.twitter.conversions.storage._
import com.twitter.conversions.time._
import com.twitter.logging.config._
import com.twitter.ostrich.admin.config._
import net.lag.kestrel.config._
import net.lag.kestrel.admin.config._

new KestrelConfig {
  listenAddress = "0.0.0.0"
  memcacheListenPort = 22133
  textListenPort = 2222
  thriftListenPort = 2229
  admin.httpPort = 2223

  // Enable admin interface
  admin.statsNodes = new StatsConfig {
    reporters = new SwiftypeTimeSeriesCollectorConfig
  }

  val currentPath = new java.io.File(".").getAbsolutePath

  queuePath = currentPath + "/var/spool"

  clientTimeout = None

  expirationTimerFrequency = 60.seconds
  maxOpenTransactions = 1000000

  // Default queue settings
  default.defaultJournalSize        = 32.megabytes
  default.maxMemorySize             = 16.megabytes
  default.maxJournalSize            = 1024.megabytes
  default.syncJournal               = 10.seconds
  default.maxQueueAge               = 300.seconds
  default.disableAggressiveRewrites = true

  admin.statsNodes = new StatsConfig {
    reporters = new TimeSeriesCollectorConfig
  }

  // Custom queue settings
  queues = List(
    new QueueBuilder {
      name = "after_fetch_error_retries_mix"
      maxQueueAge = None
      maxMemorySize = 128.megabytes
        maxSize = 128.megabytes
    },
    new QueueBuilder {
      name = "after_robots_filter"
      maxQueueAge = None
      maxMemorySize = 128.megabytes
        maxSize = 128.megabytes
    },
    new QueueBuilder {
      name = "crawler_response"
      maxQueueAge = None
      maxMemorySize = 256.megabytes
        maxSize = 256.megabytes
    },
    new QueueBuilder {
      name = "fetch_error_retries_1"
      maxQueueAge = None
      maxMemorySize = 128.megabytes
        maxSize = 128.megabytes
    },
    new QueueBuilder {
      name = "fetch_error_retries_2"
      maxQueueAge = None
      maxMemorySize = 128.megabytes
        maxSize = 128.megabytes
    },
    new QueueBuilder {
      name = "fetch_error_retries_3"
      maxQueueAge = None
      maxMemorySize = 128.megabytes
        maxSize = 128.megabytes
    },
    new QueueBuilder {
      name = "fetched_responses"
      maxQueueAge = None
      maxMemorySize = 256.megabytes
        maxSize = 256.megabytes
    },
    new QueueBuilder {
      name = "http_fetcher"
      maxQueueAge = None
      maxMemorySize = 128.megabytes
        maxSize = 128.megabytes
    },
    new QueueBuilder {
      name = "seed"
      maxQueueAge = None
      maxMemorySize = 128.megabytes
    },
    new QueueBuilder {
      name = "after_fetch_error_retries_mix:retries"
      maxQueueAge = None
      maxMemorySize = 16.megabytes
    },
    new QueueBuilder {
      name = "after_robots_filter:retries"
      maxQueueAge = None
      maxMemorySize = 16.megabytes
    },
    new QueueBuilder {
      name = "crawler_response:retries"
      maxQueueAge = None
      maxMemorySize = 16.megabytes
    },
    new QueueBuilder {
      name = "fetch_error_retries_1:retries"
      maxQueueAge = None
      maxMemorySize = 16.megabytes
    },
    new QueueBuilder {
      name = "fetch_error_retries_2:retries"
      maxQueueAge = None
      maxMemorySize = 16.megabytes
    },
    new QueueBuilder {
      name = "fetch_error_retries_3:retries"
      maxQueueAge = None
      maxMemorySize = 16.megabytes
    },
    new QueueBuilder {
      name = "fetched_responses:retries"
      maxQueueAge = None
      maxMemorySize = 16.megabytes
    },
    new QueueBuilder {
      name = "http_fetcher:retries"
      maxQueueAge = None
      maxMemorySize = 16.megabytes
    },
    new QueueBuilder {
      name = "seed:retries"
      maxQueueAge = None
      maxMemorySize = 16.megabytes
    },
    new QueueBuilder {
      name = "dead"
      maxQueueAge = None
      maxMemorySize = 16.megabytes
    }
  )

  loggers = new LoggerConfig {
    level = Level.INFO
    handlers = new FileHandlerConfig {
      filename = currentPath + "/var/kestrel.log"
      roll = Policy.Never
    }
  }
}
