(ns cpp.ftb-1
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
     [:b "Part 1"]
     " | "
     [:a {:href "/cards.html#!/cpp.ftb_2"} "Part 2"]]]))

(defcard
  "
## Enter Datomic.

- Fix datomic schema => test data should load.
- Add tx functions to datomic schema.
- Port test suite to `clj`.
- Refactor and fix tests `with d/with`.
  ")
