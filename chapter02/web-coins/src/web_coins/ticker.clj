(ns web-coins.ticker
  (:require
   [clojure.edn :as edn]
   [org.httpkit.server :as server]
   [org.httpkit.timer :as timer]))


(defonce top-coins
  (edn/read-string
   (slurp "resources/coin_data/top_coins.edn")))

;;;

(defonce clients-store
  (atom {}))

(defn register-client
  [store channel req]
  (swap! store assoc channel req))

(defn unregister-client
  [store channel]
  (swap! store dissoc channel))

;;;

(defonce prices-store
  (atom {}))

(defn init-mock!
  [store]
  (let [ks (map :sym top-coins)
        kvs (map #(vector % (rand)) ks)]
    (reset! store (into {} kvs))))

(defn listen-mock!
  [server clients prices]
  (let [sym (rand-nth (keys @prices))
        p (rand)
        cs (keys @clients)
        msg (str sym ":" p)]
    (swap! prices assoc sym p)
    (doseq [c cs]
      (server/send! c msg)))
  (swap! server assoc :next-tick
         (timer/schedule-task 100 (listen-mock! server clients prices)))
  nil)

(defn init!
  [prices init-fn listen-fn]
  (init-fn prices)
  (listen-fn prices)
  nil)

(defn send-all!
  [channel prices]
  (doseq [[k v] (seq @prices)]
    (server/send! channel (str k ":" v) false)))
