(ns delegance.core
  "The core namespace for Delegance."
  (:require [delegance.protocols :refer :all]))

(defn- poll-job
  "Poll the state of a job, returning the result when the job is complete."
  [state job-id poll-rate]
  (loop []
    (let [value (get! state job-id)]
      (if (:complete? value)
        (:result value)
        (do (Thread/sleep poll-rate)
            (recur))))))

(defn delegate
  "Delegate a quoted Clojure form to be evaluated by a remote worker process.
  Workers can be started using the delegance.worker/run-worker function.
  A promise is returned that will be delivered the result of the evaluation.
  The config argument should be a map specifing a :queue and a :state, which
  should respectively implement the Queue and State protocols in the
  delegance.protocols namespace."
  [config form]
  (let [{queue :queue, state :state} config
        job-id (java.util.UUID/randomUUID)
        result (promise)]
    (put state job-id {:form form})
    (push queue job-id)
    (future (deliver result (poll-job state job-id 1000)))
    result))
