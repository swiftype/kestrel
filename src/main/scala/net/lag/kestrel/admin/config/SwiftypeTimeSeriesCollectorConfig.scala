package net.lag.kestrel
package admin
package config

import com.twitter.ostrich.stats.StatsCollection
import com.twitter.ostrich.admin.config.StatsReporterConfig
import com.twitter.ostrich.admin.AdminHttpService
import com.twitter.ostrich.admin.TimeSeriesCollector

// Patches are in Ostrich directly now so this is just here for backwards compatibility
class SwiftypeTimeSeriesCollectorConfig extends StatsReporterConfig {
  def apply() = { (collection: StatsCollection, admin: AdminHttpService) =>
    val service = new TimeSeriesCollector(collection)
    service.registerWith(admin)
    service
  }
}
