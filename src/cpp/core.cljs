(ns cpp.core
  (:require
   cpp.btf-1
   cpp.btf-2
   cpp.ftb-1
   cpp.ftb-2
   cpp.ete-1
   cpp.ete-2
   [sablono.core :refer [html] :include-macros true]
   [devcards.core :refer [defcard defcard-rg deftest] :include-macros true]))

;; remember to run lein figwheel and then browse to
;; http://localhost:3449/cards.html

(enable-console-print!)

(defcard
  ""
  (html
   [:div
    [:h1 "Full Stack DataLog"]
    [:p {:class "lead"}
     [:span {:class "h1"} "H"] "ow might we leverage on"
      [:i " DataScript * Datomic "] "synergies to provide a dynamic development workflow,
      reduce time to market, and fuel a zero-waiting, scalable user experience."]
    [:ol
     [:li {:class "h3"} [:a {:href "/cards.html#!/cpp.btf_1"} "Back To Front"]]
     [:li {:class "h3"} [:a {:class "h3":href "/cards.html#!/cpp.ftb_1"} "Front To Back"]]
     [:li {:class "h3"} [:a {:class "h3":href "/cards.html#!/cpp.ete_1"} "End To End"]]]]))

;;;;;

; (defn main []
;   ;; conditionally start the app based on whether the #main-app-area
;   ;; node is on the page
;   (if-let [node (.getElementById js/document "main-app-area")]
;     (.render js/ReactDOM (sab/html [:div "This is working"]) node)))
;
; (main)
