(ns cpp.cogs.db
  (:require
   cpp.cogs.da
   [datomic.api :as d]))

(def schema
  [{:db/ident :object/name
    :db/unique :db.unique/value
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one}
   cpp.da/tx-retract-object
   cpp.da/tx-upsert-object])

(def data
  [{:object/name "Chair"}{:object/name "Table"}{:object/name "Door"}
   {:object/name "Mac"}])

(def conn
  (let [uri (str "datomic:mem://app-main-" (d/squuid))
        _   (d/create-database uri)
        conn (d/connect uri)]
    @(d/transact conn schema)
    @(d/transact conn data)
    conn))
