(ns delegance.worker
  "Functions for managing the workers that process the forms queued up with
  the delegance.core/delegate function."
  (:require [delegance.protocols :refer :all]))

(defn- process-next-job
  "Process the next job on the job queue."
  [{:keys [queue store eval] :or {eval clojure.core/eval}}]
  (when-let [[job-id store-id] (reserve queue)]
    (try
      (let [job-store (get! store store-id)
            result    (eval (:form job-store))]
        (modify store store-id assoc :complete? true :result result))
      (catch Exception ex
        (modify store store-id assoc :complete? true)
        (prn ex))
      (finally
        (finish queue job-id)))))

(defn run-worker
  "Start a worker process running that will periodically poll a queue for work.
  The config should be a map containing the following options:
    :queue - an implementation of delegance.protocols/Queue
    :store - an implementation of delegance.protocols/KeyValueStore
    :rate  - the poll rate in milliseconds (default 1000)
    :eval  - the function to use to eval forms (default clojure.core/eval)

  This function returns a worker map that can be used with the shutdown-worker
  function."
  [{:keys [rate] :or {rate 1000} :as config}]
  {:config config
   :process (future (loop []
                      (process-next-job config)
                      (Thread/sleep rate)
                      (recur)))})

(defn shutdown-worker
  "Shut down a worker started with the run-worker function."
  [worker]
  (future-cancel (:process worker)))
