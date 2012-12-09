(ns delegance.protocols)

(defprotocol Store
  (store [store data])
  (fetch [store key]))

(defprotocol Queue
  (push [queue data])
  (reserve [queue])
  (finish [queue key]))