(defproject fetch-cc-data "0.1.0-SNAPSHOT"

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/tools.cli "0.4.1"]
                 [cheshire "5.8.0"]]

  :main fetch-cc-data.main
  :aot [fetch-cc-data.main])
