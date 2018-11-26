(ns repl)
;;; REPL sessions log


(require '[cheshire.core :as json])
;=> nil
(slurp "https://min-api.cryptocompare.com/data/price?fsym=BTC&tsyms=USD,JPY,EUR")
;=> "{\"USD\":6320.35,\"JPY\":699429.9,\"EUR\":5430.2}"
(json/parse-string *1)
;=> {"USD" 6320.35, "JPY" 699429.9, "EUR" 5430.2}
(spit "resources/BTC_price.edn" *1)
;=> nil
(slurp "resources/BTC_price.edn")
;=> "{\"USD\" 6321.85, \"JPY\" 699478.46, \"EUR\" 5431.01}"
(require '[clojure.edn :as edn])
;=> nil
(edn/read-string *2)
;=> {"USD" 6321.85, "JPY" 699478.46, "EUR" 5431.01}
