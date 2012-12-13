(ns delegance.protocols)

(def default-store-timeout (* 60 60 24 30))

(defprotocol Store
  (store* [store data timeout])
  (fetch [store key]))

(defn store
  ([store data] (store* store data default-store-timeout))
  ([store data timeout] (store* store data timeout)))

(defprotocol Queue
  (push [queue data])
  (reserve [queue timeout])
  (finish [queue job]))

(defprotocol State
  (get! [state key])
  (put [state key val])
  (modify* [state key func]))

(defn modify [state key func & args]
  (modify* state key #(apply func % args)))
