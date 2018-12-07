(ns web-coins.server
  (:require
   [web-coins.ticker :as ticker]
   [ring.middleware.reload :refer [wrap-reload]]
   [compojure.core :refer [defroutes GET]]
   [compojure.route :refer [resources not-found]]
   [clojure.edn :as edn]
   [org.httpkit.server :as server]
   [org.httpkit.timer :as timer]))

(defn handle-home
  [req]
  req)


(defn handle-sym
  [req]
  req)


(defn handle-ticker
  [req]
  (server/with-channel req channel
    (server/on-close channel (fn [_] (ticker/unregister-client ticker/clients-store channel)))
    (ticker/register-client ticker/clients-store channel req)
    (server/send! channel (str "BTC" 0) false)))

(defroutes webapp
  (GET "/" req handle-home)
  ; (GET "/req" req (str "<pre>" (with-out-str (pprint req)) "</pre>"))
  (GET "/ticker" req handle-ticker)
  (GET "/coins/:sym" [sym :as req] handle-sym)
  (resources "/")
  (not-found "NOT FOUND"))


;;;

(def server
  (atom {}))

(defn start!
  []
  (ticker/init! ticker/prices-store ticker/init-mock!
                (partial ticker/listen-mock! server ticker/clients-store))
  (swap! server update :stop-fn
         #(if-let [stop %]
            (do (println "Already started.") stop)
            (let [r (server/run-server (wrap-reload webapp) {:port 8080})]
              (println "Started.")
              r))))

(defn stop!
  []
  (when-let [task (:next-tick @server)]
    (timer/cancel task))
  (swap! server update :stop-fn
         #(if-let [stop %]
            (do (apply stop []) nil)
            nil))
  (println "Stopped."))
