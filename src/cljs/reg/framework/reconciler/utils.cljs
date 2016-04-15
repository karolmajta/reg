(ns reg.framework.reconciler.utils
  (:require [cljs.core.async :refer [put!]]))

(defn bind-events-on-add [events previous next bind-fn]
  (let [[_ options children] next
         listeners (for [k (filter #(.startsWith (name %) "on-") (keys options))]
                     [(second (.split (name k) "-")) #(do
                                                       (put! events (get options k))
                                                       (when (get-in options [k :prevent-default])
                                                         (.preventDefault %)))])]
    (doseq [[event-name f] listeners]
      (bind-fn event-name f))))

(defn unbind-events-on-remove [events previous next unbind-fn]
  (let [[_ options children] previous
        event-names (for [k (filter #(.startsWith (name %) "on-") (keys options))]
                      (second (.split (name k) "-")))]
    (doseq [event-name event-names]
      (unbind-fn event-name))))
