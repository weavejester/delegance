(ns delegance.worker
  (:import [java.util.concurrent ScheduledThreadPoolExecutor TimeUnit])
  (:require [delegance.protocols :refer :all]))

(def ^:private executor
  (ScheduledThreadPoolExecutor. 32))

(defn- every [rate runnable]
  (.scheduleAtFixedRate executor runnable 0 rate TimeUnit/SECONDS))

(defn- process-available-jobs [{queue :queue, state :state}]
  (when-let [[job-id state-id] (reserve queue)]
    (try
      (let [job-state (get! state state-id)
            result    (eval (:form job-state))]
        (modify state state-id assoc :result result))
      (catch Exception e
        (prn e))
      (finally
        (finish queue job-id)))))

(defn run-worker
  ([client]
     (run-worker 1 client))
  ([rate client]
     (every rate #(process-available-jobs client))))

(defn shutdown-workers []
  (.shutdown executor))
