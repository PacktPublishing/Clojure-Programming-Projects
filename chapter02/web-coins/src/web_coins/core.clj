(ns web-coins.core
  (:require
   [clojure.pprint :refer [pprint]]
   [ring.middleware.defaults :refer [wrap-defaults api-defaults site-defaults]]
   [ring.middleware.keyword-params :refer [wrap-keyword-params]]
   [ring.middleware.params :refer [wrap-params]]
   [ring.middleware.reload :refer [wrap-reload]]
   [ring.middleware.format :refer [wrap-restful-format]]
   [co.deps.ring-etag-middleware :refer [wrap-file-etag]]
   [compojure.core :refer [defroutes ANY GET POST DELETE PUT routes wrap-routes context]]
   [compojure.route :refer [resources not-found]]
   [taoensso.timbre :refer [log set-level! tracef debugf infof warnf errorf]]))


(def conn nil)
(def broadcast! nil)

(defn handle-status
  [req]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body "Status OK-ish"})


;====================================
; Routes

(defroutes www
  ; (GET "/status" req (#'handle-status req))
  (GET "/req" req (str "<pre>" (with-out-str (pprint req)) "</pre>"))
  (wrap-routes (resources "/js") wrap-file-etag)
  (resources "/")
  (not-found "NOT FOUND"))

(defn api [{:keys [conn bcast-fn]}]
  (routes
    (context "/api" []
      (GET "/req" req (with-out-str (pprint req)))
      (GET "/coins" req (#'api/h-list conn req))
      (GET "/coins/:sym" [sym :as req] (#'api/h-cois conn req key)))))
      ; (DELETE "/coins/:key" [key :as req] (#'api/h-retract conn req key b-cast-fn))
      ; (POST "/coins/:name" [name :as req] (#'api/h-insert conn req name b-cast-fn))
      ; (PUT "/coins/:key/:name" [key name :as req] (#'api/h-update conn req name key b-cast-fn)))))

; (defroutes chsk
;   (GET  "/chsk"  ring-req (#'ring-ajax-get-or-ws-handshake ring-req))
;   (POST "/chsk"  ring-req (#'ring-ajax-post                ring-req)))

(def dev-handler
  (routes
   (wrap-routes chsk wrap-defaults site-defaults)
   (wrap-routes
    (wrap-routes (api {:conn conn :bcast-fn broadcast!})
                 wrap-defaults api-defaults)
    wrap-restful-format)
   (wrap-routes www wrap-defaults site-defaults)))

;;;
