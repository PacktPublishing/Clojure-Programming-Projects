(ns repl)


(comment

 (require '[cheshire.core :as json]
          '[clojure.set :as set])

 (def api-host "https://min-api.cryptocompare.com")
 (def uri-info "/data/top/totalvol?limit=10&tsym=USD")

 (def resp
   (json/parse-string
    (slurp (str api-host uri-info))))

 (def keys-mapping
   {"Name" :sym
    "FullName" :full-name
    "ProofType" :proof-type
    "Algorithm" :algorithm
    "ImageUrl" :image-url})

 (get-in resp ["Data" 0 "CoinInfo"])
 (set/rename-keys
  (select-keys *1 (keys keys-mapping))
  keys-mapping)

 ;;;

 (def uri-histo "/data/histoday?fsym=BTC&tsym=USD&limit=10")

 (def resp
   (json/parse-string
    (slurp (str api-host uri-histo))))

 (get-in resp ["Data" 0])
 (java.util.Date. (* 1000 (long (get *1 "time"))))

 ;;;

 (require '[fetch-cc-data.core :as f])
 (let [coins (f/fetch-top-coins! 3)]
   (println "SAVING COINS:" (count coins))
   (doseq [c coins]
     (let [s (:sym c)
           h (f/fetch-coin-history! s 3)]
       (println "SAVING HISTORY FOR" s ": " (count h)))))


 nil)
