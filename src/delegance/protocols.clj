(ns delegance.protocols)

(defprotocol Store
  (store [store data timeout])
  (fetch [store key]))

(defprotocol Queue
  (push [queue data])
  (reserve [queue timeout])
  (finish [queue key]))

(defprotocol State
  (get! [state key])
  (put! [state key val])
  (update!* [state key func]))

(defn update! [state key func & args]
  (update!* state key #(apply func % args)))
