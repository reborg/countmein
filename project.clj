(defproject countmein "0.0.1-SNAPSHOT"
  :description "Fast counter in Clojure"
  :main countmein.parallel
  :jvm-opts ["-Xmx4g"]
  :java-source-paths ["bloom"] ; Java source is stored separately.
  :dependencies [[org.clojure/clojure "1.5.0"]]
  :profiles {:dev {:dependencies [[midje "1.5.0"]]}})
