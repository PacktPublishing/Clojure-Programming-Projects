# Chapter 1

### What we're going to build.

A command line data fetcher.

### What you need to know.

- REPL expressions will not evaluate if parens mismatch.
- Visually checking clojure expressions is challenging at the beginning. It'll become a second nature if you do your workout. It reads inside-out.
- Don't freak at the stack trace, you'll see parsecs of them. We'll dive into common errors along the way.
- If you get stuck, just hit `CTRL-D` to exit the current REPL, or just close your terminal. And start gain.

### What you need to have.

[TODO]

### Using an external library

[TODO]

### Parsing JSON from a web endpoint:

Start a REPL session (always from the main project folder).
```sh
$ lein repl
```

Get the JSON data from https://min-api.cryptocompare.com public API.
```clj
user=> (slurp "https://min-api.cryptocompare.com/data/price?fsym=BTC&tsym
s=USD,JPY,EUR")
"{\"USD\":7314.26,\"JPY\":852240.67,\"EUR\":6235.53}"
```
[EXPLAIN]

Read JSON into a clojure hashmap:
```clj
user=> (require '[cheshire.core :as json])
nil
user=> (json/parse-string (slurp "https://min-api.cryptocompare.com/data/
price?fsym=BTC&tsyms=USD,JPY,EUR"))
{"USD" 7309.87, "JPY" 853367.73, "EUR" 6232.41}
```
[EXPLAIN]

Save clojure data to a file.
```clj
user=> (spit "resources/public/BTC_price.edn" (json/parse-string (slurp "
https://min-api.cryptocompare.com/data/price?fsym=BTC&tsyms=USD,JPY,EUR")
))
nil
```

Done!

[EXPLAIN]

[Outcome] You have written your first useful clojure program, using only three functions.

[HINT] `edn` is clojure data in text form. because code is data is code is..., _clojure code is itself valid edn_.

[HINT] Think data-in data-out, chances are this is right.



### Calling a program.

Create a main entry point:
```clj
;;src/cpp/main.clj
(ns cpp.main)

(defn -main [& args]
  (println "My main.")
  0)
```
[EXPLAIN]

Add a `:main` alias to your `deps.edn`:
```clj
;;/deps.edn
{:deps
 ;...
 :aliases

 {:figwheel
  {:main-opts
   ["-m" "figwheel.main" "-b" "fig" "-r"]
  ;...
  :main
  {:main-opts
   ["-m" "cpp.main"]}
  ;...
```
[EXPLAIN]

Create (or copy from `repl.sh`) a shell script to start the docker runtime and run your alias `main`:

```sh
#/main.sh

docker run -i -t\
       -v `pwd`/rt/data:/root\
       -v `pwd`/.:/cpp\
       -w /cpp\
       cpp/clj\
       clojure -A:main

```
[EXPLAIN]

Try it:
```sh
$ sh main.sh
My main.
```

In the `cpp.main` namespace, write a function that _automates_ your previous session at the REPL:
```clj
;;src/cpp/main.clj
(ns cpp.main
  (:require [cheshire.core :as json]))

(defn get-coin-price
  "Get price data for sym, save it to <sym>_price.edn"
  [sym]
  (spit
   (str "resources/public/"
        sym "_price.edn")
   (json/parse-string
    (slurp
     (str "https://min-api.cryptocompare.com/data/price?fsym="
          sym "&tsyms=USD,JPY,EUR")))))

(defn -main [& args]
  (get-coin-price "BTC")
  (println (slurp (str "resources/public/" "BTC" "_price.edn")))
  0)
```
[EXPLAIN]

Try your function at the REPL:
```clj
user=> (require '[cpp.main :as m :reload true])
nil
user=> (m/get-coin "ETH")
nil
```
Open and check `resources/public/ETH_price.edn`.
[EXPLAIN]

[HINT] Requiring namespaces is a bit different at the REPL and in the source code. Spot the differences? Here you have an example of reloading the namespace code, and using an alias `m`.

Try your program:
```sh
$ rm resources/public/*_price.edn
$ sh main.sh
{"USD" 7965.93, "JPY" 922665.71, "EUR" 6806.64}
```

[Outcome] You have written your first command line program.
