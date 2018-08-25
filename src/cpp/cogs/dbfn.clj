(ns cpp.cogs.dbfn
  (:require
    [datomic.api :as d]))

(defmacro defdbfn
  "Define a portable db/transaction function.
   (defdbfn f [db x y] (...))
   DataScript: (transact conn [[db.fn/call f 1 2]])
   Datomic: (transact conn [{schema...} f ...])
   then: (transact conn [[:my.ns/f 1 2]])"
  {:arglists '([name docstring? meta-map? args-vector & body])}
  [& args]
  (let [m (if (symbol? (first args))
           (meta (first args))
           {})
        [label args] (if (symbol? (first args))
                      (vector (first args) (rest args))
                      (throw (IllegalArgumentException. "First argument must be a symbol.")))
        [doc args] (if (string? (first args))
                    (vector (first args) (rest args))
                    (vector nil args))
        [m args] (if (map? (first args))
                   (vector (merge m (first args)) (rest args))
                   (vector m args))
        [args body] (if (vector? (first args))
                     (vector (first args) (rest args))
                     (throw (IllegalArgumentException. "Args must be a vector.")))
        m (if doc (assoc m :doc doc) m)
        label (with-meta label m)
        label-dbfn (symbol (str *ns* "/dbfn-" label))]
     (if (:ns &env)
       `(def ~label (fn ~args ~@body))
       `(do
          (def ~label-dbfn (fn ~args ~@body))
          (def ~label
            {:db/ident ~(keyword (str *ns* "/" label))
             :db/fn ~(d/function {:lang "clojure"
                                  :requires [[(symbol (str *ns*))]]
                                  :params args
                                  :code (concat (list label-dbfn) args)})})))))
