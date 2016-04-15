(ns reg.framework.reconciler.core
  (:require [cljs.core.async :refer [put!]]
            [reg.framework.reconciler.app]
            [reg.framework.reconciler.window]
            [reg.framework.reconciler.content]))


(defprotocol IReconciler
  (reconcile [this next]))

(defrecord Reconciler [previous window-manager events]
  IReconciler
  (reconcile [this next]
    (reg.framework.reconciler.reconcile/reconcile-element {:window-manager (:window-manager this)
                        :events (:events this)} @(:previous this) next)
    (reset! (:previous this) next)))

(defn create []
  (map->Reconciler {:previous (atom nil)}))