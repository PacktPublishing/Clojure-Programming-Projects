# Chapter 4

### What we're going to build.

The Coins web server.

### What you need to know.

[TODO]

## Getting started.

### Serving data on the web.

Ring is the de-facto web framework, Compojure is its routing companion. It's all made from a collection of libraries that work well together around a simple functional interface. More on this later...

Create a Ring handler and give it a route.

Create a namespace for your web handlers, and write the first one:
```clj
;;src/cpp/webserver/core.clj
(ns cpp.webserver.core)

(defn handle-BTC
  "Takes a ring request map and returns a ring response map."
  [req]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (slurp "resources/public/data/BTC_price.edn")})
```
[EXPLAIN]

Open the file `src/cpp/http.clj`.

First, require your webserver namespace:
```clj
;;src/cpp/http.clj
(ns cpp.http
  (:require
   [cpp.webserver.core :as webserver] ; <-- add this line
   ;... leave the rest as is
; ...
```
Then add a route to your handler in the `cpp.http` namespace, where `www` routes are defined:
```
;====================================
; Routes

(defroutes www
  (GET "/BTC" req (#'webserver/handle-BTC req) ; insert this line, note the `#'` bit
  ; ... )

```
Save your files.

[EXPLAIN]

From another terminal, start a development server:
```sh
$ lein devsrv
Starting server on port 8080 ...
```

Browse to: http://localhost:8080/BTC.
You should see the BTC prices.

[EXPLAIN]

[Outcome]: You have just served your first dynamic web page using clojure.

### Hot code reloading.

Now, change your handler, enclose the data string within a html tag:
```clj
;;src/cpp/webserver/core.clj
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
