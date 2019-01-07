; (ns web-coins.ui
;   (:require
;    [hiccup.core :refer [html]]))
;
; (defn page-home
;   [_]
;   (html [:h1 "Home Page"]))
;
(ns web-coins.ui
  (:require
   [cheshire.core :as json]
   [clojure.edn :as edn]
   [clojure.set :as set]
   [hiccup.core :refer [html]]
   [hiccup.page :refer [html5 include-css include-js]]))

(defn home
  [_]
  (html [:h1 "Home Page"]))

(def img-host
  "https://cryptocompare.com")

; (defn coin-row
;   [{:keys [sym full-name proof-type algorithm image-url]}]
;   [:tr.row
;    [:td.col-xs-2
;     [:img.img-responsive {:src (str img-host image-url)}]]
;    [:td.col-xs-10
;     [:span sym " - "]
;     [:span {:id (str sym "ticker")} "N/A"]
;     [:div.h4 full-name]]])

(defn coin-row
  [{:keys [sym full-name proof-type algorithm image-url]}]
  [:tr.row
   [:td.col-xs-2
    [:a {:href (str "/coins/" sym)}
     [:img.img-responsive {:src (str img-host image-url)}]]]
   [:td.col-xs-10
    [:span sym " - "]
    [:span {:id (str sym "ticker")} "N/A"]
    [:div.h4 [:a {:href (str "/coins/" sym)} full-name]]]])

(defn coins-table
  [coins]
  [:table.table
   [:caption.lead "Top " (count coins) " Coins"]
   [:tbody
    (map coin-row coins)]])

(defonce top-coins
  (edn/read-string
   (slurp "resources/coin_data/top_coins.edn")))

(defn page-home
  []
  (html5
   [:title "WebCoins"]
   [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
   (include-css "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css")
   (include-css "/css/styles.css")
   (include-js "/js/ws.js")
   [:div.container-fluid
    (coins-table top-coins)]))

(def ohlc-mapping
  {:time-stamp "date"
   :open "open"
   :low "low"
   :high "high"
   :close "close"})

(defn json-ohlc
  [sym]
  (let [history
        (->> (edn/read-string
              (slurp (format "resources/coin_data/%s_history.edn" sym)))
             (map #(select-keys % [:time-stamp :open :low :high :close]))
             (map #(set/rename-keys % ohlc-mapping)))]
    (json/generate-string history)))

(defn coin-header
  [{:keys [sym full-name proof-type algorithm image-url]}]
  [:div.row
   [:div.col-xs-3
    [:img.img-responsive {:src (str img-host image-url)}]]
   [:div.col-xs-9
    [:span sym " - "]
    [:span {:id (str sym "ticker")} "N/A"]
    [:div.h4 full-name]
    [:span "Proof: " proof-type ", Algo: " algorithm]]])

(defn page-coin
  [sym]
  (let [{:keys [sym full-name proof-type algorithm image-url] :as coin}
        (first (filter #(= (:sym %) sym) top-coins))]
    (html5
     [:title (str "WebCoins - " sym)]
     [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
     (include-css "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css")
     (include-css "/css/styles.css")
     (include-js "/js/ws.js")
     (include-js "https://www.amcharts.com/lib/3/amcharts.js")
     (include-js "https://www.amcharts.com/lib/3/serial.js")
     (include-js "https://www.amcharts.com/lib/3/themes/light.js")

     [:div.container-fluid
      (coin-header coin)
      [:div#ohlc-chart]]
     [:script (str "var ohlcChartDataProvider = " (json-ohlc sym) ";")]
     (include-js "/js/chart.js"))))
