(ns cpp.cogs
  (:require
   [cljs.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [datascript.core :as d]
   [taoensso.timbre :refer [tracef debugf infof warnf errorf] :include-macros true]
   [taoensso.sente  :as sente :refer [cb-success?]]
   [taoensso.sente.packers.transit :as sente-transit]))

;====================================
; Forms

(defn input-value
  [form k]
  (get-in form [k :value] ""))

(defn input-error
  [form k]
  (get-in form [k :error] ""))

(defn form-error
  [form]
  (or (:form/error form) ""))

(defn form-key
  [form]
  (:form/key form))




;====================================
; DB

(defn install-card-db
  "Install a DataScript DB in the provided store. Returns the store.
   This is intended for devcards usage, not for production.
   store: an atom (or a type implementing IAtom, such as `reagent.core/atom`).
   schema: optional DataScript schema.
   tx: initial data, as DataScript transaction data."
  ([store]
   (install-card-db store nil nil))
  ([store schema]
   (install-card-db store schema nil))
  ([store schema tx]
   (let [c (if schema
             (d/create-conn schema)
             (d/create-conn))
         d (when tx
             (:tx-data (d/transact! c tx)))]
     (swap! store
            #(-> %
                 (assoc-in [:__db :type] :history)
                 (assoc-in [:__db :schema] (or schema {}))
                 (assoc-in [:__db :datoms] (or d []))))
     store)))

(defn install-db
  "Install a DataScript DB in the provided store. Returns the store.
   store: an atom (or a type implementing IAtom, such as `reagent.core/atom`).
   schema: optional DataScript schema.
   tx: initial data, as DataScript transaction data."
  ([store]
   (install-db store nil nil))
  ([store schema]
   (install-db store schema nil))
  ([store schema tx]
   (let [conn (if schema
                (d/create-conn schema)
                (d/create-conn))]
     (d/transact! conn (or tx []))
     (swap! store assoc-in [:__db :conn] conn)
     store)))

;;;

(defmulti db
  "Obtain the DB value from the store (equiv. to datascript.core/db)."
  (fn [store]
    (let [state (if (satisfies? IDeref store) @store store)]
      (get-in state [:__db :type])))
  :default :conn)

(defmethod db :conn
  [store]
  (let [state (if (satisfies? IDeref store) @store store)
        conn (get-in state [:__db :conn])]
    (if-not conn
      (throw (js/Error. "DB not found in store"))
      (d/db conn))))

(defmethod db :history
  [store]
  (let [state (if (satisfies? IDeref store) @store store)
        {:keys [schema datoms]} (:__db state)]
    (if-not (and schema datoms)
      (throw (js/Error. "DB not found in store"))
      (d/init-db datoms schema))))

;;;

(defmulti transact
  "Apply transaction tx to the db present in state.
   state must be deref'd from a previously initialised store.
   Different from datascript.core/transact, it takes a store VALUE, not a REF.
   Returns new state (NOT the tx result as would DS/Datomic API,
   tx-data can be found at [:__db :last-transaction])."
  (fn [state tx] (get-in state [:__db :type]))
  :default :conn)

(defmethod transact :conn
  [state tx]
  (let [conn (get-in state [:__db :conn])
        _ (when-not conn
            (throw (js/Error. "DB not found in state")))
        tx-res (d/transact! conn tx)]
    (assoc-in state [:__db :last-transaction] tx-res)))

(defmethod transact :history
  [state tx]
  (let [{:keys [schema datoms]} (:__db state)
        _ (when-not (and schema datoms)
            (throw (js/Error. "DB not found in state")))
        c (d/conn-from-datoms datoms schema)
        tx-res (d/transact! c tx)]
    (-> state
        (assoc-in [:__db :datoms] (d/datoms (:db-after tx-res) :eavt))
        (assoc-in [:__db :last-transaction] tx-res))))


;====================================
; UI

;;; dispatch

