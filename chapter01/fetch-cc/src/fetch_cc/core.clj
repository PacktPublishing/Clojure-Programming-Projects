(ns fetch-cc.core
  (:require
   [clojure.edn :as edn]
   [cheshire.core :as json]))

  ; "Get the current price of BTC in USD, Yen, and Euro from Cryptocompare API.
  ;  return: a map of the form {\"USD\" 6320.35, \"JPY\" 699429.9, \"EUR\" 5430.2}")

(defn fetch-btc-current-price!
  []
  (json/parse-string
   (slurp
    "https://min-api.cryptocompare.com/data/price?fsym=BTC&tsyms=USD,JPY,EUR")))


(defn save-btc-price!
  "Save a BTC price to the file: 'resources/BTC_price.edn'.
   Overwrites existing file.
   Throws if '$PWD/resources' folder is not found.
   price: a map as returned by fetch-btc-current-price!
   return: nil"
  [price]
  (spit "resources/BTC_price.edn" price))


(defn load-btc-price!
  "Load a BTC price from the file: 'resources/BTC_price.edn'.
   Throws if the file is not found.
   return: a map as returned by fetch-btc-current-price!"
  []
  (edn/read-string
   (slurp "resources/BTC_price.edn")))



;;; REPL
(comment

 (require '[fetch-cc.core :refer :all reload-all true])

 nil)
