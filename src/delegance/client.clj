(ns delegance.client
  "The Delegance client that sends jobs to the worker processes."
  (:require [delegance.protocols :refer :all]))

(defn- poll-job
  "Poll the store of a job, returning the result when the job is complete."
  [store job-id poll-rate]
  (loop []
    (let [value (get! store job-id)]
      (if (:complete? value)
        (:result value)
        (do (Thread/sleep poll-rate)
            (recur))))))

(defn delegate
  "Delegate a quoted Clojure form to be evaluated by a remote worker process.
  Workers can be started using the delegance.worker/run-worker function.
  A promise is returned that will be delivered the result of the evaluation.
  The config argument should be a map specifing a :queue and a :store, which
  should respectively implement the Queue and Store protocols in the
  delegance.protocols namespace."
  [config form]
  (let [{queue :queue, store :store} config
        job-id (java.util.UUID/randomUUID)
        result (promise)]
    (put store job-id {:form form})
    (push queue job-id)
    (future (deliver result (poll-job store job-id 1000)))
    result))
