(ns cpp.ete-1
  (:require
   [cpp.cogs :as cogs :refer [dispatch!]]
   [cpp.da :as da]
   [datascript.core :as d]
   [cljs-http.client :as http]
   [taoensso.timbre :refer [log set-level! tracef debugf infof warnf errorf]]
   [clojure.core.async :refer [go <! timeout] :include-macros true]
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

(declare response-bad)

(defn db-retract
  [state key store]
  (let [new-state (cogs/transact state [[:db.fn/call da/tx-retract-object key]])]
    (go (let [resp (<! (http/delete (str "/api/objects/" key)))]
          (when-not (:success resp)
            (dispatch! store response-bad resp))))
    new-state))

(defn db-upsert
  [state object ref store]
  (if (or (= ref (:object/name object))
          (= "" (:object/name object)))
    state
    (let [new-state (cogs/transact state [[:db.fn/call da/tx-upsert-object object ref]])]
      (go (let [resp (if ref
                       (<! (http/put (str "/api/objects/" ref "/" (:object/name object))))
                       (<! (http/post (str "/api/objects/" (:object/name object)))))]
            (when-not (:success resp)
              (dispatch! store response-bad resp))))
      new-state)))



;====================================
; Transforms

(defn db->display
  [object]
  (when object
    (dissoc object :db/id)))

(defn db->form
  [object]
  (when object
    (dissoc object :db/id)))

(defn form->db
  [form]
  form)


;====================================
; Remote Events

(defn response-bad
  [state resp]
  (warnf "Smelly response: %s" resp)
  state)

(defn response-init-data
  [state data]
  (infof "Initial data: %s" data)
  (cogs/transact state data))



(defmethod cogs/server-event :default
  [state [_ {:as event :keys [msg data uid]}]]
  (warnf "Unhandled server event: %s" event)
  state)

(defmethod cogs/server-event :init
  [state event]
  (warnf "Server event: %s" event)
  state)

(defmethod cogs/server-event :retract
  [state [_ {:as event :keys [msg data uid]}]]
  (infof "Server event: %s" event)
  (try
    (cogs/transact state [[:db.fn/call da/tx-retract-object data]])
    (catch js/Error e
      (warnf "DUP: " uid)
      state)))

(defmethod cogs/server-event :insert
  [state [_ {:as event :keys [msg data uid]}]]
  (infof "Server event: %s" event)
  (try
    (cogs/transact state [[:db.fn/call da/tx-upsert-object
                           {:object/name data} nil]])
    (catch js/Error e
      (warnf "DUP: " uid)
      state)))

(defmethod cogs/server-event :update
  [state [_ {:as event :keys [msg data uid]}]]
  (infof "Server event: %s" event)
  (try
    (cogs/transact state [[:db.fn/call da/tx-upsert-object
                           {:object/name (:name data)} (:key data)]])
    (catch js/Error e
      (warnf "DUP: " uid)
      state)))


;====================================
; Actions

(defn action-edit
  [state name]
  (-> state
      (dissoc :error)
      (assoc :input (db->form (db-find state name)))
      (assoc :ref name)))

(defn action-change-input
  [state value]
  (-> state
      (dissoc :error)
      (assoc-in [:input :object/name] value)))

(defn action-save
  [state store]
  (try
    (-> state
        (db-upsert (form->db (:input state)) (:ref state) store)
        (dissoc :input)
        (dissoc :ref)
        (dissoc :error))
    (catch js/Error e
      (assoc state :error (.-message e)))))

(defn action-delete
  [state name store]
  (db-retract state name store))



;====================================
; UI

(defn ui-row
  [store props]
  (let [{:object/keys [name]} props ;note ns destructuring
        h-edit #(dispatch! store action-edit name)
        h-delete #(dispatch! store action-delete name store)]
    (html
      [:tr {:key name}
       [:td
        [:button {:class "btn btn-xs btn-default" :on-click h-edit} "E"]
        [:button {:class "btn btn-xs btn-default" :on-click h-delete} "X"]
        [:span {:on-click h-edit} "   " name]]])))

(defn ui-table
  [store props]
  (let [{:keys [objects]} props]
    (html
      [:table {:class "table table-condensed"}
       [:tbody (map (partial ui-row store) objects)]])))

(defn ui-form
  [store props]
  (let [{:keys [input error]} props
        h-change #(dispatch! store action-change-input
                             (-> % .-target .-value))
        h-click #(do (.preventDefault %) (dispatch! store action-save store))]
    (html
     [:form {:class "form-inline"}
      [:div {:class "form-group"}
       [:input {:class "form-control" :on-change h-change
                :value (or (:object/name input) "")}]
       [:button {:class "btn btn-default" :on-click h-click} "Save"]
       [:span {:class "text-danger"} "   " error]]])))

(defn ui-screen
  [store props]
  (let [{:keys [title objects input error]} props]
    (html
     [:div
      [:h3 title]
      (ui-form store {:input input :error error})
      (ui-table store {:objects objects})])))

(defn app
  [store]
  (let [state @store
        {:keys [input error]} state
        objects (map db->display (db-list state))]
    (html
     (ui-screen store {:title "Objects" :objects objects :error error
                       :input input}))))



(defonce app-store
  (let [store (-> (atom {})
                  (cogs/install-card-db schema [] #_da/test-data)
                  (cogs/install-socket :object/broadcast))]
    (go (let [resp (<! (http/get (str "http://localhost:3449/api/objects")))]
          (<! (timeout 200))
          (if (:success resp)
            (dispatch! store response-init-data (:body resp))
            (dispatch! store response-bad resp))))
    store))


;====================================
; Cards

(defcard
  (html
   [:div
    [:h3 "End To End"]
    [:div
     [:a {:href "/cards.html#!/cpp.core"} "Home"]
     " | "
     [:b "Part 1"]
     " | "
     [:a {:href "/cards.html#!/cpp.ete_2"} "Part 2"]]]))

(defcard
  "
## Connecting The Ends.

- Stash AJAX calls in the local data functions AFTER the local transaction.
- Optimistic transactions: no more spinner! On (rare) remote error, rollback or fetch the _human_ opinion (keep my changes!).
- Concurrency matters: functional, end-to-end testing via `dispatch!`.
- We'll see next time how we can yet improve the error/success ratio...
  ")

(defcard app
  (fn [store _]
    (app store))
  app-store)
  ; {:inspect-data true})
  ; {:history true})


#_(deftest test-db
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

        (let [state (db-upsert state {:object/name "Armchair"} nil store)
              res (db-find state "Armchair")]
          (is (= res {:object/name "Armchair"})
              "Should return inserted object 'Armchair'.")
          (is (thrown? ExceptionInfo (db-upsert state {:object/name "Armchair"} nil store))
              "Should throw on duplicate insert 'Armchair'."))
        (let [state (db-upsert state {:object/name "Armchair"} "Chair" store)
              res (db-find state "Armchair")]
          (is (= res {:object/name "Armchair"})
              "Should return updated object 'Armchair'.")
          (let [res (db-find state "Chair")]
            (is (nil? res)
                "Should return nil (object updated)."))
          (is (thrown? ExceptionInfo (db-upsert state {:object/name "Armchair"} "Chair" store))
              "Should throw on updating unknown ref 'Chair'.")
          (is (thrown? ExceptionInfo (db-upsert state {:object/name 666} nil store))
              "Should throw on invalid object '{:object/name 666}'.")))

      (testing "db-retract"

        (let [state (db-retract state "Table" store)
              res (db-find state "Table")]
          (is (nil? res)
              "Should not find retracted object 'Table'.")
          (let [state (db-retract state "Table" store)
                res (db-find state "Table")]
            (is (nil? res)
                "Retract should be idempotent"))))))
