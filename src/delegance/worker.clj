(ns delegance.worker
  "Functions for managing the workers that process the forms queued up with
  the delegance.core/delegate function."
  (:import [java.util.concurrent ScheduledThreadPoolExecutor TimeUnit])
  (:require [delegance.protocols :refer :all]))

(defn- process-available-jobs [{queue :queue, state :state}]
  (when-let [[job-id state-id] (reserve queue)]
    (try
      (let [job-state (get! state state-id)
            result    (eval (:form job-state))]
        (modify state state-id assoc :complete? true :result result))
      (catch Exception ex
        (modify state state-id assoc :complete? true)
        (prn ex))
      (finally
        (finish queue job-id)))))

(defn run-worker
  "Start a worker process running that will poll a queue at a specified rate
  in milliseconds. The config should contain a :queue and a :state, which
  should respectively implement the Queue and State protocols in the
  delegance.protocols namespace. Items should be pushed onto the queue using
  the delegance.core/delegate function. This function returns a worker map."
  ([config]
     (run-worker client 1000))
  ([config rate]
     (let [executor (ScheduledThreadPoolExecutor. 1)
           process  #(process-available-jobs client)]
       (.scheduleAtFixedRate executor process 0 rate TimeUnit/MILLISECONDS)
       {:client client :executor executor})))

(defn shutdown-worker
  "Shut down a worker started with the run-worker function."
  [worker]
  (.shutdown (:executor worker)))
