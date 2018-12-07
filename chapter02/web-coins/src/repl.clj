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


;;;use markup files
(require '[web-coins.ui :as ui :reload-all true])

(defn handle-home
  [req]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (ui/page-home req)})


;;;ring middleware
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
(def js-ws
  "var tickerSocket = new WebSocket('ws://localhost:8080/ticker');
   tickerSocket.onmessage = function (event) {
     var e = document.getElementById('ticker');
     e.textContent = event.data;
     console.log(event.data);
   }")

(defn handle-page
  [_]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (html [:h1#ticker "No SSE"][:script js-ws])})

(require '[org.httpkit.timer :as timer])

(defn handle-ticker
  [req]
  (server/with-channel req channel
   (loop [i 0]
     (when (< i 1000)
       (timer/schedule-task
        (* i 200)
        (server/send! channel (str "Tick from server: " i) false))
       (recur (inc i))))))

(stop)

(require '[compojure.route :refer [resources]])

(defroutes webapp
  (GET "/" req handle-page)
  (GET "/ticker" req handle-ticker))

(def stop (server/run-server (wrap-reload webapp) {:port 8080}))


(require 'web-coins.server :reload-all true)
