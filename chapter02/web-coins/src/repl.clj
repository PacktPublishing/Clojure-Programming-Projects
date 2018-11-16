(ns repl)

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
(defn html-nav
  [active-uri]
  (html
   [:nav
    (if (#{"" "/"} active-uri)
      [:span "Home"]
      [:a {:href "/"} "Home"])
    [:span " | "]
    (if (= "/req" active-uri)
      [:span "Request"]
      [:a {:href "/req"} "Request"])]))
(html-nav "/req")
(defn html-home
  []
  (html
   [:body
    (html-nav)
    [:main
     [:h1 "My Hiccup Home"]
     [:p "This is my homepage, made with Hiccup!"]]]))
(defn html-req
  [req]
  (html
   [:body
    (html-nav)
    [:main
     [:h1 "Request map:"]
     [:pre (with-out-str (pprint req))]]]))
(defn handle-home
  [_]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (html-home)})
(defn handle-req
  [req]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (html-req req)})
(defroutes webapp
  (GET "/" req handle-home)
  (GET "/req" req handle-req))

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

;;;wrapping

(defn say [s]
  (println s))
(defn wrap-hello [f]
  (fn [s]
    (apply f [(str "hello " s)])))
(def say-hello (wrap-hello say))
(say-hello "world")

(defn shout [s]
  (println (.toUpperCase s)))
(def shout-hello (wrap-hello shout))
(shout-hello "world")

(defn wrap-bang [f]
  (fn [s]
    (apply f [(str s "!!!")])))
(def say-hello-bang
  (-> say
      (wrap-hello)
      (wrap-bang)))
(say-hello-bang "world")
(def shout-hello-bang
  (-> shout
      (wrap-hello)
      (wrap-bang)))
(shout-hello-bang "world")

(def shout-bang-hello
  (-> shout
      (wrap-bang)
      (wrap-hello)))
(shout-bang-hello "world")

(defn increase [n] (inc n))
(defn wrap-divide-by-2 [f]
  (fn [n] (/ (apply f [n]) 2)))
(defn wrap-substract-one [f]
  (fn [n] (- (apply f [n]) 1)))
(def inc-div-sub
  (-> increase
      (wrap-divide-by-2)
      (wrap-substract-one)))
(def inc-sub-div
  (-> increase
      (wrap-substract-one)
      (wrap-divide-by-2)))
(inc-div-sub 4)
(inc-sub-div 4)



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
      (when (< id 1000)
        (timer/schedule-task
         (* id 200) ;; send a message every 200ms
         (server/send! channel (str "Tick from server #" id) false)) ; false => don't close after send
        (recur (inc id))))
    (timer/schedule-task 100000 (server/close channel)))) ;; close in 100s.

(stop)

(require '[compojure.route :refer [resources not-found]])
(defroutes webapp
  (GET "/" req handle-home)
  (GET "/req" req (str "<pre>" (with-out-str (pprint req)) "</pre>"))
  (GET "/ticker" req handle-ticker)
  (resources "/")
  (not-found "NOT FOUND"))

(def stop (server/run-server (wrap-reload webapp) {:port 8080}))




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
