(ns cpp.test.fetcher.core
  (:require
   [cpp.fetcher.core :as fetcher]
   [clojure.set :as set]
   [clojure.test :refer [deftest is run-tests]]))

(deftest test-get-top-coins
  (let [coins (fetcher/get-top-coins 10)]
    (is (= (count coins) 10))
    (is (= (set (keys (first coins)))
           #{:symbol :full-name :image-url :algorithm :proof-type}))))


;;; REPL
(comment

 (require '[clojure.test :refer [deftest is run-tests]])

 (require '[cpp.test.fetcher.core :as fetcher :reload-all true])

 (run-tests 'cpp.test.fetcher.core)

 nil)
