(ns fetch-cc-data.core-test
  (:require
   [clojure.test :refer [deftest testing is run-tests]]
   [fetch-cc-data.core :as fetch-cc-data]))


(deftest fetch-top-coins-test
  (testing "Testing fetch-infos! fn"
    (let [top3 (fetch-cc-data/fetch-top-coins!
                3 "test/fetch_cc_data/top_coins.json")
          info (first top3)]
      (is (= 3 (count top3))
          "N=3 should return 3 items")
      (is (= #{:sym :full-name :proof-type :algorithm :image-url}
             (set (keys info)))
          "CC info should have the set of required keys"))))


(deftest fetch-coin-history-test
  (testing "Testing fetch-coin-history! fn"
    (let [last3 (fetch-cc-data/fetch-coin-history!
                 "BTC" 3 "test/fetch_cc_data/BTC_history.json")
          day (first last3)]
      (is (= 3 (count last3))
          "N=3 should return 3 items")
      (is (= #{:time-stamp :open :close :high :low :vol-from :vol-to}
             (set (keys day)))
          "CC history item should have the set of required keys")
      (is (inst? (:time-stamp day))
          "Time stamp should be an instant"))))


(deftest overall-processing-test
  (testing "Testing the overall processing"
    (let [mem (atom {})
          ;mocking (save-to-edn! data path)
          se-fn #(swap! mem assoc %2 %1)]
      (fetch-cc-data/overall-processing! 3 3 "DIR" se-fn)
      (is (= #{"DIR/top_coins.edn" "DIR/BTC_history.edn" "DIR/ETH_history.edn"
               "DIR/XRP_history.edn"}
             (set (keys @mem)))
          "Mock files should exist")
      (is (= 12 (reduce + 0 (map #(count (get @mem %))
                                 ["DIR/top_coins.edn" "DIR/BTC_history.edn"
                                  "DIR/ETH_history.edn" "DIR/XRP_history.edn"])))
          "Total item count should be 12"))))


;;; REPL
(comment

 ; only once
 (require '[clojure.test :refer [run-tests]])

 ; after each change
 (require '[fetch-cc-data.core-test :refer :all :reload-all true])
 (run-tests 'fetch-cc-data.core-test)

 ; generate test files
 (spit "test/fetch_cc_data/top_coins.json"
       (slurp (str "https://min-api.cryptocompare.com"
                   "/data/top/totalvol?limit=10&tsym=USD")))

 (spit "test/fetch_cc_data/BTC_history.json"
       (slurp (str "https://min-api.cryptocompare.com"
                   "/data/histoday?fsym=BTC&tsym=USD&limit=10")))

 nil)
