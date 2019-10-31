#!/usr/bin/env ruby

require 'rubygems'
require 'kjess'
require 'getoptlong'
require 'ops-api'
require 'timeout'

#---------------------------------------------------------------------------------------------------
def run_report_multiple_times(report_count, report_interval)
  raise "No report block given!" unless block_given?

  # Make sure we exit with the given time interval (in case of issues with the report)
  Timeout.timeout(report_count * report_interval + 1) do
    next_report = Time.now.to_f
    report_count.times do |report_number|
      while (Time.now.to_f < next_report) do
        sleep(0.1)
      end

      begin
        yield
      rescue => e
        puts "#{Time.now}: ERROR: failed report #{report_number}: #{e}"
      end

      next_report = next_report + report_interval
    end
  end
end

#------------------------------------------------------------------------------
def filters_to_regex(filters)
  filters = filters.to_s.strip.downcase
  return nil if filters == ''

  filters = filters.split(',')
  return Regexp.new(filters.join('|'))
end

#------------------------------------------------------------------------------
def show_help
  puts "Usage: #{$0} [args]"
  puts 'Where args are:'
  puts '  --kestrel-host=host     | -H host  Kestrel host to connect to (default: localhost)'
  puts '  --kestrel-port=port     | -p port  Kestrel memcache port to connect to (default: 22133)'
  puts '  --with-queue-stats=bool | -q bool  Enables/disables per-queue stats (default: false)'
  puts '  --include-queues=list   | -i list  Comma-separated list of queue filters to select queues'
  puts '  --exclude-queues=list   | -e list  Comma-separated list of queue filters to exclude queues'
  puts '  --graphite-host=host    | -G host  Enables graphite reporting (default: metrics are printed to console)'
  puts '  --graphite-port=port    | -g port  Graphite TCP port (default: 2003)'
  puts '  --report-count=cnt      | -N cnt   Number of reports to send (default: 1)'
  puts '  --report-interval=sec   | -I sec   Delay between reports, in seconds (default: 10)'
  puts '  --help                  | -h       This help'
  puts
  exit(0)
end

#------------------------------------------------------------------------------
# Parse options
opts = GetoptLong.new(
  [ '--kestrel-host',    '-H', GetoptLong::REQUIRED_ARGUMENT ],
  [ '--kestrel-port',    '-p', GetoptLong::REQUIRED_ARGUMENT ],

  [ '--with-queue-stats','-q', GetoptLong::REQUIRED_ARGUMENT ],
  [ '--exclude-queues',  '-e', GetoptLong::REQUIRED_ARGUMENT ],
  [ '--include-queues',  '-i', GetoptLong::REQUIRED_ARGUMENT ],

  [ '--graphite-host',   '-G', GetoptLong::REQUIRED_ARGUMENT ],
  [ '--graphite-port',   '-g', GetoptLong::REQUIRED_ARGUMENT ],
  [ '--report-count',    '-N', GetoptLong::REQUIRED_ARGUMENT ],
  [ '--report-interval', '-I', GetoptLong::REQUIRED_ARGUMENT ],
  [ '--help',            '-h', GetoptLong::NO_ARGUMENT ]
)

# Default settings
kestrel_host = "localhost"
kestrel_port = 22133

enable_queue_stats = false
include_filters = nil
exclude_filters = nil

graphite_host = nil
graphite_port = 2003

report_count = 1
report_interval = 10

metric_hostname = OpsApi::Env.node_name
datacenter = OpsApi::Env.datacenter

# Process options
opts.each do |opt, arg|
  case opt
    when "--kestrel-host"
      kestrel_host = arg
      metric_hostname = arg.gsub(/\.swiftype\.(com|net)$/, '')
    when "--kestrel-port"
      kestrel_port = arg.to_i
    when "--with-queue-stats"
      arg_value = arg.to_s.strip.downcase
      enable_queue_stats = %w[ yes true on ].member?(arg_value)
    when "--exclude-queues"
      exclude_filters = filters_to_regex(arg)
    when "--include-queues"
      include_filters = filters_to_regex(arg)
    when "--graphite-host"
      graphite_host = arg
    when "--graphite-port"
      graphite_port = arg.to_i
    when "--report-count"
      report_count = arg.to_i
    when "--report-interval"
      report_interval = arg.to_f
    when "--help"
      show_help
  end
end

#---------------------------------------------------------------------------------------------------
# Calculate network timeots based on the report interval
timeout = [ report_interval - 1, 1 ].max

# Connect to kestrel
kestrel_connection = {
  :host => kestrel_host,
  :port => kestrel_port,
  :connect_timeout => timeout,
  :write_timeout => timeout,
  :read_timeout => timeout
}
kestrel = KJess::Client.new(kestrel_connection)

