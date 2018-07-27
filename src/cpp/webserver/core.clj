(ns cpp.webserver.core)

(defn handle-BTC
  "Takes a ring request map and returns a ring response map."
  [req]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (str "<pre>"
              (slurp "resources/public/data/BTC_price.edn")
              "</pre>")})
