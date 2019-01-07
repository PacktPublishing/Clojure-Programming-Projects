(ns web-coins.server
  (:require
   [compojure.core :refer [defroutes GET]]
   [org.httpkit.server :as server]
   [ring.middleware.reload :refer [wrap-reload]]))

;;; handlers

(defn handle-home
  [req]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body "LIST"})

(defn handle-coins
  [req]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (str "DETAIL: " (get-in req [:params :sym]
                                     "No symbol!"))})

(defn handle-ticker
  [req]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body "INITIAL PRICES"})

;;; routes

(defroutes webapp
  (GET "/" _ handle-home)
  (GET "/coins/:sym" _ handle-coins)
  (GET "/ticker" _ handle-ticker))

;;; middleware

; (defn wrap-connected-clients
;   [handler store]
;   (fn [req]
;     (handler (assoc req :connected-clients
;                     (:connected-clients @store)))))
;
; (defn wrap-last-prices
;   [handler store]
;   (fn [req]
;     (handler (assoc req :last-prices
;                     (:last-prices @store)))))

;;; server

(defn start!
  [*state]
  (swap! *state
         (fn [state]
           (if-let [stop-fn (:stop-fn state)]
             (println "Server already started.")
             (assoc state :stop-fn
                    (server/run-server
                     (wrap-reload webapp)
                     {:port 8080})))))
  (println "Server started."))

(defn stop!
  [*state]
  (swap! *state
         (fn [state]
           (if-let [stop-fn (:stop-fn state)]
             (do
               (stop-fn)
               (dissoc state :stop-fn))
             (println "Server already stopped."))))
  (println "Server stopped."))



; (defn handle-ticker
;   [req]
;   (server/with-channel req channel
;     (server/on-close channel (fn [_] (ticker/unregister-client ticker/clients-store channel)))
;     (ticker/register-client ticker/clients-store channel req)
;     (server/send! channel (str "BTC" 0) false)))
;
; (defroutes webapp
;   (GET "/" req handle-home)
;   ; (GET "/req" req (str "<pre>" (with-out-str (pprint req)) "</pre>"))
;   (GET "/ticker" req handle-ticker)
;   (GET "/coins/:sym" [sym :as req] handle-sym)
;   (resources "/")
;   (not-found "NOT FOUND"))
;
;
; ;;;
;
; (def server
;   (atom {}))
;
; (defn start!
;   []
;   (ticker/init! ticker/prices-store ticker/init-mock!
;                 (partial ticker/listen-mock! server ticker/clients-store))
;   (swap! server update :stop-fn
;          #(if-let [stop %]
;             (do (println "Already started.") stop)
;             (let [r (server/run-server (wrap-reload webapp) {:port 8080})]
;               (println "Started.")
;               r))))
;
; (defn stop!
;   []
;   (when-let [task (:next-tick @server)]
;     (timer/cancel task))
;   (swap! server update :stop-fn
;          #(if-let [stop %]
;             (do (apply stop []) nil)
;             nil))
;   (println "Stopped."))
