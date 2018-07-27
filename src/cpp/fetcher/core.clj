(ns cpp.fetcher.core
  (:require [cheshire.core :as json]))

(defn get-coin-price
  "Get price data for sym, save it to <sym>_price.edn"
  [sym]
  (spit
   (str "resources/public/data/"
        sym "_price.edn")
   (json/parse-string
    (slurp
     (str "https://min-api.cryptocompare.com/data/price?fsym="
          sym "&tsyms=USD,JPY,EUR")))))

(defn -main [& args]
  (.mkdir (java.io.File. "resources/public/data"))
  (get-coin-price "BTC")
  (println (slurp (str "resources/public/data/" "BTC" "_price.edn")))
  0)
