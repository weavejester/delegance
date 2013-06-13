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
        (prn ex)
        (modify store store-id assoc :complete? true))
      (finally
        (finish queue job-id)))))

(defn- call-with-timeout
  "Call a function but interrupt after the specified timeout in milliseconds."
  [func timeout-ms]
  (let [f (future-call func)
        x (deref f timeout-ms nil)]
    (when-not (future-done? f)
      (future-cancel f))
    x))

(defn run-worker
  "Start a worker process running that will periodically poll a queue for work.
  The config should be a map containing the following options:
    :queue   - an implementation of delegance.protocols/Queue
    :store   - an implementation of delegance.protocols/KeyValueStore
    :rate    - the poll rate in milliseconds (default 1000)
    :eval    - the function to use to eval forms (default clojure.core/eval)
    :timeout - the maximum time in milliseconds a single job is allowed to take

  This function returns a worker map that can be used with the shutdown-worker
  function."
  [{:keys [rate timeout] :or {rate 1000} :as config}]
  (let [process (if timeout
                  #(call-with-timeout (partial process-next-job config) timeout)
                  #(process-next-job config))]
    {:config config
     :process (future (loop []
                        (process)
                        (Thread/sleep rate)
                        (recur)))}))

(defn shutdown-worker
  "Shut down a worker started with the run-worker function."
  [worker]
  (future-cancel (:process worker)))
