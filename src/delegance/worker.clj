(ns delegance.worker
  (:import [java.util.concurrent ScheduledThreadPoolExecutor TimeUnit])
  (:require [delegance.protocols :refer :all]))

(def executor
  (ScheduledThreadPoolExecutor. 32))

(defn- every [rate runnable]
  (.scheduleAtFixedRate executor runnable 0 rate TimeUnit/SECONDS))

(defn- process-available-jobs [{queue :queue, state :state}]
  (when-let [{job-id :data :as job} (reserve queue 300)]
    (try
      (let [job-state (get! state job-id)
            result    (eval (:form job-state))]
        (modify state job-id assoc :result result))
      (catch Exception e
        (prn e))
      (finally
        (finish queue job)))))

(defn worker
  ([client]
     (worker 1 client))
  ([rate client]
     (every rate #(process-available-jobs client))))
