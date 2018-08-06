(ns cpp.core
  (:require
   [sablono.core :refer [html] :include-macros true]
   [devcards.core :refer [defcard defcard-rg deftest] :include-macros true]))

;; remember to run lein figwheel and then browse to
;; http://localhost:3449/cards.html

(enable-console-print!)

(defcard
  ""
  (html
   [:div
    [:h1 "CPP"]]))

;;;;;

; (defn main []
;   ;; conditionally start the app based on whether the #main-app-area
;   ;; node is on the page
;   (if-let [node (.getElementById js/document "main-app-area")]
;     (.render js/ReactDOM (sab/html [:div "This is working"]) node)))
;
; (main)