package net.lag.kestrel
package admin
package config

import com.twitter.ostrich.stats.StatsCollection
import com.twitter.ostrich.admin.config.StatsReporterConfig
import com.twitter.ostrich.admin.AdminHttpService

class SwiftypeTimeSeriesCollectorConfig extends StatsReporterConfig {
  def apply() = { (collection: StatsCollection, admin: AdminHttpService) =>
    val service = new SwiftypeTimeSeriesCollector(collection)
    service.registerWith(admin)
    service
  }
}
