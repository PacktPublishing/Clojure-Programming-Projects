(defproject cpp "0.1.0-SNAPSHOT"
  :description "Code for the book Clojure Programming Projects"
  :url "https://github.com/PacktPublishing/Clojure-Programming-Projects"
  :license {:name "MIT"
            :url "https://github.com/PacktPublishing/Clojure-Programming-Projects/blob/master/LICENSE"}
  :min-lein-version "2.7.1"

  :aliases
  {"rebl" ["trampoline" "run" "-m" "rebel-readline.main"]
   "fetcher" ["trampoline" "run" "-m" "cpp.fetcher.core"]}
   ; "devsrv" ["trampoline" "run" "-m" "cpp.http"]

  :dependencies
  [[org.clojure/clojure "1.9.0"]
   [org.clojure/clojurescript "1.10.238"]
   ;  :exclusions [[com.google.guava/guava]]]
   ; [org.clojure/core.async "0.4.474"]
   ;---
   ; [clj-time "0.14.4"]
   ; [cheshire "5.8.0"]
   ; [com.taoensso/sente "1.12.0"]
   ; [com.taoensso/timbre "4.10.0"]
   ; [http-kit "2.3.0"]
   ; [ring "1.6.3"]
   ; [ring/ring-defaults "0.3.2"]
   ; [ring-cors "0.1.11"]
   ; [ring-middleware-format "0.7.2"]
   ; [compojure "1.6.0"]
   ; [co.deps/ring-etag-middleware "0.2.0"]
   ; [com.datomic/datomic-free "0.9.5697"]
   ; [danlentz/clj-uuid "0.1.7"]
   ; ;---
   [devcards "0.2.4"]
   [sablono "0.8.4"]
   [cljsjs/react "16.3.0-1"]
   [cljsjs/react-dom "16.3.0-1"]
   ; [datascript "0.16.6"]
   [cljs-http "0.1.44"]]
   ; [camel-snake-kebab "0.4.0"]]

  ; :managed-dependencies
  ; [[com.google.guava/guava "24.1-jre"]]
  ;
  ; :exclusions
  ; [com.google.errorprone/error_prone_annotations]

  :figwheel
  {:css-dirs ["resources/public/css"]}
   ; :ring-handler cpp.cogs.http/dev-handler}
   ; :server-port 3450
   ; :nrepl-port 7888}

  :cljsbuild
  {:builds
   [{:id "coins-cards"
     :source-paths ["src/cpp/webapps/coins"]
     :figwheel {:devcards true}
     :compiler {:main       "cpp.webapps.coins.core"
                :asset-path "js/compiled/coins/cards_out"
                :output-to  "resources/public/js/compiled/cpp_cards.js"
                :output-dir "resources/public/js/compiled/coins/cards_out"
                :source-map-timestamp true}}]}
    ; {:id "dev"
    ;  :source-paths ["src"]
    ;  :figwheel true
    ;  :compiler {:main       "cpp.core"
    ;             :asset-path "js/compiled/out"
    ;             :output-to  "resources/public/js/compiled/cpp.js"
    ;             :output-dir "resources/public/js/compiled/out"
    ;             :source-map-timestamp true}}]}
  ;   {:id "prod"
  ;    :source-paths ["src"]
  ;    :compiler {:main       "cpp.core"
  ;               :asset-path "js/compiled/out"
  ;               :output-to  "resources/public/js/compiled/cpp.js"
  ;               :optimizations :advanced}}]}

  :plugins [[lein-figwheel "0.5.16"]
            [lein-cljsbuild "1.1.5" :exclusions [org.clojure/clojure]]]

  :clean-targets ^{:protect false} ["resources/public/js/compiled"
                                    "target"]

  :source-paths ["src"]

  :profiles
  {:dev
   {:dependencies [[org.clojure/tools.namespace "0.2.11"]
                   [com.bhauman/rebel-readline "0.1.4"]]
                   ; [ring/ring-mock "0.3.2"]
                   ; [binaryage/devtools "0.9.2"]
                   ; [figwheel-sidecar "0.5.16"]]}})
                   ; [com.cemerick/piggieback "0.2.1"]]
    :source-paths ["src" "dev" "test/clj"]}})
    ;; for CIDER
    ;; :plugins [[cider/cider-nrepl "0.12.0"]]
    ; :repl-options {:init (set! *print-length* 50)
    ;                :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}}
