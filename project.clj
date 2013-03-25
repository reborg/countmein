(defproject countmein "0.0.1-SNAPSHOT"
  :description "Fast counter in Clojure"
  :main countmein.core
  :jvm-opts ["-Xmx4g"]
  :dependencies [[org.clojure/clojure "1.5.0"]
                [org.apache.hadoop/hadoop-core "1.1.1"]]
  :profiles {:dev {:dependencies [[midje "1.5.0"]]}})
