(ns cpp.webapps.coins.core
  (:require
   [cljs-http.client :as http]
   [clojure.core.async :refer [go <!] :include-macros true]
   [sablono.core :refer [html] :include-macros true]
   [devcards.core :refer [defcard] :include-macros true]))

(defn fetch-coins!
  [store]
  (go
   (swap! store assoc :coins
          (vec (:body (<! (http/get "/data/coins.edn"))))))
  nil)

(defcard
  "# Data in store"
  (fn [store]
    (fetch-coins! store)
    (html
     [:pre (str @store)]))
  {}
  {:inspect-data true})

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
    (fetch-coins! store)
    (let [{:keys [coins]} @store]
      (coins-ul coins)))
  {})

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
      [:td full-name] [:td symbol]
      [:td algorithm] [:td proof-type]])))

(defn coins-table
  [coins]
  (html
   [:table {:class "table table-striped table-condensed"}
    [:thead [:tr [:th ""] [:th "Name"] [:th "Symbol"]
                 [:th "Algorithm"] [:th "Proof Type"]]]
    [:tbody
     (map coins-row coins)]]))

(defcard
  "# Table"
  (fn [store]
    (fetch-coins! store)
    (let [{:keys [coins]} @store]
      (coins-table coins)))
  {})
