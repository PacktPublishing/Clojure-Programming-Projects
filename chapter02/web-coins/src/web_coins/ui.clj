(ns web-coins.ui
  (:require
   [hiccup.core :refer [html]]))

(defn page-home
  [_]
  (html [:h1 "Home Page"]))
