(ns metabill.core
  (:require [clojure.java.shell :as shell]
            [clojure.edn :as edn]
            [clojure.string :as string]))

(def ^:dynamic metabill-file-path "target/metabill.edn")

(def build-meta
  {:time (fn []
           (pr-str (System/currentTimeMillis)))
   :commit-hash (fn []
                  (string/trim (:out (shell/sh "git" "rev-parse" "--short" "HEAD"))))})

(defn- make-build-meta-data []
  (->> build-meta
       (map (fn [[k f]] [k (f)]))
       (into {})))

(defn save-build-meta-data []
  (let [d (make-build-meta-data)]
    (spit metabill-file-path (pr-str d))
    d))

(defn load-build-meta-data []
  (edn/read-string (slurp metabill-file-path)))

;;; with

(defn with-meta-data
  [f ks]
  (let [d (load-build-meta-data)]
    (str f
         (some->> ks
                  (keep #(get d %))
                  (string/join "_")
                  (str "?")))))

(def with-build-time #(with-meta-data % [:time]))
(def with-build-commit-hash #(with-meta-data % [:commit-hash]))
(def with-build-meta #(with-meta-data % (keys build-meta)))

;;; get

(defn get-meta-data
  [k]
  (let [d (load-build-meta-data)]
    (get d k)))

(def get-build-time #(get-meta-data :time))
(def get-build-commit-hash #(get-meta-data :commit-hash))
(def get-build-meta load-build-meta-data)
