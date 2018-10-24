(ns web-coins.server
  (:require
   [web-coins.ui :as ui]
   [clojure.pprint :refer [pprint]]
   [org.httpkit.server :as server]
   [org.httpkit.timer :as timer]
   [compojure.core :refer [defroutes GET]]
   [ring.middleware.reload :refer [wrap-reload]]
   [compojure.route :refer [resources not-found]]))

;;; handlers

(defn handle-home
  [req]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (ui/page-home req)})

(defn handle-ticker
  [req]
  (server/with-channel req channel
    (server/on-close channel (fn [status] (println "channel closed, " status)))
    (loop [id 0]
      (when (< id 1000)
        (timer/schedule-task
         (* id 200) ;; send a message every 200ms
         (server/send! channel (str "Tick from server #" id) false)) ; false => don't close after send
        (recur (inc id))))
    (timer/schedule-task 100000 (server/close channel)))) ;; close in 100s.

;;; routes

(defroutes webapp
  (GET "/" req handle-home)
  (GET "/req" req (str "<pre>" (with-out-str (pprint req)) "</pre>"))
  (GET "/ticker" req handle-ticker)
  (resources "/")
  (not-found "NOT FOUND"))

;;; server

(defonce server (atom {:stop-fn nil}))

(defn start!
  []
  (swap! server update :stop-fn
         #(if-let [stop %]
            (do (println "Already started.") stop)
            (let [r (server/run-server (wrap-reload webapp) {:port 8080})]
              (println "Started.")
              r))))

(defn stop!
  []
  (swap! server update :stop-fn
         #(if-let [stop %]
            (do (apply stop []) nil)
            nil))
  (println "Stopped."))

(defn restart!
  []
  (stop!)
  (start!))


;;; REPL
(comment

 (require '[web-coins.server :refer :all :reload-all true])

 nil)
