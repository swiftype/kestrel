# Different options where sbt binary could be located
SBT_OPTIONS = %w[
  /usr/bin/sbt
  /usr/local/bin/sbt
  /opt/sbt/bin/sbt
  /home/build/scala/sbt/bin/sbt
]

#-------------------------------------------------------------------------------
def sbt_binary
  return ENV['SBT_BINARY'] if ENV['SBT_BINARY']

  SBT_OPTIONS.each do |sbt_option|
    return sbt_option if File.executable?(sbt_option)
  end
end

#-------------------------------------------------------------------------------
task :default => :package_dist

desc 'Build package'
task :package_dist do
  sh "JAVA_OPTS='-Dhttps.protocols=TLSv1.1,TLSv1.2' #{sbt_binary} package-dist"
end

desc 'Clean the build directory'
task :clean do
  sh "#{sbt_binary} clean"
end
