(ns cpp.cogs.da
  #?@(:cljs
       ((:require
         [datascript.core :as d]
         [clojure.spec.alpha :as s])
        (:require-macros
         [cpp.cogs.dbfn :refer [defdbfn]]))
      :clj
       ((:require
         [cpp.cogs.dbfn :refer [defdbfn]]
         [datomic.api :as d]
         [clojure.spec.alpha :as s]))))

;====================================
; Data (portable)

(def test-data
  [{:object/name "Chair"}{:object/name "Table"}{:object/name "Door"}
   {:object/name "Mac"}])

(s/def :object/name string?)

(s/def :entity/object
  (s/keys :req [:object/name]))

(defn q-list-objects
  [db]
  (let [res (d/q '[:find [(pull ?e [:object/name]) ...] ;remove extras, especially db/id!!!
                   :in $
                   :where [?e :object/name _]]
                 db)]
     (sort-by :object/name res)))

(defn q-find-object
  [db key]
  (when-let [eid (d/entity db [:object/name key])]
    (into {} (d/touch eid))))

(defdbfn tx-retract-object
  [_ key]
  [[:db/retractEntity [:object/name key]]])

(defdbfn tx-upsert-object
  [db object key]
  (when-not (s/valid? :entity/object object)
    (throw (ex-info "Invalid object." object :invalid-object)))
  (if key
    ;---update
    (if-let [found-eid (:db/id (d/entity db [:object/name key]))]
      [(assoc object :db/id found-eid)]
      (throw (ex-info "Update key not found." object :update-key-not-found)))
    ;---insert
    (if-let [found-eid (:db/id (d/entity db [:object/name (:object/name object)]))]
      (throw (ex-info "Duplicate key found." object :duplicate-key-found))
      [object])))
