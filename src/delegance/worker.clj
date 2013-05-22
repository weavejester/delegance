(ns delegance.worker
  (:import [java.util.concurrent ScheduledThreadPoolExecutor TimeUnit])
  (:require [delegance.protocols :refer :all]))

(defn- process-available-jobs [{queue :queue, state :state}]
  (when-let [[job-id state-id] (reserve queue)]
    (try
      (let [job-state (get! state state-id)
            result    (eval (:form job-state))]
        (modify state state-id assoc :result result :complete? true))
      (catch Exception e
        (prn e))
      (finally
        (finish queue job-id)))))

(def default-worker-max-threads
  (+ 2 (.availableProcessors (Runtime/getRuntime))))

(defn run-worker
  ([client]
     (run-worker client 1000))
  ([client rate]
     (run-worker client rate default-worker-max-threads))
  ([client rate max-threads]
     (let [executor (ScheduledThreadPoolExecutor. max-threads)
           process  #(process-available-jobs client)]
       (.scheduleAtFixedRate executor process 0 rate TimeUnit/MILLISECONDS)
       {:client client :executor executor})))

(defn shutdown-worker [worker]
  (.shutdown (:executor worker)))
