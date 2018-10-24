(ns repl)

(comment

 (require '[org.httpkit.server :as server])
 (defn webapp
   [req]
   {:status 200
    :headers {"Content-Type" "text/html"}
    :body "<h1>My Server</h1>"})
 (def stop (server/run-server #'webapp {:port 8080}))

 ;;;redef webapp, check browser
 (defn webapp
   [req]
   {:status 200
    :headers {"Content-Type" "text/html"}
    :body "<h1>My Hot Server</h1>"})

 ;;;add routing
 (require '[compojure.core :refer [defroutes GET]])
 (defroutes webapp
   (GET "/req" req (str "<pre>" (with-out-str (pprint req)) "</pre>")))

 (defroutes webapp
   (GET "/" req "<h1>Home</h1>")
   (GET "/req" req (str "<pre>" (with-out-str (pprint req)) "</pre>")))

 ;;;add html
 (require '[hiccup.core :refer [html]])
 (defn page-home
   []
   (html [:h1 "My Hiccup Home"]))
 (defn handle-home
   [_]
   {:status 200
    :headers {"Content-Type" "text/html"}
    :body (page-home)})
 (defroutes webapp
   (GET "/" req handle-home)
   (GET "/req" req (str "<pre>" (with-out-str (pprint req)) "</pre>")))

 (defn page-home
   []
   (html [:h1 "My New Hiccup Home"]))

 ;;;use markup files
 (require '[web-coins.ui :as ui :reload-all true])
 (defn handle-home
   [req]
   {:status 200
    :headers {"Content-Type" "text/html"}
    :body (ui/page-home req)})

 ;;;reload
 (require '[ring.middleware.reload :refer [wrap-reload]])
 (stop)
 (def stop (server/run-server (wrap-reload webapp) {:port 8080}))

 ;;;ws
 (require '[org.httpkit.timer :as timer])
 (defn handle-ticker
   [req]
   (server/with-channel req channel
     (server/on-close channel (fn [status] (println "channel closed, " status)))
     (loop [id 0]
       (when (< id 10)
         (timer/schedule-task
          (* id 200) ;; send a message every 200ms
          (server/send! channel (str "message from server #" id) false)) ; false => don't close after send
         (recur (inc id))))
     (timer/schedule-task 10000 (server/close channel)))) ;; close in 10s.

 (stop)

 (require '[compojure.route :refer [resources not-found]])
 (defroutes webapp
   (GET "/" req handle-home)
   (GET "/req" req (str "<pre>" (with-out-str (pprint req)) "</pre>"))
   (GET "/ticker" req handle-ticker)
   (resources "/")
   (not-found "NOT FOUND"))

 (def stop (server/run-server (wrap-reload webapp) {:port 8080}))


 nil)


(comment

 (require '[org.httpkit.server :as server]
          '[clojure.edn :as edn]
          '[hiccup.core :refer [html]])

 (defn app [req]
   {:status  200
    :headers {"Content-Type" "text/html"}
    :body    (top-coins)})

 (def stop (server/run-server #'app {:port 8080}))

 (def img-host
   "https://cryptocompare.com")

 (defn coin-row
   [{:keys [sym full-name proof-type algorithm image-url]}]
   [:tr.row
    [:td.col-xs-2
     [:img.img-responsive {:src (str img-host image-url)}]]
    [:td.col-xs-10
     [:span sym]
     [:div.h4 full-name]]])
     ; [:div proof-type ", " algorithm]]])

 (defn coins-table
   [coins]
   [:table.table
    [:caption.lead "Top " (count coins) " Coins"]
    [:tbody
     (map coin-row coins)]])

 (defn html-page
   [title content]
   (str
    "<!DOCTYPE html>"
    "<html><head>"
    "<title>" title "</title>"
    "<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css\" integrity=\"sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u\" crossorigin=\"anonymous\">"
    "<link rel=\"stylesheet\" href=\"css/styles.css\">"
    "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
    "</head><body><div class=\"container-fluid\">"
    content
    "</div></body></html>"))


 (defn top-coins
   []
   (let [coins (edn/read-string (slurp "resources/coin_data/top_coins.edn"))]
      (html-page
       "Top Coins"
       (html (coins-table coins)))))



 nil)
