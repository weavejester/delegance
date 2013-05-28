(ns delegance.worker
  "Functions for managing the workers that process the forms queued up with
  the delegance.core/delegate function."
  (:import [java.util.concurrent ScheduledThreadPoolExecutor TimeUnit])
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
  The config should contain at least a :queue and a :store, which should
  respectively implement the Queue and Store protocols in the
  delegance.protocols namespace. Items should be pushed onto the queue using
  the delegance.core/delegate function. This function returns a worker map."
  [{:keys [rate] :or {rate 1000} :as config}]
  (let [executor (ScheduledThreadPoolExecutor. 1)
        process  #(process-next-job config)]
    (.scheduleAtFixedRate executor process 0 rate TimeUnit/MILLISECONDS)
    {:config config :executor executor}))

(defn shutdown-worker
  "Shut down a worker started with the run-worker function."
  [worker]
  (.shutdown (:executor worker)))
