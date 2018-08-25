(ns cpp.cogs.api
  (:require
   [cpp.cogs.da :as da]
   [datomic.api :as d]
   [clojure.core.async :refer [go put! >! chan]]
   [taoensso.timbre :refer [log set-level! tracef debugf infof warnf errorf]]))

(def data
  [{:object/name "Chair"}{:object/name "Table"}{:object/name "Door"}
   {:object/name "Mac"}])

(defn h-reset
  [conn req b-cast-fn]
  @(d/transact conn data)
  (b-cast-fn {:msg :reset :data data :uid (get-in req [:client-id])})
  {:status 200})

(defn h-list
  [conn req]
  (let [db (d/db conn)
        res (da/q-list-objects db)]
    {:status 200
     :headers {"Content-Type" "application/edn"}
     :body res}))

(defn h-find
  [conn _ key]
  (let [db (d/db conn)
        res (da/q-find-object db key)]
    (if res
      {:status 200
       :headers {"Content-Type" "application/edn"}
       :body res}
      {:status 404
       :headers {"Content-Type" "application/edn"}
       :body res})))

(defn h-retract
  [conn req key b-cast-fn]
  @(d/transact conn [[:fsdl.da/tx-retract-object key]])
  (b-cast-fn {:msg :retract :data key :uid (get-in req [:client-id])})
  {:status 200
   :headers {"Content-Type" "application/edn"}})

(defn h-insert
  [conn req name b-cast-fn]
  (try
    @(d/transact conn [[:fsdl.da/tx-upsert-object {:object/name name} nil]])
    (b-cast-fn {:msg :insert :data name :uid (get-in req [:client-id])})
    {:status 200
     :headers {"Content-Type" "application/edn"}}
    (catch Exception e
      (warnf (.getMessage e) name)
      {:status 409
       :headers {"Content-Type" "application/edn"}
       :body (.getMessage e)})))

(defn h-update
  [conn req name key b-cast-fn]
  (try
    @(d/transact conn [[:fsdl.da/tx-upsert-object {:object/name name} key]])
    (b-cast-fn {:msg :update :data {:key key :name name :uid (get-in req [:client-id])}})
    {:status 200
     :headers {"Content-Type" "application/edn"}}
    (catch Exception e
      (warnf (.getMessage e) name)
      {:status 404
       :headers {"Content-Type" "application/edn"}
       :body (.getMessage e)})))
