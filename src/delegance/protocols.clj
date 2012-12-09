(ns delegance.protocols)

(defprotocol Store
  (store [store data timeout])
  (fetch [store key]))

(defprotocol Queue
  (push [queue data])
  (reserve [queue timeout])
  (finish [queue key]))
