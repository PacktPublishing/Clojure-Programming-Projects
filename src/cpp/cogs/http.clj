(ns cpp.cogs.http
  (:require
   [cpp.webserver.core :as webserver]
   [cpp.cogs.db :as db]
   [cpp.cogs.api :as api]
   [clojure.pprint :refer [pprint]]
   [clojure.core.async :as async
    :refer [put! <! <!! chan go go-loop thread close!]]
   [org.httpkit.server :as kit]
   [ring.middleware.defaults :refer [wrap-defaults api-defaults site-defaults]]
   [ring.middleware.keyword-params :refer [wrap-keyword-params]]
   [ring.middleware.params :refer [wrap-params]]
   [ring.middleware.reload :refer [wrap-reload]]
   [ring.middleware.format :refer [wrap-restful-format]]
   [co.deps.ring-etag-middleware :refer [wrap-file-etag]]
   [compojure.core :refer [defroutes ANY GET POST DELETE PUT routes wrap-routes context]]
   [compojure.route :refer [resources not-found]]
   [taoensso.sente :as sente]
   [taoensso.sente.server-adapters.http-kit :refer [get-sch-adapter]]
   [taoensso.timbre :refer [log set-level! tracef debugf infof warnf errorf]]
   [clj-uuid :as uuid]))

(set-level! :info)

(defn handle-status
  [req]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body "Status OK-ish"})

(defn handle-data-init
  [req conn]
  {:status 200
   :headers {"Content-Type" "application/edn"}
   :body [[{:a 1}{:a 2}{:a 3}]]})

;====================================
; WebSocket

(let [packer :edn
      chsk-server (sente/make-channel-socket-server! (get-sch-adapter)
                                                     {:packer packer
                                                      :user-id-fn (fn [ring-req] (:client-id ring-req))})
      {:keys [ch-recv send-fn connected-uids
              ajax-post-fn ajax-get-or-ws-handshake-fn]} chsk-server]

  (def ring-ajax-post                ajax-post-fn)
  (def ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
  (def ch-chsk                       ch-recv)
  (def chsk-send!                    send-fn)
  (def connected-uids                connected-uids))

(add-watch connected-uids :connected-uids
  (fn [_ _ old new]
    (when (not= old new)
      (debugf "Connected uids change: %s" new))))

(defmulti -event-msg-handler
  "Multimethod to handle Sente `event-msg`s"
  :id)

(defn event-msg-handler
  "Wraps `-event-msg-handler` with logging, error catching, etc."
  [{:as ev-msg :keys [id ?data event]}]
  (debugf "Received: %s" ev-msg)
  (-event-msg-handler ev-msg))

(defmethod -event-msg-handler
  :default
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (let [session (:session ring-req)
        uid     (:uid     session)]
    (when-not (= id :chsk/ws-ping)
      (warnf "Unhandled event: %s" event))
    (when ?reply-fn
      (?reply-fn {:umatched-event-echoed-from-server event}))))

(defonce router_ (atom nil))
(defn  stop-router! [] (when-let [stop-fn @router_] (stop-fn)))
(defn start-router! []
  (stop-router!)
  (reset! router_
    (sente/start-server-chsk-router!
      ch-chsk event-msg-handler)))

(start-router!)

(defn broadcast! [msg]
  (let [uids (:any @connected-uids)]
    (debugf "Broadcasting server>user: %s uids" (count uids))
    (doseq [uid uids]
      (chsk-send! uid [:object/broadcast msg]))))



;====================================
; Routes

(defroutes www
  (GET "/BTC" req (#'webserver/handle-BTC req))
  (GET "/status" req (#'handle-status req))
  (GET "/req" req (str "<pre>" (with-out-str (pprint req)) "</pre>"))
  (wrap-routes (resources "/js") wrap-file-etag)
  (resources "/")
  (not-found "NOT FOUND"))

(defn api [{:keys [conn b-cast-fn]}]
  (routes
    (context "/api" []
      (GET "/req" req (with-out-str (pprint req)))
      (GET "/reset" req (#'api/h-reset conn req b-cast-fn))
      (GET "/objects" req (#'api/h-list conn req))
      (GET "/objects/:key" [key :as req] (#'api/h-find conn req key))
      (DELETE "/objects/:key" [key :as req] (#'api/h-retract conn req key b-cast-fn))
      (POST "/objects/:name" [name :as req] (#'api/h-insert conn req name b-cast-fn))
      (PUT "/objects/:key/:name" [key name :as req] (#'api/h-update conn req name key b-cast-fn)))))

(defroutes chsk
  (GET  "/chsk"  ring-req (#'ring-ajax-get-or-ws-handshake ring-req))
  (POST "/chsk"  ring-req (#'ring-ajax-post                ring-req)))

(def dev-handler
  (routes
   (wrap-routes chsk wrap-defaults site-defaults)
   (wrap-routes
    (wrap-routes (api {:conn db/conn :b-cast-fn broadcast!})
                 wrap-defaults api-defaults)
    wrap-restful-format)
   (wrap-routes www wrap-defaults site-defaults)))

;;;

(defn -main
  "Runs the server. If SERVER_PORT isn't set a environment variable,
  8080 is used as default."
  [& args]
  (let [port (Integer/parseInt (or (System/getenv "SERVER_PORT") "8080"))]
    (println "Starting server on port" port "...")
    (kit/run-server (wrap-reload dev-handler) {:port port})))
