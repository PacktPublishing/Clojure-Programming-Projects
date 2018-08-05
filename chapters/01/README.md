# Chapter 1

## What we're going to build.

A command line data fetcher.

## What you need to know.

- REPL expressions will not evaluate if parens mismatch, you'll get a newline until they match, or you can `CTRL-C` to get a fresh prompt.
- Visually checking clojure expressions is challenging at the beginning. It'll become a second nature if you do your workout. It reads inside-out.
- Editing clojure files without the help of a structural editing tool (Parinfer or Paredit) is cumbersome. The management can't be held responsible for code breakage if you don't use one.
- Don't freak at the stack trace, you'll see parsecs of them. We'll dive into common errors along the way.
- If you get stuck, just hit `CTRL-D` to exit the current REPL, or just close your terminal. And start gain.

## Getting started.

[TODO]

### Using an external library

Before we start let's add a dependency to our project.
We'll use `cheshire` to parse JSON into EDN (more on this later).
Open `project.clj` and add the following coordinates to the `:dependencies` vector:
```clj
(defproject cpp "0.1.0-SNAPSHOT"
  :description "Code for the book Clojure Programming Projects"

[...]

  :dependencies
  [[org.clojure/clojure "1.9.0"]
   [cheshire "5.8.0"] ; <-- insert this line

[...]
```
Save your changes.

[HINT] Modifications to the `project.clj` file require a fresh start to pick up the changes.

Start a REPL session (always from the main project folder).
```sh
$ lein repl
nREPL server started on port 52812 on host 127.0.0.1 - nrepl://127.0.0.1:52812
REPL-y 0.3.7, nREPL 0.2.12
Clojure 1.9.0
Java HotSpot(TM) 64-Bit Server VM 1.8.0_131-b11
    Docs: (doc function-name-here)
          (find-doc "part-of-name-here")
  Source: (source function-name-here)
 Javadoc: (javadoc java-object-or-class-here)
    Exit: Control+D or (exit) or (quit)
 Results: Stored in vars *1, *2, *3, an exception in *e

user=>
```

### Parsing JSON from a web endpoint:

Get the JSON data from https://min-api.cryptocompare.com public API.
```clj
user=> (slurp "https://min-api.cryptocompare.com/data/price?fsym=BTC&tsyms=USD,JPY,EUR")
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

Save clojure data to an EDN file.
```clj
user=> (spit "resources/public/BTC_price.edn" (json/parse-string (slurp "
https://min-api.cryptocompare.com/data/price?fsym=BTC&tsyms=USD,JPY,EUR")
))
nil
```

Done!

[Outcome] You have written your first useful clojure program, using only three functions.

[EXPLAIN]

[HINT] `edn` is clojure data in text form. because code is data is code is..., _clojure code is itself valid edn_.

[HINT] Think data-in data-out, chances are this is right.



### Running a clojure program using `Leiningen`.

First we'll make a fresh namespace (a clojure module) for our program.

Create a file `src/cpp/fetcher/core.clj`.

Create a main entry point:
```clj
;;src/cpp/fetcher/core.clj
(ns cpp.fetcher.core)

(defn -main [& args]
  (println "My main.")
  0)
```
[EXPLAIN]

Add a `fetcher` alias to your `project.clj`:
```clj
;;project.clj
(defproject cpp "0.1.0-SNAPSHOT"

[...]

  :aliases
  {"rebl" ["trampoline" "run" "-m" "rebel-readline.main"]
   "fetcher" ["trampoline" "run" "-m" "cpp.fetcher.core"]} ; <--- added

  :dependencies
  [[org.clojure/clojure "1.9.0"]

[...]
```
[EXPLAIN]

Try it:
```sh
$ lein fetcher
My main.
```

Write a function that _automates_ your previous session at the REPL:
```clj
;;src/cpp/fetcher/core.clj
(ns cpp.fetcher.core
  (:require [cheshire.core :as json]))

(defn get-coin-price
  "Get price data for sym, save it to <sym>_price.edn"
  [sym]
  (spit
   (str "resources/public/data/" ; into `data` folder
        sym "_price.edn")
   (json/parse-string
    (slurp
     (str "https://min-api.cryptocompare.com/data/price?fsym="
          sym "&tsyms=USD,JPY,EUR")))))

(defn -main [& args]
  (.mkdir (java.io.File. "resources/public/data"))
  (get-coin-price "BTC")
  (println (slurp (str "resources/public/data/" "BTC" "_price.edn")))
  0)
```
[EXPLAIN]

Try your function at the REPL:
```clj
user=> (require '[cpp.fetcher.core :as m :reload true])
nil
user=> (m/get-coin "ETH")
nil
```
Open and check `resources/public/data/ETH_price.edn`.
[EXPLAIN]

[HINT] Requiring namespaces is a bit different at the REPL and in the source code. Spot the differences? Here you have an example of reloading the namespace code, and using an alias `m`.

Try your main program:
```sh
$ rm resources/public/data/*
$ lein fetcher
{"USD" 7965.93, "JPY" 922665.71, "EUR" 6806.64}
```

[Outcome] You have written your first command line program.

## The Project

Complete the coin data fetcher:
- aggregate all available coin data into one file per coin (see data description below)
- pass coin symbol argument to the command line
- deal with possible errors
- make the files human-readable (using `pprint`)
