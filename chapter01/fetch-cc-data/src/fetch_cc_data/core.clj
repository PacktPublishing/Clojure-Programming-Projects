(ns fetch-cc-data.core
  (:require
   [cheshire.core :as json]
   [clojure.set :as set]
   [clojure.pprint :refer [pprint]]))


(def api-host
  "https://min-api.cryptocompare.com")


(defn fetch-top-coins!
  "Fetch top n coins basic info.
   n: number of items to fetch
   rsc: optional file name or url to slurp, defaults to CyptoCompare API.
   return: a seq of maps of the form:
   {:sym \"BTC\",
    :full-name \"Bitcoin\",
    :proof-type \"PoW\",
    :algorithm \"SHA256\",
    :image-url \"/media/19633/btc.png\"}"
  ([n]
   (let [uri (format "/data/top/totalvol?limit=%s&tsym=USD" n)
         rsc (str api-host uri)]
     (fetch-top-coins! n rsc)))
  ([n rsc]
   (let [mappings {"Name" :sym
                   "FullName" :full-name
                   "ProofType" :proof-type
                   "Algorithm" :algorithm
                   "ImageUrl" :image-url}]
     (->> (get (json/parse-string (slurp rsc)) "Data")
          (map #(get % "CoinInfo"))
          (take n)
          (map #(select-keys % (keys mappings)))
          (map #(set/rename-keys % mappings))))))


(defn fetch-coin-history!
  "Fetch n entries from coin OHLCV daily history (prices are USD).
   sym: coin symbol
   n: number of items
   rsc: optional file name or url to slurp, defaults to CyptoCompare API.
   return: a seq of maps of the form:
   {:time-stamp #inst \"2018-10-06T00:00:00.000-00:00\",
    :open 6632.88,
    :close 6589.94,
    :high 6637.08,
    :low 6563.25,
    :vol-from 24787.61,
    :vol-to 1.6272210398E8}"
  ([sym n]
   (let [uri (format "/data/histoday?fsym=%s&tsym=USD&limit=%s" sym n)
         rsc (str api-host uri)]
     (fetch-coin-history! sym n rsc)))
  ([sym n rsc]
   (let [mappings {"time" :time-stamp
                   "open" :open
                   "close" :close
                   "high" :high
                   "low" :low
                   "volumefrom" :vol-from
                   "volumeto" :vol-to}]
     (->> (get (json/parse-string (slurp rsc)) "Data")
          (take n)
          (map #(select-keys % (keys mappings)))
          (map #(set/rename-keys % mappings))
          (map #(update % :time-stamp
                        (fn [i] (java.util.Date. (* 1000 (long i))))))))))


(defn save-to-edn!
  "Save data prettified in an edn file."
  [data path]
  (let [content (with-out-str (pprint data))]
    (spit path content)))


(defn overall-processing!
  "Fetch top volume coins info and their OHLCV histories.
   Save data to EDN files, 1 for all the coin infos and 1 per coin history.
   n-coin: number of coins.
   n-history: number of history days.
   dir: existing folder path, absolute or relative to $PWD.
   save-fn: optional function for saving files, defaults to save-to-edn!."
  ([n-coin n-history dir]
   (overall-processing! n-coin n-history dir save-to-edn!))
  ([n-coin n-history dir save-fn]
   (let [coins (fetch-top-coins! n-coin)]
     (save-fn coins (str dir "/top_coins.edn"))
     (doseq [c coins]
       (let [s (:sym c)
             hist (fetch-coin-history! s n-history)]
         (save-fn hist (str dir (format "/%s_history.edn" s))))))))



;;; REPL
(comment

 (require '[fetch-cc-data.core :refer :all :reload-all true])

 nil)
