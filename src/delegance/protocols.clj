(ns delegance.protocols)

(defprotocol Queue
  (push [queue data])
  (reserve [queue])
  (finish [queue job-id]))

(defprotocol State
  (get! [state key])
  (put [state key val])
  (modify* [state key func]))

(defn modify [state key func & args]
  (modify* state key #(apply func % args)))
