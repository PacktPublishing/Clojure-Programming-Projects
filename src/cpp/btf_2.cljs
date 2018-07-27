(ns cpp.btf-2
  (:require
   [cpp.cogs :as cogs :refer [dispatch!]]
   [cpp.da :as da]
   [datascript.core :as d]
   [clojure.test :refer [is testing] :include-macros true]
   [sablono.core :refer [html] :include-macros true]
   [devcards.core :refer [defcard deftest] :include-macros true]))

;====================================
; Data (local)

(def schema
  {:db/ident {:db/unique :db.unique/identity}
   :object/name {:db/unique :db.unique/identity}})

(defn db-list
  [state]
  (let [db (cogs/db state)]
    (da/q-list-objects db)))

(defn db-find
  [state key]
  (let [db (cogs/db state)]
    (da/q-find-object db key)))

(defn db-retract ;>>> db.fn
  [state key]
  (cogs/transact state [[:db.fn/call da/tx-retract-object key]]))

(defn db-upsert  ;>>> db.fn
  [state object ref]
  (if (or (= ref (:object/name object))
          (= "" (:object/name object)))
    state
    (cogs/transact state [[:db.fn/call da/tx-upsert-object object ref]])))


;====================================
; Cards

(defcard
  (html
   [:div
    [:h3 "Back To Front"]
    [:div
     [:a {:href "/cards.html#!/cpp.core"} "Home"]
     " | "
     [:a {:href "/cards.html#!/cpp.btf_1"} "Part 1"]
     " | "
     [:b "Part 2"]]]))

(defcard
  "
## Backport prep.

- Extern queries.
- Upsert/retract --> tx function, with constraints.
- `clojure.spec` FTW!
- Errors --> `ex-info`.
- Move portable code to `cljc`.
- Make portable tx functions (see `cpp.db/defdbfn` macro).
  ")

(deftest test-db
  (let [store (-> (atom {})
                  (cogs/install-card-db schema da/test-data))
        state @store]

    (testing "db-list"

      (let [res (db-list state)]
        (is (= res [{:object/name "Chair"}
                    {:object/name "Door"}
                    {:object/name "Mac"}
                    {:object/name "Table"}])
            "Should return the sorted list of objects.")))

    (testing "db-find"

      (let [res (db-find state "Chair")]
        (is (= res {:object/name "Chair"})
            "Should return the object with name 'Chair'"))
      (let [res (db-find state "Armchair")]
        (is (nil? res)
            "Should return nil (object not found).")))

    (testing "db-upsert"

      (let [state (db-upsert state {:object/name "Armchair"} nil)
            res (db-find state "Armchair")]
        (is (= res {:object/name "Armchair"})
            "Should return inserted object 'Armchair'.")
        (is (thrown? ExceptionInfo (db-upsert state {:object/name "Armchair"} nil))
            "Should throw on duplicate insert 'Armchair'."))
      (let [state (db-upsert state {:object/name "Armchair"} "Chair")
            res (db-find state "Armchair")]
        (is (= res {:object/name "Armchair"})
            "Should return updated object 'Armchair'.")
        (let [res (db-find state "Chair")]
          (is (nil? res)
              "Should return nil (object updated)."))
        (is (thrown? ExceptionInfo (db-upsert state {:object/name "Armchair"} "Chair"))
            "Should throw on updating unknown ref 'Chair'.")
        (is (thrown? ExceptionInfo (db-upsert state {:object/name 666} nil))
            "Should throw on invalid object '{:object/name 666}'.")))

    (testing "db-retract"

      (let [state (db-retract state "Table")
            res (db-find state "Table")]
        (is (nil? res)
            "Should not find retracted object 'Table'.")
        (let [state (db-retract state "Table")
              res (db-find state "Table")]
          (is (nil? res)
              "Retract should be idempotent"))))))
