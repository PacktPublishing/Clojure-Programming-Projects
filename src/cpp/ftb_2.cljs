(ns cpp.ftb-2
  (:require
   [sablono.core :refer [html] :include-macros true]
   [devcards.core :refer [defcard deftest] :include-macros true]))

(defcard
  (html
   [:div
    [:h3 "Front To Back"]
    [:div
     [:a {:href "/cards.html#!/cpp.core"} "Home"]
     " | "
     [:a {:href "/cards.html#!/cpp.ftb_1"} "Part 1"]
     " | "
     [:b "Part 2"]]]))

(defcard
  "
## ReSTitution.

- Expose queries and transactions via a ReST-like API.
- Notice we are re-using 100% of the front-end data access code.
- We can extend our data model and constraint rules in a single place.

  ")
