(ns delegance.protocols
  "Protocols that implement the queue and storage system Delegance uses.")

(defprotocol Queue
  "A protocol defining a remote queue."
  (push [queue data]
    "Push data onto the queue.")
  (reserve [queue]
    "Reserve data from the top of the queue. This function should return a
    [job-id data] vector, where the job-id is a unique ID.")
  (finish [queue job-id]
    "Mark a job as complete, effectively removing it from the queue."))

(defprotocol KeyValueStore
  "A protocol defining a remote, persistent key-value store."
  (get! [store key]
    "Retrieve a value by its key.")
  (put [store key val]
    "Set the value of key.")
  (modify* [store key func]
    "Update the value stored against a key with a function."))

(defn modify
  "Update the value stored against a key with a function. Additional arguments
  may be supplied which will be added to the end of the function call."
  [store key func & args]
  (modify* store key #(apply func % args)))
