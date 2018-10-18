(ns fetch-cc.core-test
  (:require
   [clojure.test :refer [deftest testing is run-tests]]
   [fetch-cc.core :as fetch-cc]))


(deftest end-to-end-test
  (testing "Fetch BTC price and save to file."
    (let [price (fetch-cc/fetch-btc-current-price!)
          _ (fetch-cc/save-btc-price! price)
          saved-price (fetch-cc/load-btc-price!)]
      (is (= price saved-price)
          "Saved price should match fetched price."))))



;;; REPL
(comment

 ; only once
 (require '[clojure.test :refer [run-tests]])

 ; after each change
 (require '[fetch-cc.core-test :refer :all reload-all true])
 (run-tests 'fetch-cc.core-test)

 nil)
