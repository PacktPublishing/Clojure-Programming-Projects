(ns fetch-cc.main
  (:gen-class)
  (:require
   [fetch-cc.core :as fetch-cc]))


(defn -main
  [& args]
  (println "Fetching BTC price...")
  (fetch-cc/save-btc-price!
   (fetch-cc/fetch-btc-current-price!))
  (println "BTC price has been written to resources/BTC_price.edn"))
