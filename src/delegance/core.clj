(ns delegance.core
  (:require [delegance.protocols :refer :all]
            [delegance.util :refer (random-uuid)]))

(defn- poll-job
  "Poll a getter function until data is received"
  [state job-id poll-rate]
  (loop []
    (let [value (get! state job-id)]
      (if (:complete? value)
        (:result value)
        (do (Thread/sleep poll-rate)
            (recur))))))

(defn delegate-eval [client form]
  (let [{queue :queue, state :state} client
        job-id (random-uuid)
        result (promise)]
    (put state job-id {:form form})
    (push queue job-id)
    (future (deliver result (poll-job state job-id 1000)))
    result))

(defn resolve-form [form]
  (eval (read-string (str \` (pr-str form)))))

(defmacro delegate [client form]
  `(delegate-eval ~client '~(resolve-form form)))
