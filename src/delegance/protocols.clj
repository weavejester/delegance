(ns delegance.protocols)

(defprotocol Store
  (store [store data timeout])
  (fetch [store key]))

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
