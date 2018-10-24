(ns web-coins.ui
  (:require
   [hiccup.core :refer [html]]
   [hiccup.page :refer [html5 include-css include-js]]))

(defn page
  [& content]
  (html5
   (include-css "/css/styles.css")
   (include-js "/js/ws.js")
   content))

(defn page-home
  [_]
  (page
   [:h1 "Hotty Page"]
   [:h2#ticker "Ticker"]))