# Test kestrel connection
begin
  kestrel.ping
rescue => e
  puts "Error while connecting to kestrel (ping): #{e}"
  exit(1)
end

#---------------------------------------------------------------------------------------------------
run_report_multiple_times(report_count, report_interval) do
  # Get data from kestrel
  stats = kestrel.stats

  # Process info and create a metrics list
  metrics = {}

  # Broker-wide metrics
  metrics['uptime_sec'] = stats['uptime']

  metrics['current_connections'] = stats['curr_connections']
  metrics['current_items'] = stats['curr_items']

  metrics['total_connections'] = stats['total_connections']
  metrics['total_items'] = stats['total_items']

  metrics['total_bytes_written'] = stats['bytes_written']
  metrics['total_bytes_read'] = stats['bytes_read']

  metrics['total_get_commands'] = stats['cmd_get']
  metrics['total_set_commands'] = stats['cmd_set']
  metrics['total_peek_commands'] = stats['cmd_peek']

  metrics['total_get_hits'] = stats['get_hits']
  metrics['total_get_misses'] = stats['get_misses']

  metrics['total_queue_creates'] = stats['queue_creates']
  metrics['total_queue_deletes'] = stats['queue_deletes']
  metrics['total_queue_expires'] = stats['queue_expires']

  # Queue stats
  if enable_queue_stats
    stats['queues'].each do |queue_name, queue_stats|
      # Skip queues based on filters
      next if include_filters && !queue_name.match(include_filters)
      next if exclude_filters && queue_name.match(exclude_filters)

      # Metric prefix for all stats in this queue
      prefix = "queues.#{queue_name.downcase.gsub(/\W+/, '-')}"

      # Get metrics for the queues
      metrics["%s.total_items" % prefix] = queue_stats['total_items']
      metrics["%s.total_expired_items" % prefix] = queue_stats['expired_items']
      metrics["%s.total_discarded_items" % prefix] = queue_stats['discarded']

      metrics["%s.total_transactions" % prefix] = queue_stats['transactions']
      metrics["%s.total_cancelled_transactions" % prefix] = queue_stats['canceled_transactions']
      metrics["%s.current_open_transactions" % prefix] = queue_stats['open_transactions']

      metrics["%s.total_flushes" % prefix] = queue_stats['total_flushes']

      metrics["%s.age_sec" % prefix] = queue_stats['age']
      metrics["%s.current_waiters" % prefix] = queue_stats['waiters']
      metrics["%s.current_log_size" % prefix] = queue_stats['logsize']

      metrics["%s.current_items" % prefix] = queue_stats['items']
      metrics["%s.current_bytes" % prefix] = queue_stats['bytes']
      metrics["%s.current_items_in_memory" % prefix] = queue_stats['mem_items']
      metrics["%s.current_bytes_in_memory" % prefix] = queue_stats['mem_bytes']
    end
  end

  # Aggregate queue stats (initialize)
  metrics['total_transactions'] = 0
  metrics['total_cancelled_transactions'] = 0
  metrics['current_open_transactions'] = 0

  metrics['total_flushes'] = 0

  metrics['current_waiters'] = 0
  metrics['current_log_size'] = 0

  metrics['current_bytes'] = 0
  metrics['current_items_in_memory'] = 0
  metrics['current_bytes_in_memory'] = 0

  # Aggregate queue stats (calculate)
  stats['queues'].each do |queue_name, queue_stats|
    metrics['total_transactions']           += queue_stats['transactions'].to_i
    metrics['total_cancelled_transactions'] += queue_stats['canceled_transactions'].to_i
    metrics['current_open_transactions']    += queue_stats['open_transactions'].to_i

    metrics['total_flushes']                += queue_stats['total_flushes'].to_i

    metrics['current_waiters']              += queue_stats['waiters'].to_i
    metrics['current_log_size']             += queue_stats['logsize'].to_i

    metrics['current_bytes']                += queue_stats['bytes'].to_i
    metrics['current_items_in_memory']      += queue_stats['mem_items'].to_i
    metrics['current_bytes_in_memory']      += queue_stats['mem_bytes'].to_i
  end

  now = Time.now.to_i
  host = metric_hostname.tr('.', '-')

  # Compose graphite stats message
  graphite_message = ""
  metrics.each do |metric, value|
    graphite_message << "#{datacenter}.kestrel.#{host}-#{kestrel_port}.#{metric} #{value} #{now}\n"
  end

  # Send message to graphite or print to console
  if graphite_host
    socket = TCPSocket.new(graphite_host, graphite_port)
    socket.send(graphite_message, 0)
    socket.close
  else
    puts "Metrics (not sent to graphite because graphite host is not specified):"
    puts graphite_message
  end

  # Log OK
  puts "#{Time.now}: OK"
end