(s/def ::store
  (s/with-gen
    (s/and #(satisfies? IDeref %)
           #(satisfies? ISwap %)
           #(map? (deref %)))
    #(gen/fmap atom (s/gen map?))))

(s/def ::transition
  (s/with-gen
    (s/fspec :args (s/cat :state map?
                          :args (s/* any?))
             :ret map?)
    #(gen/return (fn [s & m] s))))

(s/fdef dispatch!
  :args (s/cat :store ::store
               :transition ::transition
               :args (s/* any?))
  :ret nil?)

(defn dispatch!
  "Apply a 'transition' (action/event) to 'store', with zero or more 'args'.
   'transition': a function that takes a map (the current state),
   zero or more arguments, and returns a map (the new state).
   'store': an atom (or any compatible type).
   'args': anything.
   Eval to: nil."
  [store transition & args]
  (apply swap! store transition args)
  nil)

;====================================
; Store

; (defprotocol IDispatch
;   (-dispatch! [o f args]))
;
; (deftype Store
;   [state meta validator watches]
;   Object
;   (equiv [this other]
;     (-equiv this other))
;   IEquiv
;   (-equiv [o other] (identical? o other))
;   IMeta
;   (-meta [_] meta)
;   IHash
;   (-hash [this] (goog/getUid this))
;   IAtom
;   IDeref
;   (-deref [_] (:__state @state))
;   IWatchable
;   (-notify-watches [this oldval newval]
;     (doseq [[key f] watches]
;       (f key this (:__state oldval) (:__state newval))))
;   (-add-watch [this key f]
;     (set! (.-watches this) (assoc watches key f))
;     this)
;   (-remove-watch [this key]
;     (set! (.-watches this) (dissoc watches key)))
;   IReset
;   (-reset! [_ val] (swap! state assoc :__state val))
;   ISwap ;[o f] [o f a] [o f a b] [o f a b xs]
;   (-swap! [_ f] (apply swap! state update :__state [f]))
;   (-swap! [_ f a] (apply swap! state update :__state [f a]))
;   (-swap! [_ f a b] (apply swap! state update :__state [f a b]))
;   (-swap! [_ f a b xs] (apply swap! state update :__state (concat [f a b] xs)))
;   IDispatch
;   (-dispatch! [_ f args] (apply swap! state update :__state f args) nil))
;
; (defn ^Store store
;   ([] (store {}))
;   ([x]
;    (let [s (Store.
;             (if (satisfies? IAtom x)
;               (reset! x {:__state @x})
;               (atom {:__state x}))
;             nil nil nil)]
;      (add-watch (.-state s) :w #(-notify-watches s %3 %4))
;      s))
;   ([x & {:keys [meta validator]}]
;    (let [s (Store. (atom {:__state x}) meta validator nil)]
;      (add-watch (.-state s) :w #(-notify-watches s %3 %4))
;      s)))
;
; (defn store? [x] (instance? Store x))
;
; (defn dispatch!
;   [store f & args]
;   (-dispatch! store f args))
;


;====================================
; WebSocket

(let [packer :edn
      {:keys [chsk ch-recv send-fn state]}
      (sente/make-channel-socket-client! "/chsk" {:packer packer})]

  (def chsk       chsk)
  (def ch-chsk    ch-recv)
  (def chsk-send! send-fn)
  (def chsk-state state))

(defmulti -event-msg-handler
  "Multimethod to handle Sente `event-msg`s"
  :id)

(defn event-msg-handler
  "Wraps `-event-msg-handler` with logging, error catching, etc."
  [{:as ev-msg :keys [id ?data event]}]
  (-event-msg-handler ev-msg))

(defmethod -event-msg-handler
  :default
  [{:as ev-msg :keys [event]}]
  (warnf "Unhandled event: %s" event))

(defmethod -event-msg-handler :chsk/state
  [{:as ev-msg :keys [?data]}]
  (let [[old-state-map new-state-map] ?data]
    (if (:first-open? new-state-map)
      (infof "Channel socket open: %s" new-state-map)
      (debugf "Channel socket state change: %s" new-state-map))))


(defmulti server-event
  (fn [state [_ {:keys [msg data]}]] msg))


(defonce __socket-domains (atom {}))

; {:ch-recv #object[cljs.core.async.impl.channels.ManyToManyChannel]
;  :send-fn #object[G__10821]
;  :state #object [cljs.core.Atom
;                  {:val {:type :ws,
;                         :open? true,
;                         :ever-opened? true,
;                         :uid :taoensso.sente/nil-uid,
;                         :csrf-token nil,
;                         :handshake-data nil,
;                         :first-open? true}}],
;  :event [:chsk/recv [:chsk/ws-ping]],
;  :id :chsk/recv,
;  :?data [:chsk/ws-ping]}
(defmethod -event-msg-handler :chsk/recv
  [{:as ev-msg :keys [?data]}]
  (infof "Received: %s" ev-msg)
  (if-let [[k & _] ?data]
    (case k
      :chsk/ws-ping (debugf "Ping event: %s" ?data)

      (if-let [store (get @__socket-domains k)]
        (dispatch! store server-event ?data)
        (warnf "Unhandled domain event: %s" ?data)))))



(defmethod -event-msg-handler :chsk/ws-ping
  [{:as ev-msg :keys [?data]}]
  (let [[?uid ?csrf-token ?handshake-data] ?data]
    (debugf "Ping event: %s" ?data)))

(defmethod -event-msg-handler :chsk/handshake
  [{:as ev-msg :keys [?data]}]
  (let [[?uid ?csrf-token ?handshake-data] ?data]
    (debugf "Handshake: %s" ?data)))

(defonce router_ (atom nil))
(defn  stop-router! [] (when-let [stop-f @router_] (stop-f)))
(defn start-router! []
  (stop-router!)
  (reset! router_
    (sente/start-client-chsk-router!
      ch-chsk event-msg-handler)))

(start-router!) ;++++++++++++++++++++++++++++++++++++++++++++++++

(defn install-socket
  "Install a sente websocket in the store.
   Domain is a kw for filtering messages sent/received by this store."
  [store domain]
  (swap! store
         #(-> %
              (assoc-in [:__socket :type] :sente)
              (assoc-in [:__socket :domain] domain)))
  (swap! __socket-domains assoc domain store)
  store)
