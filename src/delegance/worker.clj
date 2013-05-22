(ns delegance.worker
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
  ([client]
     (run-worker client 1000))
  ([client rate]
     (let [executor (ScheduledThreadPoolExecutor. 1)
           process  #(process-available-jobs client)]
       (.scheduleAtFixedRate executor process 0 rate TimeUnit/MILLISECONDS)
       {:client client :executor executor})))

(defn shutdown-worker [worker]
  (.shutdown (:executor worker)))
