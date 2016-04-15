(ns reg.framework.reconciler.reconcile
  (:require [clojure.set :as s]))


(defmulti reconcile-add (fn [context previous next]
                          (first next)))

(defmulti reconcile-remove (fn [context previous next]
                             (first previous)))

(defmulti reconcile-update (fn [context previous next]
                             (if (= (first previous) (first next))
                               (first next)
                               (throw (js/Error. "Cannot reconcile two elements of various types")))))

(defmulti should-reconcile-children (fn [context previous next]
                                      (or (first previous) (first next))))

(defmulti extended-context (fn [context previous next]
                            (or (first previous) (first next))))

(defmethod should-reconcile-children :default [context previous next] true)
(defmethod extended-context :default [context previous next] context)


(declare reconcile-children)
(defn reconcile-element [context previous next]
      (when (and (nil? previous) (not (nil? next)))
            (reconcile-add context previous next)
            (reconcile-children (extended-context context previous next) previous next))
      (when (and (not (nil? previous)) (nil? next))
            (reconcile-children (extended-context context previous next) previous next)
            (reconcile-remove context previous next))
      (when (and (not (nil? previous)) (not (nil? next)))
            (reconcile-update context previous next)
            (reconcile-children (extended-context context previous next) previous next)))

(defn reconcile-children [context previous next]
      (when (should-reconcile-children context previous next)
        (let [[previous-component previous-options & previous-children] (filter #(not (nil? %)) previous)
              [next-component previous-options & next-children] (filter #(not (nil? %)) next)
              previous-children-map (into {} (map #(vector (:key (second %)) %)) previous-children)
              next-children-map (into {} (map #(vector (:key (second %)) %)) next-children)
              all-keys (s/union
                         (into #{} (keys previous-children-map))
                         (into #{} (keys next-children-map)))
              child-pairs (for [k all-keys] [(get previous-children-map k) (get next-children-map k)])]

             (doseq [[previous-child next-child] child-pairs]
                    (reconcile-element context previous-child next-child)))))