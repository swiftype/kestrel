/*
 * Copyright 2011 Twitter, Inc.
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
package admin

import scala.collection.{JavaConversions, Map, mutable, immutable}
import com.twitter.ostrich.stats._

// This is a hacky monkey-patch that stops memory from leaking.
class SwiftypeStatsListener(collection: StatsCollection) extends StatsListener(collection) {

  // These shadow StatsListener immutable fields and so should always point to the same object
  val lastCounterMap = getSuperClassPrivateField("lastCounterMap").asInstanceOf[mutable.HashMap[String, Long]]
  val lastMetricMap = getSuperClassPrivateField("lastMetricMap").asInstanceOf[mutable.HashMap[String, Histogram]]

  override def getCounters(): Map[String, Long] = synchronized {
    val counters = super.getCounters()
    lastCounterMap.retain { case (k, v) => counters.contains(k) }
    counters
  }

  override def getMetrics(): Map[String, Histogram] = synchronized {
    val metrics = super.getMetrics()
    lastMetricMap.retain { case (k, v) => metrics.contains(k) }
    metrics
  }

  private def getSuperClassPrivateField(fieldName: String): Object = {
    val field = getClass().getSuperclass().getDeclaredField("com$twitter$ostrich$stats$StatsListener$$" + fieldName)
    field.setAccessible(true)
    field.get(this)
  }
}
