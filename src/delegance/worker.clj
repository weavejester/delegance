(ns delegance.worker
  (:import [java.util.concurrent ScheduledThreadPoolExecutor TimeUnit])
  (:require [delegance.protocols :refer :all]))

(def executor
  (ScheduledThreadPoolExecutor. 32))

(defn- every [rate runnable]
  (.scheduleAtFixedRate executor runnable 0 rate TimeUnit/SECONDS))

(defn- process-available-jobs [{queue :queue, state :state, storage :store}]
  (when-let [{job-id :data :as job} (reserve queue 300)]
    (try
      (let [job-state (get! state job-id)
            job-form  (fetch storage (:form job-state))
            result-id (store storage (eval job-form))]
        (modify state job-id assoc :result result-id))
      (finally (finish queue job)))))

(defn worker
  ([client]
     (worker 1 client))
  ([rate client]
     (every rate #(process-available-jobs client))))
