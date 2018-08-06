# Chapter 1

## What we're going to build.

A command line data fetcher.

## What you need to know.

We are starting easy, but you should possess a _minimal_ knowledge of Clojure. You don't need to be actually _mastering_ any Clojure concept to follow along, but you will definitely feel lost if the following sounds totally alien to you:

- bindings and functions: `let`, `def`, `defn`, `fn`, lambda form, eg. `#(+ %1 %2)`.
- basic types: `string`, `integer` and `float` numbers, `boolean`, `nil` and the concept of `truthy`.
- compound types: `list`, `vector`, `hashmap`, `()`, `[]` and `{}` literals, the `sequence` abstraction, and basic operations (`first`, `rest`, `nth`, `conj`, `assoc`, `update`, `into`, `map`, `filter`, `reduce`, ...).
- a touch of `destructuring` will help a lot in understanding code written by others.  
- basic `leiningen` project layout and `namespaces`.

There are countless resources on the web (search for "clojure _<X>_" where _<X>_ is one of the above terms), and many A-to-Z Clojure books to get you started (some even are online and free). Limit yourself to the above concepts, so you'll be back and ready-set within a couple of hours at most. Remember: you don't need to know everything of Clojure to build useful programs!

Here's a few pointers:

[TODO]

Also, please read this before trying Clojure at home:

- Reading clojure expressions is challenging at the beginning. It'll _quickly_ become a second nature if you do your workout, and the payoff is well worth the effort.
- Clojure (as all LISPs) mostly reads inside-out rather than line by line.
- Editing clojure files without the help of a structural editing tool (Parinfer or Paredit) is not good for mental health. The management can't be held responsible for nervous breakages due to lack of proper editor setup.
- Do not try to setup or customise your own IDE if you are new to Clojure. By the time you are done with your awesome config, those using the provided setup will have finished the book.
- Expressions typed at the REPL will not evaluate if parens do not match. Upon pressing `ENTER`, you'll get a newline instead of evaluating.
- If you get stuck, hit `CTRL-C` to try to discard the current expression, or `CTRL-D` to exit the current REPL, or just close your terminal, and happily start gain.
- Don't freak about stack traces, you'll see parsecs of them. We'll see and explain common errors along the way.

## Getting started.

At this point, we assume your environment and your editor are correctly setup (see `SETUP.md`). You should be able to start a REPL session from the main project folder.

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

Start a REPL session (the banner text may vary).
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
- aggregate top 10 coins data into one file per coin (see model description below)
- pass coin symbol argument to the command line
- deal with possible errors
- make the files human-readable (using `pprint`)
