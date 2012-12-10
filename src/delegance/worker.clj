(ns delegance.worker
  (:import [java.util.concurrent ScheduledThreadPoolExecutor TimeUnit])
  (:require [delegance.protocols :refer :all]))

(def executor
  (ScheduledThreadPoolExecutor. 32))

(defn- every [delay runnable]
  (.scheduleAtFixedRate executor runnable 0 delay TimeUnit/SECONDS))

(defn- process-available-jobs [queue]
  (when-let [job (reserve queue 300)]
    (try
      (eval (:data job))
      (finally (finish queue (:id job))))))

(defn worker
  ([queue]
     (worker 1 queue))
  ([delay queue]
     (every delay #(process-available-jobs queue))))