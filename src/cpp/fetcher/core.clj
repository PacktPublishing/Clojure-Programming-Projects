(ns cpp.fetcher.core
  (:require
   [clojure.set :as set]
   [clojure.pprint :refer [pprint]]
   [cheshire.core :as json])
  (:import
   [java.net URLEncoder]))

(def host
  "https://www.cryptocompare.com")

(def api-host
  "https://min-api.cryptocompare.com")

(def extra-params
  (str
    "&extraParams="
    (URLEncoder/encode "Packt Publishing - Clojure Programming Projects" "UTF-8")))

(defn- coin-info
  "Extract coin info from raw coin data.
   raw-coin: a map containing raw coin data.
   return: a map with keys :symbol, :full-name, :image-url, :algorithm, :proof-type."
  [raw-coin]
  (-> raw-coin
      (get "CoinInfo")
      (select-keys ["Name" "FullName" "ImageUrl" "Algorithm" "ProofType"])
      (set/rename-keys {"Name" :symbol
                        "FullName" :full-name
                        "ImageUrl" :image-url
                        "Algorithm" :algorithm
                        "ProofType" :proof-type})))

(defn get-top-coins
  "Fetch the current top coins (total volume across all markets).
   num: number (eg. 50 for top 50). If less than 10, top 10 is assumed.
   return: a vector of maps (see fn `coin-info`)."
  [num]
  (let [uri (str api-host
                 (format "/data/top/totalvol?limit=%s&tsym=USD" num)
                 extra-params)
        raw-coins (get (json/parse-string (slurp uri)) "Data")]
    (map coin-info raw-coins)))

(defn coins->files!
  [coins]
  (doseq [coin coins]
    (let [sym (:symbol coin)
          file-name (format "resources/public/data/%s.edn" sym)
          content (with-out-str (pprint coin))]
      (spit file-name content)))
  nil)

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
  (coins->files! (get-top-coins 10))

  ; (get-coin-price "BTC")
  ; (println (slurp (str "resources/public/data/" "BTC" "_price.edn")))
  0)


;;; REPL
(comment

 (require '[cheshire.core :as json])

 (require '[cpp.fetcher.core :as fetcher :reload true])

 nil)
