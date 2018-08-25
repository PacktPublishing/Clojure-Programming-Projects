(ns cpp.test.fetcher.core
  (:require
   [cpp.fetcher.core :as fetcher]
   [clojure.set :as set]
   [clojure.test :refer [deftest is run-tests]]))

(deftest test-top-coins
  (let [coins (fetcher/top-coins 10)]
    (is (= (count coins) 10))
    (is (= (set (keys (first coins)))
           #{:symbol :full-name :image-url :algorithm :proof-type}))))


(deftest test-coin-history
  (let [history (fetcher/coin-history "BTC")]
    (is (= (set (keys (first history)))
           #{:time :open :high :low :close :volume-from :volume-to}))))


;;; REPL
(comment

 (require '[clojure.test :refer [deftest is run-tests]])

 (require '[cpp.test.fetcher.core :as fetcher :reload-all true])

 (run-tests 'cpp.test.fetcher.core)

 nil)
