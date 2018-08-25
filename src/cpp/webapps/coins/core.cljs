(ns cpp.webapps.coins.core
  (:require
   [clojure.core.async :refer [go <!] :include-macros true]
   [clojure.pprint :refer [pprint]]
   [cljs-http.client :as http]
   [sablono.core :refer [html] :include-macros true]
   [devcards.core :refer [defcard] :include-macros true]))

(defn fetch-coins!
  [store]
  (go
   (swap! store assoc :coins
          (vec (:body (<! (http/get "/data/coins.edn"))))))
  nil)

(defonce store-coins
  (let [s (atom {})]
    (fetch-coins! s)
    s))

;;;

(defcard
  "# Data in store"
  (fn [store]
    (html
     [:pre (with-out-str (pprint @store))]))
  store-coins)
  ; {:inspect-data true})

;;;

(defn coins-li
  [coin]
  (let [{:keys [symbol full-name image-url algorithm proof-type]} coin]
    (html
     [:li {:key symbol}
      (interpose ", " [symbol full-name image-url algorithm proof-type])])))

(defn coins-ul
  [coins]
  (html
   [:ul
    (map coins-li coins)]))

(defcard
  "# List"
  (fn [store]
    (let [{:keys [coins]} @store]
      (coins-ul coins)))
  store-coins)

;;;

(def host
  "https://www.cryptocompare.com")

(defn coins-row
  [coin]
  (let [{:keys [symbol full-name image-url algorithm proof-type]} coin]
    (html
     [:tr {:key symbol}
      [:td [:img {:src (str host image-url)
                  :alt (str full-name "logo")
                  :width 24
                  :height 24}]]
                  ; :style {:margin 4}}]]
      [:td symbol] [:td full-name]])))
      ; [:td algorithm] [:td proof-type]])))

(defn coins-table
  [coins]
  (html
   [:table {:class "table table-striped table-condensed"}
    ; [:thead [:tr [:th ""] [:th "Symbol"] [:th "Name"]]]
                 ; [:th "Algorithm"] [:th "Proof Type"]]]
    [:tbody
     (map coins-row coins)]]))

(defcard
  "# Table"
  (fn [store]
    (let [{:keys [coins]} @store]
      (coins-table coins)))
  store-coins)
