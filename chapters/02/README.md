# Chapter 2

### What we're going to build.

Our development web server, with _reloaded_ sweetness.

### What you need to know.

- The server runs on the main thread, blocking the terminal. We can nREPL into this process and examine/change stuff, run tests, and more.

### What you need to have.

[TODO]

### Serving data on the web.

Ring is the de-facto web framework, Compojure is its routing companion. It's all made from a collection of libraries that work well together around a simple functional interface. More on this later...

Create a Ring handler and give it a route.

Create a namespace for your web handlers, and write the first one:
```clj
;;src/cpp/handlers.clj
(ns cpp.handlers
  (:require [cheshire.core :as json]))

(defn handle-BTC
  "Takes a ring request map and returns a ring response map."
  [req]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (slurp "resources/public/BTC_price.edn")})
```
[EXPLAIN]

Add a route to your handler in the `cpp.server` namespace, where `www` routes are defined (you need to require your handlers namespace):
```clj
;;src/cpp/server.clj
(ns cpp.server
  (:require
   [cpp.handlers :as handlers] ;insert this line
   ;... leave the rest as is
; ...

;====================================
; Routes

(defroutes www
  (GET "/BTC" req (#'handlers/handle-BTC req) ; insert this line, note the `#'` bit
  ; ... )

```
[EXPLAIN]

From another terminal, start a development server:
```sh
$ cd cpp
$ sh dev-srv.sh
Starting server on port 8080 ...
```

Browse to: http://localhost:8080/BTC.
You should see the BTC prices.
[EXPLAIN]

[Outcome]: You have just written your first dynamic web app.

### Hot code reloading.

Now, change your handler, enclose the data string within a html tag:
```clj
;;src/cpp/handlers.clj
   ;...
   :body (str
          "<pre>"
          (slurp "resources/public/BTC_price.edn")
          "</pre>")})
```

Save the handlers file, and reload your browser. Notice that your _handler code_ has been reloaded. You can try changing the tags, save and reload again. Or you can try and make your handler as complex as you need: db, auth, async processing, distributed, ...  all while interacting with your program _while it is running_.

[HINT] The code is reloaded from the file only when your handler actually receives a _request_. It is _not_ reloaded just by saving.

[HINT] Reloading code at the REPL is a different story. File changes have no effect _per se_, but we can still reload changes in all namespaces and dependencies, like so:

```clj
user=> (reset)
:reloading ( ...<namespaces>, ... )
...
```

[Outcome]: You have just learnt about one clojure secret weapon: interactive runtime development. With a little attention, you can write 100% reloadable code, a game changer for your programming experience.


## The project

- serve coin data on the web.
