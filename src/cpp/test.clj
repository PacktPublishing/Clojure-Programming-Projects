(ns cpp.test
  (:require
    [cpp.da :as da]
    [cpp.db :as db]
    [cpp.api :as api]
    [cpp.http :as http]
    [clojure.test :refer [deftest is testing run-tests]]
    [clojure.set :as set]
    [datomic.api :as d]
    [ring.mock.request :as mock]))

;====================================
; DB

(defn testing-conn
  "Creates an in-memory Datomic connection for testing."
  []
  (let [uri (str "datomic:mem://test-" (d/squuid))]
    (d/create-database uri)
    (d/connect uri)))

(defn test-objects-conn
  []
  (let [conn (testing-conn)]
    @(d/transact conn db/schema)
    @(d/transact conn da/test-data)
    conn))

(deftest test-da
  (let [conn (test-objects-conn)
        initial-db (d/db conn)]

    (testing "q-list-objects"

      (let [res (da/q-list-objects initial-db)]
        (is (= res
               [{:object/name "Chair"}
                {:object/name "Door"}
                {:object/name "Mac"}
                {:object/name "Table"}])
            "Should return the list of objects.")))

    (testing "q-find-object"

      (let [res (da/q-find-object initial-db "Chair")]
        (is (= res {:object/name "Chair"})
            "Should return the object with name 'Chair'"))
      (let [res (da/q-find-object initial-db "Armchair")]
        (is (nil? res)
            "Should return nil (object not found).")))

    (testing "tx-upsert-object"

      (let [db (:db-after
                (d/with (d/db conn)
                 [[:cpp.da/tx-upsert-object {:object/name "Armchair"} nil]]))
            res (da/q-find-object db "Armchair")]
        (is (= res {:object/name "Armchair"})
            "Should return inserted object 'Armchair'.")
        (is (thrown? Exception
              (d/with db
               [[:cpp.da/tx-upsert-object {:object/name "Armchair"} nil]]))
            "Should throw on duplicate insert 'Armchair'."))
      (let [db (:db-after
                (d/with (d/db conn)
                 [[:cpp.da/tx-upsert-object {:object/name "Armchair"} "Chair"]]))
            res (da/q-find-object db "Armchair")]
        (is (= res {:object/name "Armchair"})
            "Should return updated object 'Armchair'.")
        (let [res (da/q-find-object db "Chair")]
          (is (nil? res)
              "Should return nil (object updated)."))
        (is (thrown? Exception
                     (d/with db
                      [[:da/tx-upsert-object {:object/name "Armchair"} "Chair"]]))
            "Should throw on updating unknown ref 'Chair'.")
        (is (thrown? Exception
                     (d/with (d/db conn)
                      [[:da/tx-upsert-object {:object/name 666} nil]]))
            "Should throw on invalid object '{:object/name 666}'.")))

    (testing "tx-retract-object"

      (let [db (:db-after
                (d/with (d/db conn)
                  [[:cpp.da/tx-retract-object "Table"]]))
            res (da/q-find-object db "Table")]
        (is (nil? res)
            "Should not find retracted object 'Table'.")
        (let [db (:db-after
                  (d/with (d/db conn)
                    [[:cpp.da/tx-retract-object "Table"]]))
              res (da/q-find-object db "Table")]
          (is (nil? res)
              "Retract should be idempotent"))))))


;====================================
; API

(deftest test-api
  (let [conn (test-objects-conn)
        api (http/api {:conn conn})]

    (testing "h-list"

      (let [[res] (vector
                   (sort-by :object/name
                    (:body (api (mock/request :get "/api/objects")))))]
        (is (= res [{:object/name "Chair"}
                    {:object/name "Door"}
                    {:object/name "Mac"}
                    {:object/name "Table"}])
            "Should return the list of objects.")))

    (testing "h-find"

      (let [res (:body (api (mock/request :get "/api/objects/Mac")))]
        (is (= res {:object/name "Mac"})
            "Should return the Mac object."))
      (let [res (:status (api (mock/request :get "/api/objects/Mackie")))]
        (is (= res 404)
            "Should return 404.")))

    (testing "h-retract"

      (let [res (:status (api (mock/request :delete "/api/objects/Mac")))]
        (is (= res 200)
            "Should return 200.")
        (is (nil? (d/entity (d/db conn) [:object/name "Mac"]))
            "Should not find 'Mac'")))

    (testing "h-insert"

      (let [res (:status (api (mock/request :post "/api/objects/Mac")))]
        (is (= res 200)
            "Should return 200 (created).")
        (is (some? (d/entity (d/db conn) [:object/name "Mac"]))
            "Should find 'Mac'"))
      (let [res (:status (api (mock/request :post "/api/objects/Mac")))]
        (is (= res 409)
            "Should return 409 (conflict).")))

    (testing "h-update"

      (let [res (:status (api (mock/request :put "/api/objects/Mac/Mackie")))]
        (is (= res 200)
            "Should return 200 (updated).")
        (is (some? (d/entity (d/db conn) [:object/name "Mackie"]))
            "Should find 'Mackie'"))
      (let [res (:status (api (mock/request :put "/api/objects/Mac/Mackie")))]
        (is (= res 404)
            "Should return 404 (not found).")
        (is (some? (d/entity (d/db conn) [:object/name "Mackie"]))
            "Should find 'Mackie'")))))



; (run-tests 'cpp.test)
