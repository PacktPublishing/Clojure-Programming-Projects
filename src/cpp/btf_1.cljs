(ns cpp.btf-1
  (:require
   [cpp.cogs :as cogs :refer [dispatch!]]
   [datascript.core :as d]
   [clojure.test :refer [is testing] :include-macros true]
   [sablono.core :refer [html] :include-macros true]
   [devcards.core :refer [defcard deftest] :include-macros true]))

;====================================
; Data

(def test-data
  [{:object/name "Chair"}{:object/name "Table"}{:object/name "Door"}
   {:object/name "Mac"}])

(def schema
  {:db/ident {:db/unique :db.unique/identity}
   :object/name {:db/unique :db.unique/identity}})

(defn db-list
  [state]
  (let [db (cogs/db state)
        q '[:find [(pull ?e [:object/name]) ...] ;remove extras, especially db/id!!!
            :in $
            :where [?e :object/name _]]]
    (sort-by :object/name (d/q q db))))

(defn db-find
  [state key]
  (let [db (cogs/db state)
        e (d/entity db [:object/name key])]
    (when-not (empty? e)
      (into {} (d/touch e)))))

(defn db-retract
  [state key]
  (cogs/transact state [[:db/retractEntity [:object/name key]]]))

(defn db-upsert
  [state object ref]
  (cond
    ;--- unchanged
    (or (= ref (:object/name object))
        (= "" (:object/name object)))
    state
    ;--- duplicate
    (and (nil? ref) (d/entity (cogs/db state)
                              [:object/name (:object/name object)]))
    (throw (js/Error. "Cannot insert duplicate."))
    ;--- update
    (some? ref)
    (cogs/transact state [(assoc object :db/id [:object/name ref])])
    ;--- insert
    :else
    (cogs/transact state [object])))



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
  [state]
  (try
    (-> state
        (db-upsert (form->db (:input state)) (:ref state))
        (dissoc :input)
        (dissoc :ref)
        (dissoc :error))
    (catch js/Error e
      (assoc state :error (.-message e)))))

(defn action-delete
  [state name]
  (db-retract state name))



;====================================
; UI

(defn ui-row
  [store props]
  (let [{:object/keys [name]} props ;note ns destructuring
        h-edit #(dispatch! store action-edit name)
        h-delete #(dispatch! store action-delete name)]
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
        h-click #(do (.preventDefault %) (dispatch! store action-save))]
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
  (-> (atom {})
      (cogs/install-card-db schema test-data)))



;====================================
; Cards

(defcard
  (html
   [:div
    [:h3 "Back To Front"]
    [:div
     [:a {:href "/cards.html#!/cpp.core"} "Home"]
     " | "
     [:b "Part 1"]
     " | "
     [:a {:href "/cards.html#!/cpp.btf_2"} "Part 2"]]]))

(defcard
  "
## DataLog _à la_ card.

- UI first => requirements discovery and refinement.
- Cheap to pivot when no API to sync.
- Interactive data modeling, with state history. Yummy!
- `app-state` is _still_ just a map.
- Write some tests, you'll thank yourself.
- Importance of data flow and transforms: `db -> form -> db`
  ")

(defcard app
  (fn [store _]
    (app store))
  app-store
  ; {:inspect-data true})
  {:history true})


(deftest test-db
  (let [store (-> (atom {})
                  (cogs/install-card-db schema test-data))
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
        (is (thrown? js/Error (db-upsert state {:object/name "Armchair"} nil))
            "Should throw on duplicate insert 'Armchair'."))
      (let [state (db-upsert state {:object/name "Armchair"} "Chair")
            res (db-find state "Armchair")]
        (is (= res {:object/name "Armchair"})
            "Should return updated object 'Armchair'.")
        (let [res (db-find state "Chair")]
          (is (nil? res)
              "Should return nil (object updated)."))
        (is (thrown? js/Error (db-upsert state {:object/name "Armchair"} "Chair"))
            "Should throw on updating unknown ref 'Chair'.")))

    (testing "db-retract"

      (let [state (db-retract state "Table")
            res (db-find state "Table")]
        (is (nil? res)
            "Should not find retracted object 'Table'.")
        (let [state (db-retract state "Table")
              res (db-find state "Table")]
          (is (nil? res)
              "Retract should be idempotent"))))))
